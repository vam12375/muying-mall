package com.muyingmall.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import jakarta.annotation.PostConstruct;
import java.io.File;

/**
 * Web配置类
 */
@Slf4j
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${upload.path:G:/muying/muying-web/public}")
    private String uploadPath;

    @Value("${file.access.url:http://localhost:5173/avatars}")
    private String fileAccessUrl;

    /**
     * 启动时打印配置信息
     */
    @PostConstruct
    public void init() {
        log.debug("【WebConfig】upload.path 配置值: {}", uploadPath);
        log.debug("【WebConfig】file.access.url 配置值: {}", fileAccessUrl);
        
        // 验证路径是否存在
        File productsDir = new File(uploadPath + "/products");
        log.debug("【WebConfig】products 目录是否存在: {}, 路径: {}", productsDir.exists(), productsDir.getAbsolutePath());
        
        if (productsDir.exists()) {
            File[] files = productsDir.listFiles();
            log.debug("【WebConfig】products 目录文件数量: {}", files != null ? files.length : 0);
        }
    }

    /**
     * 获取 Windows 兼容的文件路径 URI
     * 将路径转换为 file:/// 格式，确保 Windows 兼容性
     */
    private String getFileLocation(String subPath) {
        // 确保路径使用正斜杠，并添加 file:/// 前缀（Windows 需要三个斜杠）
        String normalizedPath = uploadPath.replace("\\", "/");
        if (!normalizedPath.endsWith("/")) {
            normalizedPath += "/";
        }
        String location = "file:///" + normalizedPath + subPath + "/";
        log.debug("【WebConfig】资源路径映射: {} -> {}", subPath, location);
        return location;
    }

    /**
     * 配置静态资源映射
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String productsLocation = getFileLocation("products");
        log.debug("【静态资源配置】/static/products/** -> {}", productsLocation);
        
        // 配置头像文件的访问路径
        registry.addResourceHandler("/avatars/**")
                .addResourceLocations(getFileLocation("avatars"));

        // 配置评论图片的访问路径
        registry.addResourceHandler("/comment/**")
                .addResourceLocations(getFileLocation("comment"));

        // 配置商品图片的访问路径
        registry.addResourceHandler("/product-images/**")
                .addResourceLocations(productsLocation);
        
        // 配置静态图片访问路径 - 使用 /static 前缀（注意：必须在通配 /static/** 之前注册）
        registry.addResourceHandler("/static/products/**")
                .addResourceLocations(productsLocation);
        
        // 配置品牌图片访问路径
        registry.addResourceHandler("/static/brands/**")
                .addResourceLocations(getFileLocation("brands"));
        
        // 配置分类图标访问路径
        registry.addResourceHandler("/static/categorys/**")
                .addResourceLocations(getFileLocation("categorys"));
        
        // 配置详情图片访问路径
        registry.addResourceHandler("/static/details/**")
                .addResourceLocations(getFileLocation("details"));
        
        // 配置商品图片访问路径（使用独立路径避免与API冲突）
        registry.addResourceHandler("/images/products/**")
                .addResourceLocations(productsLocation);

        // 配置上传文件的访问路径
        registry.addResourceHandler("/upload/**")
                .addResourceLocations(getFileLocation(""));

        // 配置育儿圈上传图片的访问路径
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:///uploads/");

        // 配置育儿圈图片访问路径（支持日期子目录）
        registry.addResourceHandler("/static/circle/**")
                .addResourceLocations(getFileLocation("circle"));

        // 配置头像图片访问路径
        registry.addResourceHandler("/static/avatars/**")
                .addResourceLocations(getFileLocation("avatars"));

        // 注意：classpath:/static/ 的通配映射已移除，避免覆盖上面的具体路径配置
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