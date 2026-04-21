package com.muyingmall.fixtures;

import com.muyingmall.entity.Payment;
import com.muyingmall.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付测试夹具。
 */
public final class PaymentFixtures {

    private PaymentFixtures() {
    }

    /**
     * 构造一个待支付记录。
     */
    public static Payment pending(Long id, Integer orderId, Integer userId, BigDecimal amount) {
        Payment payment = new Payment();
        payment.setId(id);
        payment.setPaymentNo("PAY" + id);
        payment.setOrderId(orderId);
        payment.setOrderNo("ORD" + orderId);
        payment.setUserId(userId);
        payment.setAmount(amount);
        payment.setPaymentMethod("alipay");
        payment.setStatus(PaymentStatus.PENDING);
        payment.setCreateTime(LocalDateTime.now());
        payment.setUpdateTime(LocalDateTime.now());
        return payment;
    }

    /**
     * 构造一个支付成功记录。
     */
    public static Payment success(Long id, Integer orderId, Integer userId, BigDecimal amount) {
        Payment payment = pending(id, orderId, userId, amount);
        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setTransactionId("AP-TXN" + id);
        payment.setPayTime(LocalDateTime.now());
        payment.setPaymentTime(LocalDateTime.now());
        return payment;
    }

    /**
     * 构造指定状态的支付记录。
     */
    public static Payment withStatus(Long id, Integer orderId, Integer userId, BigDecimal amount, PaymentStatus status) {
        Payment payment = pending(id, orderId, userId, amount);
        payment.setStatus(status);
        return payment;
    }
}
