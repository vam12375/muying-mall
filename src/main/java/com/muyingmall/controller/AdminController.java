package com.muyingmall.controller;

import com.muyingmall.common.api.CommonResult;
import com.muyingmall.dto.AdminLoginDTO;
import com.muyingmall.entity.User;
import com.muyingmall.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 管理员控制器
 */
@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UserService userService;
    
    @Value("${file.upload.path}")
    private String uploadPath;
    
    @Value("${file.access.url}")
    private String accessUrl;

    /**
     * 管理员登录
     */
    @PostMapping("/login")
    public CommonResult login(@RequestBody AdminLoginDTO loginParam) {
        // 根据用户名查询用户
        User user = userService.getUserByUsername(loginParam.getAdmin_name());

        // 用户不存在或密码错误
        if (user == null || !userService.verifyPassword(user, loginParam.getAdmin_pass())) {
            return CommonResult.failed("用户名或密码错误");
        }

        // 检查用户角色是否为管理员
        if (!"admin".equals(user.getRole())) {
            return CommonResult.failed("该账号没有管理员权限");
        }

        // 用户存在且验证通过，生成token
        String token = userService.generateToken(user);

        // 封装返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);

        // 用户信息（去除敏感信息）
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getUserId());
        userInfo.put("username", user.getUsername());
        userInfo.put("nickname", user.getNickname());
        userInfo.put("avatar", user.getAvatar());
        userInfo.put("role", user.getRole());

        result.put("user", userInfo);

        return CommonResult.success(result);
    }

    /**
     * 获取当前管理员信息
     */
    @GetMapping("/info")
    public CommonResult<Map<String, Object>> getUserInfo(@RequestHeader("Authorization") String authHeader) {
        // 从token中解析用户信息
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return CommonResult.unauthorized("未提供合法的身份令牌");
        }

        User user = userService.getUserFromToken(authHeader);
        if (user == null) {
            return CommonResult.unauthorized("身份令牌无效或已过期");
        }

        // 检查用户角色是否为管理员
        if (!"admin".equals(user.getRole())) {
            return CommonResult.forbidden("该账号没有管理员权限");
        }

        // 封装用户信息
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getUserId());
        userInfo.put("username", user.getUsername());
        userInfo.put("nickname", user.getNickname());
        userInfo.put("avatar", user.getAvatar());
        userInfo.put("role", user.getRole());

        return CommonResult.success(userInfo);
    }
    
    /**
     * 文件上传接口
     * 
     * @param file 上传的文件
     * @return 上传结果，包含文件URL
     */
    @PostMapping("/upload")
    public CommonResult<Map<String, String>> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            // 验证文件
            if (file == null || file.isEmpty()) {
                return CommonResult.failed("文件不能为空");
            }
            
            // 获取文件后缀
            String originalFilename = file.getOriginalFilename();
            String suffix = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            
            // 限制文件类型
            if (!".jpg".equalsIgnoreCase(suffix) && !".jpeg".equalsIgnoreCase(suffix) &&
                !".png".equalsIgnoreCase(suffix) && !".gif".equalsIgnoreCase(suffix)) {
                return CommonResult.failed("只支持jpg、jpeg、png、gif格式的图片");
            }
            
            // 生成存储路径
            String relativeDir = "/images/" + UUID.randomUUID().toString().substring(0, 8);
            String storageDir = uploadPath + relativeDir;
            
            // 确保目录存在
            Path uploadDir = Paths.get(storageDir);
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }
            
            // 生成文件名
            String filename = UUID.randomUUID().toString().replace("-", "") + suffix;
            Path filePath = uploadDir.resolve(filename);
            
            // 保存文件
            Files.copy(file.getInputStream(), filePath);
            
            // 生成访问URL
            String fileUrl = accessUrl + relativeDir + "/" + filename;
            
            // 返回结果
            Map<String, String> result = new HashMap<>();
            result.put("url", fileUrl);
            result.put("name", originalFilename);
            
            return CommonResult.success(result);
        } catch (IOException e) {
            return CommonResult.failed("文件上传失败: " + e.getMessage());
        }
    }
}