package com.muyingmall.controller.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 静态资源控制器
 * 手动处理静态文件请求，绕过 Spring 资源处理器配置问题
 */
@Slf4j
@RestController
public class StaticResourceController {

    @Value("${upload.path:G:/muying/muying-web/public}")
    private String uploadPath;

    /**
     * 测试端点 - 验证 Controller 是否被加载
     */
    @GetMapping("/api/test/static-controller")
    public ResponseEntity<String> testController() {
        log.debug("【StaticResourceController】测试端点被调用");
        return ResponseEntity.ok("StaticResourceController is working! uploadPath=" + uploadPath);
    }

    /**
     * 处理商品图片请求 - 使用 /api/static/products 路径（避免与资源处理器冲突）
     */
    @GetMapping("/api/static/products/{fileName:.+}")
    public ResponseEntity<Resource> getProductImage(@PathVariable String fileName) {
        log.debug("【StaticResourceController】收到商品图片请求: {}", fileName);
        return serveFile("products", fileName);
    }

    /**
     * 处理商品图片请求 - 使用 /res/products 备用路径
     */
    @GetMapping("/res/products/{fileName:.+}")
    public ResponseEntity<Resource> getProductImageAlt(@PathVariable String fileName) {
        log.debug("【StaticResourceController】收到商品图片请求(备用路径): {}", fileName);
        return serveFile("products", fileName);
    }

    /**
     * 处理品牌图片请求 - /api/static/brands 路径
     */
    @GetMapping("/api/static/brands/{fileName:.+}")
    public ResponseEntity<Resource> getBrandImage(@PathVariable String fileName) {
        log.debug("【StaticResourceController】收到品牌图片请求: {}", fileName);
        return serveFile("brands", fileName);
    }

    /**
     * 处理分类图标请求 - /api/static/categorys 路径
     */
    @GetMapping("/api/static/categorys/{fileName:.+}")
    public ResponseEntity<Resource> getCategoryIcon(@PathVariable String fileName) {
        log.debug("【StaticResourceController】收到分类图标请求: {}", fileName);
        return serveFile("categorys", fileName);
    }

    /**
     * 处理详情图片请求 - /api/static/details 路径
     */
    @GetMapping("/api/static/details/{fileName:.+}")
    public ResponseEntity<Resource> getDetailImage(@PathVariable String fileName) {
        log.debug("【StaticResourceController】收到详情图片请求: {}", fileName);
        return serveFile("details", fileName);
    }

    /**
     * 处理轮播图请求 - /api/static/banners 路径
     */
    @GetMapping("/api/static/banners/{fileName:.+}")
    public ResponseEntity<Resource> getBannerImage(@PathVariable String fileName) {
        log.debug("【StaticResourceController】收到轮播图请求: {}", fileName);
        return serveFile("banners", fileName);
    }

    /**
     * 处理通用图片请求 - /api/static/images 路径
     */
    @GetMapping("/api/static/images/{fileName:.+}")
    public ResponseEntity<Resource> getGeneralImage(@PathVariable String fileName) {
        log.debug("【StaticResourceController】收到通用图片请求: {}", fileName);
        return serveFile("images", fileName);
    }

    /**
     * 处理育儿圈图片请求 - /api/static/circle 路径（支持日期子目录）
     */
    @GetMapping("/api/static/circle/{year}/{month}/{day}/{fileName:.+}")
    public ResponseEntity<Resource> getCircleImage(
            @PathVariable String year,
            @PathVariable String month,
            @PathVariable String day,
            @PathVariable String fileName) {
        String subPath = String.format("circle/%s/%s/%s", year, month, day);
        log.debug("【StaticResourceController】收到育儿圈图片请求: {}/{}", subPath, fileName);
        return serveFile(subPath, fileName);
    }

    /**
     * 处理头像图片请求 - /api/static/avatars 路径
     */
    @GetMapping("/api/static/avatars/{fileName:.+}")
    public ResponseEntity<Resource> getAvatarImageStatic(@PathVariable String fileName) {
        log.debug("【StaticResourceController】收到头像图片请求: {}", fileName);
        return serveFile("avatars", fileName);
    }

    /**
     * 处理头像图片请求（带用户ID子目录）- /api/static/avatars/{userId}
     */
    @GetMapping("/api/static/avatars/{userId}/{fileName:.+}")
    public ResponseEntity<Resource> getAvatarImageWithUserId(
            @PathVariable String userId,
            @PathVariable String fileName) {
        log.debug("【StaticResourceController】收到头像图片请求: avatars/{}/{}", userId, fileName);
        return serveFile("avatars/" + userId, fileName);
    }

    /**
     * 处理头像图片请求（旧路径兼容）
     */
    @GetMapping("/avatars/{userId}/{fileName:.+}")
    public ResponseEntity<Resource> getAvatarImage(
            @PathVariable String userId,
            @PathVariable String fileName) {
        return serveFile("avatars/" + userId, fileName);
    }

    /**
     * 通用文件服务方法
     */
    private ResponseEntity<Resource> serveFile(String subDir, String fileName) {
        try {
            // 构建文件路径
            Path basePath = Paths.get(uploadPath).normalize();
            Path filePath = basePath.resolve(subDir).resolve(fileName).normalize();
            File file = filePath.toFile();
            
            log.debug("【静态资源】请求: {}/{}, 路径: {}", subDir, fileName, filePath);

            // 安全检查：确保文件在允许的目录内
            if (!filePath.startsWith(basePath)) {
                log.warn("【静态资源】非法路径: {}", filePath);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            // 检查文件是否存在
            if (!file.exists() || !file.isFile()) {
                log.debug("【静态资源】文件不存在: {}", filePath);
                return ResponseEntity.notFound().build();
            }

            // 获取 MIME 类型
            MediaType mediaType = getMediaType(fileName);
            Resource resource = new FileSystemResource(file);
            
            return ResponseEntity.ok()
                    .contentType(mediaType)
                    .contentLength(file.length())
                    .body(resource);
                    
        } catch (Exception e) {
            log.error("【静态资源】读取失败: {}/{}", subDir, fileName, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 根据文件扩展名获取 MediaType
     */
    private MediaType getMediaType(String fileName) {
        String ext = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
        return switch (ext) {
            case "jpg", "jpeg" -> MediaType.IMAGE_JPEG;
            case "png" -> MediaType.IMAGE_PNG;
            case "gif" -> MediaType.IMAGE_GIF;
            case "webp" -> MediaType.parseMediaType("image/webp");
            case "svg" -> MediaType.parseMediaType("image/svg+xml");
            default -> MediaType.APPLICATION_OCTET_STREAM;
        };
    }
}
