package com.muyingmall.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 支付状态枚举
 */
@Getter
public enum PaymentStatus {

    /**
     * 待支付
     */
    PENDING(0, "待支付"),

    /**
     * 支付处理中
     */
    PROCESSING(1, "处理中"),

    /**
     * 支付成功
     */
    SUCCESS(2, "支付成功"),

    /**
     * 支付失败
     */
    FAILED(3, "支付失败"),

    /**
     * 已关闭
     */
    CLOSED(4, "已关闭"),

    /**
     * 退款中
     */
    REFUNDING(5, "退款中"),

    /**
     * 已退款
     */
    REFUNDED(6, "已退款");

    /**
     * 状态编码
     */
    @EnumValue
    private final int code;

    /**
     * 状态描述
     */
    @JsonValue
    private final String desc;

    PaymentStatus(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 根据状态编码获取支付状态枚举
     *
     * @param code 状态编码
     * @return 支付状态枚举
     */
    public static PaymentStatus getByCode(int code) {
        for (PaymentStatus status : PaymentStatus.values()) {
            if (status.getCode() == code) {
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
    public boolean canTransitionTo(PaymentStatus target) {
        switch (this) {
            case PENDING:
                return target == PROCESSING || target == CLOSED;
            case PROCESSING:
                return target == SUCCESS || target == FAILED || target == CLOSED;
            case SUCCESS:
                return target == REFUNDING;
            case REFUNDING:
                return target == REFUNDED || target == FAILED;
            case FAILED:
            case CLOSED:
            case REFUNDED:
                return false;
            default:
                return false;
        }
    }
}