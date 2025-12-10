package com.muyingmall.controller.user;

import com.muyingmall.common.Result;
import com.muyingmall.service.SystemConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 支付配置控制器 - 用户端
 * 提供获取启用的支付方式等公开配置信息
 */
@Slf4j
@RestController
@RequestMapping("/api/payment/config")
@RequiredArgsConstructor
@Tag(name = "支付配置", description = "获取支付相关配置信息")
public class PaymentConfigController {

    private final SystemConfigService systemConfigService;

    /**
     * 获取启用的支付方式列表
     * 根据系统配置返回当前可用的支付方式
     */
    @GetMapping("/methods")
    @Operation(summary = "获取启用的支付方式", description = "返回系统配置中启用的支付方式列表")
    public Result<List<Map<String, Object>>> getEnabledPaymentMethods() {
        try {
            List<Map<String, Object>> methods = new ArrayList<>();

            // 检查支付宝是否启用
            boolean alipayEnabled = systemConfigService.getBooleanValue("alipayEnabled", true);
            if (alipayEnabled) {
                Map<String, Object> alipay = new HashMap<>();
                alipay.put("id", "alipay");
                alipay.put("value", "alipay");
                alipay.put("label", "支付宝");
                alipay.put("name", "支付宝");
                alipay.put("image", "/pay/alipay.png");
                alipay.put("description", "推荐使用支付宝付款");
                methods.add(alipay);
            }

            // 检查微信支付是否启用
            boolean wechatEnabled = systemConfigService.getBooleanValue("wechatPayEnabled", true);
            if (wechatEnabled) {
                Map<String, Object> wechat = new HashMap<>();
                wechat.put("id", "wechat");
                wechat.put("value", "wechat");
                wechat.put("label", "微信支付");
                wechat.put("name", "微信支付");
                wechat.put("image", "/pay/wechat.png");
                wechat.put("description", "使用微信扫码支付");
                methods.add(wechat);
            }

            // 检查余额支付是否启用
            boolean balanceEnabled = systemConfigService.getBooleanValue("balancePayEnabled", true);
            if (balanceEnabled) {
                Map<String, Object> wallet = new HashMap<>();
                wallet.put("id", "wallet");
                wallet.put("value", "wallet");
                wallet.put("label", "钱包支付");
                wallet.put("name", "钱包支付");
                wallet.put("image", "/pay/wallet.png");
                wallet.put("description", "使用账户余额支付");
                methods.add(wallet);
            }

            log.info("获取启用的支付方式: alipay={}, wechat={}, balance={}", 
                    alipayEnabled, wechatEnabled, balanceEnabled);

            return Result.success(methods);
        } catch (Exception e) {
            log.error("获取支付方式配置失败", e);
            return Result.error("获取支付方式配置失败: " + e.getMessage());
        }
    }
}
