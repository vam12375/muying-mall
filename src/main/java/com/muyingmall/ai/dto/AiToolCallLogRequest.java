package com.muyingmall.ai.dto;

import lombok.Data;

/**
 * Agent 工具调用日志请求。
 */
@Data
public class AiToolCallLogRequest {

    private String traceId;

    private Long conversationId;

    private Long messageId;

    private String intent;

    private String riskLevel;

    private String toolName;

    private String toolType;

    private Object requestPayload;

    private Object responsePayload;

    private Boolean success = true;

    private String errorMessage;

    private Long durationMs;
}
