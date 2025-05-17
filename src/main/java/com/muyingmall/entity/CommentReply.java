package com.muyingmall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 评价回复实体类
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("comment_reply")
public class CommentReply implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 回复ID
     */
    @TableId(value = "reply_id", type = IdType.AUTO)
    private Integer replyId;

    /**
     * 评价ID
     */
    private Integer commentId;

    /**
     * 回复内容
     */
    private String content;

    /**
     * 回复类型：1-商家回复，2-用户追评
     */
    private Integer replyType;

    /**
     * 回复用户ID（商家回复时为管理员ID）
     */
    private Integer replyUserId;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 回复用户信息（非数据库字段）
     */
    @TableField(exist = false)
    private User replyUser;

    /**
     * 关联的评价（非数据库字段）
     */
    @TableField(exist = false)
    private Comment comment;
}