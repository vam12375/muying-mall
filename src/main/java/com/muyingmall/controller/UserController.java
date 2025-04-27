package com.muyingmall.controller;

import com.muyingmall.common.response.Result;
import com.muyingmall.dto.LoginDTO;
import com.muyingmall.dto.LoginResponseDTO;
import com.muyingmall.dto.UserDTO;
import com.muyingmall.entity.User;
import com.muyingmall.service.UserService;
import com.muyingmall.util.JwtUtils;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 用户控制器
 */
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
@Tag(name = "用户管理", description = "用户注册、登录、信息管理等接口")
public class UserController {

    private final UserService userService;
    private final JwtUtils jwtUtils;

    /**
     * 用户注册
     */
    @PostMapping("/register")
    @Operation(summary = "用户注册")
    public Result<User> register(@RequestBody @Valid UserDTO userDTO) {
        User user = userService.register(userDTO);
        // 清除敏感信息
        user.setPassword(null);
        return Result.success(user, "注册成功");
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    @Operation(summary = "用户登录")
    public Result<LoginResponseDTO> login(@RequestBody @Valid LoginDTO loginDTO, HttpSession session) {
        User user = userService.login(loginDTO);

        // 清除敏感信息
        user.setPassword(null);

        // 生成JWT令牌
        String token = jwtUtils.generateToken(user.getUserId(), user.getUsername(), user.getRole());

        // 创建响应DTO
        LoginResponseDTO responseDTO = new LoginResponseDTO(user, token);

        // 将用户信息存入session
        session.setAttribute("user", user);

        // 设置session过期时间，如果勾选了记住我，则延长session有效期
        if (loginDTO.getRememberMe()) {
            // 设置为7天
            session.setMaxInactiveInterval(7 * 24 * 60 * 60);
        }

        return Result.success(responseDTO, "登录成功");
    }

    /**
     * 退出登录
     */
    @PostMapping("/logout")
    @Operation(summary = "退出登录")
    public Result<Void> logout(HttpSession session) {
        session.invalidate();
        return Result.success(null, "退出成功");
    }

    /**
     * 获取当前登录用户信息 (基于JWT)
     */
    @GetMapping("/info")
    @Operation(summary = "获取当前登录用户信息")
    public Result<User> getUserInfo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return Result.error(401, "用户未认证");
        }

        String username = authentication.getName();
        User user = userService.findByUsername(username);

        if (user == null) {
            return Result.error(404, "用户不存在");
        }

        // 清除敏感信息
        user.setPassword(null);
        return Result.success(user);
    }

    /**
     * 修改用户信息 (基于JWT)
     */
    @PutMapping("/info")
    @Operation(summary = "修改用户信息")
    public Result<Void> updateUserInfo(@RequestBody User user) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return Result.error(401, "用户未认证");
        }

        String currentUsername = authentication.getName();
        User currentUser = userService.findByUsername(currentUsername);

        if (currentUser == null) {
            return Result.error(404, "用户不存在");
        }

        // 设置用户ID，防止篡改
        user.setUserId(currentUser.getUserId());

        // 设置为null的字段，防止修改不允许修改的信息
        user.setUsername(null);
        user.setPassword(null);
        user.setRole(null);
        user.setStatus(null);
        user.setCreateTime(null);
        user.setUpdateTime(null);

        // 只允许修改昵称、头像、性别、生日、邮箱、手机号等允许修改的字段
        User updateData = new User();
        updateData.setUserId(currentUser.getUserId());
        updateData.setNickname(user.getNickname());
        updateData.setAvatar(user.getAvatar());
        updateData.setGender(user.getGender());
        updateData.setBirthday(user.getBirthday());
        updateData.setEmail(user.getEmail());
        updateData.setPhone(user.getPhone());

        boolean success = userService.updateUserInfo(updateData);
        if (success) {
            return Result.success(null, "修改成功");
        } else {
            return Result.error(500, "修改失败");
        }
    }

    /**
     * 上传用户头像
     */
    @PostMapping("/avatar")
    @Operation(summary = "上传用户头像")
    public Result<String> uploadAvatar(@RequestParam("file") MultipartFile file) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return Result.error(401, "用户未认证");
        }

        String username = authentication.getName();
        User user = userService.findByUsername(username);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }

        try {
            String avatarUrl = userService.uploadAvatar(user.getUserId(), file);
            return Result.success(avatarUrl, "上传成功");
        } catch (Exception e) {
            return Result.error("上传失败: " + e.getMessage());
        }
    }

    /**
     * 修改密码 (基于JWT)
     */
    @PutMapping("/password")
    @Operation(summary = "修改密码")
    public Result<Void> changePassword(
            @RequestParam("oldPassword") String oldPassword,
            @RequestParam("newPassword") String newPassword) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return Result.error(401, "用户未认证");
        }

        String username = authentication.getName();
        User user = userService.findByUsername(username);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }

        boolean success = userService.changePassword(user.getUserId(), oldPassword, newPassword);
        if (success) {
            // 修改密码成功后，前端需要清除token并要求重新登录
            return Result.success(null, "修改成功，请重新登录");
        } else {
            return Result.error(500, "修改失败，旧密码错误或服务异常");
        }
    }

    /**
     * 获取当前登录用户信息 (保留基于Session的方式，如果还需要)
     */
    @GetMapping("/info-session")
    @Operation(summary = "获取当前登录用户信息 (Session)")
    public Result<User> getUserInfoFromSession(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return Result.error("未登录");
        }

        User latestUser = userService.getById(user.getUserId());
        if (latestUser == null) {
            session.invalidate();
            return Result.error("用户不存在");
        }

        // 清除敏感信息
        latestUser.setPassword(null);

        return Result.success(latestUser);
    }
}