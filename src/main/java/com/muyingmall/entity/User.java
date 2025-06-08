package com.muyingmall.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户实体类
 */
@Data
@TableName("user")
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    @TableId(value = "user_id", type = IdType.AUTO)
    private Integer userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 头像
     */
    private String avatar;

    /**
     * 性别：0-未知，1-男，2-女
     */
    private String gender;

    /**
     * 生日
     */
    private LocalDateTime birthday;

    /**
     * 用户状态：0-禁用，1-正常
     */
    private Integer status;

    /**
     * 角色：admin-管理员，user-普通用户
     */
    private String role;

    /**
     * 用户积分
     */
    @TableField(exist = false)
    private Integer points;

    /**
     * 会员等级
     */
    @TableField(exist = false)
    private Integer level;

    /**
     * 用户钱包余额
     * 注意：该字段已从user表移至user_account表，此处标记为非数据库字段
     */
    @TableField(exist = false)
    private BigDecimal balance;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}