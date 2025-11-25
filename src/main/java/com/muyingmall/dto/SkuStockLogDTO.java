package com.muyingmall.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * SKU库存日志DTO
 * 
 * @author 青柠檬
 * @date 2024-11-24
 */
@Data
public class SkuStockLogDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 日志ID
     */
    private Long logId;

    /**
     * SKU ID
     */
    private Long skuId;

    /**
     * SKU编码
     */
    private String skuCode;

    /**
     * SKU名称
     */
    private String skuName;

    /**
     * 订单ID
     */
    private Integer orderId;

    /**
     * 变更类型：DEDUCT-扣减，RESTORE-恢复，ADJUST-调整
     */
    private String changeType;

    /**
     * 变更数量（正数为增加，负数为减少）
     */
    private Integer changeQuantity;

    /**
     * 变更前库存
     */
    private Integer beforeStock;

    /**
     * 变更后库存
     */
    private Integer afterStock;

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
}
