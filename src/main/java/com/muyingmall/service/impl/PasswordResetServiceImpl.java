package com.muyingmall.service.impl;

import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.muyingmall.dto.CaptchaDTO;
import com.muyingmall.dto.PasswordResetRequestDTO;
import com.muyingmall.dto.PasswordResetVerifyDTO;
import com.muyingmall.entity.SysNotice;
import com.muyingmall.entity.User;
import com.muyingmall.service.PasswordResetService;
import com.muyingmall.service.SysNoticeService;
import com.muyingmall.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 密码重置服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordResetServiceImpl implements PasswordResetService {

    private final DefaultKaptcha captchaProducer;
    private final StringRedisTemplate redisTemplate;
    private final UserService userService;
    private final SysNoticeService sysNoticeService;
    private final PasswordEncoder passwordEncoder;

    // Redis Key前缀
    private static final String CAPTCHA_KEY_PREFIX = "captcha:";
    private static final String RESET_CODE_PREFIX = "reset:code:";
    private static final String RESET_TOKEN_PREFIX = "reset:token:";
    
    // 验证码有效期（分钟）
    private static final int CAPTCHA_EXPIRE_MINUTES = 5;
    private static final int RESET_CODE_EXPIRE_MINUTES = 10;

    @Override
    public CaptchaDTO generateCaptcha() {
        // 生成验证码文本
        String captchaText = captchaProducer.createText();
        // 生成验证码图片
        BufferedImage image = captchaProducer.createImage(captchaText);
        
        // 生成唯一Key
        String captchaKey = UUID.randomUUID().toString().replace("-", "");
        
        // 存储到Redis，设置过期时间
        redisTemplate.opsForValue().set(
            CAPTCHA_KEY_PREFIX + captchaKey, 
            captchaText.toUpperCase(), 
            CAPTCHA_EXPIRE_MINUTES, 
            TimeUnit.MINUTES
        );
        
        // 图片转Base64
        String base64Image = imageToBase64(image);
        
        log.info("生成图形验证码，key: {}", captchaKey);
        return new CaptchaDTO(captchaKey, "data:image/png;base64," + base64Image);
    }

    @Override
    public Map<String, Object> requestPasswordReset(PasswordResetRequestDTO requestDTO) {
        // 1. 验证图形验证码
        String storedCaptcha = redisTemplate.opsForValue().get(CAPTCHA_KEY_PREFIX + requestDTO.getCaptchaKey());
        if (storedCaptcha == null) {
            throw new RuntimeException("验证码已过期，请重新获取");
        }
        if (!storedCaptcha.equalsIgnoreCase(requestDTO.getCaptchaCode())) {
            throw new RuntimeException("图形验证码错误");
        }
        // 验证成功后删除验证码
        redisTemplate.delete(CAPTCHA_KEY_PREFIX + requestDTO.getCaptchaKey());
        
        // 2. 查找用户
        User user = userService.findByUsername(requestDTO.getAccount());
        if (user == null) {
            user = userService.getByEmail(requestDTO.getAccount());
        }
        if (user == null) {
            throw new RuntimeException("账号不存在");
        }
        
        // 3. 生成6位数字验证码
        String verifyCode = generateRandomCode(6);
        
        // 4. 生成重置令牌
        String resetToken = UUID.randomUUID().toString().replace("-", "");
        
        // 5. 存储验证码和用户信息到Redis
        redisTemplate.opsForValue().set(
            RESET_CODE_PREFIX + resetToken, 
            verifyCode, 
            RESET_CODE_EXPIRE_MINUTES, 
            TimeUnit.MINUTES
        );
        redisTemplate.opsForValue().set(
            RESET_TOKEN_PREFIX + resetToken, 
            user.getUserId().toString(), 
            RESET_CODE_EXPIRE_MINUTES, 
            TimeUnit.MINUTES
        );
        
        // 6. 发送通知到后台管理系统
        sendResetNoticeToAdmin(user, verifyCode);
        
        log.info("用户 {} 请求密码重置，验证码已发送到后台", user.getUsername());
        
        Map<String, Object> result = new HashMap<>();
        result.put("resetToken", resetToken);
        result.put("message", "验证码已发送到后台管理系统，请联系管理员获取");
        result.put("expireMinutes", RESET_CODE_EXPIRE_MINUTES);
        return result;
    }

    @Override
    public void checkVerifyCode(String resetToken, String verifyCode) {
        // 验证数字验证码（仅校验，不删除）
        String storedCode = redisTemplate.opsForValue().get(RESET_CODE_PREFIX + resetToken);
        if (storedCode == null) {
            throw new RuntimeException("验证码已过期，请重新申请");
        }
        if (!storedCode.equals(verifyCode)) {
            throw new RuntimeException("验证码错误");
        }
        log.info("验证码校验通过，resetToken: {}", resetToken);
    }

    @Override
    public boolean verifyAndResetPassword(PasswordResetVerifyDTO verifyDTO) {
        // 1. 验证两次密码是否一致
        if (!verifyDTO.getNewPassword().equals(verifyDTO.getConfirmPassword())) {
            throw new RuntimeException("两次输入的密码不一致");
        }
        
        // 2. 验证数字验证码
        String storedCode = redisTemplate.opsForValue().get(RESET_CODE_PREFIX + verifyDTO.getResetToken());
        if (storedCode == null) {
            throw new RuntimeException("验证码已过期，请重新申请");
        }
        if (!storedCode.equals(verifyDTO.getVerifyCode())) {
            throw new RuntimeException("验证码错误");
        }
        
        // 3. 获取用户ID
        String userIdStr = redisTemplate.opsForValue().get(RESET_TOKEN_PREFIX + verifyDTO.getResetToken());
        if (userIdStr == null) {
            throw new RuntimeException("重置令牌无效");
        }
        
        // 4. 更新密码
        Integer userId = Integer.parseInt(userIdStr);
        User user = userService.getUserById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        // 加密新密码
        user.setPassword(passwordEncoder.encode(verifyDTO.getNewPassword()));
        userService.updateUser(user);
        
        // 5. 清除Redis中的验证码和令牌
        redisTemplate.delete(RESET_CODE_PREFIX + verifyDTO.getResetToken());
        redisTemplate.delete(RESET_TOKEN_PREFIX + verifyDTO.getResetToken());
        
        log.info("用户 {} 密码重置成功", user.getUsername());
        return true;
    }

    /**
     * 发送密码重置通知到后台管理系统
     */
    private void sendResetNoticeToAdmin(User user, String verifyCode) {
        SysNotice notice = new SysNotice();
        notice.setTitle("用户密码重置请求 - " + user.getUsername());
        notice.setContent(String.format(
            "用户 [%s] 请求重置密码\n" +
            "用户ID: %d\n" +
            "邮箱: %s\n" +
            "手机: %s\n" +
            "验证码: %s\n" +
            "有效期: %d分钟\n\n" +
            "请将验证码告知用户以完成密码重置。",
            user.getUsername(),
            user.getUserId(),
            user.getEmail() != null ? user.getEmail() : "未设置",
            user.getPhone() != null ? user.getPhone() : "未设置",
            verifyCode,
            RESET_CODE_EXPIRE_MINUTES
        ));
        notice.setType("system");
        notice.setStatus("published");
        notice.setIsPinned(1);  // 置顶显示
        notice.setAuthor("系统");
        notice.setPublishTime(LocalDateTime.now());
        notice.setViewCount(0);
        
        sysNoticeService.publishNotice(notice);
    }

    /**
     * 生成指定位数的随机数字验证码
     */
    private String generateRandomCode(int length) {
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }

    /**
     * 图片转Base64
     */
    private String imageToBase64(BufferedImage image) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(image, "png", baos);
            return Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (Exception e) {
            log.error("图片转Base64失败", e);
            throw new RuntimeException("验证码生成失败");
        }
    }
}
