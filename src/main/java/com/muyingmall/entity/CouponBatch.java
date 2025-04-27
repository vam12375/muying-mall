package com.muyingmall.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 优惠券批次实体类
 */
@Data
@TableName("coupon_batch")
public class CouponBatch implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 批次ID
     */
    @TableId(value = "batch_id", type = IdType.AUTO)
    private Integer batchId;

    /**
     * 优惠券名称
     */
    private String couponName;

    /**
     * 规则ID
     */
    private Integer ruleId;

    /**
     * 优惠券总数量
     */
    private Integer totalCount;

    /**
     * 已分配数量
     */
    private Integer assignCount;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 规则信息（非数据库字段）
     */
    @TableField(exist = false)
    private CouponRule couponRule;
}