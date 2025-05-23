package com.muyingmall.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.muyingmall.common.response.Result;
import com.muyingmall.dto.OrderCreateDTO;
import com.muyingmall.dto.DirectPurchaseDTO;
import com.muyingmall.entity.Order;
import com.muyingmall.entity.OrderProduct;
import com.muyingmall.entity.User;
import com.muyingmall.enums.OrderStatus;
import com.muyingmall.mapper.OrderProductMapper;
import com.muyingmall.service.OrderService;
import com.muyingmall.service.UserService;
import com.muyingmall.util.EnumUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

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
    private final OrderProductMapper orderProductMapper;

    /**
     * 创建订单
     */
    @PostMapping("/create")
    @Operation(summary = "创建订单")
    public Result<Map<String, Object>> createOrder(@RequestBody @Valid OrderCreateDTO orderCreateDTO) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return Result.error(401, "用户未认证");
        }

        String username = authentication.getName();
        User user = userService.findByUsername(username);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }

        try {
            // 从DTO获取支付方式和优惠券ID
            String paymentMethod = orderCreateDTO.getPaymentMethod();
            Long couponId = orderCreateDTO.getCouponId();
            List<Integer> cartIds = orderCreateDTO.getCartIds();
            BigDecimal shippingFee = orderCreateDTO.getShippingFee();
            Integer pointsUsed = orderCreateDTO.getPointsUsed();

            // 打印订单创建参数，方便调试
            System.out.println("创建订单参数: userId=" + user.getUserId() +
                    ", addressId=" + orderCreateDTO.getAddressId() +
                    ", paymentMethod=" + paymentMethod +
                    ", couponId=" + couponId +
                    ", cartIds=" + cartIds +
                    ", shippingFee=" + shippingFee +
                    ", pointsUsed=" + pointsUsed);

            // 调用服务层创建订单，添加支付方式和优惠券ID
            Map<String, Object> orderInfo = orderService.createOrder(
                    user.getUserId(),
                    orderCreateDTO.getAddressId(),
                    orderCreateDTO.getRemark(),
                    orderCreateDTO.getPaymentMethod(),
                    orderCreateDTO.getCouponId(),
                    orderCreateDTO.getCartIds(),
                    orderCreateDTO.getShippingFee(),
                    pointsUsed);

            return Result.success(orderInfo, "创建成功");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("订单创建失败: " + e.getMessage());
        }
    }

    /**
     * 获取订单列表
     */
    @GetMapping("/list")
    @Operation(summary = "获取订单列表")
    public Result<Map<String, Object>> getOrderList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String status) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return Result.error(401, "用户未认证");
        }

        String username = authentication.getName();
        User user = userService.findByUsername(username);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }

        Page<Order> orderPage = orderService.getUserOrders(user.getUserId(), page, pageSize, status);

        // 将Page对象转换为前端需要的格式
        Map<String, Object> result = new HashMap<>();
        result.put("list", orderPage.getRecords());
        result.put("total", orderPage.getTotal());

        return Result.success(result);
    }

    /**
     * 获取订单详情
     */
    @GetMapping("/{orderId}")
    @Operation(summary = "获取订单详情")
    public Result<Order> getOrderDetail(@PathVariable("orderId") Integer orderId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
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
     * 可取消的订单状态: 待支付、待发货、已发货
     * 已完成、已取消、已退款等状态的订单不可取消
     */
    @PutMapping("/{orderId}/cancel")
    @Operation(summary = "取消订单", description = "取消订单，仅待支付、待发货、已发货状态的订单可以取消")
    public Result<Void> cancelOrder(
            @PathVariable("orderId") Integer orderId,
            @RequestParam(required = false) String reason) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
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
    @PutMapping("/{orderId}/receive")
    @Operation(summary = "确认收货")
    public Result<Void> confirmReceive(@PathVariable("orderId") Integer orderId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
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
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
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
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
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

    /**
     * 获取订单统计数据
     */
    @GetMapping("/stats")
    @Operation(summary = "获取订单统计")
    public Result<Map<String, Object>> getOrderStats() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return Result.error(401, "用户未认证");
        }

        String username = authentication.getName();
        User user = userService.findByUsername(username);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }

        try {
            Map<String, Object> statistics = orderService.getOrderStatistics(user.getUserId());

            // 将统计数据转换为前端需要的格式
            Map<String, Object> formattedStats = new HashMap<>();
            formattedStats.put("pendingPayment", statistics.get("pendingPaymentCount"));
            formattedStats.put("pendingShipment", statistics.get("pendingShipmentCount"));
            formattedStats.put("pendingReceive", statistics.get("shippedCount"));
            formattedStats.put("completed", statistics.get("completedCount"));
            formattedStats.put("cancelled", statistics.get("cancelledCount"));
            formattedStats.put("total", statistics.get("totalCount"));

            return Result.success(formattedStats);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取订单统计失败: " + e.getMessage());
        }
    }

    /**
     * 获取订单商品数据状态
     */
    @GetMapping("/products/status")
    @Operation(summary = "获取订单商品数据状态")
    public Result<Map<String, Object>> getOrderProductsStatus() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return Result.error(401, "用户未认证");
        }

        String username = authentication.getName();
        User user = userService.findByUsername(username);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }

        try {
            // 获取用户所有订单ID
            LambdaQueryWrapper<Order> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Order::getUserId, user.getUserId());
            List<Order> orders = orderService.list(queryWrapper);

            List<Integer> orderIds = orders.stream()
                    .map(Order::getOrderId)
                    .collect(Collectors.toList());

            // 统计有商品数据的订单数
            int ordersWithProducts = 0;
            int totalProducts = 0;

            if (!orderIds.isEmpty()) {
                for (Integer orderId : orderIds) {
                    LambdaQueryWrapper<OrderProduct> productQueryWrapper = new LambdaQueryWrapper<>();
                    productQueryWrapper.eq(OrderProduct::getOrderId, orderId);
                    int count = orderProductMapper.selectCount(productQueryWrapper).intValue();

                    if (count > 0) {
                        ordersWithProducts++;
                    }

                    totalProducts += count;
                }
            }

            Map<String, Object> result = new HashMap<>();
            result.put("totalOrders", orders.size());
            result.put("ordersWithProducts", ordersWithProducts);
            result.put("ordersWithoutProducts", orders.size() - ordersWithProducts);
            result.put("totalProducts", totalProducts);

            return Result.success(result);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取订单商品数据状态失败: " + e.getMessage());
        }
    }

    /**
     * 获取订单数据状态并尝试修复
     */
    @GetMapping("/debug/status")
    @Operation(summary = "获取订单数据状态并尝试修复（仅开发环境）")
    public Result<Map<String, Object>> getOrderDebugStatus() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return Result.error(401, "用户未认证");
        }

        String username = authentication.getName();
        User user = userService.findByUsername(username);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }

        try {
            // 获取用户所有订单
            LambdaQueryWrapper<Order> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Order::getUserId, user.getUserId());
            List<Order> orders = orderService.list(queryWrapper);

            // 检查并修复订单状态（确保全部小写）
            int fixedCount = 0;
            for (Order order : orders) {
                OrderStatus oldStatus = order.getStatus();
                if (oldStatus == null) {
                    order.setStatus(EnumUtil.getOrderStatusByCode("pending_payment"));
                    orderService.updateById(order);
                    fixedCount++;
                } else {
                    String statusCode = EnumUtil.getOrderStatusCode(oldStatus);
                    if (statusCode != null && !statusCode.equals(statusCode.toLowerCase())) {
                        order.setStatus(EnumUtil.getOrderStatusByCode(statusCode.toLowerCase()));
                        orderService.updateById(order);
                        fixedCount++;
                    }
                }
            }

            // 检查是否每个订单都有对应的商品
            int ordersWithoutProducts = 0;
            for (Order order : orders) {
                LambdaQueryWrapper<OrderProduct> productQueryWrapper = new LambdaQueryWrapper<>();
                productQueryWrapper.eq(OrderProduct::getOrderId, order.getOrderId());
                int count = orderProductMapper.selectCount(productQueryWrapper).intValue();

                if (count == 0) {
                    ordersWithoutProducts++;
                }
            }

            Map<String, Object> result = new HashMap<>();
            result.put("totalOrders", orders.size());
            result.put("fixedStatusCount", fixedCount);
            result.put("ordersWithoutProducts", ordersWithoutProducts);

            // 统计各种状态的订单数
            Map<String, Integer> statusCounts = new HashMap<>();
            for (Order order : orders) {
                OrderStatus status = order.getStatus();
                String statusCode = EnumUtil.getOrderStatusCode(status);
                statusCounts.put(statusCode, statusCounts.getOrDefault(statusCode, 0) + 1);
            }
            result.put("statusCounts", statusCounts);

            return Result.success(result, "诊断完成");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("诊断失败: " + e.getMessage());
        }
    }

    /**
     * 直接购买商品（不添加到购物车）
     */
    @PostMapping("/direct-purchase")
    @Operation(summary = "直接购买商品")
    public Result<Map<String, Object>> directPurchase(@RequestBody @Valid DirectPurchaseDTO purchaseDTO) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return Result.error(401, "用户未认证");
        }

        String username = authentication.getName();
        User user = userService.findByUsername(username);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }

        try {
            // 打印直接购买请求参数，方便调试
            System.out.println("直接购买参数: userId=" + user.getUserId() +
                    ", addressId=" + purchaseDTO.getAddressId() +
                    ", productId=" + purchaseDTO.getProductId() +
                    ", quantity=" + purchaseDTO.getQuantity() +
                    ", specs=" + purchaseDTO.getSpecs() +
                    ", paymentMethod=" + purchaseDTO.getPaymentMethod() +
                    ", couponId=" + purchaseDTO.getCouponId() +
                    ", shippingFee=" + purchaseDTO.getShippingFee() +
                    ", pointsUsed=" + purchaseDTO.getPointsUsed());

            // 调用服务层创建订单
            Map<String, Object> orderInfo = orderService.directPurchase(
                    user.getUserId(),
                    purchaseDTO.getAddressId(),
                    purchaseDTO.getProductId(),
                    purchaseDTO.getQuantity(),
                    purchaseDTO.getSpecs(),
                    purchaseDTO.getRemark(),
                    purchaseDTO.getPaymentMethod(),
                    purchaseDTO.getCouponId(),
                    purchaseDTO.getShippingFee(),
                    purchaseDTO.getPointsUsed());

            return Result.success(orderInfo, "创建成功");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("直接购买失败: " + e.getMessage());
        }
    }
}