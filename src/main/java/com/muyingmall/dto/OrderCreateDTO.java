package com.muyingmall.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.DecimalMin;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 订单创建数据传输对象
 */
@Data
@Schema(description = "订单创建请求参数")
public class OrderCreateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 收货地址ID
     */
    @Schema(description = "收货地址ID", example = "1", required = true)
    @NotNull(message = "收货地址不能为空")
    private Integer addressId;

    /**
     * 支付方式
     */
    @Schema(description = "支付方式", example = "alipay", allowableValues = { "alipay", "wechat",
            "wallet" }, required = true)
    @NotBlank(message = "支付方式不能为空")
    private String paymentMethod;

    /**
     * 购物车商品ID列表，如果为空则表示购买购物车中选中的商品
     */
    @Schema(description = "购物车商品ID列表，为空时购买所有选中商品", example = "[1, 2, 3]")
    private List<Integer> cartIds;

    /**
     * 订单备注
     */
    @Schema(description = "订单备注信息", example = "请尽快发货，谢谢！", maxLength = 200)
    private String remark;

    /**
     * 使用的优惠券ID
     */
    @Schema(description = "优惠券ID，不使用优惠券时为空", example = "1001")
    private Long couponId;

    /**
     * 使用的积分数量
     * 每100积分可抵扣1元，最多抵扣50元
     */
    @Schema(description = "使用的积分数量，每100积分抵扣1元，最多5000积分", example = "1000", minimum = "0", maximum = "5000")
    @Min(value = 0, message = "积分不能为负数")
    @Max(value = 5000, message = "单次最多使用5000积分")
    private Integer pointsUsed;

    /**
     * 运费
     */
    @Schema(description = "运费金额", example = "10.00", minimum = "0")
    @DecimalMin(value = "0", message = "运费不能为负数")
    private BigDecimal shippingFee;
}