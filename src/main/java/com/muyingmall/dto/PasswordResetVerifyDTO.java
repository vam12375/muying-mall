package com.muyingmall.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 密码重置验证DTO
 * 用于第二步：验证数字验证码并设置新密码
 */
@Data
@Schema(description = "密码重置验证请求")
public class PasswordResetVerifyDTO {

    @NotBlank(message = "重置令牌不能为空")
    @Schema(description = "重置令牌，由第一步返回", required = true)
    private String resetToken;

    @NotBlank(message = "验证码不能为空")
    @Pattern(regexp = "^\\d{6}$", message = "验证码必须是6位数字")
    @Schema(description = "6位数字验证码", required = true)
    private String verifyCode;

    @NotBlank(message = "新密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度必须在6-20位之间")
    @Schema(description = "新密码，6-20位", required = true)
    private String newPassword;

    @NotBlank(message = "确认密码不能为空")
    @Schema(description = "确认新密码", required = true)
    private String confirmPassword;
}
