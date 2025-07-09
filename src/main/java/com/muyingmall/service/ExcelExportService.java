package com.muyingmall.service;

import com.muyingmall.entity.AdminLoginRecord;
import com.muyingmall.entity.AdminOperationLog;

import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Excel导出服务接口
 */
public interface ExcelExportService {

    /**
     * 导出登录记录到Excel
     *
     * @param response HTTP响应对象
     * @param adminId 管理员ID（可选）
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @param loginStatus 登录状态（可选）
     * @param ipAddress IP地址（可选）
     */
    void exportLoginRecords(HttpServletResponse response, Integer adminId,
                           LocalDateTime startTime, LocalDateTime endTime,
                           String loginStatus, String ipAddress) throws Exception;

    /**
     * 导出操作日志到Excel
     *
     * @param response HTTP响应对象
     * @param adminId 管理员ID（可选）
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @param operationType 操作类型（可选）
     * @param module 操作模块（可选）
     * @param operationResult 操作结果（可选）
     */
    void exportOperationLogs(HttpServletResponse response, Integer adminId,
                            LocalDateTime startTime, LocalDateTime endTime,
                            String operationType, String module,
                            String operationResult) throws Exception;

    /**
     * 导出登录记录列表到Excel
     *
     * @param response HTTP响应对象
     * @param records 登录记录列表
     * @param fileName 文件名
     */
    void exportLoginRecordsList(HttpServletResponse response, List<AdminLoginRecord> records, String fileName) throws Exception;

    /**
     * 导出操作日志列表到Excel
     *
     * @param response HTTP响应对象
     * @param logs 操作日志列表
     * @param fileName 文件名
     */
    void exportOperationLogsList(HttpServletResponse response, List<AdminOperationLog> logs, String fileName) throws Exception;

    /**
     * 设置Excel响应头
     *
     * @param response HTTP响应对象
     * @param fileName 文件名
     */
    void setExcelResponseHeaders(HttpServletResponse response, String fileName);
}
