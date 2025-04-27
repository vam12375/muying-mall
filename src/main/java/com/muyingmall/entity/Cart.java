package com.muyingmall.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 购物车实体类
 */
@Data
@TableName("cart")
public class Cart implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 购物车ID
     */
    @TableId(value = "cart_id", type = IdType.AUTO)
    private Integer cartId;

    /**
     * 用户ID
     */
    private Integer userId;

    /**
     * 商品ID
     */
    private Integer productId;

    /**
     * 商品数量
     */
    private Integer quantity;

    /**
     * 是否选中：0-未选中，1-已选中
     */
    private Integer selected;

    /**
     * 商品规格JSON
     */
    private String specs;

    /**
     * 商品规格哈希值，用于唯一索引
     */
    private String specsHash;

    /**
     * 价格快照
     */
    private BigDecimal priceSnapshot;

    /**
     * 状态：0-失效，1-有效
     */
    private Integer status;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}