package com.muyingmall.controller.common;

import com.alipay.api.AlipayApiException;
import com.muyingmall.service.AlipayRefundService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * 支付宝通知控制器
 */
@RestController
@RequestMapping("/api/payment/alipay")
@RequiredArgsConstructor
@Slf4j
@io.swagger.v3.oas.annotations.tags.Tag(name = "支付管理", description = "支付宝支付回调通知处理")
public class AlipayNotifyController {

    private final AlipayRefundService alipayRefundService;

    /**
     * 处理支付宝退款异步通知
     * 
     * 支付宝退款异步通知文档: https://opendocs.alipay.com/open/203/105286
     */
    @PostMapping("/refund/notify")
    public String handleRefundNotify(HttpServletRequest request) {
        log.debug("接收到支付宝退款异步通知");

        // 获取所有请求参数
        Map<String, String> params = new HashMap<>();
        Enumeration<String> parameterNames = request.getParameterNames();

        while (parameterNames.hasMoreElements()) {
            String name = parameterNames.nextElement();
            String[] values = request.getParameterValues(name);
            StringBuilder valueStr = new StringBuilder();

            for (int i = 0; i < values.length; i++) {
                valueStr.append(values[i]);
                if (i != values.length - 1) {
                    valueStr.append(",");
                }
            }

            params.put(name, valueStr.toString());
        }

        // 输出所有请求参数便于调试
        if (log.isDebugEnabled()) {
            params.forEach((key, value) -> log.debug("参数：{} = {}", key, value));
        }

        // 获取关键参数进行日志输出
        String outRequestNo = params.get("out_request_no");
        String refundStatus = params.get("refund_status");

        log.debug("支付宝退款通知 - 退款单号: {}, 退款状态: {}", outRequestNo, refundStatus);

        try {
            boolean result = alipayRefundService.handleRefundNotify(params);
            if (result) {
                log.debug("支付宝退款通知处理成功");
                return "success"; // 处理成功返回success字符串，支付宝将不再重发通知
            } else {
                log.warn("支付宝退款通知处理失败，等待支付宝重新通知");
                return "fail"; // 处理失败返回fail字符串，支付宝将重发通知
            }
        } catch (AlipayApiException e) {
            log.error("处理支付宝退款通知时发生异常: {}", e.getMessage(), e);
            return "fail"; // 出现异常返回fail字符串，支付宝将重发通知
        } catch (Exception e) {
            log.error("处理支付宝退款通知时发生未预期异常: {}", e.getMessage(), e);
            return "fail"; // 出现异常返回fail字符串，支付宝将重发通知
        }
    }
}