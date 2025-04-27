package com.muyingmall.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付实体类
 */
@Data
@TableName("payment")
public class Payment implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 支付ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 支付单号
     */
    private String paymentNo;

    /**
     * 订单ID
     */
    private Integer orderId;

    /**
     * 订单号
     */
    private String orderNo;

    /**
     * 用户ID
     */
    private Integer userId;

    /**
     * 支付金额
     */
    private BigDecimal amount;

    /**
     * 支付方式: alipay, wechat, bank
     */
    private String paymentMethod;

    /**
     * 支付状态: 0-待支付 1-支付中 2-支付成功 3-支付失败 4-已关闭
     */
    private Integer status;

    /**
     * 支付时间
     */
    private LocalDateTime payTime;

    /**
     * 第三方支付流水号
     */
    private String transactionId;

    /**
     * 支付成功时间
     */
    private LocalDateTime paymentTime;

    /**
     * 支付超时时间
     */
    private LocalDateTime expireTime;

    /**
     * 异步通知地址
     */
    private String notifyUrl;

    /**
     * 同步返回地址
     */
    private String returnUrl;

    /**
     * 支付附加信息
     */
    private String extra;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}