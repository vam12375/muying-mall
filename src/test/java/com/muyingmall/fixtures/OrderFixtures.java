package com.muyingmall.fixtures;

import com.muyingmall.entity.Order;
import com.muyingmall.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单测试夹具。
 * 集中封装 Order 实体的标准构造，避免各测试类重复样板代码。
 */
public final class OrderFixtures {

    private OrderFixtures() {
    }

    /**
     * 构造一个待支付订单。
     */
    public static Order pendingPayment(Integer orderId, Integer userId, BigDecimal amount) {
        Order order = new Order();
        order.setOrderId(orderId);
        order.setOrderNo("ORD" + orderId);
        order.setUserId(userId);
        order.setTotalAmount(amount);
        order.setActualAmount(amount);
        order.setStatus(OrderStatus.PENDING_PAYMENT);
        order.setCreateTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        return order;
    }

    /**
     * 构造一个已支付待发货订单。
     */
    public static Order paid(Integer orderId, Integer userId, BigDecimal amount) {
        Order order = pendingPayment(orderId, userId, amount);
        order.setStatus(OrderStatus.PENDING_SHIPMENT);
        order.setPaymentMethod("alipay");
        order.setPayTime(LocalDateTime.now());
        order.setPaidTime(LocalDateTime.now());
        return order;
    }

    /**
     * 构造指定状态的订单。
     */
    public static Order withStatus(Integer orderId, Integer userId, BigDecimal amount, OrderStatus status) {
        Order order = pendingPayment(orderId, userId, amount);
        order.setStatus(status);
        return order;
    }
}
