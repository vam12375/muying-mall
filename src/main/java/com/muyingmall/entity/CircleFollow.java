package com.muyingmall.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 育儿圈关注实体类
 */
@Data
@TableName("circle_follow")
public class CircleFollow implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 关注ID
     */
    @TableId(value = "follow_id", type = IdType.AUTO)
    private Long followId;

    /**
     * 用户ID(关注者)
     */
    private Integer userId;

    /**
     * 被关注用户ID
     */
    private Integer followUserId;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    // ========== 非数据库字段 ==========

    /**
     * 被关注用户信息
     */
    @TableField(exist = false)
    private User followUser;
}
