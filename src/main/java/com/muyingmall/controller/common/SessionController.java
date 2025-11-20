package com.muyingmall.controller.common;

import com.muyingmall.common.api.Result;
import com.muyingmall.entity.User;
import com.muyingmall.service.UserService;
import com.muyingmall.util.JwtUtils;
import io.jsonwebtoken.Claims;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 会话控制器 - 处理会话相关操作
 */
@RestController
@RequestMapping("/session")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "系统功能", description = "会话管理、会话同步等功能")
public class SessionController {

    private final JwtUtils jwtUtils;
    private final UserService userService;

    /**
     * 同步当前会话
     * 此接口主要用于确认会话有效性并进行会话刷新
     * 通过简单返回当前登录用户信息，避免前端重复请求用户信息
     */
    @PostMapping("/sync")
    public Result<Void> syncSession(HttpSession session) {
        // 从会话中获取用户信息
        User user = (User) session.getAttribute("user");

        // 检查用户是否已登录
        if (user == null) {
            return Result.error(400, "未登录");
        }

        // 记录会话同步日志
        log.info("用户会话已同步: {}", user.getUsername());

        // 会话同步成功，不需要返回用户数据，前端已有缓存
        return Result.success(null, "会话同步成功");
    }
}