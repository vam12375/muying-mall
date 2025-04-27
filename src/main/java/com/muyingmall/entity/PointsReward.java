package com.muyingmall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 积分奖励实体类
 */
@Data
@TableName("points_reward")
public class PointsReward implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 奖励ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 奖励名称
     */
    private String name;

    /**
     * 奖励描述
     */
    private String description;

    /**
     * 兑换所需积分
     */
    private Integer points;

    /**
     * 奖励类型(product-实物商品, coupon-优惠券, service-服务)
     */
    private String type;

    /**
     * 奖励图片
     */
    private String image;

    /**
     * 库存数量
     */
    private Integer stock;

    /**
     * 已兑换数量
     */
    private Integer exchangedCount;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;

    /**
     * 是否显示
     */
    private Integer visible;

    /**
     * 关联商品ID
     */
    private Long productId;

    /**
     * 关联优惠券ID
     */
    private Long couponId;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}