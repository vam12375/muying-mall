package com.muyingmall.controller.user;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.muyingmall.common.api.Result;
import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.muyingmall.dto.CaptchaDTO;
import com.muyingmall.dto.LoginDTO;
import com.muyingmall.dto.LoginResponseDTO;
import com.muyingmall.dto.UserDTO;
import com.muyingmall.entity.User;
import com.muyingmall.service.UserService;
import com.muyingmall.service.impl.LoginCacheService;
import com.muyingmall.util.IpUtil;
import com.muyingmall.util.JwtUtils;
import com.muyingmall.util.LoginRateLimiter;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.time.Duration;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;
import jakarta.servlet.http.HttpServletRequest;
import javax.imageio.ImageIO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

/**
 * 用户控制器
 */
@Slf4j
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
@Tag(name = "用户管理", description = "用户注册、登录、信息管理等接口")
public class UserController {

    private final UserService userService;
    private final JwtUtils jwtUtils;
    private final LoginRateLimiter loginRateLimiter;
    private final LoginCacheService loginCacheService;
    private final com.muyingmall.util.RedisUtil redisUtil;
    private final DefaultKaptcha captchaProducer;
    private final org.springframework.data.redis.core.StringRedisTemplate redisTemplate;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    /**
     * 前端登录页渲染 Turnstile 组件时使用的站点公钥。
     */
    @Value("${cloudflare.turnstile.site-key:}")
    private String turnstileSiteKey;

    /**
     * 后端调用 Cloudflare siteverify 接口时使用的校验密钥。
     */
    @Value("${cloudflare.turnstile.secret-key:}")
    private String turnstileSecretKey;

    private static final String CAPTCHA_KEY_PREFIX = "captcha:";
    private static final int CAPTCHA_EXPIRE_MINUTES = 5;
    private static final String TURNSTILE_VERIFY_URL = "https://challenges.cloudflare.com/turnstile/v0/siteverify";

