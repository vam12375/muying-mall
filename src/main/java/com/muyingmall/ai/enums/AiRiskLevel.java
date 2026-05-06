package com.muyingmall.ai.enums;

import lombok.Getter;

/**
 * AI Agent 风险等级。
 * 高风险动作只允许建议和建单，不允许 Agent 直接退款、发券或改订单。
 */
@Getter
public enum AiRiskLevel {

    LOW("LOW", "低风险"),
    MEDIUM("MEDIUM", "中风险"),
    HIGH("HIGH", "高风险");

    private final String code;
    private final String desc;

    AiRiskLevel(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
