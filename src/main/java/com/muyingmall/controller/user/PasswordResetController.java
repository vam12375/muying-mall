package com.muyingmall.controller.user;

import com.muyingmall.common.api.Result;
import com.muyingmall.dto.CaptchaDTO;
import com.muyingmall.dto.PasswordResetRequestDTO;
import com.muyingmall.dto.PasswordResetVerifyDTO;
import com.muyingmall.service.PasswordResetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 密码重置控制器
 * 提供忘记密码功能的API接口
 */
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
@Tag(name = "密码重置", description = "忘记密码、验证码获取、密码重置等接口")
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    /**
     * 获取图形验证码
     */
    @GetMapping("/captcha")
    @Operation(summary = "获取图形验证码", description = "生成图形验证码，返回Base64编码的图片和验证码Key")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "获取成功"),
        @ApiResponse(responseCode = "500", description = "服务器错误")
    })
    public Result<CaptchaDTO> getCaptcha() {
        CaptchaDTO captcha = passwordResetService.generateCaptcha();
        return Result.success(captcha);
    }

    /**
     * 请求密码重置
     * 验证图形验证码，发送数字验证码到后台管理系统
     */
    @PostMapping("/password/reset/request")
    @Operation(summary = "请求密码重置", description = "验证图形验证码后，生成6位数字验证码并发送通知到后台管理系统")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "请求成功，验证码已发送"),
        @ApiResponse(responseCode = "400", description = "参数错误或验证码错误"),
        @ApiResponse(responseCode = "404", description = "账号不存在")
    })
    public Result<Map<String, Object>> requestPasswordReset(@RequestBody @Valid PasswordResetRequestDTO requestDTO) {
        try {
            Map<String, Object> result = passwordResetService.requestPasswordReset(requestDTO);
            return Result.success(result, "验证码已发送到后台管理系统");
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        }
    }

    /**
     * 验证并重置密码
     */
    @PostMapping("/password/reset/verify")
    @Operation(summary = "验证并重置密码", description = "验证6位数字验证码，验证通过后重置密码")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "密码重置成功"),
        @ApiResponse(responseCode = "400", description = "验证码错误或已过期"),
        @ApiResponse(responseCode = "500", description = "服务器错误")
    })
    public Result<Void> verifyAndResetPassword(@RequestBody @Valid PasswordResetVerifyDTO verifyDTO) {
        try {
            passwordResetService.verifyAndResetPassword(verifyDTO);
            return Result.success(null, "密码重置成功，请使用新密码登录");
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        }
    }
}
