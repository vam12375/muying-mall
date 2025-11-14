package com.muyingmall.config;

import org.springframework.boot.autoconfigure.web.servlet.WebMvcRegistrations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

/**
 * Web MVC 配置
 * 
 * Source: 解决 Spring Boot 3.x 中 /admin/** 路径被当成静态资源的问题
 * 遵循 KISS, YAGNI, SOLID 原则
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    /**
     * 配置路径匹配
     * 确保 /admin/** 等 API 路径不会被静态资源处理器拦截
     */
    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        // 使用后缀模式匹配（Spring Boot 3.x 默认禁用）
        configurer.setUseTrailingSlashMatch(true);
    }

    /**
     * 配置静态资源处理
     * 明确指定静态资源路径，避免拦截 API 请求
     * 
     * 关键：不调用 super.addResourceHandlers()，禁用 Spring Boot 默认的 /** 资源处理器
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 只处理明确指定的静态资源路径
        registry.addResourceHandler("/upload/**")
                .addResourceLocations("file:upload/");
        
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/");
        
        // 不要添加 /** 的通配符处理，避免拦截所有请求
    }
    
    /**
     * 自定义 RequestMappingHandlerMapping
     * 提高 Controller 映射的优先级，确保 API 请求优先于静态资源处理
     */
    @Bean
    public WebMvcRegistrations webMvcRegistrations() {
        return new WebMvcRegistrations() {
            @Override
            public RequestMappingHandlerMapping getRequestMappingHandlerMapping() {
                RequestMappingHandlerMapping mapping = new RequestMappingHandlerMapping();
                // 设置最高优先级，确保 Controller 优先处理请求
                mapping.setOrder(-1);
                return mapping;
            }
        };
    }
}
