package com.muyingmall.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 管理员操作日志实体类
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("admin_operation_logs")
public class AdminOperationLog implements Serializable {

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
     * 操作名称
     */
    @TableField("operation")
    private String operation;

    /**
     * 操作模块
     */
    @TableField("module")
    private String module;

    /**
     * 操作类型(CREATE/READ/UPDATE/DELETE)
     */
    @TableField("operation_type")
    private String operationType;

    /**
     * 操作目标类型
     */
    @TableField("target_type")
    private String targetType;

    /**
     * 操作目标ID
     */
    @TableField("target_id")
    private String targetId;

    /**
     * 请求方法(GET/POST/PUT/DELETE)
     */
    @TableField("request_method")
    private String requestMethod;

    /**
     * 请求URL
     */
    @TableField("request_url")
    private String requestUrl;

    /**
     * 请求参数
     */
    @TableField("request_params")
    private String requestParams;

    /**
     * 响应状态码
     */
    @TableField("response_status")
    private Integer responseStatus;

    /**
     * IP地址
     */
    @TableField("ip_address")
    private String ipAddress;

    /**
     * 用户代理信息
     */
    @TableField("user_agent")
    private String userAgent;

    /**
     * 操作结果(success/failed)
     */
    @TableField("operation_result")
    private String operationResult;

    /**
     * 错误信息
     */
    @TableField("error_message")
    private String errorMessage;

    /**
     * 执行时间(毫秒)
     */
    @TableField("execution_time_ms")
    private Long executionTimeMs;

    /**
     * 操作描述
     */
    @TableField("description")
    private String description;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 操作类型枚举
     */
    public enum OperationType {
        CREATE("CREATE", "新增"),
        READ("READ", "查看"),
        UPDATE("UPDATE", "更新"),
        DELETE("DELETE", "删除"),
        EXPORT("EXPORT", "导出"),
        IMPORT("IMPORT", "导入"),
        LOGIN("LOGIN", "登录"),
        LOGOUT("LOGOUT", "登出");

        private final String code;
        private final String description;

        OperationType(String code, String description) {
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
     * 操作结果枚举
     */
    public enum OperationResult {
        SUCCESS("success", "成功"),
        FAILED("failed", "失败");

        private final String code;
        private final String description;

        OperationResult(String code, String description) {
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
