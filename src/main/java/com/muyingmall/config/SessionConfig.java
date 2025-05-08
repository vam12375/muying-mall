package com.muyingmall.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;
import org.springframework.session.web.http.HeaderHttpSessionIdResolver;
import org.springframework.session.web.http.HttpSessionIdResolver;

/**
 * Spring Session配置类
 * maxInactiveIntervalInSeconds: 设置会话最大非活动间隔，超过这个时间会话将失效（7天）
 */
@Configuration
@EnableRedisHttpSession(maxInactiveIntervalInSeconds = 604800) // 7天 = 7*24*60*60秒
public class SessionConfig {

    /**
     * 配置会话ID解析器，同时从cookie和header中获取会话ID
     * 这在前后端分离的应用中特别有用，前端可以通过header发送会话ID
     */
    @Bean
    public HttpSessionIdResolver httpSessionIdResolver() {
        return HeaderHttpSessionIdResolver.xAuthToken(); // 使用X-Auth-Token头传递会话ID
    }
}