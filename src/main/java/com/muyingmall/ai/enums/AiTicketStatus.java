package com.muyingmall.ai.enums;

import lombok.Getter;

/**
 * AI 工单状态。
 */
@Getter
public enum AiTicketStatus {

    PENDING("PENDING", "待处理"),
    PROCESSING("PROCESSING", "处理中"),
    RESOLVED("RESOLVED", "已解决"),
    CLOSED("CLOSED", "已关闭");

    private final String code;
    private final String desc;

    AiTicketStatus(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
