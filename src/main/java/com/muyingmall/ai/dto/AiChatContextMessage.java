package com.muyingmall.ai.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * AI 同一会话上下文消息，用于向 FastAPI Agent 传递已裁剪的历史内容。
 */
@Data
public class AiChatContextMessage {

    private Long id;

    private String role;

    private String content;

    private String intent;

    private String riskLevel;

    private LocalDateTime createTime;
}
