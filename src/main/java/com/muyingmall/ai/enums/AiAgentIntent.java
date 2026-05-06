package com.muyingmall.ai.enums;

import lombok.Getter;

/**
 * AI Agent 意图枚举。
 * 只保留第一版业务闭环需要的意图，避免过早扩展复杂多 Agent。
 */
@Getter
public enum AiAgentIntent {

    SHOPPING_GUIDE("SHOPPING_GUIDE", "AI导购"),
    ORDER_QUERY("ORDER_QUERY", "订单查询"),
    REFUND_CHECK("REFUND_CHECK", "售后判断"),
    KNOWLEDGE_QA("KNOWLEDGE_QA", "育儿知识问答"),
    COMPLAINT_HANDOFF("COMPLAINT_HANDOFF", "投诉转人工"),
    UNKNOWN("UNKNOWN", "未知意图");

    private final String code;
    private final String desc;

    AiAgentIntent(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
