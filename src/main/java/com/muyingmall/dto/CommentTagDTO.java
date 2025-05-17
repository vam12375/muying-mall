package com.muyingmall.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 评价标签数据传输对象
 */
@Data
public class CommentTagDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 标签ID
     */
    private Integer tagId;

    /**
     * 标签名称
     */
    private String tagName;

    /**
     * 标签类型：1-系统标签，2-用户自定义标签
     */
    private Integer tagType;

    /**
     * 关联的商品分类ID（可为空）
     */
    private Integer productCategoryId;

    /**
     * 使用次数
     */
    private Integer usageCount;

    /**
     * 状态：0-禁用，1-启用
     */
    private Integer status;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 是否已选中（用于前端展示）
     */
    private Boolean selected = false;
}