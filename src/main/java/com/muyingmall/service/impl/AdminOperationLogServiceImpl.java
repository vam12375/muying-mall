package com.muyingmall.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.muyingmall.entity.AdminOperationLog;
import com.muyingmall.event.AdminOperationEvent;
import com.muyingmall.mapper.AdminOperationLogMapper;
import com.muyingmall.service.AdminOperationLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 管理员操作日志服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminOperationLogServiceImpl extends ServiceImpl<AdminOperationLogMapper, AdminOperationLog>
        implements AdminOperationLogService {

    private final AdminOperationLogMapper operationLogMapper;
    private final ObjectMapper objectMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public Long recordOperation(Integer adminId, String adminName, String operation, String module,
            String operationType, String targetType, String targetId,
            HttpServletRequest request, Integer responseStatus,
            String operationResult, String errorMessage, Long executionTimeMs,
            String description) {
        try {
            AdminOperationLog operationLog = new AdminOperationLog();
            operationLog.setAdminId(adminId);
            operationLog.setAdminName(adminName);
            operationLog.setOperation(operation);
            operationLog.setModule(module);
            operationLog.setOperationType(operationType);
            operationLog.setTargetType(targetType);
            operationLog.setTargetId(targetId);
            operationLog.setResponseStatus(responseStatus);
            operationLog.setOperationResult(operationResult);
            operationLog.setErrorMessage(errorMessage);
            operationLog.setExecutionTimeMs(executionTimeMs);

            if (request != null) {
                operationLog.setRequestMethod(request.getMethod());
                operationLog.setRequestUrl(request.getRequestURI());
                operationLog.setRequestParams(parseRequestParams(request));
                operationLog.setIpAddress(getClientIpAddress(request));
                operationLog.setUserAgent(request.getHeader("User-Agent"));
            }

            // 生成操作描述
            if (!StringUtils.hasText(description)) {
                description = generateOperationDescription(operation, module, targetType, targetId);
            }
            operationLog.setDescription(description);

            save(operationLog);

            // 发布操作事件
            eventPublisher.publishEvent(new AdminOperationEvent(this, operationLog));

            log.info("记录管理员操作日志成功: adminId={}, operation={}, module={}",
                    adminId, operation, module);

            return operationLog.getId();
        } catch (Exception e) {
            log.error("记录管理员操作日志失败: adminId={}, operation={}, error={}",
                    adminId, operation, e.getMessage(), e);
            return null;
        }
    }

    @Override
    public IPage<AdminOperationLog> getOperationLogsPage(Integer page, Integer size, Integer adminId,
            LocalDateTime startTime, LocalDateTime endTime,
            String operationType, String module,
            String operationResult) {
        log.info(
                "Service层查询参数 - page: {}, size: {}, adminId: {}, startTime: {}, endTime: {}, operationType: {}, module: {}, operationResult: {}",
                page, size, adminId, startTime, endTime, operationType, module, operationResult);

        Page<AdminOperationLog> pageParam = new Page<>(page, size);
        IPage<AdminOperationLog> result = operationLogMapper.selectOperationLogsPage(pageParam, adminId, startTime,
                endTime,
                operationType, module, operationResult);

        log.info("Service层查询结果 - total: {}, records: {}", result.getTotal(), result.getRecords().size());
        return result;
    }

    @Override
    public Map<String, Object> getOperationStatistics(Integer adminId, Integer days) {
        Map<String, Object> statistics = new HashMap<>();

        LocalDateTime endTime = LocalDateTime.now();
        LocalDateTime startTime = endTime.minusDays(days);

        // 总操作次数
        Long totalOperations = operationLogMapper.countOperations(adminId, null, null, null);
        statistics.put("totalOperations", totalOperations != null ? totalOperations : 0);

        // 今日操作次数
        LocalDateTime todayStart = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        LocalDateTime todayEnd = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59);
        Long todayOperations = operationLogMapper.countOperations(adminId, todayStart, todayEnd, null);
        statistics.put("todayOperations", todayOperations != null ? todayOperations : 0);

        // 本周操作次数
        LocalDateTime weekStart = LocalDateTime.now().minusDays(7);
        Long weekOperations = operationLogMapper.countOperations(adminId, weekStart, endTime, null);
        statistics.put("weekOperations", weekOperations != null ? weekOperations : 0);

        // 本月操作次数
        LocalDateTime monthStart = LocalDateTime.now().minusDays(30);
        Long monthOperations = operationLogMapper.countOperations(adminId, monthStart, endTime, null);
        statistics.put("monthOperations", monthOperations != null ? monthOperations : 0);

        return statistics;
    }

    @Override
    public List<AdminOperationLog> getRecentOperations(Integer adminId, Integer limit) {
        return operationLogMapper.selectRecentOperations(adminId, limit);
    }

    @Override
    public Map<String, Integer> getOperationTypeDistribution(Integer adminId, Integer days) {
        List<Map<String, Object>> typeStats = operationLogMapper.selectOperationTypeStats(adminId, days);
        Map<String, Integer> distribution = new HashMap<>();

        for (Map<String, Object> stat : typeStats) {
            String operationType = (String) stat.get("operation_type");
            Long count = (Long) stat.get("count");
            distribution.put(operationType, count.intValue());
        }

        return distribution;
    }

    @Override
    public List<Map<String, Object>> getModuleOperationStats(Integer adminId, Integer days) {
        return operationLogMapper.selectModuleStats(adminId, days);
    }

    @Override
    public String parseRequestParams(HttpServletRequest request) {
        try {
            Map<String, Object> params = new HashMap<>();

            // 获取查询参数
            if (request.getParameterMap() != null) {
                for (Map.Entry<String, String[]> entry : request.getParameterMap().entrySet()) {
                    String key = entry.getKey();
                    String[] values = entry.getValue();
                    if (values.length == 1) {
                        params.put(key, values[0]);
                    } else {
                        params.put(key, Arrays.asList(values));
                    }
                }
            }

            // 过滤敏感信息
            params.remove("password");
            params.remove("oldPassword");
            params.remove("newPassword");
            params.remove("token");

            return objectMapper.writeValueAsString(params);
        } catch (Exception e) {
            log.warn("解析请求参数失败: {}", e.getMessage());
            return "{}";
        }
    }

    @Override
    public String generateOperationDescription(String operation, String module, String targetType, String targetId) {
        StringBuilder description = new StringBuilder();

        if (StringUtils.hasText(operation)) {
            description.append("执行了").append(operation);
        }

        if (StringUtils.hasText(module)) {
            description.append("，模块：").append(module);
        }

        if (StringUtils.hasText(targetType) && StringUtils.hasText(targetId)) {
            description.append("，操作对象：").append(targetType).append("(ID:").append(targetId).append(")");
        }

        return description.toString();
    }

    /**
     * 获取客户端真实IP
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String ipAddress = request.getHeader("X-Forwarded-For");
        if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getRemoteAddr();
        }

        // 如果是多个IP地址，取第一个
        if (ipAddress != null && ipAddress.contains(",")) {
            ipAddress = ipAddress.split(",")[0].trim();
        }

        return ipAddress;
    }
}
