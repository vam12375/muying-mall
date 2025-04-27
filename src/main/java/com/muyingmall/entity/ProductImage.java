package com.muyingmall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 商品图片实体类
 */
@Data
@TableName("product_image")
public class ProductImage implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 图片ID
     */
    @TableId(value = "image_id", type = IdType.AUTO)
    private Integer imageId;

    /**
     * 商品ID
     */
    private Integer productId;

    /**
     * 图片URL
     */
    private String imageUrl;
    
    /**
     * 图片类型：main-主图，detail-详情图，desc-描述图
     */
    private String type;

    /**
     * 排序
     */
    private Integer sortOrder;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}