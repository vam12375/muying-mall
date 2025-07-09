package com.muyingmall.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 管理员登录记录实体类
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("admin_login_records")
public class AdminLoginRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 管理员ID
     */
    @TableField("admin_id")
    private Integer adminId;

    /**
     * 管理员用户名
     */
    @TableField("admin_name")
    private String adminName;

    /**
     * 登录时间
     */
    @TableField("login_time")
    private LocalDateTime loginTime;

    /**
     * 登出时间
     */
    @TableField("logout_time")
    private LocalDateTime logoutTime;

    /**
     * IP地址
     */
    @TableField("ip_address")
    private String ipAddress;

    /**
     * 登录地点
     */
    @TableField("location")
    private String location;

    /**
     * 用户代理信息
     */
    @TableField("user_agent")
    private String userAgent;

    /**
     * 设备类型(Desktop/Mobile/Tablet)
     */
    @TableField("device_type")
    private String deviceType;

    /**
     * 浏览器信息
     */
    @TableField("browser")
    private String browser;

    /**
     * 操作系统
     */
    @TableField("os")
    private String os;

    /**
     * 登录状态(success/failed)
     */
    @TableField("login_status")
    private String loginStatus;

    /**
     * 失败原因
     */
    @TableField("failure_reason")
    private String failureReason;

    /**
     * 会话ID
     */
    @TableField("session_id")
    private String sessionId;

    /**
     * 会话时长(秒)
     */
    @TableField("duration_seconds")
    private Integer durationSeconds;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 登录状态枚举
     */
    public enum LoginStatus {
        SUCCESS("success", "成功"),
        FAILED("failed", "失败");

        private final String code;
        private final String description;

        LoginStatus(String code, String description) {
            this.code = code;
            this.description = description;
        }

        public String getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 设备类型枚举
     */
    public enum DeviceType {
        DESKTOP("Desktop", "桌面端"),
        MOBILE("Mobile", "移动端"),
        TABLET("Tablet", "平板端");

        private final String code;
        private final String description;

        DeviceType(String code, String description) {
            this.code = code;
            this.description = description;
        }

        public String getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }
    }
}
