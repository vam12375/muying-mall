package com.muyingmall.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 系统日志数据传输对象
 * 来源：自定义实现
 */
@Data
public class SystemLogDTO {
    
    /**
     * 日志ID
     */
    private Long id;
    
    /**
     * 管理员ID
     */
    private Integer adminId;
    
    /**
     * 管理员用户名
     */
    private String adminName;
    
    /**
     * 操作名称
     */
    private String operation;
    
    /**
     * 操作模块
     */
    private String module;
    
    /**
     * 操作类型(CREATE/READ/UPDATE/DELETE)
     */
    private String operationType;
    
    /**
     * 操作目标类型
     */
    private String targetType;
    
    /**
     * 操作目标ID
     */
    private String targetId;
    
    /**
     * 请求方法(GET/POST/PUT/DELETE)
     */
    private String requestMethod;
    
    /**
     * 请求URL
     */
    private String requestUrl;
    
    /**
     * 请求参数
     */
    private String requestParams;
    
    /**
     * 响应状态码
     */
    private Integer responseStatus;
    
    /**
     * IP地址
     */
    private String ipAddress;
    
    /**
     * 用户代理信息
     */
    private String userAgent;
    
    /**
     * 操作结果(success/failed)
     */
    private String operationResult;
    
    /**
     * 错误信息
     */
    private String errorMessage;
    
    /**
     * 执行时间(毫秒)
     */
    private Long executionTimeMs;
    
    /**
     * 操作描述
     */
    private String description;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
