package com.muyingmall.controller;

import com.muyingmall.common.ApiResponse;
import com.muyingmall.entity.Payment;
import com.muyingmall.enums.PaymentStatus;
import com.muyingmall.service.PaymentStateService;
import com.muyingmall.statemachine.PaymentEvent;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 支付状态控制器
 */
@RestController
@RequestMapping("/api/v1/payments/state")
@RequiredArgsConstructor
@Tag(name = "支付状态管理")
public class PaymentStateController {

    private final PaymentStateService paymentStateService;

    @PostMapping("/transit/{paymentId}")
    @Operation(summary = "支付状态转换")
    public ApiResponse<Payment> transitPaymentState(
            @Parameter(description = "支付ID") @PathVariable Long paymentId,
            @Parameter(description = "事件") @RequestParam PaymentEvent event,
            @Parameter(description = "操作人") @RequestParam String operator,
            @Parameter(description = "原因") @RequestParam(required = false) String reason) {
        Payment payment = paymentStateService.sendEvent(paymentId, event, operator, reason);
        return ApiResponse.success(payment);
    }

    @GetMapping("/possible-states/{paymentId}")
    @Operation(summary = "获取可能的下一个状态")
    public ApiResponse<List<PaymentStatus>> getPossibleNextStates(
            @Parameter(description = "支付ID") @PathVariable Long paymentId,
            @Parameter(description = "当前状态") @RequestParam PaymentStatus currentStatus) {
        PaymentStatus[] nextStates = paymentStateService.getPossibleNextStates(currentStatus);
        List<PaymentStatus> stateList = Arrays.stream(nextStates).collect(Collectors.toList());
        return ApiResponse.success(stateList);
    }

    @GetMapping("/can-transit")
    @Operation(summary = "判断状态是否可以转换")
    public ApiResponse<Boolean> canTransit(
            @Parameter(description = "当前状态") @RequestParam PaymentStatus currentStatus,
            @Parameter(description = "目标状态") @RequestParam PaymentStatus targetStatus) {
        boolean canTransit = paymentStateService.canTransit(currentStatus, targetStatus);
        return ApiResponse.success(canTransit);
    }

    @GetMapping("/events")
    @Operation(summary = "获取所有可用的支付事件")
    public ApiResponse<List<PaymentEvent>> getAllEvents() {
        List<PaymentEvent> events = Arrays.asList(PaymentEvent.values());
        return ApiResponse.success(events);
    }
}