package com.muyingmall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 秒杀商品实体
 */
@Data
@TableName("seckill_product")
public class SeckillProduct {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long activityId;
    
    private Long productId;
    
    private Long skuId;
    
    private BigDecimal seckillPrice;
    
    private Integer seckillStock;
    
    private Integer limitPerUser;
    
    private Integer sortOrder;
    
    private LocalDateTime createTime;
    
    private LocalDateTime updateTime;
}
