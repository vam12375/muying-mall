package com.muyingmall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.muyingmall.entity.AdminLoginRecord;
import com.muyingmall.mapper.AdminLoginRecordMapper;
import com.muyingmall.service.AdminLoginRecordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 管理员登录记录服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminLoginRecordServiceImpl extends ServiceImpl<AdminLoginRecordMapper, AdminLoginRecord>
        implements AdminLoginRecordService {

    private final AdminLoginRecordMapper loginRecordMapper;

    @Override
    public Long recordLogin(Integer adminId, String adminName, HttpServletRequest request,
            String loginStatus, String failureReason) {
        try {
            AdminLoginRecord record = new AdminLoginRecord();
            record.setAdminId(adminId);
            record.setAdminName(adminName);
            record.setLoginTime(LocalDateTime.now());
            record.setLoginStatus(loginStatus);
            record.setFailureReason(failureReason);

            // 获取IP地址
            String ipAddress = getClientIpAddress(request);
            record.setIpAddress(ipAddress);

            // 获取地理位置
            String location = getLocationByIp(ipAddress);
            record.setLocation(location);

            // 解析用户代理信息
            String userAgent = request.getHeader("User-Agent");
            record.setUserAgent(userAgent);

            if (StringUtils.hasText(userAgent)) {
                Map<String, String> deviceInfo = parseDeviceInfo(userAgent);
                record.setDeviceType(deviceInfo.get("deviceType"));
                record.setBrowser(deviceInfo.get("browser"));
                record.setOs(deviceInfo.get("os"));
            }

            // 生成会话ID
            if (AdminLoginRecord.LoginStatus.SUCCESS.getCode().equals(loginStatus)) {
                String sessionId = UUID.randomUUID().toString();
                record.setSessionId(sessionId);
                // 将会话ID存储到session中，用于后续的登出记录
                request.getSession().setAttribute("adminSessionId", sessionId);
            }

            save(record);
            log.info("记录管理员登录信息成功: adminId={}, loginStatus={}, ip={}",
                    adminId, loginStatus, ipAddress);

            return record.getId();
        } catch (Exception e) {
            log.error("记录管理员登录信息失败: adminId={}, error={}", adminId, e.getMessage(), e);
            return null;
        }
    }

    @Override
    public boolean recordLogout(String sessionId) {
        try {
            if (!StringUtils.hasText(sessionId)) {
                return false;
            }

            LambdaQueryWrapper<AdminLoginRecord> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(AdminLoginRecord::getSessionId, sessionId)
                    .isNull(AdminLoginRecord::getLogoutTime);

            AdminLoginRecord record = getOne(queryWrapper);
            if (record != null) {
                LocalDateTime logoutTime = LocalDateTime.now();
                record.setLogoutTime(logoutTime);

                // 计算会话时长
                if (record.getLoginTime() != null) {
                    long durationSeconds = ChronoUnit.SECONDS.between(record.getLoginTime(), logoutTime);
                    record.setDurationSeconds((int) durationSeconds);
                }

                updateById(record);
                log.info("记录管理员登出信息成功: sessionId={}, duration={}秒",
                        sessionId, record.getDurationSeconds());
                return true;
            }
        } catch (Exception e) {
            log.error("记录管理员登出信息失败: sessionId={}, error={}", sessionId, e.getMessage(), e);
        }
        return false;
    }

    @Override
    public boolean updateSessionDuration(String sessionId, Integer durationSeconds) {
        try {
            if (!StringUtils.hasText(sessionId) || durationSeconds == null) {
                return false;
            }

            LambdaQueryWrapper<AdminLoginRecord> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(AdminLoginRecord::getSessionId, sessionId);

            AdminLoginRecord record = getOne(queryWrapper);
            if (record != null) {
                record.setDurationSeconds(durationSeconds);
                updateById(record);
                return true;
            }
        } catch (Exception e) {
            log.error("更新会话时长失败: sessionId={}, error={}", sessionId, e.getMessage(), e);
        }
        return false;
    }

    @Override
    public IPage<AdminLoginRecord> getLoginRecordsPage(Integer page, Integer size, Integer adminId,
            LocalDateTime startTime, LocalDateTime endTime,
            String loginStatus, String ipAddress) {
        Page<AdminLoginRecord> pageParam = new Page<>(page, size);
        return loginRecordMapper.selectLoginRecordsPage(pageParam, adminId, startTime, endTime,
                loginStatus, ipAddress);
    }

    @Override
    public Map<String, Object> getLoginStatistics(Integer adminId, Integer days) {
        Map<String, Object> statistics = new HashMap<>();

        LocalDateTime endTime = LocalDateTime.now();
        LocalDateTime startTime = endTime.minusDays(days);

        // 总登录次数
        Long totalLogins = loginRecordMapper.countSuccessLogins(adminId, null, null);
        statistics.put("totalLogins", totalLogins != null ? totalLogins : 0);

        // 今日登录次数
        LocalDateTime todayStart = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        LocalDateTime todayEnd = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59);
        Long todayLogins = loginRecordMapper.countSuccessLogins(adminId, todayStart, todayEnd);
        statistics.put("todayLogins", todayLogins != null ? todayLogins : 0);

        // 本周登录次数
        LocalDateTime weekStart = LocalDateTime.now().minusDays(7);
        Long weekLogins = loginRecordMapper.countSuccessLogins(adminId, weekStart, endTime);
        statistics.put("weekLogins", weekLogins != null ? weekLogins : 0);

        // 本月登录次数
        LocalDateTime monthStart = LocalDateTime.now().minusDays(30);
        Long monthLogins = loginRecordMapper.countSuccessLogins(adminId, monthStart, endTime);
        statistics.put("monthLogins", monthLogins != null ? monthLogins : 0);

        // 平均在线时长
        Double avgOnlineTime = loginRecordMapper.selectAvgOnlineTime(adminId, days);
        statistics.put("avgOnlineTime", avgOnlineTime != null ? avgOnlineTime / 3600.0 : 0.0); // 转换为小时

        // 最长会话时长
        Integer maxSessionTime = loginRecordMapper.selectMaxSessionTime(adminId, days);
        statistics.put("longestSession", maxSessionTime != null ? maxSessionTime / 3600.0 : 0.0); // 转换为小时

        // 最后登录时间
        List<AdminLoginRecord> recentLogins = getRecentLogins(adminId, 1);
        if (!recentLogins.isEmpty()) {
            statistics.put("lastLoginTime", recentLogins.get(0).getLoginTime());
        }

        return statistics;
    }

    @Override
    public List<AdminLoginRecord> getRecentLogins(Integer adminId, Integer limit) {
        return loginRecordMapper.selectRecentLogins(adminId, limit);
    }

    @Override
    public int[] getHourlyActiveStats(Integer adminId, Integer days) {
        List<Map<String, Object>> hourlyStats = loginRecordMapper.selectHourlyLoginStats(adminId, days);
        int[] activeHours = new int[24];

        for (Map<String, Object> stat : hourlyStats) {
            Integer hour = (Integer) stat.get("hour");
            Long count = (Long) stat.get("count");
            if (hour != null && hour >= 0 && hour < 24) {
                activeHours[hour] = count.intValue();
            }
        }

        return activeHours;
    }

    @Override
    public Map<String, String> parseDeviceInfo(String userAgent) {
        Map<String, String> deviceInfo = new HashMap<>();

        if (!StringUtils.hasText(userAgent)) {
            return deviceInfo;
        }

        // 检测设备类型
        String deviceType = "Desktop";
        if (userAgent.toLowerCase().contains("mobile")) {
            deviceType = "Mobile";
        } else if (userAgent.toLowerCase().contains("tablet") || userAgent.toLowerCase().contains("ipad")) {
            deviceType = "Tablet";
        }
        deviceInfo.put("deviceType", deviceType);

        // 检测浏览器
        String browser = "Unknown";
        if (userAgent.contains("Chrome")) {
            Pattern pattern = Pattern.compile("Chrome/([\\d.]+)");
            Matcher matcher = pattern.matcher(userAgent);
            if (matcher.find()) {
                browser = "Chrome " + matcher.group(1);
            }
        } else if (userAgent.contains("Firefox")) {
            Pattern pattern = Pattern.compile("Firefox/([\\d.]+)");
            Matcher matcher = pattern.matcher(userAgent);
            if (matcher.find()) {
                browser = "Firefox " + matcher.group(1);
            }
        } else if (userAgent.contains("Safari") && !userAgent.contains("Chrome")) {
            Pattern pattern = Pattern.compile("Version/([\\d.]+).*Safari");
            Matcher matcher = pattern.matcher(userAgent);
            if (matcher.find()) {
                browser = "Safari " + matcher.group(1);
            }
        } else if (userAgent.contains("Edge")) {
            Pattern pattern = Pattern.compile("Edge/([\\d.]+)");
            Matcher matcher = pattern.matcher(userAgent);
            if (matcher.find()) {
                browser = "Edge " + matcher.group(1);
            }
        }
        deviceInfo.put("browser", browser);

        // 检测操作系统
        String os = "Unknown";
        if (userAgent.contains("Windows NT 10.0")) {
            os = "Windows 10";
        } else if (userAgent.contains("Windows NT 6.3")) {
            os = "Windows 8.1";
        } else if (userAgent.contains("Windows NT 6.1")) {
            os = "Windows 7";
        } else if (userAgent.contains("Mac OS X")) {
            Pattern pattern = Pattern.compile("Mac OS X ([\\d_]+)");
            Matcher matcher = pattern.matcher(userAgent);
            if (matcher.find()) {
                os = "macOS " + matcher.group(1).replace("_", ".");
            }
        } else if (userAgent.contains("Linux")) {
            os = "Linux";
        } else if (userAgent.contains("Android")) {
            Pattern pattern = Pattern.compile("Android ([\\d.]+)");
            Matcher matcher = pattern.matcher(userAgent);
            if (matcher.find()) {
                os = "Android " + matcher.group(1);
            }
        } else if (userAgent.contains("iPhone OS")) {
            Pattern pattern = Pattern.compile("iPhone OS ([\\d_]+)");
            Matcher matcher = pattern.matcher(userAgent);
            if (matcher.find()) {
                os = "iOS " + matcher.group(1).replace("_", ".");
            }
        }
        deviceInfo.put("os", os);

        return deviceInfo;
    }

    @Override
    public String getLocationByIp(String ipAddress) {
        // 简单的IP地址地理位置映射，实际项目中可以使用第三方IP地理位置服务
        if (!StringUtils.hasText(ipAddress)) {
            return "未知";
        }

        // 本地IP地址
        if (ipAddress.startsWith("192.168.") || ipAddress.startsWith("10.") ||
                ipAddress.startsWith("172.") || ipAddress.equals("127.0.0.1") ||
                ipAddress.equals("0:0:0:0:0:0:0:1") || ipAddress.equals("::1")) {
            return "本地网络";
        }

        // 这里可以集成第三方IP地理位置服务，如高德、百度等
        // 目前返回模拟数据
        String[] locations = { "北京", "上海", "广州", "深圳", "杭州", "南京", "成都", "武汉" };
        int index = Math.abs(ipAddress.hashCode()) % locations.length;
        return locations[index];
    }

    @Override
    public String getClientIpAddress(HttpServletRequest request) {
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
