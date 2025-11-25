package com.muyingmall.interceptor;

import com.muyingmall.entity.AdminLoginRecord;
import com.muyingmall.entity.User;
import com.muyingmall.service.AdminLoginRecordService;
import com.muyingmall.service.UserService;
import com.muyingmall.util.IpUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;

/**
 * 管理员登录拦截器 - 记录登录信息
 */
@Slf4j
@Component
public class AdminLoginInterceptor implements HandlerInterceptor {
    
    @Autowired
    private AdminLoginRecordService loginRecordService;
    
    @Autowired
    private UserService userService;
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 只拦截登录请求
        if (request.getRequestURI().contains("/admin/login") && "POST".equals(request.getMethod())) {
            // 在登录成功后记录，这里只是标记
            request.setAttribute("loginAttempt", true);
        }
        return true;
    }
    
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // 登录成功后记录
        if (Boolean.TRUE.equals(request.getAttribute("loginAttempt")) && response.getStatus() == 200) {
            try {
                // 从请求头获取token（如果登录成功）
                String authHeader = response.getHeader("Authorization");
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    User user = userService.getUserFromToken(authHeader);
                    if (user != null && "admin".equals(user.getRole())) {
                        recordLogin(request, user, "success", null);
                    }
                }
            } catch (Exception e) {
                log.error("记录登录信息失败", e);
            }
        }
    }
    
    /**
     * 记录登录信息
     */
    public void recordLogin(HttpServletRequest request, User user, String status, String failureReason) {
        try {
            AdminLoginRecord record = new AdminLoginRecord();
            record.setAdminId(user.getUserId());
            record.setAdminName(user.getUsername());
            record.setLoginTime(LocalDateTime.now());
            record.setIpAddress(IpUtil.getRealIp(request));
            record.setLocation(IpUtil.getIpLocation(record.getIpAddress()));
            record.setUserAgent(request.getHeader("User-Agent"));
            record.setLoginStatus(status);
            record.setFailureReason(failureReason);
            
            // 解析设备信息
            String userAgent = request.getHeader("User-Agent");
            if (userAgent != null) {
                record.setDeviceType(parseDeviceType(userAgent));
                record.setBrowser(parseBrowser(userAgent));
                record.setOs(parseOS(userAgent));
            }
            
            loginRecordService.save(record);
        } catch (Exception e) {
            log.error("保存登录记录失败", e);
        }
    }
    
    private String parseDeviceType(String userAgent) {
        if (userAgent.contains("Mobile")) return "Mobile";
        if (userAgent.contains("Tablet")) return "Tablet";
        return "Desktop";
    }
    
    private String parseBrowser(String userAgent) {
        if (userAgent.contains("Chrome")) return "Chrome";
        if (userAgent.contains("Firefox")) return "Firefox";
        if (userAgent.contains("Safari")) return "Safari";
        if (userAgent.contains("Edge")) return "Edge";
        return "Unknown";
    }
    
    private String parseOS(String userAgent) {
        if (userAgent.contains("Windows")) return "Windows";
        if (userAgent.contains("Mac")) return "MacOS";
        if (userAgent.contains("Linux")) return "Linux";
        if (userAgent.contains("Android")) return "Android";
        if (userAgent.contains("iOS")) return "iOS";
        return "Unknown";
    }
}
