package com.muyingmall.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 育儿圈帖子实体类
 */
@Data
@TableName("circle_post")
public class CirclePost implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 帖子ID
     */
    @TableId(value = "post_id", type = IdType.AUTO)
    private Long postId;

    /**
     * 用户ID
     */
    private Integer userId;

    /**
     * 话题ID
     */
    private Integer topicId;

    /**
     * 帖子内容
     */
    private String content;

    /**
     * 图片列表(JSON数组)
     */
    private String images;

    /**
     * 关联商品ID
     */
    private Integer productId;

    /**
     * 关联SKU ID
     */
    private Integer skuId;

    /**
     * SKU规格信息(JSON)
     */
    private String skuSpecs;

    /**
     * 浏览量
     */
    private Integer viewCount;

    /**
     * 点赞数
     */
    private Integer likeCount;

    /**
     * 评论数
     */
    private Integer commentCount;

    /**
     * 分享数
     */
    private Integer shareCount;

    /**
     * 是否置顶：0-否，1-是
     */
    private Integer isTop;

    /**
     * 是否热门：0-否，1-是
     */
    private Integer isHot;

    /**
     * 状态：0-删除，1-正常，2-审核中
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

    // ========== 非数据库字段 ==========

    /**
     * 用户信息
     */
    @TableField(exist = false)
    private User user;

    /**
     * 话题信息
     */
    @TableField(exist = false)
    private CircleTopic topic;

    /**
     * 关联商品信息
     */
    @TableField(exist = false)
    private Product product;

    /**
     * 关联SKU信息
     */
    @TableField(exist = false)
    private ProductSku sku;

    /**
     * 当前用户是否已点赞
     */
    @TableField(exist = false)
    private Boolean isLiked;
}
