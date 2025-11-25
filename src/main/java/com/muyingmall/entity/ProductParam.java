package com.muyingmall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 商品参数实体类
 */
@Data
@TableName("product_param")
public class ProductParam {
    
    /**
     * 参数ID
     */
    @TableId(type = IdType.AUTO)
    private Integer paramId;
    
    /**
     * 商品ID
     */
    private Integer productId;
    
    /**
     * 参数名称
     */
    private String paramName;
    
    /**
     * 参数值
     */
    private String paramValue;
    
    /**
     * 参数单位
     */
    private String paramUnit;
    
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
