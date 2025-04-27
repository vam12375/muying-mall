package com.muyingmall.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * 跨域配置
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

        // 设置允许的来源
        if (allowCredentials && "*".equals(allowedOrigins)) {
            // 如果需要支持credentials，不能使用通配符*，需要明确指定域名
            config.addAllowedOrigin("http://localhost:5173"); // Vite默认端口
        } else {
            String[] origins = allowedOrigins.split(",");
            for (String origin : origins) {
                config.addAllowedOrigin(origin.trim());
            }
        }

        // 设置允许的HTTP方法
        String[] methods = allowedMethods.split(",");
        for (String method : methods) {
            config.addAllowedMethod(method.trim());
        }

        // 设置允许的头信息
        String[] headers = allowedHeaders.split(",");
        for (String header : headers) {
            config.addAllowedHeader(header.trim());
        }

        // 是否允许发送Cookie
        config.setAllowCredentials(allowCredentials);

        // 预检请求的有效期，单位为秒
        config.setMaxAge(maxAge);

        source.registerCorsConfiguration("/**", config);

        return new CorsFilter(source);
    }
}