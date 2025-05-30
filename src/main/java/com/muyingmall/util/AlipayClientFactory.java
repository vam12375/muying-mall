package com.muyingmall.util;

import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.muyingmall.config.AlipayConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

/**
 * 支付宝客户端工厂类
 * 提供AlipayClient的单例实现，避免重复创建实例
 */
@Component
@RequiredArgsConstructor
public class AlipayClientFactory {

    private final AlipayConfig alipayConfig;

    private static AlipayClient alipayClient;

    /**
     * 初始化支付宝客户端
     */
    @PostConstruct
    public void init() {
        alipayClient = new DefaultAlipayClient(
                alipayConfig.getGatewayUrl(),
                alipayConfig.getAppId(),
                alipayConfig.getPrivateKey(),
                "json",
                "UTF-8",
                alipayConfig.getPublicKey(),
                "RSA2");
    }

    /**
     * 获取支付宝客户端实例
     * 
     * @return AlipayClient 支付宝客户端实例
     */
    public static AlipayClient getAlipayClient() {
        return alipayClient;
    }
}