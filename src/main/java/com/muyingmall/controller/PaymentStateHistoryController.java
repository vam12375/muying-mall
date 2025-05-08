package com.muyingmall.controller;

import com.muyingmall.common.ApiResponse;
import com.muyingmall.entity.PaymentStateLog;
import com.muyingmall.service.PaymentStateLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 支付状态历史控制器
 */
@RestController
@RequestMapping("/api/v1/payments/history")
@RequiredArgsConstructor
@Tag(name = "支付状态历史")
public class PaymentStateHistoryController {

    private final PaymentStateLogService paymentStateLogService;

    @GetMapping("/payment/{paymentId}")
    @Operation(summary = "获取支付状态变更历史")
    public ApiResponse<List<PaymentStateLog>> getPaymentStateHistory(
            @Parameter(description = "支付ID") @PathVariable Long paymentId) {
        List<PaymentStateLog> history = paymentStateLogService.getPaymentStateHistory(paymentId);
        return ApiResponse.success(history);
    }

    @GetMapping("/order/{orderId}")
    @Operation(summary = "获取订单关联的支付状态变更历史")
    public ApiResponse<List<PaymentStateLog>> getPaymentStateHistoryByOrderId(
            @Parameter(description = "订单ID") @PathVariable Integer orderId) {
        List<PaymentStateLog> history = paymentStateLogService.getPaymentStateHistoryByOrderId(orderId);
        return ApiResponse.success(history);
    }
}