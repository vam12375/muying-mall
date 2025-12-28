package com.muyingmall.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 运费规则配置实体类
 */
@Data
@TableName("shipping_rule")
public class ShippingRule implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 规则ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 规则名称
     */
    private String ruleName;

    /**
     * 最小距离（公里）
     */
    private BigDecimal minDistance;

    /**
     * 最大距离（公里，NULL表示无上限）
     */
    private BigDecimal maxDistance;

    /**
     * 基础运费（元）
     */
    private BigDecimal baseFee;

    /**
     * 每公里加价（元）
     */
    private BigDecimal perKmFee;

    /**
     * 免运费门槛（订单金额）
     */
    private BigDecimal freeThreshold;

    /**
     * 是否启用：0-禁用，1-启用
     */
    private Integer isActive;

    /**
     * 优先级（数字越大优先级越高）
     */
    private Integer priority;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
