package com.muyingmall.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

/**
 * WebSocket 配置类
 * 用于秒杀结果实时通知
 */
@Configuration
public class WebSocketConfig {

    /**
     * 注入ServerEndpointExporter
     * 这个Bean会自动注册使用了@ServerEndpoint注解声明的WebSocket endpoint
     */
    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }
}
