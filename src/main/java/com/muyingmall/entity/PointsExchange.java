package com.muyingmall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 积分兑换记录实体类
 */
@Data
@TableName("points_exchange")
public class PointsExchange implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 兑换单号
     */
    private String orderNo;

    /**
     * 用户ID
     */
    private Integer userId;

    /**
     * 商品ID
     */
    private Integer productId;

    /**
     * 兑换数量
     */
    private Integer quantity;

    /**
     * 消耗积分
     */
    private Integer points;

    /**
     * 收货地址ID
     */
    private Integer addressId;

    /**
     * 手机号码
     */
    private String phone;

    /**
     * 状态(0:待发货,1:已发货,2:已完成,3:已取消)
     */
    private Integer status;

    /**
     * 物流单号
     */
    private String trackingNo;

    /**
     * 物流公司
     */
    private String trackingCompany;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}