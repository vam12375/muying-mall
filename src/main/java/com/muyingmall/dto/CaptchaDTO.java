package com.muyingmall.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 图形验证码响应DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "图形验证码响应")
public class CaptchaDTO {

    @Schema(description = "验证码唯一标识，用于后续验证")
    private String captchaKey;

    @Schema(description = "验证码图片Base64编码")
    private String captchaImage;
}
