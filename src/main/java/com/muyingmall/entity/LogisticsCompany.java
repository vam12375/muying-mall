package com.muyingmall.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 物流公司实体类
 */
@Data
@TableName("logistics_company")
public class LogisticsCompany implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 物流公司ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 物流公司代码，用于生成物流单号前缀
     */
    private String code;

    /**
     * 物流公司名称
     */
    private String name;

    /**
     * 联系人
     */
    private String contact;

    /**
     * 联系电话
     */
    private String phone;

    /**
     * 公司地址
     */
    private String address;

    /**
     * 状态：0-禁用，1-启用
     */
    private Integer status;

    /**
     * 物流公司logo
     */
    private String logo;

    /**
     * 排序
     */
    private Integer sortOrder;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
} 