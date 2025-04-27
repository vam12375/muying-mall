package com.muyingmall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 优惠券实体类
 */
@Data
@TableName("coupon")
public class Coupon implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 优惠券ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 优惠券名称
     */
    private String name;
    
    /**
     * 批次ID
     */
    private Integer batchId;
    
    /**
     * 规则ID
     */
    private Integer ruleId;

    /**
     * 优惠券类型：FIXED-固定金额, PERCENTAGE-百分比折扣
     */
    private String type;

    /**
     * 优惠券面值/折扣值
     */
    private BigDecimal value;

    /**
     * 最低消费金额
     */
    private BigDecimal minSpend;

    /**
     * 最大折扣金额（针对百分比折扣）
     */
    private BigDecimal maxDiscount;

    /**
     * 状态：ACTIVE-可用, INACTIVE-不可用
     */
    private String status;

    /**
     * 适用分类ID，多个用逗号分隔
     */
    private String categoryIds;

    /**
     * 适用品牌ID，多个用逗号分隔
     */
    private String brandIds;

    /**
     * 适用商品ID，多个用逗号分隔
     */
    private String productIds;

    /**
     * 是否可叠加使用：0-不可叠加，1-可叠加
     */
    private Integer isStackable;

    /**
     * 发行总量，0表示不限量
     */
    private Integer totalQuantity;

    /**
     * 已使用数量
     */
    private Integer usedQuantity;

    /**
     * 已领取数量
     */
    private Integer receivedQuantity;
    
    /**
     * 每用户最大领取次数，0表示不限制
     */
    private Integer userLimit;

    /**
     * 有效期开始时间
     */
    private LocalDateTime startTime;

    /**
     * 有效期结束时间
     */
    private LocalDateTime endTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 是否已领取（非数据库字段）
     */
    @TableField(exist = false)
    private Boolean isReceived;
}