package com.muyingmall.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 评论实体类
 */
@Data
@TableName("comment")
public class Comment implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 评价ID
     */
    @TableId(value = "comment_id", type = IdType.AUTO)
    private Integer commentId;

    /**
     * 用户ID
     */
    private Integer userId;

    /**
     * 商品ID
     */
    private Integer productId;

    /**
     * 订单ID
     */
    private Integer orderId;

    /**
     * 评价内容
     */
    private String content;

    /**
     * 评分(1-5)
     */
    private Integer rating;

    /**
     * 评价图片
     */
    private String images;

    /**
     * 是否匿名：0-否，1-是
     */
    private Integer isAnonymous;

    /**
     * 状态：0-隐藏，1-显示
     */
    private Integer status;

    /**
     * 是否已回复：0-否，1-是
     */
    private Boolean hasReplied;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 用户信息（非数据库字段）
     */
    @TableField(exist = false)
    private User user;

    /**
     * 商品信息（非数据库字段）
     */
    @TableField(exist = false)
    private Product product;

    // ========== SQL查询直接映射的用户字段 ==========
    
    /**
     * 用户名（SQL JOIN查询时直接映射）
     */
    @TableField(exist = false)
    private String username;

    /**
     * 用户昵称（SQL JOIN查询时直接映射）
     */
    @TableField(exist = false)
    private String nickname;

    /**
     * 用户头像（SQL JOIN查询时直接映射）
     */
    @TableField(exist = false)
    private String avatar;

    /**
     * 商品名称（SQL JOIN查询时直接映射）
     */
    @TableField(exist = false)
    private String productName;

    /**
     * 商品图片（SQL JOIN查询时直接映射）
     */
    @TableField(exist = false)
    private String productImage;
}