package com.muyingmall.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 物流轨迹实体类
 */
@Data
@TableName("logistics_track")
public class LogisticsTrack implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 轨迹ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 物流ID
     */
    private Long logisticsId;

    /**
     * 轨迹时间
     */
    private LocalDateTime trackingTime;

    /**
     * 当前位置
     */
    private String location;

    /**
     * 当前状态
     */
    private String status;

    /**
     * 轨迹内容
     */
    private String content;

    /**
     * 操作人
     */
    private String operator;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}