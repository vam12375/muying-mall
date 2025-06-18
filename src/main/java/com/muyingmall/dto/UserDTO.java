package com.muyingmall.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.io.Serializable;

/**
 * 用户数据传输对象
 */
@Data
@Schema(description = "用户注册/更新请求参数")
public class UserDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户名
     */
    @Schema(description = "用户名，用于登录", example = "john_doe", minLength = 4, maxLength = 20, required = true)
    @NotBlank(message = "用户名不能为空")
    @Size(min = 4, max = 20, message = "用户名长度必须在4-20个字符之间")
    private String username;

    /**
     * 密码
     */
    @Schema(description = "登录密码", example = "123456", minLength = 6, maxLength = 20, required = true, format = "password")
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度必须在6-20个字符之间")
    private String password;

    /**
     * 确认密码
     */
    @Schema(description = "确认密码，必须与密码一致", example = "123456", format = "password")
    private String confirmPassword;

    /**
     * 邮箱
     */
    @Schema(description = "邮箱地址，用于找回密码和接收通知", example = "john.doe@example.com", required = true, format = "email")
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;

    /**
     * 手机号
     */
    @Schema(description = "手机号码，用于接收短信验证码", example = "13800138000", pattern = "^1[3-9]\\d{9}$")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    /**
     * 昵称
     */
    @Schema(description = "用户昵称，用于显示", example = "小明", maxLength = 50)
    private String nickname;
}