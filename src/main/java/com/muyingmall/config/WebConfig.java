package com.muyingmall.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web配置类
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${upload.path:E:/11/muying-web/public}")
    private String uploadPath;

    @Value("${file.access.url:http://localhost:5173/avatars}")
    private String fileAccessUrl;

    /**
     * 配置静态资源映射
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 配置头像文件的访问路径
        registry.addResourceHandler("/avatars/**")
                .addResourceLocations("file:" + uploadPath + "/avatars/");

        // 配置评论图片的访问路径
        registry.addResourceHandler("/comment/**")
                .addResourceLocations("file:" + uploadPath + "/comment/");

        // 配置商品图片的访问路径
        registry.addResourceHandler("/products/**")
                .addResourceLocations("file:" + uploadPath + "/products/");

        // 配置上传文件的访问路径
        registry.addResourceHandler("/upload/**")
                .addResourceLocations("file:" + uploadPath + "/");

        // 其他静态资源
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/");
    }

    /**
     * 添加拦截器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 这里可以添加自定义拦截器，例如登录拦截器等
    }

    // 注意：跨域配置已移至CorsConfig类，避免冲突
}