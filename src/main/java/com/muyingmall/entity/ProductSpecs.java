package com.muyingmall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 商品规格实体类
 */
@Data
@TableName("product_specs")
public class ProductSpecs implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 规格ID
     */
    @TableId(value = "spec_id", type = IdType.AUTO)
    private Integer specId;

    /**
     * 商品ID
     */
    private Integer productId;

    /**
     * 规格名称（如：颜色、尺寸等）
     */
    private String specName;

    /**
     * 规格值列表（JSON格式）
     */
    private String specValues;

    /**
     * 排序
     */
    private Integer sortOrder;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}