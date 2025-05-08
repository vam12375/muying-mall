package com.muyingmall.statemachine;

import com.muyingmall.entity.Order;
import com.muyingmall.enums.OrderStatus;
import lombok.Data;

/**
 * 订单状态上下文
 */
@Data
public class OrderStateContext {

    /**
     * 订单
     */
    private Order order;

    /**
     * 原订单状态
     */
    private OrderStatus oldStatus;

    /**
     * 新订单状态
     */
    private OrderStatus newStatus;

    /**
     * 触发的事件
     */
    private OrderEvent event;

    /**
     * 状态变更的原因
     */
    private String reason;

    /**
     * 状态变更的操作者
     */
    private String operator;

    /**
     * 状态变更时间
     */
    private long timestamp = System.currentTimeMillis();

    /**
     * 创建上下文
     *
     * @param order 订单
     * @return 订单状态上下文
     */
    public static OrderStateContext of(Order order) {
        OrderStateContext context = new OrderStateContext();
        context.setOrder(order);
        context.setOldStatus(order.getStatus());
        return context;
    }
}