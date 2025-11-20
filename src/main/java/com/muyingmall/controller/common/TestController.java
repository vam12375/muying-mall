package com.muyingmall.controller.common;

import com.muyingmall.common.api.Result;
import com.muyingmall.util.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.HashMap;
import java.util.Map;

/**
 * 测试控制器
 */
@RestController
@RequestMapping("/test")
@io.swagger.v3.oas.annotations.tags.Tag(name = "系统功能", description = "测试接口、连接测试、认证测试等功能")
public class TestController {

    @Autowired
    private JwtUtils jwtUtils;

    /**
     * 测试连接
     */
    @GetMapping("/connection")
    public Result<Map<String, Object>> testConnection() {
        Map<String, Object> data = new HashMap<>();
        data.put("message", "后端连接成功");
        data.put("timestamp", System.currentTimeMillis());
        data.put("status", "online");
        return Result.success(data);
    }

    /**
     * 测试需要认证的接口
     */
    @GetMapping("/auth")
    @PreAuthorize("isAuthenticated()")
    public Result<Map<String, Object>> testAuth() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Map<String, Object> data = new HashMap<>();
        data.put("message", "认证成功");
        data.put("user", authentication.getName());
        data.put("authorities", authentication.getAuthorities());
        return Result.success(data);
    }

    /**
     * 测试JWT生成，用于演示目的
     * 注意：实际生产环境不应该暴露此接口
     */
    @GetMapping("/jwt-demo")
    public Result<Map<String, Object>> testJwt() {
        // 生成一个演示令牌
        String demoToken = jwtUtils.generateToken(999, "demo_user", "ROLE_USER");

        Map<String, Object> data = new HashMap<>();
        data.put("message", "JWT演示令牌生成成功");
        data.put("demoToken", demoToken);
        data.put("note", "此令牌仅用于测试目的，不应在生产环境中使用");

        return Result.success(data);
    }
}