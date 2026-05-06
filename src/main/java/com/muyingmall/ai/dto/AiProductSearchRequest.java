package com.muyingmall.ai.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * Agent 商品搜索请求。
 */
@Data
public class AiProductSearchRequest {

    private String keyword;

    private Integer babyAgeMonth;

    private Integer categoryId;

    private Integer brandId;

    private BigDecimal minPrice;

    private BigDecimal maxPrice;

    private Integer limit = 6;
}
