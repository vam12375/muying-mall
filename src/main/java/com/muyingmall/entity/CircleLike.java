package com.muyingmall.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 育儿圈点赞实体类
 */
@Data
@TableName("circle_like")
public class CircleLike implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 点赞ID
     */
    @TableId(value = "like_id", type = IdType.AUTO)
    private Long likeId;

    /**
     * 用户ID
     */
    private Integer userId;

    /**
     * 目标ID(帖子ID或评论ID)
     */
    private Long targetId;

    /**
     * 目标类型：1-帖子，2-评论
     */
    private Integer targetType;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    // ========== 常量定义 ==========
    
    /** 目标类型：帖子 */
    public static final int TARGET_TYPE_POST = 1;
    
    /** 目标类型：评论 */
    public static final int TARGET_TYPE_COMMENT = 2;
}
