package com.muyingmall.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 库存变动日志实体类
 */
@Data
@TableName("inventory_log")
public class InventoryLog implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 日志ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 商品ID
     */
    private Integer productId;

    /**
     * 变动数量，正数为增加，负数为减少
     */
    private Integer changeAmount;

    /**
     * 类型：purchase-进货，sale-销售，return-退货，adjustment-调整
     */
    private String type;

    /**
     * 关联ID，如订单号
     */
    private String referenceId;

    /**
     * 变动后剩余库存
     */
    private Integer remaining;

    /**
     * 操作人
     */
    private String operator;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 商品信息（非数据库字段）
     */
    @TableField(exist = false)
    private Product product;
}