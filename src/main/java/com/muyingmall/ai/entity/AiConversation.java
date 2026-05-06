package com.muyingmall.ai.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * AI Agent 会话实体。
 */
@Data
@TableName("ai_conversation")
public class AiConversation implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String conversationNo;

    private Integer userId;

    private String channel;

    private String status;

    private String currentIntent;

    private String riskLevel;

    private String lastMessage;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
