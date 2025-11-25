package com.muyingmall.dto;

import lombok.Data;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Map;

/**
 * 购物车添加数据传输对象
 */
@Data
public class CartAddDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 商品ID
     */
    @NotNull(message = "商品ID不能为空")
    private Integer productId;

    /**
     * 商品数量
     */
    @NotNull(message = "商品数量不能为空")
    @Min(value = 1, message = "商品数量必须大于0")
    private Integer quantity;

    /**
     * 是否选中：0-未选中，1-已选中
     */
    private Integer selected = 1;

    /**
     * 商品规格，key为规格名，value为规格值
     * @deprecated 使用 skuId 替代
     */
    @Deprecated
    private Map<String, String> specs;

    /**
     * SKU ID（有SKU的商品必传）
     */
    private Long skuId;

    /**
     * SKU名称（可选，用于显示）
     */
    private String skuName;
}