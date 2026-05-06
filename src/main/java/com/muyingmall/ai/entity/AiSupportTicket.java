package com.muyingmall.ai.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * AI Agent 人工接管工单实体。
 */
@Data
@TableName("ai_support_ticket")
public class AiSupportTicket implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String ticketNo;

    private Long conversationId;

    private Integer userId;

    private Integer orderId;

    private Integer productId;

    private String title;

    private String content;

    private String intent;

    private String riskLevel;

    private String status;

    private String source;

    private Integer assigneeId;

    private String assigneeName;

    private String handleRemark;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private LocalDateTime closeTime;
}
