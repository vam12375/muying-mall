package com.muyingmall.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 商品分类实体类
 */
@Data
@TableName("category")
public class Category implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @TableId(value = "category_id", type = IdType.AUTO)
    private Integer categoryId;
    
    private Integer parentId;
    
    private String name;
    
    private String icon;
    
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