package com.muyingmall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 积分商城商品实体类
 */
@Data
@TableName("points_product")
public class PointsProduct implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 商品名称
     */
    private String name;

    /**
     * 商品描述
     */
    private String description;

    /**
     * 商品图片
     */
    private String image;

    /**
     * 所需积分
     */
    private Integer points;
    
    /**
     * 库存数量
     */
    private Integer stock;
    
    /**
     * 商品分类(virtual:虚拟商品,physical:实物商品,coupon:优惠券,vip:会员特权)
     */
    private String category;
    
    /**
     * 是否需要收货地址(0:否,1:是)
     */
    private Integer needAddress;
    
    /**
     * 是否需要手机号(0:否,1:是)
     */
    private Integer needPhone;
    
    /**
     * 是否热门(0:否,1:是)
     */
    private Integer isHot;
    
    /**
     * 状态(0:下架,1:上架)
     */
    private Integer status;
    
    /**
     * 排序号
     */
    private Integer sortOrder;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}