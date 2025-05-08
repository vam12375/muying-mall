package com.muyingmall.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 品牌实体类
 */
@Data
@TableName("brand")
public class Brand implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "brand_id", type = IdType.AUTO)
    private Integer brandId;

    private String name;

    private String logo;

    private String description;

    private Integer sortOrder;

    /**
     * 状态：0-禁用，1-正常
     */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}