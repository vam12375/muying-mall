package com.muyingmall.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 商品SKU DTO
 * 
 * @author AI Assistant
 * @date 2024-11-24
 */
@Data
public class ProductSkuDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * SKU ID
     */
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
     * 规格值列表
     * 示例: [{"spec_name":"规格","spec_value":"1段(0-6个月)"}]
     */
    private List<Map<String, String>> specValues;

    /**
     * 乐观锁版本号
     */
    private Integer version;
}
