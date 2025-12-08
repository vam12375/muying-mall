package com.muyingmall.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 育儿圈消息通知实体类
 */
@Data
@TableName("circle_message")
public class CircleMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 消息ID
     */
    @TableId(value = "message_id", type = IdType.AUTO)
    private Long messageId;

    /**
     * 接收用户ID
     */
    private Integer userId;

    /**
     * 发送用户ID
     */
    private Integer fromUserId;

    /**
     * 消息类型：1-点赞帖子，2-点赞评论，3-评论，4-回复，5-关注
     */
    private Integer type;

    /**
     * 目标ID(帖子ID或评论ID)
     */
    private Long targetId;

    /**
     * 消息内容摘要
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

    // ========== 非数据库字段 ==========

    /**
     * 发送用户信息
     */
    @TableField(exist = false)
    private User fromUser;

    /**
     * 关联帖子信息
     */
    @TableField(exist = false)
    private CirclePost post;

    // ========== 消息类型常量 ==========
    public static final int TYPE_LIKE_POST = 1;
    public static final int TYPE_LIKE_COMMENT = 2;
    public static final int TYPE_COMMENT = 3;
    public static final int TYPE_REPLY = 4;
    public static final int TYPE_FOLLOW = 5;
}
