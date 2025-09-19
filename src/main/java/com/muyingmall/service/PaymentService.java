package com.muyingmall.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.muyingmall.entity.Payment;

/**
 * 支付服务接口
 */
public interface PaymentService extends IService<Payment> {

    /**
     * 创建支付订单
     * 
     * @param payment 支付信息
     * @return 支付订单
     */
    Payment createPayment(Payment payment);

    /**
     * 根据支付单号查询支付信息
     * 
     * @param paymentNo 支付单号
     * @return 支付信息
     */
    Payment getByPaymentNo(String paymentNo);

    /**
     * 更新支付状态
     * 
     * @param paymentNo 支付单号
     * @param status    支付状态
     * @return 是否更新成功
     */
    boolean updatePaymentStatus(String paymentNo, Integer status);

    /**
     * 根据订单号查询支付信息
     * 
     * @param orderNo 订单号
     * @return 支付信息
     */
    Payment getByOrderNo(String orderNo);

    /**
     * 处理退款
     * 
     * @param paymentNo 支付单号
     * @param refundAmount 退款金额
     * @param reason 退款原因
     * @return 退款是否成功
     */
    boolean processRefund(String paymentNo, java.math.BigDecimal refundAmount, String reason);
}