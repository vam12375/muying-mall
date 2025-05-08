package com.muyingmall.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.muyingmall.enums.PaymentStatus;
import com.muyingmall.statemachine.PaymentEvent;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 支付状态变更日志实体类
 */
@Data
@TableName("payment_state_log")
public class PaymentStateLog implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 日志ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    @TableField(updateStrategy = FieldStrategy.NEVER)
    private Long id;

    /**
     * 支付ID
     */
    private Long paymentId;

    /**
     * 支付单号
     */
    private String paymentNo;

    /**
     * 订单ID
     */
    private Integer orderId;

    /**
     * 订单编号
     */
    private String orderNo;

    /**
     * 原状态
     */
    private PaymentStatus oldStatus;

    /**
     * 新状态
     */
    private PaymentStatus newStatus;

    /**
     * 触发事件
     */
    private PaymentEvent event;

    /**
     * 操作者
     */
    private String operator;

    /**
     * 变更原因
     */
    private String reason;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 创建支付状态变更日志
     *
     * @param paymentId 支付ID
     * @param paymentNo 支付单号
     * @param orderId   订单ID
     * @param orderNo   订单编号
     * @param oldStatus 原状态
     * @param newStatus 新状态
     * @param event     触发事件
     * @param operator  操作者
     * @param reason    原因
     * @return 支付状态变更日志
     */
    public static PaymentStateLog of(Long paymentId, String paymentNo, Integer orderId, String orderNo,
            PaymentStatus oldStatus, PaymentStatus newStatus,
            PaymentEvent event, String operator, String reason) {
        PaymentStateLog log = new PaymentStateLog();
        log.setPaymentId(paymentId);
        log.setPaymentNo(paymentNo);
        log.setOrderId(orderId);
        log.setOrderNo(orderNo);
        log.setOldStatus(oldStatus);
        log.setNewStatus(newStatus);
        log.setEvent(event);
        log.setOperator(operator);
        log.setReason(reason);
        return log;
    }
}