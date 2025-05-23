package com.muyingmall.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 退款处理日志实体类
 */
@Data
@TableName(value = "refund_log", autoResultMap = true)
public class RefundLog implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 日志ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 退款ID
     */
    private Long refundId;

    /**
     * 退款单号
     */
    private String refundNo;

    /**
     * 旧状态
     */
    private String oldStatus;

    /**
     * 新状态
     */
    private String newStatus;

    /**
     * 操作者类型：USER-用户, ADMIN-管理员, SYSTEM-系统
     */
    private String operatorType;

    /**
     * 操作者ID
     */
    private Integer operatorId;

    /**
     * 操作者名称
     */
    private String operatorName;

    /**
     * 处理备注
     */
    private String comment;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 关联的退款信息（非数据库字段）
     */
    @TableField(exist = false)
    private Refund refund;
}