package com.muyingmall.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 秒杀请求DTO
 */
@Data
public class SeckillRequestDTO {

    @NotNull(message = "秒杀商品ID不能为空")
    private Long seckillProductId;

    @NotNull(message = "购买数量不能为空")
    @Min(value = 1, message = "购买数量至少为1")
    private Integer quantity;

    @NotNull(message = "收货地址不能为空")
    private Long addressId;

    @NotNull(message = "支付方式不能为空")
    private String paymentMethod;
}
