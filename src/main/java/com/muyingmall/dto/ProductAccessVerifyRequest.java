package com.muyingmall.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 访问验证请求参数。
 * 既用于商品详情页访问验证，也用于全站访问频率验证接口，
 * 前端需要同时提交验证会话标识和 Turnstile 返回令牌。
 */
@Data
@Schema(description = "商品访问验证请求")
public class ProductAccessVerifyRequest {

    /**
     * 后端下发的验证会话标识，用于定位待放行的访问上下文。
     */
    @Schema(description = "验证会话Key", example = "product_access_123456")
    private String verifyKey;

    /**
     * Cloudflare Turnstile 返回的人机验证令牌。
     */
    @Schema(description = "Cloudflare Turnstile 返回的 token", example = "0.xxxxxxxxxxxxxxxxx")
    private String turnstileToken;
}
