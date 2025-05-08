package com.muyingmall.statemachine;

import com.muyingmall.entity.Payment;
import com.muyingmall.enums.PaymentStatus;
import lombok.Data;

/**
 * 支付状态上下文
 */
@Data
public class PaymentStateContext {

    /**
     * 支付
     */
    private Payment payment;

    /**
     * 原支付状态
     */
    private PaymentStatus oldStatus;

    /**
     * 新支付状态
     */
    private PaymentStatus newStatus;

    /**
     * 触发的事件
     */
    private PaymentEvent event;

    /**
     * 状态变更的原因
     */
    private String reason;

    /**
     * 状态变更的操作者
     */
    private String operator;

    /**
     * 第三方交易号
     */
    private String transactionId;

    /**
     * 状态变更时间
     */
    private long timestamp = System.currentTimeMillis();

    /**
     * 创建上下文
     *
     * @param payment 支付
     * @return 支付状态上下文
     */
    public static PaymentStateContext of(Payment payment) {
        PaymentStateContext context = new PaymentStateContext();
        context.setPayment(payment);
        context.setOldStatus(payment.getStatus());
        return context;
    }
}