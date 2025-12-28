package com.muyingmall.enums;

/**
 * 消息类型枚举
 * 优化：支持大写和小写格式的code，提高兼容性
 */
public enum MessageType {
    /**
     * 订单消息
     */
    ORDER("order", "订单消息"),

    /**
     * 系统消息
     */
    SYSTEM("system", "系统消息"),

    /**
     * 提醒消息
     */
    REMIND("remind", "提醒消息"),

    /**
     * 积分消息
     */
    POINTS("points", "积分消息"),

    /**
     * 签到消息
     */
    CHECKIN("checkin", "签到消息"),

    /**
     * 催发货消息
     */
    SHIPPING_REMINDER("shipping_reminder", "催发货消息"),

    /**
     * 优惠券消息
     */
    COUPON("coupon", "优惠券消息"),

    /**
     * 评价奖励消息
     */
    COMMENT_REWARD("comment_reward", "评价奖励消息");

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
     * 优化：支持大写、小写、数字等多种格式，提高兼容性
     * 
     * @param code 类型编码
     * @return 对应的枚举实例，如果不存在则返回null
     */
    public static MessageType getByCode(String code) {
        if (code == null || code.isEmpty()) {
            return null;
        }
        
        // 先尝试精确匹配（小写格式）
        for (MessageType type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        
        // 尝试大写格式匹配（兼容旧数据）
        String upperCode = code.toUpperCase();
        for (MessageType type : values()) {
            if (type.name().equals(upperCode)) {
                return type;
            }
        }
        
        // 尝试转换为小写下划线格式匹配
        String lowerCode = code.toLowerCase();
        for (MessageType type : values()) {
            if (type.getCode().equals(lowerCode)) {
                return type;
            }
        }
        
        // 处理数字类型（旧数据可能使用数字）
        // 2 -> remind, 4 -> points 等
        try {
            int numCode = Integer.parseInt(code);
            switch (numCode) {
                case 1: return ORDER;
                case 2: return REMIND;
                case 3: return SYSTEM;
                case 4: return POINTS;
                case 5: return CHECKIN;
                default: return null;
            }
        } catch (NumberFormatException e) {
            // 不是数字，继续其他匹配
        }
        
        return null;
    }
}