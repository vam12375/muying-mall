package com.muyingmall.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 订单状态枚举
 */
@Getter
public enum OrderStatus {

    /**
     * 待确认（TCC事务Try阶段，资源已预留）
     */
    PENDING_CONFIRMATION("pending_confirmation", "待确认"),

    /**
     * 待支付
     */
    PENDING_PAYMENT("pending_payment", "待支付"),

    /**
     * 已支付，待发货
     */
    PENDING_SHIPMENT("pending_shipment", "待发货"),

    /**
     * 已发货
     */
    SHIPPED("shipped", "已发货"),

    /**
     * 已完成
     */
    COMPLETED("completed", "已完成"),

    /**
     * 已取消
     */
    CANCELLED("cancelled", "已取消"),

    /**
     * 退款中
     */
    REFUNDING("refunding", "退款中"),

    /**
     * 已退款
     */
    REFUNDED("refunded", "已退款");

    /**
     * 状态编码
     */
    @EnumValue
    @JsonValue
    private final String code;

    /**
     * 状态描述
     */
    private final String desc;

    OrderStatus(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 根据状态编码获取订单状态枚举
     *
     * @param code 状态编码
     * @return 订单状态枚举
     */
    public static OrderStatus getByCode(String code) {
        for (OrderStatus status : OrderStatus.values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }

    /**
     * 判断是否可以转换为目标状态
     *
     * @param target 目标状态
     * @return 是否可以转换
     */
    public boolean canTransitionTo(OrderStatus target) {
        switch (this) {
            case PENDING_CONFIRMATION:
                // TCC待确认状态可转为待支付（Confirm）或取消（Cancel）
                return target == PENDING_PAYMENT || target == CANCELLED;
            case PENDING_PAYMENT:
                return target == PENDING_SHIPMENT || target == CANCELLED;
            case PENDING_SHIPMENT:
                return target == SHIPPED || target == CANCELLED || target == REFUNDING;
            case SHIPPED:
                return target == COMPLETED || target == CANCELLED || target == REFUNDING;
            case COMPLETED:
                return target == REFUNDING;
            case REFUNDING:
                return target == REFUNDED || target == COMPLETED;
            case REFUNDED:
            case CANCELLED:
                return false;
            default:
                return false;
        }
    }
}