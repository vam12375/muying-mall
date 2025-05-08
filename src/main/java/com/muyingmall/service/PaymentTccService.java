package com.muyingmall.service;

import com.muyingmall.entity.Payment;
import com.muyingmall.tcc.TccAction;

/**
 * 支付TCC事务服务接口
 */
public interface PaymentTccService extends TccAction<Payment, Payment> {

    /**
     * 创建支付并执行TCC事务
     *
     * @param payment 支付信息
     * @return 支付信息
     */
    Payment createPaymentWithTcc(Payment payment);

    /**
     * 处理支付并执行TCC事务
     *
     * @param paymentNo     支付单号
     * @param transactionId 第三方交易号
     * @return 支付信息
     */
    Payment processPaymentWithTcc(String paymentNo, String transactionId);

    /**
     * 关闭支付并执行TCC事务
     *
     * @param paymentNo 支付单号
     * @return 支付信息
     */
    Payment closePaymentWithTcc(String paymentNo);
}