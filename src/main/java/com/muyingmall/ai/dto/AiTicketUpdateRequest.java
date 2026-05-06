package com.muyingmall.ai.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 管理端更新 AI 工单请求。
 */
@Data
public class AiTicketUpdateRequest {

    @NotBlank(message = "工单状态不能为空")
    private String status;

    private Integer assigneeId;

    private String assigneeName;

    private String handleRemark;
}
