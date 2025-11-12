package com.muyingmall.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户积分实体类
 */
@Data
@TableName("user_points")
public class UserPoints implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 积分ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 积分总数
     */
    private Integer points;

    /**
     * 会员等级
     */
    private String level;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 用户信息（非数据库字段）
     */
    @TableField(exist = false)
    private User user;

    /**
     * 已获得积分总数（非数据库字段，用于统计展示）
     */
    @TableField(exist = false)
    private Integer totalEarned;

    /**
     * 已使用积分总数（非数据库字段，用于统计展示）
     */
    @TableField(exist = false)
    private Integer totalUsed;

    /**
     * 可用积分（非数据库字段，当前等同于points）
     */
    @TableField(exist = false)
    private Integer availablePoints;

    /**
     * 已过期积分（非数据库字段，暂未实现过期机制，默认为0）
     */
    @TableField(exist = false)
    private Integer expiredPoints;

    /**
     * 即将过期积分（非数据库字段，暂未实现过期机制，默认为0）
     */
    @TableField(exist = false)
    private Integer expiringSoonPoints;

    /**
     * 用户名（非数据库字段，用于前端展示）
     */
    @TableField(exist = false)
    private String username;

    /**
     * 会员等级名称（非数据库字段，用于前端展示）
     */
    @TableField(exist = false)
    private String levelName;
} 