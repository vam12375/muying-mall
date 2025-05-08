package com.muyingmall.event;

import lombok.Getter;

import java.math.BigDecimal;

/**
 * 订单完成事件 (用户确认收货后触发)
 */
@Getter
public class OrderCompletedEvent {

    private final Integer orderId;
    private final Integer userId;
    private final BigDecimal actualAmount;
    private final String orderNo;

    public OrderCompletedEvent(Integer orderId, Integer userId, BigDecimal actualAmount, String orderNo) {
        this.orderId = orderId;
        this.userId = userId;
        this.actualAmount = actualAmount;
        this.orderNo = orderNo;
    }
}