package com.muyingmall.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.muyingmall.enums.LogisticsStatus;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 物流实体类
 */
@Data
@TableName(value = "logistics", autoResultMap = true)
public class Logistics implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 物流ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 订单ID
     */
    private Integer orderId;

    /**
     * 物流公司ID
     */
    private Integer companyId;

    /**
     * 物流单号
     */
    private String trackingNo;

    /**
     * 物流状态：CREATED-已创建，SHIPPING-运输中，DELIVERED-已送达，EXCEPTION-异常
     */
    private LogisticsStatus status;

    /**
     * 发件人姓名
     */
    private String senderName;

    /**
     * 发件人电话
     */
    private String senderPhone;

    /**
     * 发件地址
     */
    private String senderAddress;

    /**
     * 发货地经度（高德地图坐标）
     */
    private Double senderLongitude;

    /**
     * 发货地纬度（高德地图坐标）
     */
    private Double senderLatitude;

    /**
     * 收件人姓名
     */
    private String receiverName;

    /**
     * 收件人电话
     */
    private String receiverPhone;

    /**
     * 收件地址
     */
    private String receiverAddress;

    /**
     * 收货地经度（高德地图坐标）
     */
    private Double receiverLongitude;

    /**
     * 收货地纬度（高德地图坐标）
     */
    private Double receiverLatitude;

    /**
     * 发货时间
     */
    private LocalDateTime shippingTime;

    /**
     * 送达时间
     */
    private LocalDateTime deliveryTime;

    /**
     * 备注
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

    /**
     * 物流公司信息（非数据库字段）
     */
    @TableField(exist = false)
    private LogisticsCompany company;

    /**
     * 物流轨迹列表（非数据库字段）
     */
    @TableField(exist = false)
    private List<LogisticsTrack> tracks;

    /**
     * 订单信息（非数据库字段）
     */
    @TableField(exist = false)
    private Order order;

    /**
     * 获取物流状态码，用于JSON序列化
     *
     * @return 状态码字符串
     */
    @JsonProperty("status")
    public String getStatusCode() {
        return status != null ? status.getCode() : null;
    }
}