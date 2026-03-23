package com.muyingmall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.muyingmall.common.exception.BusinessException;
import com.muyingmall.dto.SeckillOrderDTO;
import com.muyingmall.dto.SeckillRequestDTO;
import com.muyingmall.entity.SeckillActivity;
import com.muyingmall.entity.SeckillOrder;
import com.muyingmall.entity.SeckillProduct;
import com.muyingmall.mapper.SeckillActivityMapper;
import com.muyingmall.mapper.SeckillOrderMapper;
import com.muyingmall.mapper.SeckillProductMapper;
import com.muyingmall.service.OrderService;
import com.muyingmall.service.SeckillOrderService;
import com.muyingmall.service.SeckillService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 秒杀订单服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SeckillOrderServiceImpl implements SeckillOrderService {

    private static final int DEFAULT_LIMIT_PER_SKU = 5;

    private final SeckillService seckillService;
    private final SeckillProductMapper seckillProductMapper;
    private final SeckillActivityMapper seckillActivityMapper;
    private final SeckillOrderMapper seckillOrderMapper;
    private final OrderService orderService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long executeSeckill(Integer userId, SeckillRequestDTO request) {
        // 1. 基础参数校验
        if (request == null || request.getSeckillProductId() == null || request.getQuantity() == null
                || request.getQuantity() <= 0) {
            throw new BusinessException("秒杀参数不合法");
        }

        // 2. 查询秒杀商品信息
        SeckillProduct seckillProduct = seckillProductMapper.selectById(request.getSeckillProductId());
        if (seckillProduct == null) {
            throw new BusinessException("秒杀商品不存在");
        }

        // 3. 校验秒杀活动状态和时间
        SeckillActivity activity = seckillActivityMapper.selectById(seckillProduct.getActivityId());
        if (activity == null || activity.getStatus() != 1) {
            throw new BusinessException("秒杀活动未开始或已结束");
        }
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(activity.getStartTime()) || now.isAfter(activity.getEndTime())) {
            throw new BusinessException("秒杀活动未开始或已结束");
        }

        // 4. 只统计"已支付"秒杀数量，达到上限后直接拒绝。
        int effectiveLimit = resolveEffectiveLimitPerUser(seckillProduct);
        int paidQuantity = Math.max(0, seckillOrderMapper.countUserPurchase(
                userId,
                seckillProduct.getActivityId(),
                request.getSeckillProductId()));

        if (paidQuantity >= effectiveLimit) {
            throw new BusinessException("每位用户最多可成功购买" + effectiveLimit + "件，您已达上限");
        }

        int remainAllowed = effectiveLimit - paidQuantity;
        if (request.getQuantity() > remainAllowed) {
            throw new BusinessException("当前最多还可成功购买" + remainAllowed + "件");
        }

        // 5. 待支付订单拦截：同一用户同一秒杀商品仅允许一个待支付占位，防止无限抢单。
        int pendingQuantity = Math.max(0, seckillOrderMapper.countUserPendingPurchase(
                userId,
                seckillProduct.getActivityId(),
                request.getSeckillProductId()));
        if (pendingQuantity > 0) {
            throw new BusinessException("你有待支付的秒杀订单，请先完成支付或等待超时后重试");
        }

        // 6. 计算活动剩余时间作为Redis用户集合过期时间
        long remainingSeconds = Duration.between(now, activity.getEndTime()).getSeconds();
        Long expireSeconds = Math.max(remainingSeconds, 60);

        // 7. 使用Lua脚本原子性扣减库存（含用户去重）
        int luaResult = seckillService.deductStockWithLua(
                request.getSeckillProductId(),
                seckillProduct.getSkuId(),
                request.getQuantity(),
                userId,
                expireSeconds);

        if (luaResult == -1) {
            throw new BusinessException("商品已售罄");
        } else if (luaResult == -2) {
            throw new BusinessException("您已参与过该秒杀活动");
        } else if (luaResult == -3) {
            throw new BusinessException("库存数据异常，请稍后重试");
        } else if (luaResult != 1) {
            throw new BusinessException("秒杀失败，请稍后重试");
        }

        try {
            // 8. 扣减数据库秒杀库存（乐观锁）
            int deductResult = seckillProductMapper.deductStock(
                    request.getSeckillProductId(),
                    request.getQuantity());

            if (deductResult <= 0) {
                seckillService.restoreStockWithLua(
                        request.getSeckillProductId(),
                        seckillProduct.getSkuId(),
                        request.getQuantity(),
                        userId);
                throw new BusinessException("库存不足，秒杀失败");
            }

            // 9. 使用directPurchase创建订单（传入秒杀价覆盖SKU原价）
            String payMethod = (request.getPaymentMethod() != null) ? request.getPaymentMethod() : "ALIPAY";
            Map<String, Object> orderResult = orderService.directPurchase(
                    userId,
                    request.getAddressId().intValue(),
                    seckillProduct.getProductId().intValue(),
                    request.getQuantity(),
                    null,
                    seckillProduct.getSkuId(),
                    "秒杀订单",
                    payMethod,
                    null,
                    BigDecimal.ZERO,
                    0,
                    seckillProduct.getSeckillPrice()
            );

            // 10. 获取订单ID并创建秒杀订单记录
            Long orderId = ((Number) orderResult.get("orderId")).longValue();

            SeckillOrder seckillOrder = new SeckillOrder();
            seckillOrder.setOrderId(orderId);
            seckillOrder.setUserId(userId);
            seckillOrder.setActivityId(seckillProduct.getActivityId());
            seckillOrder.setSeckillProductId(seckillProduct.getId());
            seckillOrder.setSkuId(seckillProduct.getSkuId());
            seckillOrder.setQuantity(request.getQuantity());
            seckillOrder.setSeckillPrice(seckillProduct.getSeckillPrice());
            seckillOrder.setStatus(0);

            seckillOrderMapper.insert(seckillOrder);

            log.info("秒杀成功: userId={}, orderId={}, skuId={}", userId, orderId, seckillProduct.getSkuId());

            return orderId;

        } catch (Exception e) {
            log.error("秒杀订单创建失败，恢复库存: userId={}, skuId={}", userId, seckillProduct.getSkuId(), e);
            seckillService.restoreStockWithLua(
                    request.getSeckillProductId(),
                    seckillProduct.getSkuId(),
                    request.getQuantity(),
                    userId);
            throw new BusinessException("秒杀失败: " + e.getMessage());
        }
    }

    @Override
    public boolean canUserParticipate(Integer userId, Long seckillProductId) {
        SeckillProduct seckillProduct = seckillProductMapper.selectById(seckillProductId);
        if (seckillProduct == null) {
            return false;
        }

        Integer purchasedCount = seckillOrderMapper.countUserPurchase(
                userId,
                seckillProduct.getActivityId(),
                seckillProductId);

        Integer pendingCount = seckillOrderMapper.countUserPendingPurchase(
                userId,
                seckillProduct.getActivityId(),
                seckillProductId);

        if (Math.max(0, pendingCount) > 0) {
            return false;
        }

        return Math.max(0, purchasedCount) < resolveEffectiveLimitPerUser(seckillProduct);
    }

    /**
     * 计算用户对该SKU的限购数量。
     * 优先使用商品配置的 limitPerUser；未配置时使用默认值。
     */
    private int resolveEffectiveLimitPerUser(SeckillProduct seckillProduct) {
        if (seckillProduct != null && seckillProduct.getLimitPerUser() != null
                && seckillProduct.getLimitPerUser() > 0) {
            return seckillProduct.getLimitPerUser();
        }
        return DEFAULT_LIMIT_PER_SKU;
    }

    @Override
    public IPage<SeckillOrderDTO> getOrderPage(Page<SeckillOrderDTO> page, Long activityId, Integer userId,
            String status, LocalDateTime startTime, LocalDateTime endTime) {
        Integer statusCode = convertStatusToCode(status);
        return seckillOrderMapper.selectOrderPage(page, activityId, userId, statusCode, startTime, endTime);
    }

    @Override
    public SeckillOrderDTO getOrderDetailById(Long id) {
        SeckillOrderDTO orderDTO = seckillOrderMapper.selectOrderDetail(id);
        if (orderDTO != null) {
            orderDTO.setStatusText(convertStatusToText(orderDTO.getStatus()));
        }
        return orderDTO;
    }

    @Override
    public long countByActivityId(Long activityId) {
        return seckillOrderMapper.selectCount(
                new LambdaQueryWrapper<SeckillOrder>()
                        .eq(SeckillOrder::getActivityId, activityId));
    }

    @Override
    public long countByActivityIdAndStatus(Long activityId, String status) {
        Integer statusCode = convertStatusToCode(status);
        return seckillOrderMapper.selectCount(
                new LambdaQueryWrapper<SeckillOrder>()
                        .eq(SeckillOrder::getActivityId, activityId)
                        .eq(SeckillOrder::getStatus, statusCode));
    }

    @Override
    public BigDecimal sumAmountByActivityId(Long activityId) {
        LambdaQueryWrapper<SeckillOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SeckillOrder::getActivityId, activityId)
                .eq(SeckillOrder::getStatus, 1);

        return seckillOrderMapper.selectList(wrapper).stream()
                .map(order -> order.getSeckillPrice().multiply(new BigDecimal(order.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public long countByProductId(Long productId) {
        return seckillOrderMapper.selectCount(
                new LambdaQueryWrapper<SeckillOrder>()
                        .eq(SeckillOrder::getSeckillProductId, productId));
    }

    @Override
    public long countByProductIdAndStatus(Long productId, String status) {
        Integer statusCode = convertStatusToCode(status);
        return seckillOrderMapper.selectCount(
                new LambdaQueryWrapper<SeckillOrder>()
                        .eq(SeckillOrder::getSeckillProductId, productId)
                        .eq(SeckillOrder::getStatus, statusCode));
    }

    @Override
    public Integer sumQuantityByProductId(Long productId) {
        LambdaQueryWrapper<SeckillOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SeckillOrder::getSeckillProductId, productId)
                .eq(SeckillOrder::getStatus, 1);

        return seckillOrderMapper.selectList(wrapper).stream()
                .mapToInt(SeckillOrder::getQuantity)
                .sum();
    }

    @Override
    public BigDecimal sumAmountByProductId(Long productId) {
        LambdaQueryWrapper<SeckillOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SeckillOrder::getSeckillProductId, productId)
                .eq(SeckillOrder::getStatus, 1);

        return seckillOrderMapper.selectList(wrapper).stream()
                .map(order -> order.getSeckillPrice().multiply(new BigDecimal(order.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public long countByTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        LambdaQueryWrapper<SeckillOrder> wrapper = new LambdaQueryWrapper<>();
        if (startTime != null) {
            wrapper.ge(SeckillOrder::getCreateTime, startTime);
        }
        if (endTime != null) {
            wrapper.le(SeckillOrder::getCreateTime, endTime);
        }
        return seckillOrderMapper.selectCount(wrapper);
    }

    @Override
    public long countByTimeRangeAndStatus(LocalDateTime startTime, LocalDateTime endTime, String status) {
        Integer statusCode = convertStatusToCode(status);
        LambdaQueryWrapper<SeckillOrder> wrapper = new LambdaQueryWrapper<>();
        if (startTime != null) {
            wrapper.ge(SeckillOrder::getCreateTime, startTime);
        }
        if (endTime != null) {
            wrapper.le(SeckillOrder::getCreateTime, endTime);
        }
        wrapper.eq(SeckillOrder::getStatus, statusCode);
        return seckillOrderMapper.selectCount(wrapper);
    }

    @Override
    public BigDecimal sumAmountByTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        LambdaQueryWrapper<SeckillOrder> wrapper = new LambdaQueryWrapper<>();
        if (startTime != null) {
            wrapper.ge(SeckillOrder::getCreateTime, startTime);
        }
        if (endTime != null) {
            wrapper.le(SeckillOrder::getCreateTime, endTime);
        }
        wrapper.eq(SeckillOrder::getStatus, 1);

        return seckillOrderMapper.selectList(wrapper).stream()
                .map(order -> order.getSeckillPrice().multiply(new BigDecimal(order.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public Map<String, Object> getSalesTrend(int days) {
        LocalDateTime startTime = LocalDateTime.now().minusDays(days);
        List<Map<String, Object>> dailyData = seckillOrderMapper.selectSalesTrendByDay(startTime);

        Map<String, Object> trend = new HashMap<>();
        trend.put("days", days);
        trend.put("data", dailyData);

        return trend;
    }

    @Override
    public long countDistinctUsersByTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        LambdaQueryWrapper<SeckillOrder> wrapper = new LambdaQueryWrapper<>();
        if (startTime != null) {
            wrapper.ge(SeckillOrder::getCreateTime, startTime);
        }
        if (endTime != null) {
            wrapper.le(SeckillOrder::getCreateTime, endTime);
        }

        return seckillOrderMapper.selectList(wrapper).stream()
                .map(SeckillOrder::getUserId)
                .distinct()
                .count();
    }

    @Override
    public SeckillOrder getByOrderId(Integer orderId) {
        if (orderId == null) {
            return null;
        }

        LambdaQueryWrapper<SeckillOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SeckillOrder::getOrderId, orderId);

        return seckillOrderMapper.selectOne(wrapper);
    }

    @Override
    public boolean updateById(SeckillOrder seckillOrder) {
        if (seckillOrder == null || seckillOrder.getId() == null) {
            return false;
        }

        int rows = seckillOrderMapper.updateById(seckillOrder);
        return rows > 0;
    }

    private Integer convertStatusToCode(String status) {
        if (status == null) {
            return null;
        }
        switch (status.toUpperCase()) {
            case "PENDING":
                return 0;
            case "SUCCESS":
                return 1;
            case "CANCELLED":
                return 2;
            default:
                return null;
        }
    }

    private String convertStatusToText(Integer status) {
        if (status == null) {
            return "未知";
        }
        switch (status) {
            case 0:
                return "待支付";
            case 1:
                return "已支付";
            case 2:
                return "已取消";
            default:
                return "未知";
        }
    }
}
