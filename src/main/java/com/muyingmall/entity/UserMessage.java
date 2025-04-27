package com.muyingmall.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户消息实体类
 */
@Data
@TableName("user_message")
public class UserMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 消息ID
     */
    @TableId(value = "message_id", type = IdType.ASSIGN_UUID)
    private String messageId;

    /**
     * 用户ID
     */
    private Integer userId;

    /**
     * 消息类型：ORDER-订单消息，SYSTEM-系统消息，REMIND-提醒消息
     */
    private String type;

    /**
     * 消息标题
     */
    private String title;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 是否已读：0-未读，1-已读
     */
    private Integer isRead;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 阅读时间
     */
    private LocalDateTime readTime;

    /**
     * 状态：0-已删除，1-正常
     */
    private Integer status;

    /**
     * 额外信息，如关联的订单ID等
     */
    private String extra;

    /**
     * 用户信息（非数据库字段）
     */
    @TableField(exist = false)
    private User user;
}