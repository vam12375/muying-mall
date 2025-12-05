package com.muyingmall.service.impl;

import com.muyingmall.common.constants.CacheConstants;
import com.muyingmall.entity.User;
import com.muyingmall.util.RedisUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 登录缓存服务 - 优化登录性能
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LoginCacheService {

    private final RedisUtil redisUtil;

    // 登录会话缓存时间：30分钟
    private static final long SESSION_EXPIRE_TIME = 1800L;

    /**
     * 缓存用户登录会话
     * 
     * @param token JWT令牌
     * @param user 用户信息
     */
    public void cacheUserSession(String token, User user) {
        String cacheKey = CacheConstants.USER_TOKEN_KEY + token;
        // 缓存用户信息，避免每次请求都查询数据库
        redisUtil.set(cacheKey, user, SESSION_EXPIRE_TIME);
        log.debug("缓存用户登录会话: userId={}, token={}...", user.getUserId(), 
                  token.length() > 10 ? token.substring(0, 10) : token);
    }

    /**
     * 从缓存获取用户会话
     * 
     * @param token JWT令牌
     * @return 用户信息，不存在返回null
     */
    public User getUserSession(String token) {
        String cacheKey = CacheConstants.USER_TOKEN_KEY + token;
        Object cached = redisUtil.get(cacheKey);
        if (cached instanceof User) {
            log.debug("从缓存命中用户会话: token={}...", 
                      token.length() > 10 ? token.substring(0, 10) : token);
            return (User) cached;
        }
        return null;
    }

    /**
     * 清除用户登录会话
     * 
     * @param token JWT令牌
     */
    public void clearUserSession(String token) {
        String cacheKey = CacheConstants.USER_TOKEN_KEY + token;
        redisUtil.del(cacheKey);
        log.debug("清除用户登录会话: token={}...", 
                  token.length() > 10 ? token.substring(0, 10) : token);
    }
}
