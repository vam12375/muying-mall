package com.muyingmall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 评价模板实体类
 */
@Data
@TableName("comment_template")
public class CommentTemplate implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 模板ID
     */
    @TableId(value = "template_id", type = IdType.AUTO)
    private Integer templateId;

    /**
     * 模板名称
     */
    private String templateName;

    /**
     * 模板内容
     */
    private String templateContent;

    /**
     * 模板类型：1-系统预设，2-用户自定义
     */
    private Integer templateType;

    /**
     * 适用评分范围（最小值）
     */
    private Integer minRating;

    /**
     * 适用评分范围（最大值）
     */
    private Integer maxRating;

    /**
     * 适用商品类别ID
     */
    private Integer categoryId;

    /**
     * 使用次数
     */
    private Integer useCount;

    /**
     * 创建用户ID（系统模板为null）
     */
    private Integer userId;

    /**
     * 排序权重
     */
    private Integer weight;

    /**
     * 状态：0-禁用，1-启用
     */
    private Integer status;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}