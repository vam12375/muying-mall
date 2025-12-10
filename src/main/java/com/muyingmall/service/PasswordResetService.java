package com.muyingmall.service;

import com.muyingmall.dto.CaptchaDTO;
import com.muyingmall.dto.PasswordResetRequestDTO;
import com.muyingmall.dto.PasswordResetVerifyDTO;

import java.util.Map;

/**
 * 密码重置服务接口
 */
public interface PasswordResetService {

    /**
     * 生成图形验证码
     * @return 包含captchaKey和captchaImage的DTO
     */
    CaptchaDTO generateCaptcha();

    /**
     * 请求密码重置（验证图形验证码，发送数字验证码通知）
     * @param requestDTO 请求参数
     * @return 包含resetToken的Map
     */
    Map<String, Object> requestPasswordReset(PasswordResetRequestDTO requestDTO);

    /**
     * 验证数字验证码并重置密码
     * @param verifyDTO 验证参数
     * @return 是否成功
     */
    boolean verifyAndResetPassword(PasswordResetVerifyDTO verifyDTO);
}
