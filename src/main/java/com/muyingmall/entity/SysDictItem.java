package com.muyingmall.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 字典项实体
 * 用于管理字典类型下的具体选项
 */
@Data
@TableName("sys_dict_item")
public class SysDictItem {

    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 所属字典类型编码
     */
    private String dictCode;

    /**
     * 字典项标签（显示名称）
     */
    private String label;

    /**
     * 字典项值
     */
    private String value;

    /**
     * 排序值，越小越靠前
     */
    private Integer sort;

    /**
     * 状态：enabled-启用，disabled-禁用
     */
    private String status;

    /**
     * 备注说明
     */
    private String remark;

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
