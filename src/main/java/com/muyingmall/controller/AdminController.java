package com.muyingmall.controller;

import com.muyingmall.common.response.Result;
import com.muyingmall.dto.AdminLoginDTO;
import com.muyingmall.entity.User;
import com.muyingmall.service.UserService;
import com.muyingmall.util.JwtUtils;

import io.jsonwebtoken.Claims;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 管理员控制器
 */
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@Tag(name = "管理员管理", description = "管理员登录、信息管理等接口")
public class AdminController {

    private final UserService userService;
    private final JwtUtils jwtUtils;

    /**
     * 管理员登录
     */
    @PostMapping("/login")
    @Operation(summary = "管理员登录")
    public Result<String> login(@RequestBody @Valid AdminLoginDTO loginDTO) {
        // 调用service验证管理员身份
        User admin = userService.adminLogin(loginDTO);

        if (admin == null) {
            return Result.error("用户名或密码错误");
        }

        // 生成JWT令牌
        String token = jwtUtils.generateToken(admin.getUserId(), admin.getUsername(), admin.getRole());

        return Result.success(token, "登录成功");
    }

    /**
     * 获取管理员信息
     */
    @GetMapping("/info")
    @Operation(summary = "获取管理员信息")
    public Result<Map<String, Object>> getAdminInfo(@RequestHeader("Authorization") String token) {
        // 验证token
        if (token == null || !token.startsWith("Bearer ")) {
            return Result.error("未登录或token无效");
        }

        String actualToken = token.substring(7);
        if (!jwtUtils.validateToken(actualToken)) {
            return Result.error("token已过期");
        }

        // 解析token
        Claims claims = jwtUtils.getClaimsFromToken(actualToken);
        Integer userId = claims.get("userId", Integer.class);

        // 获取管理员信息
        User admin = userService.getById(userId);
        if (admin == null || !"admin".equals(admin.getRole())) {
            return Result.error("管理员不存在");
        }

        Map<String, Object> adminInfo = new HashMap<>();
        adminInfo.put("adminId", admin.getUserId());
        adminInfo.put("adminName", admin.getUsername());
        adminInfo.put("adminAvatar", admin.getAvatar());

        return Result.success(adminInfo);
    }
}