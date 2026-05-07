package com.muyingmall.ai.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * AI 聊天请求。
 */
@Data
public class AiChatRequest {

    private Long conversationId;

    @NotBlank(message = "用户消息不能为空")
    private String message;

    private String channel = "WEB";

    private Integer babyAgeMonth;

    private Map<String, Object> metadata;

    private List<AiChatContextMessage> history;

    private Integer maxContextChars;
}
