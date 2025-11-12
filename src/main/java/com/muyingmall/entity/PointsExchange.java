package com.muyingmall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
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
     * 状态(pending:待处理,processing:处理中,shipped:已发货,completed:已完成,cancelled:已取消)
     */
    private String status;

    /**
     * 物流单号
     */
    private String trackingNumber;

    /**
     * 物流公司
     */
    private String logisticsCompany;

    /**
     * 发货时间
     */
    private LocalDateTime shipTime;

    /**
     * 联系人
     */
    private String contact;

    /**
     * 收货地址
     */
    private String address;

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

    /**
     * 用户信息（非数据库字段）
     */
    @TableField(exist = false)
    private User user;

    /**
     * 商品信息（非数据库字段）
     */
    @TableField(exist = false)
    private PointsProduct product;

    /**
     * 用户名（非数据库字段，用于前端展示）
     */
    @TableField(exist = false)
    private String username;

    /**
     * 商品名称（非数据库字段，用于前端展示）
     */
    @TableField(exist = false)
    private String productName;

    /**
     * 商品图片（非数据库字段，用于前端展示）
     */
    @TableField(exist = false)
    private String productImage;
}