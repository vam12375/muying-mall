package com.muyingmall.statemachine;

/**
 * 退款事件枚举
 */
public enum RefundEvent {
    /**
     * 提交申请
     */
    SUBMIT,

    /**
     * 批准退款
     */
    APPROVE,

    /**
     * 拒绝退款
     */
    REJECT,

    /**
     * 开始处理退款
     */
    PROCESS,

    /**
     * 退款成功
     */
    COMPLETE,

    /**
     * 退款失败
     */
    FAIL,

    /**
     * 取消退款
     */
    CANCEL
}