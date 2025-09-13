package com.muyingmall.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.muyingmall.entity.AdminOperationLog;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 管理员操作日志服务接口
 */
public interface AdminOperationLogService extends IService<AdminOperationLog> {

    /**
     * 记录操作日志
     *
     * @param adminId         管理员ID
     * @param adminName       管理员用户名
     * @param operation       操作名称
     * @param module          操作模块
     * @param operationType   操作类型
     * @param targetType      操作目标类型
     * @param targetId        操作目标ID
     * @param request         HTTP请求对象
     * @param responseStatus  响应状态码
     * @param operationResult 操作结果
     * @param errorMessage    错误信息
     * @param executionTimeMs 执行时间(毫秒)
     * @param description     操作描述
     * @return 日志ID
     */
    Long recordOperation(Integer adminId, String adminName, String operation, String module,
            String operationType, String targetType, String targetId,
            HttpServletRequest request, Integer responseStatus,
            String operationResult, String errorMessage, Long executionTimeMs,
            String description);

    /**
     * 分页查询操作日志
     *
     * @param page            页码
     * @param size            每页大小
     * @param adminId         管理员ID（可选）
     * @param startTime       开始时间（可选）
     * @param endTime         结束时间（可选）
     * @param operationType   操作类型（可选）
     * @param module          操作模块（可选）
     * @param operationResult 操作结果（可选）
     * @return 分页结果
     */
    IPage<AdminOperationLog> getOperationLogsPage(Integer page, Integer size, Integer adminId,
            LocalDateTime startTime, LocalDateTime endTime,
            String operationType, String module,
            String operationResult);

    /**
     * 获取操作统计信息
     *
     * @param adminId 管理员ID（可选）
     * @param days    统计天数
     * @return 统计信息
     */
    Map<String, Object> getOperationStatistics(Integer adminId, Integer days);

    /**
     * 获取最近操作记录
     *
     * @param adminId 管理员ID
     * @param limit   记录数量
     * @return 操作记录列表
     */
    List<AdminOperationLog> getRecentOperations(Integer adminId, Integer limit);

    /**
     * 获取操作类型分布统计
     *
     * @param adminId 管理员ID（可选）
     * @param days    统计天数
     * @return 操作类型分布
     */
    Map<String, Integer> getOperationTypeDistribution(Integer adminId, Integer days);

    /**
     * 获取模块操作统计
     *
     * @param adminId 管理员ID（可选）
     * @param days    统计天数
     * @return 模块操作统计
     */
    List<Map<String, Object>> getModuleOperationStats(Integer adminId, Integer days);

    /**
     * 解析请求参数
     *
     * @param request HTTP请求对象
     * @return 请求参数字符串
     */
    String parseRequestParams(HttpServletRequest request);

    /**
     * 获取操作描述
     *
     * @param operation  操作名称
     * @param module     操作模块
     * @param targetType 操作目标类型
     * @param targetId   操作目标ID
     * @return 操作描述
     */
    String generateOperationDescription(String operation, String module, String targetType, String targetId);
}
