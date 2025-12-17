package com.muyingmall.controller.common;

import com.muyingmall.common.api.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * 文件上传控制器
 */
@Slf4j
@RestController
@RequestMapping("/upload")
public class FileUploadController {

    // 上传到前端public目录
    @Value("${file.upload.path:G:/muying/muying-web/public}")
    private String uploadPath;

    /**
     * 上传图片
     */
    @PostMapping("/image")
    public Result<String> uploadImage(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error("请选择要上传的文件");
        }

        // 检查文件类型
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return Result.error("只能上传图片文件");
        }

        // 检查文件大小（5MB）
        if (file.getSize() > 5 * 1024 * 1024) {
            return Result.error("图片大小不能超过5MB");
        }

        try {
            // 生成文件名
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String newFilename = UUID.randomUUID().toString().replace("-", "") + extension;

            // 按日期分目录
            String dateDir = java.time.LocalDate.now().toString().replace("-", "/");
            String relativePath = "circle/" + dateDir;
            
            // 创建目录
            Path dirPath = Paths.get(uploadPath, relativePath);
            if (!Files.exists(dirPath)) {
                Files.createDirectories(dirPath);
            }

            // 保存文件
            Path filePath = dirPath.resolve(newFilename);
            file.transferTo(filePath.toFile());

            // 返回访问URL（直接使用/circle/路径，前端public目录可直接访问）
            String fileUrl = "/" + relativePath + "/" + newFilename;
            log.debug("文件上传成功: {}", fileUrl);
            
            return Result.success(fileUrl);
        } catch (IOException e) {
            log.error("文件上传失败", e);
            return Result.error("文件上传失败: " + e.getMessage());
        }
    }
}
