package com.muyingmall.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 商品实体类
 */
@Data
@TableName("product")
public class Product implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 商品ID
     */
    @TableId(value = "product_id", type = IdType.AUTO)
    private Integer productId;

    /**
     * 分类ID
     */
    private Integer categoryId;

    /**
     * 品牌ID
     */
    private Integer brandId;

    /**
     * 商品名称
     */
    private String productName;

    /**
     * 商品编号
     */
    private String productSn;

    /**
     * 商品主图
     */
    private String productImg;

    /**
     * 商品详情
     */
    private String productDetail;

    /**
     * 现价
     */
    private BigDecimal priceNew;

    /**
     * 原价
     */
    private BigDecimal priceOld;

    /**
     * 库存
     */
    private Integer stock;

    /**
     * 销量
     */
    private Integer sales;

    /**
     * 支持人数
     */
    private Integer support;

    /**
     * 评分
     */
    private BigDecimal rating;

    /**
     * 评价数量
     */
    private Integer reviewCount;

    /**
     * 商品状态：上架/下架
     */
    private String productStatus;

    /**
     * 是否热门：0-否，1-是
     */
    private Integer isHot;

    /**
     * 是否新品：0-否，1-是
     */
    private Integer isNew;

    /**
     * 是否推荐：0-否，1-是
     */
    private Integer isRecommend;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 商品图片列表（非数据库字段）
     */
    @TableField(exist = false)
    private List<ProductImage> images;

    /**
     * 商品规格列表（非数据库字段）
     */
    @TableField(exist = false)
    private List<ProductSpecs> specsList;

    /**
     * 分类名称（非数据库字段）
     */
    @TableField(exist = false)
    private String categoryName;

    /**
     * 品牌名称（非数据库字段）
     */
    @TableField(exist = false)
    private String brandName;
}