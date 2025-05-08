package com.muyingmall.statemachine;

/**
 * 支付事件枚举
 */
public enum PaymentEvent {
    /**
     * 创建支付
     */
    CREATE,

    /**
     * 处理中
     */
    PROCESS,

    /**
     * 支付成功
     */
    SUCCESS,

    /**
     * 支付失败
     */
    FAIL,

    /**
     * 关闭支付
     */
    CLOSE,

    /**
     * 申请退款
     */
    REFUND_REQUEST,

    /**
     * 退款成功
     */
    REFUND_SUCCESS,

    /**
     * 退款失败
     */
    REFUND_FAIL,

    /**
     * 支付超时
     */
    TIMEOUT
}