package com.muyingmall.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 密码重置请求DTO
 * 用于第一步：验证图形验证码并发送数字验证码
 */
@Data
@Schema(description = "密码重置请求")
public class PasswordResetRequestDTO {

    @NotBlank(message = "用户名/邮箱不能为空")
    @Schema(description = "用户名或邮箱", required = true)
    private String account;

    @NotBlank(message = "验证码Key不能为空")
    @Schema(description = "图形验证码Key", required = true)
    private String captchaKey;

    @NotBlank(message = "图形验证码不能为空")
    @Schema(description = "用户输入的图形验证码", required = true)
    private String captchaCode;
}
