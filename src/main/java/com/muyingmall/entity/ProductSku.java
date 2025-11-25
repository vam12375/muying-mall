package com.muyingmall.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品SKU实体类
 * 
 * @author AI Assistant
 * @date 2024-11-24
 */
@Data
@TableName("product_sku")
public class ProductSku implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * SKU ID
     */
    @TableId(value = "sku_id", type = IdType.AUTO)
    private Long skuId;

    /**
     * 商品ID
     */
    private Integer productId;

    /**
     * SKU编码
     */
    private String skuCode;

    /**
     * SKU名称
     */
    private String skuName;

    /**
     * 规格值组合（JSON格式）
     * 示例: [{"spec_name":"规格","spec_value":"1段(0-6个月)"},{"spec_name":"重量","spec_value":"900g"}]
     */
    private String specValues;

    /**
     * SKU价格
     */
    private BigDecimal price;

    /**
     * SKU库存
     */
    private Integer stock;

    /**
     * SKU图片
     */
    private String skuImage;

    /**
     * 重量(kg)
     */
    private BigDecimal weight;

    /**
     * 体积(m³)
     */
    private BigDecimal volume;

    /**
     * 状态：0-禁用，1-启用
     */
    private Integer status;

    /**
     * 排序
     */
    private Integer sortOrder;

    /**
     * 乐观锁版本号
     */
    @Version
    private Integer version;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
