package com.muyingmall.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * 登录数据传输对象
 */
@Data
public class LoginDTO implements Serializable {

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
     * 验证码Key
     */
    private String captchaKey;

    /**
     * 验证码
     */
    private String captchaCode;
}