package com.muyingmall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.muyingmall.common.exception.BusinessException;
import com.muyingmall.dto.SeckillOrderDTO;
import com.muyingmall.dto.SeckillRequestDTO;
import com.muyingmall.entity.SeckillOrder;
import com.muyingmall.entity.SeckillProduct;
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

    private final SeckillService seckillService;
    private final SeckillProductMapper seckillProductMapper;
    private final SeckillOrderMapper seckillOrderMapper;
    private final OrderService orderService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long executeSeckill(Integer userId, SeckillRequestDTO request) {
        // 1. 查询秒杀商品信息
        SeckillProduct seckillProduct = seckillProductMapper.selectById(request.getSeckillProductId());
        if (seckillProduct == null) {
            throw new BusinessException("秒杀商品不存在");
        }

        // 2. 使用Lua脚本原子性扣减库存（包含用户去重校验）
        int luaResult = ((SeckillServiceImpl) seckillService).deductStockWithLua(
                request.getSeckillProductId(),
                seckillProduct.getSkuId(),
                request.getQuantity(),
                userId);

        // 处理Lua脚本返回结果
        if (luaResult == -1) {
            throw new BusinessException("商品已售罄");
        } else if (luaResult == -2) {
            throw new BusinessException("您已参与过该秒杀活动");
        } else if (luaResult == -3) {
            throw new BusinessException("秒杀活动未开始或已结束");
        } else if (luaResult != 1) {
            throw new BusinessException("秒杀失败，请稍后重试");
        }

        try {
            // 3. 扣减数据库秒杀库存（使用乐观锁）
            int deductResult = seckillProductMapper.deductStock(
                    request.getSeckillProductId(),
                    request.getQuantity());

            if (deductResult <= 0) {
                // 库存扣减失败，恢复Redis库存和用户记录
                ((SeckillServiceImpl) seckillService).restoreStockWithLua(
                        request.getSeckillProductId(),
                        seckillProduct.getSkuId(),
                        request.getQuantity(),
                        userId);
                throw new BusinessException("库存不足，秒杀失败");
            }

            // 5. 使用directPurchase创建订单
            Map<String, Object> orderResult = orderService.directPurchase(
                    userId,
                    request.getAddressId().intValue(),
                    seckillProduct.getProductId().intValue(),
                    request.getQuantity(),
                    null, // specs
                    seckillProduct.getSkuId(),
                    "秒杀订单", // remark
                    "ALIPAY", // paymentMethod
                    null, // couponId
                    java.math.BigDecimal.ZERO, // shippingFee
                    0 // pointsUsed
            );

            // 6. 获取订单ID
            Long orderId = ((Number) orderResult.get("orderId")).longValue();

            // 7. 创建秒杀订单记录
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
            // 订单创建失败，恢复Redis库存和用户记录
            log.error("秒杀订单创建失败，恢复库存: userId={}, skuId={}", userId, seckillProduct.getSkuId(), e);
            ((SeckillServiceImpl) seckillService).restoreStockWithLua(
                    request.getSeckillProductId(),
                    seckillProduct.getSkuId(),
                    request.getQuantity(),
                    userId);
            throw new BusinessException("秒杀失败: " + e.getMessage());
        }
    }

    @Override
    public boolean canUserParticipate(Integer userId, Long seckillProductId) {
        // 查询秒杀商品信息
        SeckillProduct seckillProduct = seckillProductMapper.selectById(seckillProductId);
        if (seckillProduct == null) {
            return false;
        }

        // 查询用户已购买数量
        Integer purchasedCount = seckillOrderMapper.countUserPurchase(
                userId,
                seckillProduct.getActivityId(),
                seckillProductId);

        // 检查是否超过限购
        return purchasedCount < seckillProduct.getLimitPerUser();
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
            // 设置状态文本
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
        // 查询活动下所有已支付订单
        LambdaQueryWrapper<SeckillOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SeckillOrder::getActivityId, activityId)
                .eq(SeckillOrder::getStatus, 1); // 已支付

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
                .eq(SeckillOrder::getStatus, 1); // 已支付

        return seckillOrderMapper.selectList(wrapper).stream()
                .mapToInt(SeckillOrder::getQuantity)
                .sum();
    }

    @Override
    public BigDecimal sumAmountByProductId(Long productId) {
        LambdaQueryWrapper<SeckillOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SeckillOrder::getSeckillProductId, productId)
                .eq(SeckillOrder::getStatus, 1); // 已支付

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
        wrapper.eq(SeckillOrder::getStatus, 1); // 已支付

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

        // 查询所有订单，然后统计不同的用户ID
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

    /**
     * 将状态字符串转换为状态码
     */
    private Integer convertStatusToCode(String status) {
        if (status == null) {
            return null;
        }
        switch (status.toUpperCase()) {
            case "PENDING":
                return 0; // 待支付
            case "SUCCESS":
                return 1; // 已支付
            case "CANCELLED":
                return 2; // 已取消
            default:
                return null;
        }
    }

    /**
     * 将状态码转换为状态文本
     */
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
