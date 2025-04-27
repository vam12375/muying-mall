package com.muyingmall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户地址实体类
 */
@Data
@TableName("user_address")
public class Address implements Serializable {

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
     * 区县
     */
    private String district;

    /**
     * 详细地址
     */
    private String detail;

    /**
     * 邮编
     */
    private String postalCode;

    /**
     * 是否默认地址：0-否，1-是
     */
    private Integer isDefault;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}