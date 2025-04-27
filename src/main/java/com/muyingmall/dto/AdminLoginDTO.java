package com.muyingmall.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * 管理员登录数据传输对象
 */
@Data
public class AdminLoginDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 管理员用户名
     */
    @NotBlank(message = "用户名不能为空")
    private String admin_name;

    /**
     * 管理员密码
     */
    @NotBlank(message = "密码不能为空")
    private String admin_pass;
}