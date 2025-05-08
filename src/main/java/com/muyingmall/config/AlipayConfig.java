package com.muyingmall.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 支付宝配置类
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "alipay")
public class AlipayConfig {

    /**
     * 应用ID
     */
    private String appId;

    /**
     * 商户私钥
     */
    private String privateKey;

    /**
     * 支付宝公钥
     */
    private String publicKey;

    /**
     * 支付宝网关地址
     */
    private String gatewayUrl;

    /**
     * 异步通知地址
     */
    private String notifyUrl;

    /**
     * 同步返回地址
     */
    private String returnUrl;
}