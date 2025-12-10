package com.muyingmall.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 字典类型实体
 * 用于管理系统字典分类
 */
@Data
@TableName("sys_dict_type")
public class SysDictType {

    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 字典类型编码（唯一标识）
     */
    private String code;

    /**
     * 字典类型名称
     */
    private String name;

    /**
     * 状态：enabled-启用，disabled-禁用
     */
    private String status;

    /**
     * 备注说明
     */
    private String remark;

    /**
     * 字典项数量（非数据库字段）
     */
    @TableField(exist = false)
    private Integer itemCount;

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
