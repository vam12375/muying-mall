package com.muyingmall.enums;

/**
 * 消息类型枚举
 */
public enum MessageType {
    /**
     * 订单消息
     */
    ORDER("ORDER", "订单消息"),

    /**
     * 系统消息
     */
    SYSTEM("SYSTEM", "系统消息"),

    /**
     * 提醒消息
     */
    REMIND("REMIND", "提醒消息"),

    /**
     * 积分消息
     */
    POINTS("POINTS", "积分消息"),

    /**
     * 签到消息
     */
    CHECKIN("CHECKIN", "签到消息"),

    /**
     * 催发货消息
     */
    SHIPPING_REMINDER("SHIPPING_REMINDER", "催发货消息"),

    /**
     * 优惠券消息
     */
    COUPON("COUPON", "优惠券消息");

    private final String code;
    private final String desc;

    MessageType(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    /**
     * 根据code获取枚举实例
     * 
     * @param code 类型编码
     * @return 对应的枚举实例，如果不存在则返回null
     */
    public static MessageType getByCode(String code) {
        for (MessageType type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }
}