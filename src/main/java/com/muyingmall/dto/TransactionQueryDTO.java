package com.muyingmall.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * 交易记录查询DTO
 */
@Data
public class TransactionQueryDTO {

    /**
     * 用户ID
     */
    private Integer userId;

    /**
     * 交易类型：1-充值，2-消费，3-退款，4-管理员调整
     */
    private Integer type;

    /**
     * 交易状态：0-失败，1-成功，2-处理中
     */
    private Integer status;

    /**
     * 支付方式：alipay-支付宝，wechat-微信支付，bank-银行卡，admin-管理员操作
     */
    private String paymentMethod;

    /**
     * 交易流水号
     */
    private String transactionNo;

    /**
     * 开始时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date startTime;

    /**
     * 结束时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date endTime;

    /**
     * 关键字（用户名/昵称/邮箱/手机）
     */
    private String keyword;
}