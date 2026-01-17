package com.muyingmall.util;

import java.util.Map;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import lombok.extern.slf4j.Slf4j;

/**
 * 安全工具类，用于获取当前登录用户信息
 */
@Slf4j
public class SecurityUtil {

    /**
     * 获取当前登录用户ID
     * 
     * @return 用户ID
     */
    public static Integer getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            Object principal = authentication.getPrincipal();

            // 如果principal是UserDetails类型
            if (principal instanceof UserDetails) {
                UserDetails userDetails = (UserDetails) principal;
                // 尝试从用户名解析用户ID
                String username = userDetails.getUsername();
                try {
                    // 检查用户名是否为数字（某些系统使用用户ID作为用户名）
                    return Integer.parseInt(username);
                } catch (NumberFormatException e) {
                    // 如果用户名不是数字，可以通过用户名查询用户ID
                    // 此处可以添加数据库查询逻辑
                    log.debug("无法从UserDetails中直接获取用户ID，用户名: {}", username);
                }
            }
            // 如果principal是字符串类型（通常是用户名）
            else if (principal instanceof String) {
                String username = (String) principal;
                try {
                    return Integer.parseInt(username);
                } catch (NumberFormatException e) {
                    log.debug("无法从principal(String)中获取用户ID，用户名: {}", username);
                }
            }

            // 从Authentication的details中尝试获取userId
            Object details = authentication.getDetails();
            if (details != null && details instanceof Map) {
                try {
                    Object userId = ((Map<?, ?>) details).get("userId");
                    if (userId instanceof Integer) {
                        return (Integer) userId;
                    } else if (userId instanceof String) {
                        return Integer.parseInt((String) userId);
                    }
                } catch (Exception e) {
                    log.debug("从Authentication details中获取用户ID失败: {}", e.getMessage());
                }
            }

            // 最后尝试从authentication.getName()获取
            String name = authentication.getName();
            try {
                return Integer.parseInt(name);
            } catch (NumberFormatException e) {
                log.debug("无法从authentication.getName()获取用户ID: {}", name);
            }

            // 输出警告
            log.warn("无法确定当前用户ID。Principal类型: {}",
                    (principal != null ? principal.getClass().getName() : "null"));

            // 生产环境应该返回null或抛出异常
            log.debug("无法获取当前用户ID，返回null");
            return null;
        }
        return null;
    }

    /**
     * 获取当前登录用户名
     * 
     * @return 用户名
     */
    public static String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            return authentication.getName();
        }
        return "system";
    }

    /**
     * 判断当前用户是否有指定角色
     * 
     * @param role 角色名称
     * @return 是否拥有该角色
     */
    public static boolean hasRole(String role) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            return authentication.getAuthorities().stream()
                    .anyMatch(authority -> authority.getAuthority().equals("ROLE_" + role));
        }
        return false;
    }

    /**
     * 判断当前用户是否已认证
     * 
     * @return 是否已认证
     */
    public static boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated();
    }
}