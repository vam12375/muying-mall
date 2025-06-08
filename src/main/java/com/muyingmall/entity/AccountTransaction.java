package com.muyingmall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 账户交易记录实体类
 */
@Data
@TableName("account_transaction")
public class AccountTransaction {

    /**
     * 交易ID
     * 注意: 数据库表中的列名是 transaction_id，而不是 id
     */
    @TableId(value = "transaction_id", type = IdType.AUTO)
    private Integer id;

    /**
     * 用户ID
     */
    private Integer userId;

    /**
     * 交易类型：1-充值，2-消费，3-退款，4-管理员调整
     */
    private Integer type;

    /**
     * 交易金额
     */
    private BigDecimal amount;

    /**
     * 交易前余额
     * 注意：此字段在数据库中不存在，仅用于应用层逻辑处理
     */
    @TableField(exist = false)
    private BigDecimal beforeBalance;

    /**
     * 交易后余额
     * 注意：此字段在数据库中不存在，仅用于应用层逻辑处理
     * 数据库中使用 balance 字段存储交易后余额
     */
    @TableField(exist = false)
    private BigDecimal afterBalance;

    /**
     * 账户余额（交易后）
     * 对应数据库中的 balance 字段
     */
    private BigDecimal balance;

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
     * 账户ID
     * 注意: 此字段在 db/user_account.sql 中定义，在实际数据库中存在
     */
    @NotNull(message = "账户ID不能为空")
    private Integer accountId;

    /**
     * 关联ID（如订单ID、退款ID等）
     */
    private String relatedId;

    /**
     * 交易描述
     */
    private String description;

    /**
     * 备注
     */
    private String remark;

    /**
     * 操作人ID（管理员ID或系统ID）
     * 注意：此字段在数据库中不存在，仅用于应用层逻辑处理
     */
    @TableField(exist = false)
    private Integer operatorId;

    /**
     * 操作人名称
     * 注意：此字段在数据库中不存在，仅用于应用层逻辑处理
     */
    @TableField(exist = false)
    private String operatorName;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;

    /**
     * 关联的用户信息（非数据库字段）
     */
    @TableField(exist = false)
    private User user;

    /**
     * 构造函数 - 设置默认值，防止空值
     */
    public AccountTransaction() {
        // 初始化所有数值类型为非null值
        this.userId = 0; // 将被实际值覆盖
        this.accountId = 0; // 将被实际值覆盖
        this.type = 0;
        this.amount = BigDecimal.ZERO;
        this.balance = BigDecimal.ZERO;
        this.status = 0;
        this.beforeBalance = BigDecimal.ZERO;
        this.afterBalance = BigDecimal.ZERO;
        // 初始化日期为当前时间
        this.createTime = new Date();
        this.updateTime = new Date();
    }
}