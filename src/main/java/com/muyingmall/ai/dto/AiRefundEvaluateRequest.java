package com.muyingmall.ai.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * Agent 售后判断请求。
 */
@Data
public class AiRefundEvaluateRequest {

    private Integer orderId;

    private String orderNo;

    private BigDecimal amount;

    private String reason;
}
