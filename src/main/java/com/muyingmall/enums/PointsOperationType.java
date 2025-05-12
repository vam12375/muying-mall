package com.muyingmall.enums;

public enum PointsOperationType {
    SIGN_IN("每日签到"),
    ORDER_REWARD("订单奖励"),
    EXCHANGE_PRODUCT("积分兑换"),
    ADMIN_ADJUSTMENT("管理员调整"),
    EVENT_REWARD("活动奖励"),
    OTHER("其他");

    private final String description;

    PointsOperationType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
} 