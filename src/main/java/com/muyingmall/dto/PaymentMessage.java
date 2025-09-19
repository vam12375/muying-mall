package com.muyingmall.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付消息实体类
 * 用于RabbitMQ消息队列传输支付相关信息
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 支付ID
     */
    private Long paymentId;

    /**
     * 订单ID
     */
    private Integer orderId;

    /**
     * 订单编号
     */
    private String orderNo;

    /**
     * 用户ID
     */
    private Integer userId;

    /**
     * 支付金额
     */
    private BigDecimal amount;

    /**
     * 支付方式：alipay-支付宝, wechat-微信, wallet-钱包
     */
    private String paymentMethod;

    /**
     * 支付状态：pending-待支付, processing-支付中, success-支付成功, failed-支付失败, closed-已关闭
     */
    private String status;

    /**
     * 消息创建时间戳
     */
    private LocalDateTime timestamp;

    /**
     * 事件类型：SUCCESS-支付成功, FAILED-支付失败, REFUND-退款处理
     */
    private String eventType;

    /**
     * 第三方支付流水号
     */
    private String transactionId;

    /**
     * 额外信息（如退款原因等）
     */
    private String extra;

    /**
     * 构造方法 - 支付成功事件
     */
    public static PaymentMessage successEvent(Long paymentId, Integer orderId, String orderNo,
                                            Integer userId, BigDecimal amount, String paymentMethod,
                                            String transactionId) {
        PaymentMessage message = new PaymentMessage();
        message.setPaymentId(paymentId);
        message.setOrderId(orderId);
        message.setOrderNo(orderNo);
        message.setUserId(userId);
        message.setAmount(amount);
        message.setPaymentMethod(paymentMethod);
        message.setStatus("success");
        message.setEventType("SUCCESS");
        message.setTransactionId(transactionId);
        message.setTimestamp(LocalDateTime.now());
        return message;
    }

    /**
     * 构造方法 - 支付失败事件
     */
    public static PaymentMessage failedEvent(Long paymentId, Integer orderId, String orderNo,
                                           Integer userId, BigDecimal amount, String paymentMethod) {
        PaymentMessage message = new PaymentMessage();
        message.setPaymentId(paymentId);
        message.setOrderId(orderId);
        message.setOrderNo(orderNo);
        message.setUserId(userId);
        message.setAmount(amount);
        message.setPaymentMethod(paymentMethod);
        message.setStatus("failed");
        message.setEventType("FAILED");
        message.setTimestamp(LocalDateTime.now());
        return message;
    }

    /**
     * 构造方法 - 退款事件
     */
    public static PaymentMessage refundEvent(Long paymentId, Integer orderId, String orderNo,
                                           Integer userId, BigDecimal amount, String paymentMethod,
                                           String transactionId) {
        PaymentMessage message = new PaymentMessage();
        message.setPaymentId(paymentId);
        message.setOrderId(orderId);
        message.setOrderNo(orderNo);
        message.setUserId(userId);
        message.setAmount(amount);
        message.setPaymentMethod(paymentMethod);
        message.setStatus("refund");
        message.setEventType("REFUND");
        message.setTransactionId(transactionId);
        message.setTimestamp(LocalDateTime.now());
        return message;
    }

    /**
     * 构造方法 - 支付请求消息
     */
    public static PaymentMessage createRequestMessage(com.muyingmall.entity.Payment payment) {
        PaymentMessage message = new PaymentMessage();
        message.setPaymentId(payment.getId());
        message.setOrderId(payment.getOrderId());
        message.setOrderNo(payment.getOrderNo());
        message.setUserId(payment.getUserId());
        message.setAmount(payment.getAmount());
        message.setPaymentMethod(payment.getPaymentMethod());
        message.setStatus("pending");
        message.setEventType("REQUEST");
        message.setTimestamp(LocalDateTime.now());
        return message;
    }

    /**
     * 构造方法 - 支付失败消息
     */
    public static PaymentMessage createFailedMessage(com.muyingmall.entity.Payment payment, String reason) {
        PaymentMessage message = new PaymentMessage();
        message.setPaymentId(payment.getId());
        message.setOrderId(payment.getOrderId());
        message.setOrderNo(payment.getOrderNo());
        message.setUserId(payment.getUserId());
        message.setAmount(payment.getAmount());
        message.setPaymentMethod(payment.getPaymentMethod());
        message.setStatus("failed");
        message.setEventType("FAILED");
        message.setExtra(reason);
        message.setTransactionId(payment.getTransactionId());
        message.setTimestamp(LocalDateTime.now());
        return message;
    }

    /**
     * 构造方法 - 支付成功消息
     */
    public static PaymentMessage createSuccessMessage(com.muyingmall.entity.Payment payment) {
        PaymentMessage message = new PaymentMessage();
        message.setPaymentId(payment.getId());
        message.setOrderId(payment.getOrderId());
        message.setOrderNo(payment.getOrderNo());
        message.setUserId(payment.getUserId());
        message.setAmount(payment.getAmount());
        message.setPaymentMethod(payment.getPaymentMethod());
        message.setStatus("success");
        message.setEventType("SUCCESS");
        message.setTransactionId(payment.getTransactionId());
        message.setTimestamp(LocalDateTime.now());
        return message;
    }

    /**
     * 构造方法 - 退款消息
     */
    public static PaymentMessage createRefundMessage(com.muyingmall.entity.Payment payment, BigDecimal refundAmount) {
        PaymentMessage message = new PaymentMessage();
        message.setPaymentId(payment.getId());
        message.setOrderId(payment.getOrderId());
        message.setOrderNo(payment.getOrderNo());
        message.setUserId(payment.getUserId());
        message.setAmount(refundAmount); // 使用退款金额
        message.setPaymentMethod(payment.getPaymentMethod());
        message.setStatus("refunding");
        message.setEventType("REFUND");
        message.setTransactionId(payment.getTransactionId());
        message.setTimestamp(LocalDateTime.now());
        return message;
    }
}