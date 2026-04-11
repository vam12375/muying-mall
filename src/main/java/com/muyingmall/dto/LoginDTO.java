package com.muyingmall.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * 登录数据传输对象
 */
/**
 * 登录请求数据传输对象。
 * 用于接收前端提交的账号、密码、记住我状态以及 Turnstile 人机验证令牌。
 */
@Data
public class LoginDTO implements Serializable {

    /**
     * 序列化版本号。
     */
    private static final long serialVersionUID = 1L;

    /**
     * 用户名或邮箱
     */
    @NotBlank(message = "用户名/邮箱不能为空")
    private String username;

    /**
     * 密码
     */
    @NotBlank(message = "密码不能为空")
    private String password;

    /**
     * 记住我
     */
    private Boolean rememberMe = false;

    /**
     * Cloudflare Turnstile 返回的 token
     */
    private String turnstileToken;
}