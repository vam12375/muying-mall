package com.muyingmall.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统配置实体类
 * 用于存储系统的各项配置参数
 */
@Data
@TableName("system_config")
public class SystemConfig {

    /**
     * 配置ID
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 配置键名
     */
    private String configKey;

    /**
     * 配置值
     */
    private String configValue;

    /**
     * 配置名称（中文描述）
     */
    private String configName;

    /**
     * 配置分组
     * basic-基础配置, business-业务配置, points-积分配置,
     * payment-支付配置, logistics-物流配置, upload-上传配置
     */
    private String configGroup;

    /**
     * 值类型
     * string-字符串, number-数字, boolean-布尔, json-JSON对象, image-图片
     */
    private String valueType;

    /**
     * 配置描述
     */
    private String description;

    /**
     * 排序号
     */
    private Integer sortOrder;

    /**
     * 状态：0-禁用，1-启用
     */
    private Integer status;

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
