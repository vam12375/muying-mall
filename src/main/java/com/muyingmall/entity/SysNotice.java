package com.muyingmall.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统通知公告实体
 * 用于发布和管理系统公告
 */
@Data
@TableName("sys_notice")
public class SysNotice {

    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 公告标题
     */
    private String title;

    /**
     * 公告内容
     */
    private String content;

    /**
     * 公告类型：system-系统通知，notice-公告，feature-功能更新，activity-活动
     */
    private String type;

    /**
     * 状态：published-已发布，draft-草稿
     */
    private String status;

    /**
     * 是否置顶：1-置顶，0-不置顶
     */
    private Integer isPinned;

    /**
     * 发布者
     */
    private String author;

    /**
     * 发布时间
     */
    private LocalDateTime publishTime;

    /**
     * 浏览次数
     */
    private Integer viewCount;

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
