package com.muyingmall.statemachine;

/**
 * 订单事件枚举
 */
public enum OrderEvent {
    /**
     * 支付事件
     */
    PAID,

    /**
     * 发货事件
     */
    SHIP,

    /**
     * 收货事件
     */
    RECEIVE,

    /**
     * 取消事件
     */
    CANCEL,

    /**
     * 超时事件
     */
    TIMEOUT
}