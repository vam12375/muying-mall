package com.muyingmall.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户地址实体类
 */
@Data
@TableName("user_address")
public class UserAddress implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 地址ID
     */
    @TableId(value = "address_id", type = IdType.AUTO)
    private Integer addressId;

    /**
     * 用户ID
     */
    private Integer userId;

    /**
     * 收货人
     */
    private String receiver;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 省份
     */
    private String province;

    /**
     * 城市
     */
    private String city;

    /**
     * 区/县
     */
    private String district;

    /**
     * 详细地址
     */
    @TableField("detail")
    private String address;

    /**
     * 邮政编码
     */
    @TableField("postal_code")
    private String zip;

    /**
     * 是否默认 0-否 1-是
     */
    private Integer isDefault;

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