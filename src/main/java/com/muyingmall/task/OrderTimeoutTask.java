package com.muyingmall.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.muyingmall.entity.Order;
import com.muyingmall.entity.OrderProduct;
import com.muyingmall.entity.Product;
import com.muyingmall.enums.OrderStatus;
import com.muyingmall.mapper.OrderMapper;
import com.muyingmall.mapper.OrderProductMapper;
import com.muyingmall.service.OrderStateService;
import com.muyingmall.service.ProductService;
import com.muyingmall.statemachine.OrderEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单超时自动取消任务
 * 定时检查并取消超过5分钟未支付的订单
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class OrderTimeoutTask {

    private final OrderMapper orderMapper;
    private final OrderStateService orderStateService;
    private final OrderProductMapper orderProductMapper;
    private final ProductService productService;

    /**
     * 每分钟执行一次，检查并取消超时订单
     * cron表达式：秒 分 时 日 月 周
     */
    @Scheduled(cron = "0 */1 * * * *")
    public void cancelTimeoutOrders() {
        log.info("开始执行订单超时自动取消任务");

        try {
            // 计算5分钟前的时间点
            LocalDateTime timeoutThreshold = LocalDateTime.now().minusMinutes(5);

            // 查询超时未支付的订单
            LambdaQueryWrapper<Order> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Order::getStatus, OrderStatus.PENDING_PAYMENT)
                    .lt(Order::getCreateTime, timeoutThreshold);

            List<Order> timeoutOrders = orderMapper.selectList(queryWrapper);

            log.info("发现 {} 个超时未支付订单需要取消", timeoutOrders.size());

            // 逐个取消超时订单
            for (Order order : timeoutOrders) {
                try {
                    // 使用状态机发送超时事件
                    orderStateService.sendEvent(
                            order,
                            OrderEvent.TIMEOUT,
                            "system",
                            "订单超过5分钟未支付，系统自动取消");

                    // 恢复库存
                    restoreStock(order.getOrderId());

                    log.info("成功取消超时订单: orderId={}, orderNo={}, userId={}, createTime={}",
                            order.getOrderId(), order.getOrderNo(), order.getUserId(), order.getCreateTime());
                } catch (Exception e) {
                    log.error("取消超时订单失败: orderId={}, error={}", order.getOrderId(), e.getMessage(), e);
                }
            }

            log.info("订单超时自动取消任务执行完成，共处理 {} 个订单", timeoutOrders.size());
        } catch (Exception e) {
            log.error("订单超时自动取消任务执行异常", e);
        }
    }

    /**
     * 恢复订单商品库存
     * 
     * @param orderId 订单ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void restoreStock(Integer orderId) {
        try {
            // 查询订单商品
            LambdaQueryWrapper<OrderProduct> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(OrderProduct::getOrderId, orderId);
            List<OrderProduct> orderProducts = orderProductMapper.selectList(queryWrapper);

            if (orderProducts.isEmpty()) {
                log.warn("订单 {} 没有关联的商品，无需恢复库存", orderId);
                return;
            }

            // 逐个恢复商品库存
            for (OrderProduct orderProduct : orderProducts) {
                productService.update(
                        new LambdaUpdateWrapper<Product>()
                                .eq(Product::getProductId, orderProduct.getProductId())
                                .setSql("stock = stock + " + orderProduct.getQuantity()));

                log.info("已恢复商品库存: 商品ID={}, 数量={}", orderProduct.getProductId(), orderProduct.getQuantity());
            }

            log.info("订单 {} 的所有商品库存已恢复", orderId);
        } catch (Exception e) {
            log.error("恢复订单 {} 商品库存失败: {}", orderId, e.getMessage(), e);
            throw e; // 重新抛出异常，确保事务回滚
        }
    }
}