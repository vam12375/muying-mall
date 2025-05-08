package com.muyingmall.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * 跨域配置 - 修复版本
 */
@Configuration
public class CorsConfig {

    @Value("${cors.allowed-origins}")
    private String allowedOrigins;

    @Value("${cors.allowed-methods}")
    private String allowedMethods;

    @Value("${cors.allowed-headers}")
    private String allowedHeaders;

    @Value("${cors.allow-credentials}")
    private boolean allowCredentials;

    @Value("${cors.max-age}")
    private long maxAge;

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();

        // 设置允许的来源 - 修复支持更多开发环境
        // 强制添加常用的开发端口，确保开发环境可用
        if (allowCredentials) {
            // 如果需要支持credentials，不能使用通配符*，需要明确指定域名
            config.addAllowedOrigin("http://localhost:5173"); // Vite默认
            config.addAllowedOrigin("http://localhost:3000"); // React默认
            config.addAllowedOrigin("http://127.0.0.1:5173");
            config.addAllowedOrigin("http://127.0.0.1:3000");
            config.addAllowedOrigin("http://localhost:8081"); // 其他常用端口

            // 添加配置文件中定义的域名
            String[] origins = allowedOrigins.split(",");
            for (String origin : origins) {
                if (!origin.trim().equals("*")) {
                    config.addAllowedOrigin(origin.trim());
                }
            }
        } else {
            // 不需要credentials时可以使用通配符
            config.addAllowedOriginPattern("*");
        }

        // 设置允许的HTTP方法
        if ("*".equals(allowedMethods)) {
            config.addAllowedMethod("*");
        } else {
            String[] methods = allowedMethods.split(",");
            for (String method : methods) {
                config.addAllowedMethod(method.trim());
            }
        }

        // 设置允许的头信息
        if ("*".equals(allowedHeaders)) {
            config.addAllowedHeader("*");
        } else {
            String[] headers = allowedHeaders.split(",");
            for (String header : headers) {
                config.addAllowedHeader(header.trim());
            }
        }

        // 允许暴露的响应头
        config.addExposedHeader("Authorization");

        // 是否允许发送Cookie
        config.setAllowCredentials(allowCredentials);

        // 预检请求的有效期，单位为秒
        config.setMaxAge(maxAge);

        // 应用CORS配置到所有路径
        source.registerCorsConfiguration("/**", config);

        return new CorsFilter(source);
    }
}