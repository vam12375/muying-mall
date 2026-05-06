package com.muyingmall.ai.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * AI Agent 工具调用日志实体。
 */
@Data
@TableName("ai_tool_call_log")
public class AiToolCallLog implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String traceId;

    private Long conversationId;

    private Long messageId;

    private Integer userId;

    private String intent;

    private String riskLevel;

    private String toolName;

    private String toolType;

    private String requestPayload;

    private String responsePayload;

    private Boolean success;

    private String errorMessage;

    private Long durationMs;

    private LocalDateTime createTime;
}
