package com.muyingmall.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.muyingmall.enums.OrderStatus;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单实体类
 */
@Data
@TableName(value = "`order`", autoResultMap = true) // order是MySQL关键字，需要加反引号
public class Order implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 订单ID
     */
    @TableId(value = "order_id", type = IdType.AUTO)
    @TableField(updateStrategy = FieldStrategy.NEVER) // 防止主键字段在更新时被包含在SET语句中
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
    private OrderStatus status;

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
     * 优惠券ID
     */
    private Long couponId;

    /**
     * 优惠券金额
     */
    private BigDecimal couponAmount;

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
     * 是否已评价：0-未评价，1-已评价
     */
    private Integer isCommented;

    /**
     * 是否删除：0-未删除，1-已删除
     */
    @TableLogic
    private Integer isDeleted;

    /**
     * 版本号，用于乐观锁控制
     */
    @Version
    private Integer version;

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

    /**
     * 支付流水号（非数据库字段，来自payment表）
     */
    @TableField(exist = false)
    private String transactionId;

    /**
     * 支付超时时间（非数据库字段，来自payment表）
     */
    @TableField(exist = false)
    private LocalDateTime expireTime;

    /**
     * 获取订单状态码，用于JSON序列化
     * 确保返回前端期望的格式（如"pending_payment"）
     * 
     * @return 状态码字符串
     */
    @JsonProperty("status")
    public String getStatusCode() {
        return status != null ? status.getCode() : null;
    }
}