package com.muyingmall.controller;

import com.alipay.api.AlipayApiException;
import com.muyingmall.service.AlipayRefundService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 支付宝通知控制器
 */
@RestController
@RequestMapping("/api/payment/alipay")
@RequiredArgsConstructor
@Slf4j
public class AlipayNotifyController {

    private final AlipayRefundService alipayRefundService;

    /**
     * 处理支付宝退款异步通知
     */
    @PostMapping("/refund/notify")
    public String handleRefundNotify(HttpServletRequest request) {
        Map<String, String> params = new HashMap<>();

        // 获取支付宝通知参数
        Map<String, String[]> requestParams = request.getParameterMap();
        for (String name : requestParams.keySet()) {
            String[] values = requestParams.get(name);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i] : valueStr + values[i] + ",";
            }
            params.put(name, valueStr);
        }

        log.info("收到支付宝退款通知: {}", params);

        try {
            boolean result = alipayRefundService.handleRefundNotify(params);
            return result ? "success" : "fail";
        } catch (AlipayApiException e) {
            log.error("处理支付宝退款通知失败", e);
            return "fail";
        }
    }
}