    /**
     * 获取图形验证码
     */
    @GetMapping("/login/captcha")
    @Operation(summary = "获取登录验证码", description = "生成图形验证码，返回Base64编码的图片和验证码Key")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "获取成功")
    })
    public Result<CaptchaDTO> getCaptcha() {
        try {
            String captchaText = captchaProducer.createText();
            BufferedImage image = captchaProducer.createImage(captchaText);
            String captchaKey = UUID.randomUUID().toString().replace("-", "");

            redisTemplate.opsForValue().set(
                    CAPTCHA_KEY_PREFIX + captchaKey,
                    captchaText.toUpperCase(),
                    java.time.Duration.ofMinutes(CAPTCHA_EXPIRE_MINUTES));

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "png", baos);
            String base64Image = Base64.getEncoder().encodeToString(baos.toByteArray());

            return Result.success(new CaptchaDTO(captchaKey, "data:image/png;base64," + base64Image));
        } catch (Exception e) {
            log.error("生成验证码失败", e);
            return Result.error("验证码生成失败");
        }
    }

    /**
     * 用户注册
     */
    @PostMapping("/register")
    @Operation(summary = "用户注册", description = "创建新用户，返回用户信息（不含密码）", tags = { "用户管理" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "注册成功", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Result.class))),
            @ApiResponse(responseCode = "400", description = "请求参数错误"),
            @ApiResponse(responseCode = "409", description = "用户名已存在")
    })
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
    @Operation(summary = "用户登录", description = "用户登录并返回JWT令牌和用户信息", tags = { "用户管理" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "登录成功", content = @Content(mediaType = "application/json", schema = @Schema(implementation = LoginResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "请求参数错误"),
            @ApiResponse(responseCode = "401", description = "用户名或密码错误"),
            @ApiResponse(responseCode = "403", description = "账户已被禁用"),
            @ApiResponse(responseCode = "429", description = "请求过于频繁")
    })
    public Result<LoginResponseDTO> login(@RequestBody @Valid LoginDTO loginDTO,
            HttpSession session,
            HttpServletRequest request) {
        // 登录前必须先通过 Turnstile 人机验证，拦截脚本撞库与暴力尝试。
        if (!StringUtils.hasText(loginDTO.getTurnstileToken())) {
            return Result.error(400, "请完成人机验证");
        }

        // 先校验 Turnstile 令牌，再进入账号密码校验流程，减少无效登录请求对业务层的压力。
        if (!verifyTurnstileToken(loginDTO.getTurnstileToken(), IpUtil.getIpAddr(request))) {
            return Result.error(400, "人机验证失败，请重试");
        }

        // 获取客户端IP
        String clientIp = IpUtil.getIpAddr(request);

        // 限流检查 - 防止暴力破解
        if (!loginRateLimiter.tryAcquire(clientIp, loginDTO.getUsername())) {
            return Result.error(429, "登录请求过于频繁，请稍后再试");
        }

        // 执行登录验证
        User user = userService.login(loginDTO);

        // 清除敏感信息
        user.setPassword(null);

        // 生成JWT令牌
        String token = jwtUtils.generateToken(user.getUserId(), user.getUsername(), user.getRole());

        // 缓存用户会话，减少后续请求的数据库查询
        loginCacheService.cacheUserSession(token, user);

        // 创建响应DTO
        LoginResponseDTO responseDTO = new LoginResponseDTO(user, token);

        // 将用户信息存入session
        session.setAttribute("user", user);

        // 设置session过期时间
        if (loginDTO.getRememberMe()) {
            session.setMaxInactiveInterval(604800); // 7天
        } else {
            session.setMaxInactiveInterval(1800); // 30分钟
        }

        return Result.success(responseDTO, "登录成功");
    }

    /**
     * 调用 Cloudflare 官方 siteverify 接口校验登录页提交的 Turnstile 令牌。
     * 只有验证成功后，后续账号密码校验与会话创建流程才会继续执行。
     */
    private boolean verifyTurnstileToken(String token, String remoteIp) {
        if (!StringUtils.hasText(turnstileSecretKey)) {
            log.warn("Cloudflare Turnstile secret-key 未配置，无法校验登录人机验证");
            return false;
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
            form.add("secret", turnstileSecretKey);
            form.add("response", token);
            if (StringUtils.hasText(remoteIp)) {
                form.add("remoteip", remoteIp);
            }

            HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(form, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(TURNSTILE_VERIFY_URL, requestEntity,
                    String.class);
            String body = response.getBody();
            if (!StringUtils.hasText(body)) {
                return false;
            }

            JsonNode root = objectMapper.readTree(body);
            boolean success = root.path("success").asBoolean(false);
            if (!success) {
                log.warn("登录 Turnstile 校验失败: {}", body);
            }
            return success;
        } catch (Exception e) {
            log.error("登录 Turnstile 校验异常", e);
            return false;
        }
    }

    /**
     * 退出登录
     */
    @PostMapping("/logout")
    @Operation(summary = "退出登录", description = "清除用户会话信息", tags = { "用户管理" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "退出成功"),
            @ApiResponse(responseCode = "401", description = "用户未登录")
    })
    public Result<Void> logout(HttpSession session) {
        session.invalidate();
        return Result.success(null, "退出成功");
    }

    /**
     * 获取当前登录用户信息 (基于JWT) - 优化：只返回基本信息，不包含统计数据
     */
    @GetMapping("/info")
    @Operation(summary = "获取当前登录用户信息", description = "通过JWT令牌获取当前登录用户的详细信息（不包含统计数据，统计数据请使用/user/stats接口）", tags = {
            "用户管理" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "获取成功", content = @Content(mediaType = "application/json", schema = @Schema(implementation = User.class))),
            @ApiResponse(responseCode = "401", description = "用户未认证"),
            @ApiResponse(responseCode = "404", description = "用户不存在")
    })
    public Result<User> getUserInfo() {
        // 优化：添加Token验证，避免JWT解析错误
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return Result.error(401, "用户未认证");
        }

        String username = authentication.getName();

        // 优化：验证用户名不为空
        if (username == null || username.trim().isEmpty()) {
            log.warn("用户名为空，认证信息异常");
            return Result.error(401, "认证信息异常");
        }

        // 优化：使用缓存查询，减少数据库压力
        User user = userService.findByUsername(username);

        if (user == null) {
            log.warn("用户不存在: username={}", username);
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
    @Operation(summary = "修改用户信息", description = "更新当前登录用户的基本信息，不包括密码和其他敏感字段", tags = { "用户管理" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "修改成功"),
            @ApiResponse(responseCode = "400", description = "参数错误"),
            @ApiResponse(responseCode = "401", description = "用户未认证"),
            @ApiResponse(responseCode = "404", description = "用户不存在"),
            @ApiResponse(responseCode = "500", description = "服务器错误")
    })
    public Result<Void> updateUserInfo(@RequestBody User user) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
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
    @Operation(summary = "上传用户头像", description = "上传并更新当前用户的头像", tags = { "用户管理" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "上传成功", content = @Content(mediaType = "application/json", schema = @Schema(type = "string", description = "头像URL"))),
            @ApiResponse(responseCode = "400", description = "文件格式错误"),
            @ApiResponse(responseCode = "401", description = "用户未认证"),
            @ApiResponse(responseCode = "404", description = "用户不存在"),
            @ApiResponse(responseCode = "500", description = "上传失败")
    })
    public Result<String> uploadAvatar(
            @Parameter(description = "头像文件，支持jpg、png、gif格式，大小不超过2MB", required = true, content = @Content(mediaType = "multipart/form-data")) @RequestParam("file") MultipartFile file) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
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
    @Operation(summary = "修改密码", description = "修改当前登录用户的密码，需要提供旧密码进行验证", tags = { "用户管理" })
    @Parameters({
            @Parameter(name = "oldPassword", description = "旧密码", required = true, in = ParameterIn.QUERY),
            @Parameter(name = "newPassword", description = "新密码，长度6-20位", required = true, in = ParameterIn.QUERY)
    })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "修改成功"),
            @ApiResponse(responseCode = "400", description = "参数错误"),
            @ApiResponse(responseCode = "401", description = "用户未认证"),
            @ApiResponse(responseCode = "404", description = "用户不存在"),
            @ApiResponse(responseCode = "500", description = "修改失败，旧密码错误或服务异常")
    })
    public Result<Void> changePassword(
            @RequestParam("oldPassword") String oldPassword,
            @RequestParam("newPassword") String newPassword) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
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
     * 获取用户统计数据 - 优化：增加缓存，减少数据库查询
     */
    @GetMapping("/stats")
    @Operation(summary = "获取用户统计数据", description = "获取当前用户的订单、收藏、评价等统计信息（已缓存5分钟）", tags = { "用户管理" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "获取成功"),
            @ApiResponse(responseCode = "401", description = "用户未认证")
    })
    public Result<Map<String, Object>> getUserStats() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return Result.error(401, "用户未认证");
        }

        String username = authentication.getName();
        User user = userService.findByUsername(username);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }

        // 优化：使用缓存，TTL从60秒提升到300秒（5分钟）
        String cacheKey = "user:stats:" + user.getUserId();
        Object cached = redisUtil.get(cacheKey);
        if (cached instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> stats = (Map<String, Object>) cached;
            log.debug("从缓存获取用户统计数据: userId={}", user.getUserId());
            return Result.success(stats);
        }

        Map<String, Object> stats = userService.getUserStats(user.getUserId());

        // 缓存5分钟
        redisUtil.set(cacheKey, stats, 300);
        log.debug("缓存用户统计数据: userId={}, ttl=300秒", user.getUserId());

        return Result.success(stats);
    }

    /**
     * 获取当前登录用户信息 (保留基于Session的方式，如果还需要)
     */
    @GetMapping("/info-session")
    @Operation(summary = "获取当前登录用户信息 (Session)", description = "通过Session获取当前登录用户的信息（保留的传统方式）", tags = {
            "用户管理" }, deprecated = true)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "获取成功"),
            @ApiResponse(responseCode = "401", description = "未登录"),
            @ApiResponse(responseCode = "404", description = "用户不存在")
    })
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