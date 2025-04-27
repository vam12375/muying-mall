package com.muyingmall.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web配置类
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${file.upload.path}")
    private String uploadPath;

    @Value("${file.access.url}")
    private String accessUrl;

    /**
     * 配置静态资源映射
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 配置上传文件的访问路径
        registry.addResourceHandler("/upload/**")
                .addResourceLocations("file:" + uploadPath + "/");

        // 其他静态资源
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/");
    }

    /**
     * 跨域配置
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

    /**
     * 添加拦截器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 这里可以添加自定义拦截器，例如登录拦截器等
    }
}