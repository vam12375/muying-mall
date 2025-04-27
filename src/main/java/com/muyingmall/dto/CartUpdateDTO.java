package com.muyingmall.dto;

import lombok.Data;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 购物车更新数据传输对象
 */
@Data
public class CartUpdateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 购物车ID
     */
    @NotNull(message = "购物车ID不能为空")
    private Integer cartId;

    /**
     * 商品数量
     */
    @Min(value = 1, message = "商品数量必须大于0")
    private Integer quantity;

    /**
     * 是否选中：0-未选中，1-已选中
     */
    private Integer selected;
}