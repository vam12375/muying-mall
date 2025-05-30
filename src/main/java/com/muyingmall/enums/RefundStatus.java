package com.muyingmall.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 退款状态枚举
 */
@Getter
public enum RefundStatus {

    /**
     * 待处理
     */
    PENDING("PENDING", "待处理"),

    /**
     * 已批准
     */
    APPROVED("APPROVED", "已批准"),

    /**
     * 已拒绝
     */
    REJECTED("REJECTED", "已拒绝"),

    /**
     * 处理中
     */
    PROCESSING("PROCESSING", "处理中"),

    /**
     * 已完成
     */
    COMPLETED("COMPLETED", "已完成"),

    /**
     * 退款失败
     */
    FAILED("FAILED", "退款失败");

    /**
     * 状态编码
     */
    @JsonValue
    private final String code;

    /**
     * 状态描述
     */
    private final String desc;

    RefundStatus(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 根据状态编码获取退款状态枚举
     *
     * @param code 状态编码
     * @return 退款状态枚举
     */
    public static RefundStatus getByCode(String code) {
        if (code == null) {
            return null;
        }
        for (RefundStatus status : RefundStatus.values()) {
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
    public boolean canTransitionTo(RefundStatus target) {
        switch (this) {
            case PENDING:
                return target == APPROVED || target == REJECTED;
            case APPROVED:
                return target == PROCESSING;
            case PROCESSING:
                return target == COMPLETED || target == FAILED;
            case REJECTED:
            case COMPLETED:
            case FAILED:
                return false;
            default:
                return false;
        }
    }
}