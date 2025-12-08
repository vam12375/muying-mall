package com.muyingmall.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 育儿圈话题实体类
 */
@Data
@TableName("circle_topic")
public class CircleTopic implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 话题ID
     */
    @TableId(value = "topic_id", type = IdType.AUTO)
    private Integer topicId;

    /**
     * 话题名称
     */
    private String name;

    /**
     * 话题图标
     */
    private String icon;

    /**
     * 话题描述
     */
    private String description;

    /**
     * 帖子数量
     */
    private Integer postCount;

    /**
     * 关注数量
     */
    private Integer followCount;

    /**
     * 排序
     */
    private Integer sortOrder;

    /**
     * 状态：0-禁用，1-启用
     */
    private Integer status;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
