package com.muyingmall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单商品实体类
 */
@Data
@TableName("order_product")
public class OrderProduct implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 订单ID
     */
    private Integer orderId;

    /**
     * 商品ID
     */
    private Integer productId;

    /**
     * 商品名称
     */
    private String productName;

    /**
     * 商品图片
     */
    private String productImg;

    /**
     * 商品价格
     */
    private BigDecimal price;

    /**
     * 商品数量
     */
    private Integer quantity;

    /**
     * 规格信息
     */
    private String specs;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}