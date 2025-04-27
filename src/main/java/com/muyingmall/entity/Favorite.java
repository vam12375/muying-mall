package com.muyingmall.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 收藏实体类
 */
@Data
@TableName("favorite")
public class Favorite implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 收藏ID
     */
    @TableId(value = "favorite_id", type = IdType.AUTO)
    private Integer favoriteId;

    /**
     * 用户ID
     */
    private Integer userId;

    /**
     * 商品ID
     */
    private Integer productId;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 商品信息（非数据库字段）
     */
    @TableField(exist = false)
    private Product product;

    /**
     * 用户信息（非数据库字段）
     */
    @TableField(exist = false)
    private User user;
} 