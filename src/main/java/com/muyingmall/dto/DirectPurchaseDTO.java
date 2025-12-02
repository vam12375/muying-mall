package com.muyingmall.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 直接购买（立即购买）数据传输对象
 */
@Data
public class DirectPurchaseDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 收货地址ID
     */
    @NotNull(message = "收货地址不能为空")
    private Integer addressId;

    /**
     * 支付方式
     */
    @NotBlank(message = "支付方式不能为空")
    private String paymentMethod;

    /**
     * 商品ID
     */
    @NotNull(message = "商品ID不能为空")
    private Integer productId;

    /**
     * 商品数量
     */
    @NotNull(message = "商品数量不能为空")
    @Min(value = 1, message = "商品数量最少为1")
    private Integer quantity;

    /**
     * 商品规格（JSON格式，兼容旧版本）
     */
    private String specs;

    /**
     * SKU ID（新版本使用）
     */
    private Long skuId;

    /**
     * 备注
     */
    private String remark;

    /**
     * 使用的优惠券ID
     */
    private Long couponId;

    /**
     * 使用的积分数量
     * 每100积分可抵扣1元，最多抵扣50元
     */
    @Min(value = 0, message = "积分不能为负数")
    @Max(value = 5000, message = "单次最多使用5000积分")
    private Integer pointsUsed;

    /**
     * 运费
     */
    private BigDecimal shippingFee;
}