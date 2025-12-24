package com.muyingmall.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 秒杀商品DTO
 */
@Data
public class SeckillProductDTO {
    
    private Long id;
    private Long activityId;
    private String activityName;
    private Long productId;
    private String productName;
    private String productImage;
    private Long skuId;
    private String skuName;
    private BigDecimal originalPrice;
    private BigDecimal seckillPrice;
    private Integer seckillStock;
    private Integer limitPerUser;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer activityStatus;
    private Integer soldCount;
}
