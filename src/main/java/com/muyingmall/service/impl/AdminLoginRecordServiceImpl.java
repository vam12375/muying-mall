package com.muyingmall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.muyingmall.entity.AdminLoginRecord;
import com.muyingmall.mapper.AdminLoginRecordMapper;
import com.muyingmall.service.AdminLoginRecordService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 管理员登录记录服务实现类
 * 
 * Source: 基于 AdminLoginRecordService 接口实现
 * 遵循 KISS, YAGNI, SOLID 原则
 */
@Slf4j
@Service
public class AdminLoginRecordServiceImpl extends ServiceImpl<AdminLoginRecordMapper, AdminLoginRecord>
        implements AdminLoginRecordService {

    @Override
    public Long recordLogin(Integer adminId, String adminName, HttpServletRequest request,
            String loginStatus, String failureReason) {
        try {
            // 获取客户端IP
            String ipAddress = getClientIpAddress(request);
            
            // 获取User-Agent
            String userAgent = request.getHeader("User-Agent");
            
            // 解析设备信息
            Map<String, String> deviceInfo = parseDeviceInfo(userAgent);
            
            // 获取地理位置
            String location = getLocationByIp(ipAddress);
            
            // 创建登录记录
            AdminLoginRecord record = new AdminLoginRecord();
            record.setAdminId(adminId);
            record.setAdminName(adminName);
            record.setLoginTime(LocalDateTime.now());
            record.setIpAddress(ipAddress);
            record.setLocation(location);
            record.setUserAgent(userAgent);
            record.setDeviceType(deviceInfo.get("deviceType"));
            record.setBrowser(deviceInfo.get("browser"));
            record.setOs(deviceInfo.get("os"));
            record.setLoginStatus(loginStatus);
            record.setFailureReason(failureReason);
            record.setSessionId(request.getSession().getId());
            
            // 保存记录
            save(record);
            
            log.info("记录管理员登录: adminId={}, adminName={}, ip={}, status={}", 
                    adminId, adminName, ipAddress, loginStatus);
            
            return record.getId();
        } catch (Exception e) {
            log.error("记录登录信息失败", e);
            return null;
        }
    }

    @Override
    public boolean recordLogout(String sessionId) {
        try {
            LambdaQueryWrapper<AdminLoginRecord> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(AdminLoginRecord::getSessionId, sessionId)
                   .isNull(AdminLoginRecord::getLogoutTime)
                   .orderByDesc(AdminLoginRecord::getLoginTime)
                   .last("LIMIT 1");
            
            AdminLoginRecord record = getOne(wrapper);
            if (record != null) {
                record.setLogoutTime(LocalDateTime.now());
                
                // 计算会话时长
                if (record.getLoginTime() != null) {
                    long seconds = java.time.Duration.between(record.getLoginTime(), 
                            record.getLogoutTime()).getSeconds();
                    record.setDurationSeconds((int) seconds);
                }
                
                updateById(record);
                log.info("记录管理员登出: sessionId={}", sessionId);
                return true;
            }
            return false;
        } catch (Exception e) {
            log.error("记录登出信息失败", e);
            return false;
        }
    }

    @Override
    public boolean updateSessionDuration(String sessionId, Integer durationSeconds) {
        try {
            LambdaQueryWrapper<AdminLoginRecord> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(AdminLoginRecord::getSessionId, sessionId)
                   .orderByDesc(AdminLoginRecord::getLoginTime)
                   .last("LIMIT 1");
            
            AdminLoginRecord record = getOne(wrapper);
            if (record != null) {
                record.setDurationSeconds(durationSeconds);
                updateById(record);
                return true;
            }
            return false;
        } catch (Exception e) {
            log.error("更新会话时长失败", e);
            return false;
        }
    }

    @Override
    public IPage<AdminLoginRecord> getLoginRecordsPage(Integer page, Integer size, Integer adminId,
            LocalDateTime startTime, LocalDateTime endTime, String loginStatus, String ipAddress) {
        
        Page<AdminLoginRecord> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<AdminLoginRecord> wrapper = new LambdaQueryWrapper<>();
        
        // 条件查询
        if (adminId != null) {
            wrapper.eq(AdminLoginRecord::getAdminId, adminId);
        }
        if (startTime != null) {
            wrapper.ge(AdminLoginRecord::getLoginTime, startTime);
        }
        if (endTime != null) {
            wrapper.le(AdminLoginRecord::getLoginTime, endTime);
        }
        if (loginStatus != null && !loginStatus.isEmpty()) {
            wrapper.eq(AdminLoginRecord::getLoginStatus, loginStatus);
        }
        if (ipAddress != null && !ipAddress.isEmpty()) {
            wrapper.like(AdminLoginRecord::getIpAddress, ipAddress);
        }
        
        // 按登录时间倒序
        wrapper.orderByDesc(AdminLoginRecord::getLoginTime);
        
        return page(pageParam, wrapper);
    }

    @Override
    public Map<String, Object> getLoginStatistics(Integer adminId, Integer days) {
        Map<String, Object> stats = new HashMap<>();
        
        LocalDateTime startTime = LocalDateTime.now().minusDays(days);
        LambdaQueryWrapper<AdminLoginRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.ge(AdminLoginRecord::getLoginTime, startTime);
        
        if (adminId != null) {
            wrapper.eq(AdminLoginRecord::getAdminId, adminId);
        }
        
        // 总登录次数
        long totalLogins = count(wrapper);
        stats.put("totalLogins", totalLogins);
        
        // 成功登录次数
        LambdaQueryWrapper<AdminLoginRecord> successWrapper = new LambdaQueryWrapper<>();
        successWrapper.ge(AdminLoginRecord::getLoginTime, startTime)
                     .eq(AdminLoginRecord::getLoginStatus, "success");
        if (adminId != null) {
            successWrapper.eq(AdminLoginRecord::getAdminId, adminId);
        }
        long successLogins = count(successWrapper);
        stats.put("successLogins", successLogins);
        
        // 失败登录次数
        stats.put("failedLogins", totalLogins - successLogins);
        
        // 今日登录次数
        LocalDateTime todayStart = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        LambdaQueryWrapper<AdminLoginRecord> todayWrapper = new LambdaQueryWrapper<>();
        todayWrapper.ge(AdminLoginRecord::getLoginTime, todayStart);
        if (adminId != null) {
            todayWrapper.eq(AdminLoginRecord::getAdminId, adminId);
        }
        long todayLogins = count(todayWrapper);
        stats.put("todayLogins", todayLogins);
        
        // 本周登录次数
        LocalDateTime weekStart = LocalDateTime.now().minusDays(7);
        LambdaQueryWrapper<AdminLoginRecord> weekWrapper = new LambdaQueryWrapper<>();
        weekWrapper.ge(AdminLoginRecord::getLoginTime, weekStart);
        if (adminId != null) {
            weekWrapper.eq(AdminLoginRecord::getAdminId, adminId);
        }
        long weekLogins = count(weekWrapper);
        stats.put("weekLogins", weekLogins);
        
        // 本月登录次数
        LocalDateTime monthStart = LocalDateTime.now().minusDays(30);
        LambdaQueryWrapper<AdminLoginRecord> monthWrapper = new LambdaQueryWrapper<>();
        monthWrapper.ge(AdminLoginRecord::getLoginTime, monthStart);
        if (adminId != null) {
            monthWrapper.eq(AdminLoginRecord::getAdminId, adminId);
        }
        long monthLogins = count(monthWrapper);
        stats.put("monthLogins", monthLogins);
        
        // 平均会话时长（秒）
        List<AdminLoginRecord> records = list(wrapper);
        if (!records.isEmpty()) {
            long totalDuration = records.stream()
                    .filter(r -> r.getDurationSeconds() != null)
                    .mapToLong(AdminLoginRecord::getDurationSeconds)
                    .sum();
            long avgDuration = records.stream()
                    .filter(r -> r.getDurationSeconds() != null)
                    .count() > 0 ? totalDuration / records.stream()
                    .filter(r -> r.getDurationSeconds() != null)
                    .count() : 0;
            stats.put("avgSessionDuration", avgDuration);
        } else {
            stats.put("avgSessionDuration", 0);
        }
        
        return stats;
    }

    @Override
    public List<AdminLoginRecord> getRecentLogins(Integer adminId, Integer limit) {
        LambdaQueryWrapper<AdminLoginRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AdminLoginRecord::getAdminId, adminId)
               .orderByDesc(AdminLoginRecord::getLoginTime)
               .last("LIMIT " + limit);
        
        return list(wrapper);
    }

    @Override
    public int[] getHourlyActiveStats(Integer adminId, Integer days) {
        int[] hourlyStats = new int[24];
        
        LocalDateTime startTime = LocalDateTime.now().minusDays(days);
        LambdaQueryWrapper<AdminLoginRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.ge(AdminLoginRecord::getLoginTime, startTime);
        
        if (adminId != null) {
            wrapper.eq(AdminLoginRecord::getAdminId, adminId);
        }
        
        List<AdminLoginRecord> records = list(wrapper);
        
        // 统计每小时的登录次数
        for (AdminLoginRecord record : records) {
            if (record.getLoginTime() != null) {
                int hour = record.getLoginTime().getHour();
                hourlyStats[hour]++;
            }
        }
        
        return hourlyStats;
    }

    @Override
    public Map<String, String> parseDeviceInfo(String userAgent) {
        Map<String, String> deviceInfo = new HashMap<>();
        
        if (userAgent == null || userAgent.isEmpty()) {
            deviceInfo.put("deviceType", "Unknown");
            deviceInfo.put("browser", "Unknown");
            deviceInfo.put("os", "Unknown");
            return deviceInfo;
        }
        
        // 解析设备类型
        String deviceType = "Desktop";
        if (userAgent.contains("Mobile") || userAgent.contains("Android") || userAgent.contains("iPhone")) {
            deviceType = "Mobile";
        } else if (userAgent.contains("Tablet") || userAgent.contains("iPad")) {
            deviceType = "Tablet";
        }
        deviceInfo.put("deviceType", deviceType);
        
        // 解析浏览器
        String browser = "Unknown";
        if (userAgent.contains("Edge")) {
            browser = "Edge";
        } else if (userAgent.contains("Chrome")) {
            browser = "Chrome";
        } else if (userAgent.contains("Firefox")) {
            browser = "Firefox";
        } else if (userAgent.contains("Safari")) {
            browser = "Safari";
        } else if (userAgent.contains("Opera") || userAgent.contains("OPR")) {
            browser = "Opera";
        }
        deviceInfo.put("browser", browser);
        
        // 解析操作系统
        String os = "Unknown";
        if (userAgent.contains("Windows")) {
            os = "Windows";
        } else if (userAgent.contains("Mac OS")) {
            os = "macOS";
        } else if (userAgent.contains("Linux")) {
            os = "Linux";
        } else if (userAgent.contains("Android")) {
            os = "Android";
        } else if (userAgent.contains("iOS") || userAgent.contains("iPhone") || userAgent.contains("iPad")) {
            os = "iOS";
        }
        deviceInfo.put("os", os);
        
        return deviceInfo;
    }

    @Override
    public String getLocationByIp(String ipAddress) {
        // 简单的IP地址归属地判断
        // 实际项目中应该使用IP地址库（如GeoIP2）进行精确定位
        
        if (ipAddress == null || ipAddress.isEmpty()) {
            return "未知";
        }
        
        // 本地IP
        if (ipAddress.startsWith("127.") || ipAddress.equals("0:0:0:0:0:0:0:1") || 
            ipAddress.equals("::1") || ipAddress.startsWith("192.168.") || 
            ipAddress.startsWith("10.") || ipAddress.startsWith("172.")) {
            return "本地";
        }
        
        // 这里可以集成第三方IP地址库或API
        // 暂时返回"中国"作为默认值
        return "中国";
    }

    @Override
    public String getClientIpAddress(HttpServletRequest request) {
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
