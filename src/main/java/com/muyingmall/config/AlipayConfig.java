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

    /**
     * 退款异步通知地址
     */
    private String refundNotifyUrl;

    /**
     * 签名类型
     */
    private String signType = "RSA2";

    /**
     * 字符编码格式
     */
    private String charset = "UTF-8";

    /**
     * 格式类型
     */
    private String format = "json";

    /**
     * 最大查询重试次数
     */
    private int maxQueryRetry = 5;

    /**
     * 查询间隔时间（毫秒）
     */
    private long queryDuration = 5000;

    /**
     * 退款查询最大等待时间（秒）
     */
    private int refundQueryMaxWaitTime = 600;
}