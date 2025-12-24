package com.muyingmall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 秒杀订单实体
 */
@Data
@TableName("seckill_order")
public class SeckillOrder {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long orderId;
    
    private Integer userId;
    
    private Long activityId;
    
    private Long seckillProductId;
    
    private Long skuId;
    
    private Integer quantity;
    
    private BigDecimal seckillPrice;
    
    /**
     * 状态：0待支付，1已支付，2已取消
     */
    private Integer status;
    
    private LocalDateTime createTime;
    
    private LocalDateTime updateTime;
}
