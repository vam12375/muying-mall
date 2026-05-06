package com.muyingmall.ai.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Agent 创建人工工单请求。
 */
@Data
public class AiTicketCreateRequest {

    private Long conversationId;

    private Integer orderId;

    private Integer productId;

    @NotBlank(message = "工单标题不能为空")
    private String title;

    @NotBlank(message = "工单内容不能为空")
    private String content;

    private String intent;

    private String riskLevel;
}
