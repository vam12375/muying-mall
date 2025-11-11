package com.muyingmall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.muyingmall.common.exception.BusinessException;
import com.muyingmall.entity.PointsExchange;
import com.muyingmall.entity.PointsProduct;
import com.muyingmall.mapper.PointsExchangeMapper;
import com.muyingmall.service.PointsExchangeService;
import com.muyingmall.service.PointsOperationService;
import com.muyingmall.service.PointsProductService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 积分兑换服务实现类
 */
@Service
@RequiredArgsConstructor
public class PointsExchangeServiceImpl extends ServiceImpl<PointsExchangeMapper, PointsExchange>
        implements PointsExchangeService {

    private final PointsProductService pointsProductService;
    private final PointsOperationService pointsOperationService;

    // 状态常量定义
    private static final String STATUS_PENDING = "pending";     // 待发货/待处理
    private static final String STATUS_SHIPPED = "shipped";     // 已发货
    private static final String STATUS_COMPLETED = "completed"; // 已完成
    private static final String STATUS_CANCELLED = "cancelled"; // 已取消

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PointsExchange createExchange(PointsExchange exchange) {
        // 查询商品
        PointsProduct product = pointsProductService.getById(exchange.getProductId());
        if (product == null) {
            throw new BusinessException("商品不存在");
        }

        if (product.getStatus() != 1) {
            throw new BusinessException("商品已下架");
        }

        if (product.getStock() < exchange.getQuantity()) {
            throw new BusinessException("商品库存不足");
        }

        // 计算所需积分
        int totalPoints = product.getPoints() * exchange.getQuantity();

        // 获取用户积分
        Integer userPoints = pointsOperationService.getUserPoints(exchange.getUserId());
        if (userPoints < totalPoints) {
            throw new BusinessException("积分不足");
        }

        // 设置订单号
        exchange.setOrderNo(generateOrderNo());
        exchange.setPoints(totalPoints);
        exchange.setStatus(STATUS_PENDING); // 待发货
        exchange.setCreateTime(LocalDateTime.now());
        exchange.setUpdateTime(LocalDateTime.now());

        // 保存兑换记录
        this.save(exchange);

        // 扣减积分
        pointsOperationService.deductPoints(exchange.getUserId(), totalPoints, "exchange", exchange.getOrderNo(),
                "积分兑换商品");

        // 减少库存
        pointsProductService.update(
                new LambdaUpdateWrapper<PointsProduct>()
                        .eq(PointsProduct::getId, product.getId())
                        .setSql("stock = stock - " + exchange.getQuantity()));

        return exchange;
    }

    @Override
    public Page<PointsExchange> getUserExchanges(Integer userId, int page, int size, Integer status) {
        Page<PointsExchange> pageParam = new Page<>(page, size);

        LambdaQueryWrapper<PointsExchange> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PointsExchange::getUserId, userId);

        if (status != null) {
            queryWrapper.eq(PointsExchange::getStatus, status);
        }

        queryWrapper.orderByDesc(PointsExchange::getCreateTime);

        return page(pageParam, queryWrapper);
    }

    @Override
    public Page<Map<String, Object>> getUserExchangesWithProduct(Integer userId, int page, int size, Integer status) {
        Page<PointsExchange> pageParam = new Page<>(page, size);

        LambdaQueryWrapper<PointsExchange> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PointsExchange::getUserId, userId);

        if (status != null) {
            queryWrapper.eq(PointsExchange::getStatus, status);
        }

        queryWrapper.orderByDesc(PointsExchange::getCreateTime);

        // 查询兑换记录
        Page<PointsExchange> exchangePage = page(pageParam, queryWrapper);

        // 转换为包含商品信息的Map
        List<Map<String, Object>> records = new ArrayList<>();
        for (PointsExchange exchange : exchangePage.getRecords()) {
            Map<String, Object> record = new HashMap<>();
            record.put("id", exchange.getId());
            record.put("orderNo", exchange.getOrderNo());
            record.put("userId", exchange.getUserId());
            record.put("productId", exchange.getProductId());
            record.put("quantity", exchange.getQuantity());
            record.put("points", exchange.getPoints());
            record.put("addressId", exchange.getAddressId());
            record.put("phone", exchange.getPhone());
            record.put("status", exchange.getStatus());
            record.put("trackingNo", exchange.getTrackingNumber());
            record.put("trackingCompany", exchange.getLogisticsCompany());
            record.put("remark", exchange.getRemark());
            record.put("createTime", exchange.getCreateTime());
            record.put("updateTime", exchange.getUpdateTime());

            // 获取商品信息
            PointsProduct product = pointsProductService.getById(exchange.getProductId());
            if (product != null) {
                record.put("productName", product.getName());
                record.put("productImage", product.getImage());
                record.put("productCategory", product.getCategory());
                record.put("productDescription", product.getDescription());
            }

            records.add(record);
        }

        Page<Map<String, Object>> resultPage = new Page<>(page, size, exchangePage.getTotal());
        resultPage.setRecords(records);

        return resultPage;
    }

    @Override
    public PointsExchange getExchangeDetail(Long id) {
        return getById(id);
    }

    @Override
    public Map<String, Object> getExchangeDetailWithInfo(Long id) {
        return baseMapper.selectExchangeDetailById(id);
    }

    @Override
    public Map<String, Object> getUserExchangeStats(Integer userId) {
        return baseMapper.selectUserExchangeStats(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean ship(Long id, String trackingNo, String trackingCompany) {
        PointsExchange exchange = getById(id);
        if (exchange == null) {
            throw new BusinessException("兑换记录不存在");
        }

        if (!STATUS_PENDING.equals(exchange.getStatus())) {
            throw new BusinessException("只有待发货状态才能发货");
        }

        exchange.setStatus(STATUS_SHIPPED); // 已发货
        exchange.setTrackingNumber(trackingNo);
        exchange.setLogisticsCompany(trackingCompany);
        exchange.setUpdateTime(LocalDateTime.now());

        return updateById(exchange);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean complete(Long id) {
        PointsExchange exchange = getById(id);
        if (exchange == null) {
            throw new BusinessException("兑换记录不存在");
        }

        if (!STATUS_SHIPPED.equals(exchange.getStatus())) {
            throw new BusinessException("只有已发货状态才能完成");
        }

        exchange.setStatus(STATUS_COMPLETED); // 已完成
        exchange.setUpdateTime(LocalDateTime.now());

        return updateById(exchange);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean cancel(Long id, String reason) {
        PointsExchange exchange = getById(id);
        if (exchange == null) {
            throw new BusinessException("兑换记录不存在");
        }

        if (!STATUS_PENDING.equals(exchange.getStatus())) {
            throw new BusinessException("只有待发货状态才能取消");
        }

        exchange.setStatus(STATUS_CANCELLED); // 已取消
        exchange.setRemark(reason);
        exchange.setUpdateTime(LocalDateTime.now());

        boolean result = updateById(exchange);

        if (result) {
            // 返还积分
            pointsOperationService.addPoints(exchange.getUserId(), exchange.getPoints(), "exchange_cancel",
                    exchange.getOrderNo(), "兑换取消返还积分");

            // 恢复库存
            pointsProductService.update(
                    new LambdaUpdateWrapper<PointsProduct>()
                            .eq(PointsProduct::getId, exchange.getProductId())
                            .setSql("stock = stock + " + exchange.getQuantity()));
        }

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean cancelExchange(Long id, Integer userId) {
        PointsExchange exchange = getById(id);
        if (exchange == null) {
            throw new BusinessException("兑换记录不存在");
        }

        // 验证是否是用户自己的兑换记录
        if (!exchange.getUserId().equals(userId)) {
            throw new BusinessException("无权操作此兑换记录");
        }

        // 只有待发货状态才能取消
        if (!STATUS_PENDING.equals(exchange.getStatus())) {
            throw new BusinessException("只有待发货状态才能取消");
        }

        exchange.setStatus(STATUS_CANCELLED); // 已取消
        exchange.setRemark("用户取消兑换");
        exchange.setUpdateTime(LocalDateTime.now());

        boolean result = updateById(exchange);

        if (result) {
            // 返还积分
            int totalPoints = exchange.getPoints() * exchange.getQuantity();
            pointsOperationService.addPoints(exchange.getUserId(), totalPoints, "exchange_cancel",
                    exchange.getOrderNo(), "兑换取消返还积分");

            // 恢复库存
            pointsProductService.update(
                    new LambdaUpdateWrapper<PointsProduct>()
                            .eq(PointsProduct::getId, exchange.getProductId())
                            .setSql("stock = stock + " + exchange.getQuantity()));
        }

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean confirmReceive(Long id, Integer userId) {
        PointsExchange exchange = getById(id);
        if (exchange == null) {
            throw new BusinessException("兑换记录不存在");
        }

        // 验证是否是用户自己的兑换记录
        if (!exchange.getUserId().equals(userId)) {
            throw new BusinessException("无权操作此兑换记录");
        }

        // 只有已发货状态才能确认收货
        if (!STATUS_SHIPPED.equals(exchange.getStatus())) {
            throw new BusinessException("只有已发货状态才能确认收货");
        }

        exchange.setStatus(STATUS_COMPLETED); // 已完成
        exchange.setUpdateTime(LocalDateTime.now());

        return updateById(exchange);
    }

    /**
     * 生成订单号
     */
    private String generateOrderNo() {
        return "PE" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 6);
    }
}