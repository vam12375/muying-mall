package com.muyingmall.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 搜索统计实体类
 */
@Data
@TableName("search_statistics")
public class SearchStatistics implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 统计ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 搜索关键词
     */
    private String keyword;

    /**
     * 搜索次数
     */
    private Integer searchCount;

    /**
     * 搜索结果数量
     */
    private Long resultCount;

    /**
     * 用户ID（可选）
     */
    private Integer userId;

    /**
     * 搜索来源：web, mobile, api等
     */
    private String source;

    /**
     * 搜索IP地址
     */
    private String ipAddress;

    /**
     * 用户代理
     */
    private String userAgent;

    /**
     * 搜索时间
     */
    private LocalDateTime searchTime;

    /**
     * 响应时间（毫秒）
     */
    private Long responseTime;

    /**
     * 是否有点击结果
     */
    private Boolean hasClick;

    /**
     * 点击的商品ID
     */
    private Integer clickedProductId;

    /**
     * 搜索会话ID
     */
    private String sessionId;

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
