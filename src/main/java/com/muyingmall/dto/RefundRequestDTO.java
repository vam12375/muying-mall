package com.muyingmall.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 退款申请请求DTO
 */
@Data
public class RefundRequestDTO {

    @NotNull(message = "订单ID不能为空")
    private Integer orderId;

    @NotNull(message = "用户ID不能为空")
    private Integer userId;

    @NotNull(message = "退款金额不能为空")
    @DecimalMin(value = "0.01", message = "退款金额必须大于0")
    private BigDecimal amount;

    @NotBlank(message = "退款原因不能为空")
    private String reason;

    private String reasonDetail;

    private String evidenceImages;
}
