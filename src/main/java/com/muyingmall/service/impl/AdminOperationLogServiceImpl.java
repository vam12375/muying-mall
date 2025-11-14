package com.muyingmall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.muyingmall.entity.AdminOperationLog;
import com.muyingmall.mapper.AdminOperationLogMapper;
import com.muyingmall.service.AdminOperationLogService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 管理员操作日志服务实现类
 * 
 * Source: 基于 AdminOperationLogService 接口实现
 * 遵循 KISS, YAGNI, SOLID 原则
 */
@Slf4j
@Service
public class AdminOperationLogServiceImpl extends ServiceImpl<AdminOperationLogMapper, AdminOperationLog>
        implements AdminOperationLogService {

    @Override
    public Long recordOperation(Integer adminId, String adminName, String operation, String module,
            String operationType, String targetType, String targetId,
            HttpServletRequest request, Integer responseStatus,
            String operationResult, String errorMessage, Long executionTimeMs,
            String description) {
        try {
            // 获取客户端IP
            String ipAddress = getClientIpAddress(request);
            
            // 获取User-Agent
            String userAgent = request.getHeader("User-Agent");
            
            // 获取请求信息
            String requestMethod = request.getMethod();
            String requestUrl = request.getRequestURI();
            String requestParams = parseRequestParams(request);
            
            // 创建操作日志
            AdminOperationLog log = new AdminOperationLog();
            log.setAdminId(adminId);
            log.setAdminName(adminName);
            log.setOperation(operation);
            log.setModule(module);
            log.setOperationType(operationType);
            log.setTargetType(targetType);
            log.setTargetId(targetId);
            log.setRequestMethod(requestMethod);
            log.setRequestUrl(requestUrl);
            log.setRequestParams(requestParams);
            log.setResponseStatus(responseStatus);
            log.setIpAddress(ipAddress);
            log.setUserAgent(userAgent);
            log.setOperationResult(operationResult);
            log.setErrorMessage(errorMessage);
            log.setExecutionTimeMs(executionTimeMs);
            log.setDescription(description != null ? description : 
                    generateOperationDescription(operation, module, targetType, targetId));
            
            // 保存日志
            save(log);
            
            AdminOperationLogServiceImpl.log.info("记录管理员操作: adminId={}, operation={}, module={}, result={}", 
                    adminId, operation, module, operationResult);
            
            return log.getId();
        } catch (Exception e) {
            AdminOperationLogServiceImpl.log.error("记录操作日志失败", e);
            return null;
        }
    }

    @Override
    public IPage<AdminOperationLog> getOperationLogsPage(Integer page, Integer size, Integer adminId,
            LocalDateTime startTime, LocalDateTime endTime, String operationType, String module,
            String operationResult) {
        
        Page<AdminOperationLog> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<AdminOperationLog> wrapper = new LambdaQueryWrapper<>();
        
        // 条件查询
        if (adminId != null) {
            wrapper.eq(AdminOperationLog::getAdminId, adminId);
        }
        if (startTime != null) {
            wrapper.ge(AdminOperationLog::getCreateTime, startTime);
        }
        if (endTime != null) {
            wrapper.le(AdminOperationLog::getCreateTime, endTime);
        }
        if (operationType != null && !operationType.isEmpty()) {
            wrapper.eq(AdminOperationLog::getOperationType, operationType);
        }
        if (module != null && !module.isEmpty()) {
            wrapper.eq(AdminOperationLog::getModule, module);
        }
        if (operationResult != null && !operationResult.isEmpty()) {
            wrapper.eq(AdminOperationLog::getOperationResult, operationResult);
        }
        
        // 按创建时间倒序
        wrapper.orderByDesc(AdminOperationLog::getCreateTime);
        
        return page(pageParam, wrapper);
    }

    @Override
    public Map<String, Object> getOperationStatistics(Integer adminId, Integer days) {
        Map<String, Object> stats = new HashMap<>();
        
        LocalDateTime startTime = LocalDateTime.now().minusDays(days);
        LambdaQueryWrapper<AdminOperationLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.ge(AdminOperationLog::getCreateTime, startTime);
        
        if (adminId != null) {
            wrapper.eq(AdminOperationLog::getAdminId, adminId);
        }
        
        // 总操作次数
        long totalOperations = count(wrapper);
        stats.put("totalOperations", totalOperations);
        
        // 成功操作次数
        LambdaQueryWrapper<AdminOperationLog> successWrapper = new LambdaQueryWrapper<>();
        successWrapper.ge(AdminOperationLog::getCreateTime, startTime)
                     .eq(AdminOperationLog::getOperationResult, "success");
        if (adminId != null) {
            successWrapper.eq(AdminOperationLog::getAdminId, adminId);
        }
        long successOperations = count(successWrapper);
        stats.put("successOperations", successOperations);
        
        // 失败操作次数
        stats.put("failedOperations", totalOperations - successOperations);
        
        // 今日操作次数
        LocalDateTime todayStart = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        LambdaQueryWrapper<AdminOperationLog> todayWrapper = new LambdaQueryWrapper<>();
        todayWrapper.ge(AdminOperationLog::getCreateTime, todayStart);
        if (adminId != null) {
            todayWrapper.eq(AdminOperationLog::getAdminId, adminId);
        }
        long todayOperations = count(todayWrapper);
        stats.put("todayOperations", todayOperations);
        
        // 本周操作次数
        LocalDateTime weekStart = LocalDateTime.now().minusDays(7);
        LambdaQueryWrapper<AdminOperationLog> weekWrapper = new LambdaQueryWrapper<>();
        weekWrapper.ge(AdminOperationLog::getCreateTime, weekStart);
        if (adminId != null) {
            weekWrapper.eq(AdminOperationLog::getAdminId, adminId);
        }
        long weekOperations = count(weekWrapper);
        stats.put("weekOperations", weekOperations);
        
        // 本月操作次数
        LocalDateTime monthStart = LocalDateTime.now().minusDays(30);
        LambdaQueryWrapper<AdminOperationLog> monthWrapper = new LambdaQueryWrapper<>();
        monthWrapper.ge(AdminOperationLog::getCreateTime, monthStart);
        if (adminId != null) {
            monthWrapper.eq(AdminOperationLog::getAdminId, adminId);
        }
        long monthOperations = count(monthWrapper);
        stats.put("monthOperations", monthOperations);
        
        return stats;
    }

    @Override
    public List<AdminOperationLog> getRecentOperations(Integer adminId, Integer limit) {
        LambdaQueryWrapper<AdminOperationLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AdminOperationLog::getAdminId, adminId)
               .orderByDesc(AdminOperationLog::getCreateTime)
               .last("LIMIT " + limit);
        
        return list(wrapper);
    }

    @Override
    public Map<String, Integer> getOperationTypeDistribution(Integer adminId, Integer days) {
        LocalDateTime startTime = LocalDateTime.now().minusDays(days);
        LambdaQueryWrapper<AdminOperationLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.ge(AdminOperationLog::getCreateTime, startTime);
        
        if (adminId != null) {
            wrapper.eq(AdminOperationLog::getAdminId, adminId);
        }
        
        List<AdminOperationLog> logs = list(wrapper);
        
        // 按操作类型分组统计
        Map<String, Integer> distribution = logs.stream()
                .collect(Collectors.groupingBy(
                        log -> log.getOperationType() != null ? log.getOperationType() : "UNKNOWN",
                        Collectors.collectingAndThen(Collectors.counting(), Long::intValue)
                ));
        
        return distribution;
    }

    @Override
    public List<Map<String, Object>> getModuleOperationStats(Integer adminId, Integer days) {
        LocalDateTime startTime = LocalDateTime.now().minusDays(days);
        LambdaQueryWrapper<AdminOperationLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.ge(AdminOperationLog::getCreateTime, startTime);
        
        if (adminId != null) {
            wrapper.eq(AdminOperationLog::getAdminId, adminId);
        }
        
        List<AdminOperationLog> logs = list(wrapper);
        
        // 按模块分组统计
        Map<String, Long> moduleStats = logs.stream()
                .collect(Collectors.groupingBy(
                        log -> log.getModule() != null ? log.getModule() : "未知模块",
                        Collectors.counting()
                ));
        
        // 转换为List<Map>格式
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<String, Long> entry : moduleStats.entrySet()) {
            Map<String, Object> stat = new HashMap<>();
            stat.put("module", entry.getKey());
            stat.put("count", entry.getValue());
            result.add(stat);
        }
        
        // 按操作次数降序排序
        result.sort((a, b) -> Long.compare((Long) b.get("count"), (Long) a.get("count")));
        
        return result;
    }

    @Override
    public String parseRequestParams(HttpServletRequest request) {
        try {
            Map<String, String[]> paramMap = request.getParameterMap();
            if (paramMap.isEmpty()) {
                return null;
            }
            
            StringBuilder params = new StringBuilder();
            for (Map.Entry<String, String[]> entry : paramMap.entrySet()) {
                if (params.length() > 0) {
                    params.append("&");
                }
                params.append(entry.getKey()).append("=");
                
                String[] values = entry.getValue();
                if (values.length == 1) {
                    // 过滤敏感信息
                    if (entry.getKey().toLowerCase().contains("password") || 
                        entry.getKey().toLowerCase().contains("pwd")) {
                        params.append("******");
                    } else {
                        params.append(values[0]);
                    }
                } else {
                    params.append(Arrays.toString(values));
                }
            }
            
            // 限制参数长度
            String result = params.toString();
            if (result.length() > 500) {
                result = result.substring(0, 500) + "...";
            }
            
            return result;
        } catch (Exception e) {
            AdminOperationLogServiceImpl.log.error("解析请求参数失败", e);
            return null;
        }
    }

    @Override
    public String generateOperationDescription(String operation, String module, String targetType, String targetId) {
        StringBuilder desc = new StringBuilder();
        
        if (module != null && !module.isEmpty()) {
            desc.append("[").append(module).append("] ");
        }
        
        if (operation != null && !operation.isEmpty()) {
            desc.append(operation);
        }
        
        if (targetType != null && !targetType.isEmpty()) {
            desc.append(" ").append(targetType);
        }
        
        if (targetId != null && !targetId.isEmpty()) {
            desc.append("(ID:").append(targetId).append(")");
        }
        
        return desc.toString();
    }
    
    /**
     * 获取客户端真实IP
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        
        // 处理多个IP的情况，取第一个
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        
        return ip;
    }
}
