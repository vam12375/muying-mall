package com.muyingmall.util;

import com.muyingmall.entity.User;
import com.muyingmall.service.UserService;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * 用户上下文工具类
 * 性能优化：从JWT直接获取userId，避免重复查询数据库
 * 
 * 优化前：每个请求都查询user表 -> 8000并发 = 8000次/秒数据库查询
 * 优化后：从JWT Claims直接获取userId -> 零数据库查询
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class UserContext {

    private final JwtUtils jwtUtils;
    private final UserService userService;
    private final RedisUtil redisUtil;

    /**
     * 从当前请求上下文获取用户ID
     * 性能优化：直接从JWT Claims获取，无需查询数据库
     * 
     * @return 用户ID，未认证返回null
     */
    public Integer getCurrentUserId() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated() 
                    || "anonymousUser".equals(authentication.getPrincipal())) {
                return null;
            }

            // 从Authentication的details中获取userId（由JwtAuthenticationFilter设置）
            Object details = authentication.getDetails();
            if (details instanceof java.util.Map) {
                @SuppressWarnings("unchecked")
                java.util.Map<String, Object> detailsMap = (java.util.Map<String, Object>) details;
                Object userIdObj = detailsMap.get("userId");
                if (userIdObj instanceof Integer) {
                    return (Integer) userIdObj;
                }
            }

            // 备用方案：从username查询（仅在details未设置时使用）
            String username = authentication.getName();
            if (username != null) {
                // 尝试从Redis缓存获取userId
                String cacheKey = "user:id:" + username;
                Object cachedUserId = redisUtil.get(cacheKey);
                if (cachedUserId != null) {
                    return (Integer) cachedUserId;
                }

                // 缓存未命中，查询数据库并缓存
                User user = userService.findByUsername(username);
                if (user != null) {
                    redisUtil.set(cacheKey, user.getUserId(), 3600L); // 缓存1小时
                    return user.getUserId();
                }
            }

            return null;
        } catch (Exception e) {
            log.error("获取当前用户ID失败", e);
            return null;
        }
    }

    /**
     * 从当前请求上下文获取用户名
     * 
     * @return 用户名，未认证返回null
     */
    public String getCurrentUsername() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated() 
                    || "anonymousUser".equals(authentication.getPrincipal())) {
                return null;
            }
            return authentication.getName();
        } catch (Exception e) {
            log.error("获取当前用户名失败", e);
            return null;
        }
    }

    /**
     * 获取当前用户完整信息
     * 性能优化：优先从Redis缓存获取，减少数据库查询
     * 
     * @return User对象，未认证或不存在返回null
     */
    public User getCurrentUser() {
        try {
            Integer userId = getCurrentUserId();
            if (userId == null) {
                return null;
            }

            // 从Redis缓存获取User对象
            String cacheKey = "user:info:" + userId;
            Object cachedUser = redisUtil.get(cacheKey);
            if (cachedUser instanceof User) {
                return (User) cachedUser;
            }

            // 缓存未命中，查询数据库
            User user = userService.getById(userId);
            if (user != null) {
                // 缓存User对象（30分钟）
                redisUtil.set(cacheKey, user, 1800L);
            }
            return user;
        } catch (Exception e) {
            log.error("获取当前用户信息失败", e);
            return null;
        }
    }

    /**
     * 检查当前用户是否已认证
     * 
     * @return true-已认证，false-未认证
     */
    public boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null 
                && authentication.isAuthenticated() 
                && !"anonymousUser".equals(authentication.getPrincipal());
    }

    /**
     * 清除用户缓存
     * 用于用户信息更新后刷新缓存
     * 
     * @param userId 用户ID
     */
    public void clearUserCache(Integer userId) {
        if (userId == null) {
            return;
        }
        try {
            // 清除userId缓存
            User user = userService.getById(userId);
            if (user != null) {
                redisUtil.del("user:id:" + user.getUsername());
            }
            // 清除User对象缓存
            redisUtil.del("user:info:" + userId);
            log.debug("已清除用户缓存: userId={}", userId);
        } catch (Exception e) {
            log.error("清除用户缓存失败: userId={}", userId, e);
        }
    }
}
