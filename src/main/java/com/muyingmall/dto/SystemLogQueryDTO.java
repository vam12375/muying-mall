package com.muyingmall.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 系统日志查询参数DTO
 * 来源：自定义实现
 */
@Data
public class SystemLogQueryDTO {
    
    /**
     * 页码
     */
    private Integer page = 1;
    
    /**
     * 每页大小
     */
    private Integer pageSize = 10;
    
    /**
     * 管理员ID
     */
    private Integer adminId;
    
    /**
     * 管理员用户名（模糊查询）
     */
    private String adminName;
    
    /**
     * 操作类型
     */
    private String operationType;
    
    /**
     * 操作模块
     */
    private String module;
    
    /**
     * 操作结果
     */
    private String operationResult;
    
    /**
     * 开始时间
     */
    private LocalDateTime startTime;
    
    /**
     * 结束时间
     */
    private LocalDateTime endTime;
    
    /**
     * IP地址
     */
    private String ipAddress;
    
    /**
     * 请求方法
     */
    private String requestMethod;
    
    /**
     * 关键词搜索（操作名称、描述）
     */
    private String keyword;
}
