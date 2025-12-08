package com.muyingmall.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 育儿圈评论实体类
 */
@Data
@TableName("circle_comment")
public class CircleComment implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 评论ID
     */
    @TableId(value = "comment_id", type = IdType.AUTO)
    private Long commentId;

    /**
     * 帖子ID
     */
    private Long postId;

    /**
     * 用户ID
     */
    private Integer userId;

    /**
     * 父评论ID(回复)
     */
    private Long parentId;

    /**
     * 被回复用户ID
     */
    private Integer replyUserId;

    /**
     * 评论内容
     */
    private String content;

    /**
     * 点赞数
     */
    private Integer likeCount;

    /**
     * 状态：0-删除，1-正常
     */
    private Integer status;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    // ========== 非数据库字段 ==========

    /**
     * 用户信息
     */
    @TableField(exist = false)
    private User user;

    /**
     * 被回复用户信息
     */
    @TableField(exist = false)
    private User replyUser;

    /**
     * 子评论列表
     */
    @TableField(exist = false)
    private List<CircleComment> replies;

    /**
     * 当前用户是否已点赞
     */
    @TableField(exist = false)
    private Boolean isLiked;
}
