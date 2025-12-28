package com.muyingmall.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.muyingmall.config.mybatis.JsonTypeHandler;
import lombok.Data;
import org.apache.ibatis.type.JdbcType;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 物流轨迹实体类
 */
@Data
@TableName(value = "logistics_track", autoResultMap = true)
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
     * 经度（高德地图）
     */
    private Double longitude;

    /**
     * 纬度（高德地图）
     */
    private Double latitude;

    /**
     * 位置名称
     */
    private String locationName;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 轨迹详情JSON数据
     * 存储更丰富的轨迹信息，如图片、链接、扩展字段等
     */
    @TableField(typeHandler = JsonTypeHandler.class, jdbcType = JdbcType.VARCHAR)
    private Map<String, Object> detailsJson;
}