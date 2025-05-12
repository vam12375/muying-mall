package com.muyingmall.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 物流状态枚举
 */
@Getter
public enum LogisticsStatus {

    /**
     * 已创建
     */
    CREATED("CREATED", "已创建"),

    /**
     * 运输中
     */
    SHIPPING("SHIPPING", "运输中"),

    /**
     * 已送达
     */
    DELIVERED("DELIVERED", "已送达"),

    /**
     * 异常
     */
    EXCEPTION("EXCEPTION", "异常");

    @EnumValue
    private final String code;

    private final String desc;

    LogisticsStatus(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 获取枚举代码，用于JSON序列化
     *
     * @return 状态码
     */
    @JsonValue
    public String getCode() {
        return this.code;
    }

    /**
     * 根据状态码获取枚举实例
     *
     * @param code 状态码
     * @return 枚举实例
     */
    public static LogisticsStatus getByCode(String code) {
        for (LogisticsStatus status : LogisticsStatus.values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }
}