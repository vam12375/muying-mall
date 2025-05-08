package com.muyingmall.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.muyingmall.enums.OrderStatus;
import com.muyingmall.statemachine.OrderEvent;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 订单状态变更日志实体类
 */
@Data
@TableName("order_state_log")
public class OrderStateLog implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 日志ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    @TableField(updateStrategy = FieldStrategy.NEVER)
    private Long id;

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
    private OrderStatus oldStatus;

    /**
     * 新状态
     */
    private OrderStatus newStatus;

    /**
     * 触发事件
     */
    private OrderEvent event;

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
     * 获取原状态码，用于JSON序列化
     * 
     * @return 状态码字符串
     */
    @JsonProperty("oldState")
    public String getOldStatusCode() {
        return oldStatus != null ? oldStatus.getCode() : null;
    }

    /**
     * 获取新状态码，用于JSON序列化
     * 
     * @return 状态码字符串
     */
    @JsonProperty("newState")
    public String getNewStatusCode() {
        return newStatus != null ? newStatus.getCode() : null;
    }

    /**
     * 创建订单状态变更日志
     *
     * @param orderId   订单ID
     * @param orderNo   订单编号
     * @param oldStatus 原状态
     * @param newStatus 新状态
     * @param event     触发事件
     * @param operator  操作者
     * @param reason    原因
     * @return 订单状态变更日志
     */
    public static OrderStateLog of(Integer orderId, String orderNo, OrderStatus oldStatus, OrderStatus newStatus,
            OrderEvent event, String operator, String reason) {
        OrderStateLog log = new OrderStateLog();
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