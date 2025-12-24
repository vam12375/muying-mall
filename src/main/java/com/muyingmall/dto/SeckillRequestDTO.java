package com.muyingmall.dto;

import lombok.Data;

/**
 * 秒杀请求DTO
 */
@Data
public class SeckillRequestDTO {
    
    private Long seckillProductId;
    private Integer quantity;
    private Long addressId;
}
