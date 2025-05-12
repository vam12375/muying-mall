package com.muyingmall.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 积分操作日志表
 * </p>
 *
 * @author XXX（请替换为实际的作者名）
 * @since YYYY-MM-DD（请替换为实际的创建日期）
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("points_operation_log")
public class PointsOperationLog implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @TableField("user_id")
    private Integer userId;

    /**
     * 操作类型（例如：SIGN_IN, ORDER_REWARD, EXCHANGE_PRODUCT, ADMIN_ADJUSTMENT,
     * EVENT_REWARD, OTHER）
     */
    @TableField("operation_type")
    private String operationType;

    /**
     * 积分变动数量（正数为增加，负数为减少）
     */
    @TableField("points_change")
    private Integer pointsChange;

    /**
     * 操作后当前积分余额
     */
    @TableField("current_balance")
    private Integer currentBalance;

    /**
     * 描述
     */
    @TableField("description")
    private String description;

    /**
     * 关联订单ID（如果适用）
     */
    @TableField("related_order_id")
    private Integer relatedOrderId;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private LocalDateTime createTime;

}