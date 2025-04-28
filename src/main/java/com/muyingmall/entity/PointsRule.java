package com.muyingmall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 积分规则实体类
 */
@Data
@TableName("points_rule")
public class PointsRule implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 规则ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 规则标题
     */
    private String title;

    /**
     * 规则描述
     */
    private String description;

    /**
     * 规则类型
     */
    private String type;

    /**
     * 规则值（积分数量）
     * 数据库列名为 points_value
     */
    @TableField("points_value")
    private Integer value;

    /**
     * 排序
     * 数据库列名为 sort_order (假设)
     */
    @TableField("sort_order")
    private Integer sort;

    /**
     * 是否启用
     */
    private Integer enabled;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}