package com.muyingmall.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单消息实体类
 * 用于RabbitMQ消息队列传输订单相关信息
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderMessage implements Serializable {

    private static final long serialVersionUID = 1L;

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
     * 原订单状态
     */
    private String oldStatus;

    /**
     * 新订单状态
     */
    private String newStatus;

    /**
     * 订单总金额
     */
    private BigDecimal totalAmount;

    /**
     * 消息创建时间戳
     */
    private LocalDateTime timestamp;

    /**
     * 事件类型：CREATE-订单创建, STATUS_CHANGE-状态变更, CANCEL-订单取消, COMPLETE-订单完成
     */
    private String eventType;

    /**
     * 构造方法 - 订单创建事件
     */
    public static OrderMessage createOrderEvent(Integer orderId, String orderNo, Integer userId, BigDecimal totalAmount) {
        OrderMessage message = new OrderMessage();
        message.setOrderId(orderId);
        message.setOrderNo(orderNo);
        message.setUserId(userId);
        message.setTotalAmount(totalAmount);
        message.setEventType("CREATE");
        message.setNewStatus("pending_payment");
        message.setTimestamp(LocalDateTime.now());
        return message;
    }

    /**
     * 构造方法 - 订单状态变更事件
     */
    public static OrderMessage statusChangeEvent(Integer orderId, String orderNo, Integer userId, 
                                               String oldStatus, String newStatus, BigDecimal totalAmount) {
        OrderMessage message = new OrderMessage();
        message.setOrderId(orderId);
        message.setOrderNo(orderNo);
        message.setUserId(userId);
        message.setOldStatus(oldStatus);
        message.setNewStatus(newStatus);
        message.setTotalAmount(totalAmount);
        message.setEventType("STATUS_CHANGE");
        message.setTimestamp(LocalDateTime.now());
        return message;
    }

    /**
     * 构造方法 - 订单取消事件
     */
    public static OrderMessage cancelOrderEvent(Integer orderId, String orderNo, Integer userId, BigDecimal totalAmount) {
        OrderMessage message = new OrderMessage();
        message.setOrderId(orderId);
        message.setOrderNo(orderNo);
        message.setUserId(userId);
        message.setOldStatus("pending_payment");
        message.setNewStatus("cancelled");
        message.setTotalAmount(totalAmount);
        message.setEventType("CANCEL");
        message.setTimestamp(LocalDateTime.now());
        return message;
    }

    /**
     * 构造方法 - 订单完成事件
     */
    public static OrderMessage completeOrderEvent(Integer orderId, String orderNo, Integer userId, BigDecimal totalAmount) {
        OrderMessage message = new OrderMessage();
        message.setOrderId(orderId);
        message.setOrderNo(orderNo);
        message.setUserId(userId);
        message.setOldStatus("shipped");
        message.setNewStatus("completed");
        message.setTotalAmount(totalAmount);
        message.setEventType("COMPLETE");
        message.setTimestamp(LocalDateTime.now());
        return message;
    }
}