package com.muyingmall.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.muyingmall.common.response.Result;
import com.muyingmall.dto.OrderCreateDTO;
import com.muyingmall.entity.Order;
import com.muyingmall.entity.User;
import com.muyingmall.service.OrderService;
import com.muyingmall.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 订单控制器
 */
@RestController
@RequestMapping("/order")
@RequiredArgsConstructor
@Tag(name = "订单管理", description = "订单创建、查询、取消等接口")
public class OrderController {

    private final OrderService orderService;
    private final UserService userService;

    /**
     * 创建订单
     */
    @PostMapping
    @Operation(summary = "创建订单")
    public Result<Map<String, Object>> createOrder(@RequestBody @Valid OrderCreateDTO orderCreateDTO) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return Result.error(401, "用户未认证");
        }

        String username = authentication.getName();
        User user = userService.findByUsername(username);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }

        Map<String, Object> orderInfo = orderService.createOrder(user.getUserId(), orderCreateDTO.getAddressId(),
                orderCreateDTO.getRemark());
        return Result.success(orderInfo, "创建成功");
    }

    /**
     * 获取订单列表
     */
    @GetMapping("/list")
    @Operation(summary = "获取订单列表")
    public Result<Page<Order>> getOrderList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String status) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return Result.error(401, "用户未认证");
        }

        String username = authentication.getName();
        User user = userService.findByUsername(username);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }

        Page<Order> orderPage = orderService.getUserOrders(user.getUserId(), page, pageSize, status);
        return Result.success(orderPage);
    }

    /**
     * 获取订单详情
     */
    @GetMapping("/{orderId}")
    @Operation(summary = "获取订单详情")
    public Result<Order> getOrderDetail(@PathVariable("orderId") Integer orderId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return Result.error(401, "用户未认证");
        }

        String username = authentication.getName();
        User user = userService.findByUsername(username);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }

        Order order = orderService.getOrderDetail(orderId, user.getUserId());
        if (order == null) {
            return Result.error("订单不存在");
        }
        return Result.success(order);
    }

    /**
     * 取消订单
     */
    @PutMapping("/{orderId}/cancel")
    @Operation(summary = "取消订单")
    public Result<Void> cancelOrder(
            @PathVariable("orderId") Integer orderId,
            @RequestParam(required = false) String reason) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return Result.error(401, "用户未认证");
        }

        String username = authentication.getName();
        User user = userService.findByUsername(username);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }

        boolean success = orderService.cancelOrder(orderId, user.getUserId());
        if (!success) {
            return Result.error("取消失败");
        }
        return Result.success(null, "取消成功");
    }

    /**
     * 确认收货
     */
    @PutMapping("/{orderId}/confirm")
    @Operation(summary = "确认收货")
    public Result<Void> confirmReceive(@PathVariable("orderId") Integer orderId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return Result.error(401, "用户未认证");
        }

        String username = authentication.getName();
        User user = userService.findByUsername(username);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }

        boolean success = orderService.confirmReceive(user.getUserId(), orderId);
        if (!success) {
            return Result.error("确认收货失败");
        }
        return Result.success(null, "确认收货成功");
    }

    /**
     * 删除订单（逻辑删除）
     */
    @DeleteMapping("/{orderId}")
    @Operation(summary = "删除订单")
    public Result<Void> deleteOrder(@PathVariable("orderId") Integer orderId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return Result.error(401, "用户未认证");
        }

        String username = authentication.getName();
        User user = userService.findByUsername(username);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }

        boolean success = orderService.updateOrderStatusByAdmin(orderId, "deleted", "用户删除");
        if (!success) {
            return Result.error("删除失败");
        }
        return Result.success(null, "删除成功");
    }
    
    /**
     * 支付订单
     */
    @PostMapping("/{orderId}/pay")
    @Operation(summary = "支付订单")
    public Result<Map<String, Object>> payOrder(
            @PathVariable("orderId") Integer orderId,
            @RequestParam(required = false) String paymentMethod) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return Result.error(401, "用户未认证");
        }

        String username = authentication.getName();
        User user = userService.findByUsername(username);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }

        try {
            Map<String, Object> paymentInfo = orderService.payOrder(user.getUserId(), orderId, paymentMethod);
            return Result.success(paymentInfo, "支付处理中");
        } catch (Exception e) {
            return Result.error("支付失败: " + e.getMessage());
        }
    }
}