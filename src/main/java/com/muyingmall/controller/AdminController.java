package com.muyingmall.controller;

import com.muyingmall.common.api.CommonResult;
import com.muyingmall.dto.AdminLoginDTO;
import com.muyingmall.entity.User;
import com.muyingmall.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 管理员控制器
 */
@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UserService userService;

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
    public CommonResult getAdminInfo(@RequestHeader("Authorization") String token) {
        // 从token获取用户信息
        User user = userService.getUserFromToken(token);

        if (user == null) {
            return CommonResult.unauthorized("token已过期或无效");
        }

        // 检查用户角色是否为管理员
        if (!"admin".equals(user.getRole())) {
            return CommonResult.forbidden("该账号没有管理员权限");
        }

        // 封装用户信息（去除敏感信息）
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getUserId());
        userInfo.put("username", user.getUsername());
        userInfo.put("nickname", user.getNickname());
        userInfo.put("avatar", user.getAvatar());
        userInfo.put("role", user.getRole());

        return CommonResult.success(userInfo);
    }

    /**
     * 管理员登出
     */
    @PostMapping("/logout")
    public CommonResult logout(@RequestHeader("Authorization") String token) {
        // 清除token等操作
        userService.logout(token);
        return CommonResult.success(null);
    }
}