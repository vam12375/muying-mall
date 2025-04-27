package com.muyingmall.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户优惠券实体类
 */
@Data
@TableName("user_coupon")
public class UserCoupon implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户优惠券ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Integer userId;

    /**
     * 优惠券ID
     */
    private Long couponId;

    /**
     * 批次ID
     */
    private Integer batchId;

    /**
     * 状态：UNUSED-未使用，USED-已使用，EXPIRED-已过期
     */
    private String status;

    /**
     * 使用时间
     */
    private LocalDateTime useTime;

    /**
     * 使用订单ID
     */
    private Long orderId;

    /**
     * 领取时间
     */
    private LocalDateTime receiveTime;

    /**
     * 过期时间
     */
    private LocalDateTime expireTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 优惠券信息（非数据库字段）
     */
    @TableField(exist = false)
    private Coupon coupon;
}