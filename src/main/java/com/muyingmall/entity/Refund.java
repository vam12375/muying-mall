package com.muyingmall.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 退款申请实体类
 */
@Data
@TableName(value = "refund", autoResultMap = true)
public class Refund implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 退款ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 退款单号
     */
    private String refundNo;

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
     * 支付ID
     */
    private Long paymentId;

    /**
     * 退款金额
     */
    private BigDecimal amount;

    /**
     * 退款原因
     */
    private String refundReason;

    /**
     * 退款原因详情
     */
    private String refundReasonDetail;

    /**
     * 凭证图片
     */
    private String evidenceImages;

    /**
     * 退款状态：PENDING-待处理, APPROVED-已批准, REJECTED-已拒绝, PROCESSING-处理中, COMPLETED-已完成,
     * FAILED-退款失败
     */
    private String status;

    /**
     * 拒绝原因
     */
    private String rejectReason;

    /**
     * 退款时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime refundTime;

    /**
     * 退款账户
     */
    private String refundAccount;

    /**
     * 退款渠道：ALIPAY-支付宝, WECHAT-微信, BANK-银行卡
     */
    private String refundChannel;

    /**
     * 退款交易号
     */
    private String transactionId;

    /**
     * 处理人ID
     */
    private Integer adminId;

    /**
     * 处理人姓名
     */
    private String adminName;

    /**
     * 是否删除：0-否，1-是
     */
    @TableLogic
    private Integer isDeleted;

    /**
     * 版本号，用于乐观锁
     */
    @Version
    private Integer version;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    /**
     * 用户名（非数据库字段，用于前端显示）
     */
    @TableField(exist = false)
    private String username;

    /**
     * 关联的订单信息（非数据库字段）
     */
    @TableField(exist = false)
    private Order order;

    /**
     * 关联的用户信息（非数据库字段）
     */
    @TableField(exist = false)
    private User user;

    /**
     * 关联的支付信息（非数据库字段）
     */
    @TableField(exist = false)
    private Payment payment;
}