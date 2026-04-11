package com.muyingmall.config;

import com.muyingmall.interceptor.GlobalAccessInterceptor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import jakarta.annotation.PostConstruct;
import java.io.File;

/**
 * Web 配置类。
 * 负责统一配置静态资源映射路径，并注册全站访问频率拦截器，
 * 让前端静态资源访问与后端访问防护规则能够协同生效。
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

        /**
         * 前端静态资源根目录，用于拼接商品图、头像、评论图等文件访问路径。
         */
        @Value("${upload.path:G:/muying/muying-web/public}")
        private String uploadPath;

        /**
         * 文件访问基础地址，主要用于启动时输出调试信息。
         */
        @Value("${file.access.url:http://localhost:5173/avatars}")
        private String fileAccessUrl;

        /**
         * 全站访问频率拦截器，用于对普通页面请求执行人机验证拦截。
         */
        private final GlobalAccessInterceptor globalAccessInterceptor;

        /**
         * 启动时打印配置信息
         */
        @PostConstruct
        public void init() {
                log.debug("【WebConfig】upload.path 配置值: {}", uploadPath);
                log.debug("【WebConfig】file.access.url 配置值: {}", fileAccessUrl);

                // 验证路径是否存在
                File productsDir = new File(uploadPath + "/products");
                log.debug("【WebConfig】products 目录是否存在: {}, 路径: {}", productsDir.exists(),
                                productsDir.getAbsolutePath());

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
         * 注册全站访问频率拦截器。
         * 排除登录、上传、静态资源、接口文档等路径，避免影响基础接口与资源访问。
         */
        @Override
        public void addInterceptors(InterceptorRegistry registry) {
                registry.addInterceptor(globalAccessInterceptor)
                                .addPathPatterns("/**")
                                .excludePathPatterns(
                                                "/admin/**",
                                                "/api/admin/**",
                                                "/access/verify",
                                                "/products/*/access/verify",
                                                "/user/login",
                                                "/user/register",
                                                "/user/password/reset/**",
                                                "/upload/**",
                                                "/uploads/**",
                                                "/avatars/**",
                                                "/static/**",
                                                "/images/**",
                                                "/comment/**",
                                                "/ws/**",
                                                "/doc.html",
                                                "/swagger-ui.html",
                                                "/swagger-ui/**",
                                                "/v3/api-docs/**",
                                                "/webjars/**",
                                                "/swagger-resources/**");
        }

        // 注意：跨域配置已移至CorsConfig类，避免冲突
}