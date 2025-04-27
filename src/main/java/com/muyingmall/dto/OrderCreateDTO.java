package com.muyingmall.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * 订单创建数据传输对象
 */
@Data
public class OrderCreateDTO implements Serializable {

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
     * 商品ID列表，如果为空则表示购买购物车中选中的商品
     */
    private List<Integer> cartIds;

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
     */
    private Integer pointsUsed;
}