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
}
