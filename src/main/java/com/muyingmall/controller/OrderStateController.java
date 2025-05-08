package com.muyingmall.controller;

import com.muyingmall.common.ApiResponse;
import com.muyingmall.entity.Order;
import com.muyingmall.enums.OrderStatus;
import com.muyingmall.service.OrderStateService;
import com.muyingmall.statemachine.OrderEvent;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 订单状态控制器
 */
@RestController
@RequestMapping("/api/v1/orders/state")
@RequiredArgsConstructor
@Tag(name = "订单状态管理")
public class OrderStateController {

    private final OrderStateService orderStateService;

    @PostMapping("/transit/{orderId}")
    @Operation(summary = "订单状态转换")
    public ApiResponse<Order> transitOrderState(
            @Parameter(description = "订单ID") @PathVariable Integer orderId,
            @Parameter(description = "事件") @RequestParam OrderEvent event,
            @Parameter(description = "操作人") @RequestParam String operator,
            @Parameter(description = "原因") @RequestParam(required = false) String reason) {
        Order order = orderStateService.sendEvent(orderId, event, operator, reason);
        return ApiResponse.success(order);
    }

    @GetMapping("/possible-states/{orderId}")
    @Operation(summary = "获取可能的下一个状态")
    public ApiResponse<List<OrderStatus>> getPossibleNextStates(
            @Parameter(description = "订单ID") @PathVariable Integer orderId,
            @Parameter(description = "当前状态") @RequestParam OrderStatus currentStatus) {
        OrderStatus[] nextStates = orderStateService.getPossibleNextStates(currentStatus);
        List<OrderStatus> stateList = Arrays.stream(nextStates).collect(Collectors.toList());
        return ApiResponse.success(stateList);
    }

    @GetMapping("/can-transit")
    @Operation(summary = "判断状态是否可以转换")
    public ApiResponse<Boolean> canTransit(
            @Parameter(description = "当前状态") @RequestParam OrderStatus currentStatus,
            @Parameter(description = "目标状态") @RequestParam OrderStatus targetStatus) {
        boolean canTransit = orderStateService.canTransit(currentStatus, targetStatus);
        return ApiResponse.success(canTransit);
    }

    @GetMapping("/events")
    @Operation(summary = "获取所有可用的订单事件")
    public ApiResponse<List<OrderEvent>> getAllEvents() {
        List<OrderEvent> events = Arrays.asList(OrderEvent.values());
        return ApiResponse.success(events);
    }
}