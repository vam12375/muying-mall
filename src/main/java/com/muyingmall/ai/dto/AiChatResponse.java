package com.muyingmall.ai.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * AI 聊天响应。
 */
@Data
public class AiChatResponse {

    private Long conversationId;

    private String traceId;

    private String answer;

    private String intent;

    private String riskLevel;

    private Boolean humanHandoffRequired;

    private Long ticketId;

    private List<String> suggestions;

    private Map<String, Object> toolResults;

    /**
     * Agent 轻量工作流状态，用于后台观察意图识别、工具选择、检索、转人工等节点。
     */
    private Map<String, Object> workflow;
}
