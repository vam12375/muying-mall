package com.muyingmall.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户消息数据传输对象
 */
@Data
public class UserMessageDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 消息ID
     */
    private Long id;
    
    /**
     * 消息ID（兼容字段）
     */
    private String messageId;

    /**
     * 用户ID
     */
    private Integer userId;

    /**
     * 消息类型
     */
    private String type;

    /**
     * 消息类型描述
     */
    private String typeDesc;

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
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 阅读时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
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
     * 用户名称（非必须）
     */
    private String username;

    /**
     * 用户头像（非必须）
     */
    private String avatar;
    
    /**
     * 接收者类型：all-所有用户，user-指定用户，role-指定角色
     */
    private String recipientType;
    
    /**
     * 接收者（用户ID或角色名称）
     */
    private String recipient;
    
    /**
     * 发送时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime sendTime;
}