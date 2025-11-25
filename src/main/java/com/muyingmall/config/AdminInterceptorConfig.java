package com.muyingmall.config;

import com.muyingmall.interceptor.AdminLoginInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 管理员拦截器配置
 */
@Configuration
public class AdminInterceptorConfig implements WebMvcConfigurer {
    
    @Autowired
    private AdminLoginInterceptor adminLoginInterceptor;
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(adminLoginInterceptor)
                .addPathPatterns("/admin/**");
    }
}
