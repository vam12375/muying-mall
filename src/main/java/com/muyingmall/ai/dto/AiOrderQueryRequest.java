package com.muyingmall.ai.dto;

import lombok.Data;

/**
 * Agent 订单查询请求。
 */
@Data
public class AiOrderQueryRequest {

    private Integer orderId;

    private String orderNo;
}
