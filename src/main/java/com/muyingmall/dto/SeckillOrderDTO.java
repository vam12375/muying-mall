package com.muyingmall.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 秒杀订单DTO
 */
@Data
public class SeckillOrderDTO {
    
    private Long id;
    private Long orderId;
    private Integer userId;
    private String username;
    private Long activityId;
    private String activityName;
    private Long seckillProductId;
    private Long productId;
    private String productName;
    private String productImage;
    private Long skuId;
    private String skuName;
    private Integer quantity;
    private BigDecimal seckillPrice;
    private BigDecimal totalAmount;
    private Integer status;
    private String statusText;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
