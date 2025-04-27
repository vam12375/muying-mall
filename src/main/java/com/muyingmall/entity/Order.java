package com.muyingmall.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单实体类
 */
@Data
@TableName("`order`") // order是MySQL关键字，需要加反引号
public class Order implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 订单ID
     */
    @TableId(value = "order_id", type = IdType.AUTO)
    private Integer orderId;

    /**
     * 订单编号
     */
    private String orderNo;

    /**
     * 用户ID
     */
    private Integer userId;

    /**
     * 订单总金额
     */
    private BigDecimal totalAmount;

    /**
     * 实付金额
     */
    private BigDecimal actualAmount;

    /**
     * 订单状态：pending_payment-待付款，pending_shipment-待发货，shipped-已发货，completed-已完成，cancelled-已取消
     */
    private String status;

    /**
     * 支付ID，关联payment表
     */
    private Long paymentId;

    /**
     * 支付时间
     */
    private LocalDateTime payTime;

    /**
     * 支付方式：alipay-支付宝，wechat-微信，wallet-钱包
     */
    private String paymentMethod;

    /**
     * 配送方式
     */
    private String shippingMethod;

    /**
     * 运费
     */
    private BigDecimal shippingFee;

    /**
     * 收货地址ID
     */
    private Integer addressId;

    /**
     * 收货人姓名
     */
    private String receiverName;

    /**
     * 收货人电话
     */
    private String receiverPhone;

    /**
     * 省份
     */
    private String receiverProvince;

    /**
     * 城市
     */
    private String receiverCity;

    /**
     * 区/县
     */
    private String receiverDistrict;

    /**
     * 详细地址
     */
    private String receiverAddress;

    /**
     * 邮编
     */
    private String receiverZip;

    /**
     * 订单备注
     */
    private String remark;

    /**
     * 优惠金额
     */
    private BigDecimal discountAmount;

    /**
     * 使用的积分
     */
    private Integer pointsUsed;

    /**
     * 积分抵扣金额
     */
    private BigDecimal pointsDiscount;

    /**
     * 支付时间
     */
    private LocalDateTime paidTime;

    /**
     * 发货时间
     */
    private LocalDateTime shippingTime;

    /**
     * 物流单号
     */
    private String trackingNo;

    /**
     * 物流公司
     */
    private String shippingCompany;

    /**
     * 完成时间
     */
    private LocalDateTime completionTime;

    /**
     * 取消时间
     */
    private LocalDateTime cancelTime;

    /**
     * 取消原因
     */
    private String cancelReason;

    /**
     * 是否删除：0-未删除，1-已删除
     */
    private Integer isDeleted;

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

    /**
     * 订单商品列表（非数据库字段）
     */
    @TableField(exist = false)
    private List<OrderProduct> products;
}