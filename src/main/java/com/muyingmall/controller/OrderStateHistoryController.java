package com.muyingmall.controller;

import com.muyingmall.common.ApiResponse;
import com.muyingmall.entity.OrderStateLog;
import com.muyingmall.service.OrderStateLogService;
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
 * 订单状态历史控制器
 */
@RestController
@RequestMapping("/api/v1/orders/history")
@RequiredArgsConstructor
@Tag(name = "订单状态历史")
public class OrderStateHistoryController {

    private final OrderStateLogService orderStateLogService;

    @GetMapping("/{orderId}")
    @Operation(summary = "获取订单状态变更历史")
    public ApiResponse<List<OrderStateLog>> getOrderStateHistory(
            @Parameter(description = "订单ID") @PathVariable Integer orderId) {
        List<OrderStateLog> history = orderStateLogService.getOrderStateHistory(orderId);
        return ApiResponse.success(history);
    }
}