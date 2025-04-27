package com.muyingmall.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 优惠券规则实体类
 */
@Data
@TableName("coupon_rule")
public class CouponRule implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 规则ID
     */
    @TableId(value = "rule_id", type = IdType.AUTO)
    private Integer ruleId;

    /**
     * 规则名称
     */
    private String name;

    /**
     * 规则类型：0-满减，1-直减，2-折扣
     */
    private Integer type;

    /**
     * 规则内容JSON
     */
    private String ruleContent;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}