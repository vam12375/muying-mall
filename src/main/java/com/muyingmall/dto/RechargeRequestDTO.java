package com.muyingmall.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import java.math.BigDecimal;

/**
 * 充值请求DTO
 */
@Schema(description = "充值请求DTO")
public class RechargeRequestDTO {

    /**
     * 用户ID
     */
    @Schema(description = "用户ID")
    @NotNull
    private Integer userId;

    /**
     * 充值金额
     */
    @Schema(description = "充值金额")
    @NotNull
    @DecimalMin(value = "0.01", message = "充值金额必须大于0")
    private BigDecimal amount;

    /**
     * 支付方式
     */
    @Schema(description = "支付方式：alipay-支付宝，wechat-微信支付，bank-银行卡，admin-管理员操作")
    @NotNull
    private String paymentMethod;

    /**
     * 交易描述
     */
    @Schema(description = "交易描述")
    private String description;

    /**
     * 备注
     */
    @Schema(description = "备注")
    private String remark;

    // Getters and Setters
    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}