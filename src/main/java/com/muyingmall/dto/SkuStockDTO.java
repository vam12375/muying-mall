package com.muyingmall.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * SKU库存操作DTO
 * 
 * @author 青柠檬
 * @date 2024-11-24
 */
@Data
public class SkuStockDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * SKU ID
     */
    private Long skuId;

    /**
     * 数量
     */
    private Integer quantity;

    /**
     * 操作类型：DEDUCT-扣减，RESTORE-恢复，ADJUST-调整
     */
    private String operationType;

    /**
     * 订单ID（扣减库存时必填）
     */
    private Integer orderId;

    /**
     * 操作人
     */
    private String operator;

    /**
     * 备注
     */
    private String remark;
}
