package com.muyingmall.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 钱包信息VO
 */
@Data
@Schema(description = "钱包信息")
public class WalletInfoDTO {

    @Schema(description = "账户余额")
    private BigDecimal balance;

    @Schema(description = "冻结余额")
    private BigDecimal frozenBalance;

    @Schema(description = "积分")
    private Integer points;

    @Schema(description = "累计充值金额")
    private BigDecimal totalRecharge;

    @Schema(description = "累计消费金额")
    private BigDecimal totalConsumption;
}