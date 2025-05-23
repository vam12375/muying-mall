package com.muyingmall.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.muyingmall.common.result.Result;
import com.muyingmall.entity.Order;
import com.muyingmall.entity.Product;
import com.muyingmall.entity.User;
import com.muyingmall.service.OrderService;
import com.muyingmall.service.ProductService;
import com.muyingmall.service.RefundService;
import com.muyingmall.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 仪表盘数据控制器
 * 提供后台管理系统首页所需的统计数据
 */
@RestController
@RequestMapping("/admin/dashboard")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "仪表盘数据", description = "提供后台管理系统首页所需的统计数据")
public class DashboardController {

    private final UserService userService;
    private final ProductService productService;
    private final OrderService orderService;
    private final RefundService refundService;

    /**
     * 获取仪表盘统计数据
     */
    @GetMapping("/stats")
    @Operation(summary = "获取仪表盘统计数据")
    public Result<Map<String, Object>> getDashboardStats() {
        try {
            Map<String, Object> result = new HashMap<>();

            // 查询用户总数
            long userCount = userService.count();
            result.put("userCount", userCount);

            // 查询商品总数
            long productCount = productService.count();
            result.put("productCount", productCount);

            // 查询订单统计
            Map<String, Object> orderStats = orderService.getOrderStatistics(null);
            long orderCount = (long) orderStats.getOrDefault("totalCount", 0);
            result.put("orderCount", orderCount);

            // 计算总收入（已完成订单）
            BigDecimal totalIncome = calculateTotalIncome();
            result.put("totalIncome", totalIncome);

            // 查询退款统计
            Map<String, Object> refundStats = refundService.getRefundStatistics(null, null);
            result.put("refundStats", refundStats);

            return Result.success(result);
        } catch (Exception e) {
            log.error("获取仪表盘统计数据失败", e);
            return Result.error("获取仪表盘统计数据失败: " + e.getMessage());
        }
    }

    /**
     * 获取订单趋势数据
     */
    @GetMapping("/order-trend")
    @Operation(summary = "获取订单趋势数据")
    public Result<Map<String, Object>> getOrderTrend(
            @RequestParam(value = "days", defaultValue = "7") Integer days) {
        try {
            // 获取最近n天的日期列表
            List<LocalDate> dateList = new ArrayList<>();
            for (int i = days - 1; i >= 0; i--) {
                dateList.add(LocalDate.now().minusDays(i));
            }

            // 获取每天的订单数量
            List<Long> currentPeriodData = new ArrayList<>();
            List<Long> previousPeriodData = new ArrayList<>();

            // 格式化日期为展示格式
            List<String> formattedDates = dateList.stream()
                    .map(date -> date.format(DateTimeFormatter.ofPattern("MM-dd")))
                    .collect(Collectors.toList());

            // 查询当前周期订单数据
            for (LocalDate date : dateList) {
                LocalDateTime startOfDay = date.atStartOfDay();
                LocalDateTime endOfDay = date.plusDays(1).atStartOfDay().minusNanos(1);

                long count = orderService.count(new QueryWrapper<Order>()
                        .between("create_time", startOfDay, endOfDay));
                currentPeriodData.add(count);

                // 上一个周期同期数据
                LocalDateTime prevStartOfDay = date.minusDays(days).atStartOfDay();
                LocalDateTime prevEndOfDay = date.minusDays(days).plusDays(1).atStartOfDay().minusNanos(1);

                long prevCount = orderService.count(new QueryWrapper<Order>()
                        .between("create_time", prevStartOfDay, prevEndOfDay));
                previousPeriodData.add(prevCount);
            }

            Map<String, Object> result = new HashMap<>();
            result.put("dates", formattedDates);
            result.put("currentPeriod", currentPeriodData);
            result.put("previousPeriod", previousPeriodData);

            return Result.success(result);
        } catch (Exception e) {
            log.error("获取订单趋势数据失败", e);
            return Result.error("获取订单趋势数据失败: " + e.getMessage());
        }
    }

    /**
     * 获取商品分类数据
     */
    @GetMapping("/product-categories")
    @Operation(summary = "获取商品分类数据")
    public Result<List<Map<String, Object>>> getProductCategories() {
        try {
            // 查询所有商品分类及其商品数量
            List<Map<String, Object>> categorySales = productService.getCategorySales();

            return Result.success(categorySales);
        } catch (Exception e) {
            log.error("获取商品分类数据失败", e);
            return Result.error("获取商品分类数据失败: " + e.getMessage());
        }
    }

    /**
     * 获取月度销售额数据
     */
    @GetMapping("/monthly-sales")
    @Operation(summary = "获取月度销售额数据")
    public Result<Map<String, Object>> getMonthlySales(
            @RequestParam(value = "months", defaultValue = "6") Integer months) {
        try {
            // 获取最近n个月的销售数据
            List<LocalDate> monthList = new ArrayList<>();
            for (int i = months - 1; i >= 0; i--) {
                monthList.add(LocalDate.now().withDayOfMonth(1).minusMonths(i));
            }

            // 格式化月份为展示格式
            List<String> formattedMonths = monthList.stream()
                    .map(month -> month.format(DateTimeFormatter.ofPattern("yyyy-MM")))
                    .collect(Collectors.toList());

            // 查询每月销售额
            List<BigDecimal> salesData = new ArrayList<>();

            for (LocalDate month : monthList) {
                LocalDateTime startOfMonth = month.atStartOfDay();
                LocalDateTime endOfMonth = month.plusMonths(1).atStartOfDay().minusNanos(1);

                BigDecimal monthlySales = orderService.getSalesBetween(startOfMonth, endOfMonth);
                salesData.add(monthlySales);
            }

            Map<String, Object> result = new HashMap<>();
            result.put("months", formattedMonths);
            result.put("sales", salesData);

            return Result.success(result);
        } catch (Exception e) {
            log.error("获取月度销售额数据失败", e);
            return Result.error("获取月度销售额数据失败: " + e.getMessage());
        }
    }

    /**
     * 获取待处理事项数据
     */
    @GetMapping("/todo-items")
    @Operation(summary = "获取待处理事项数据")
    public Result<List<Map<String, Object>>> getTodoItems() {
        try {
            List<Map<String, Object>> todoItems = new ArrayList<>();

            // 待审核商品数量
            long pendingReviewProducts = productService.count(new QueryWrapper<Product>()
                    .eq("product_status", "下架")); // 使用正确的商品状态字段名
            Map<String, Object> pendingReview = new HashMap<>();
            pendingReview.put("title", "待审核商品");
            pendingReview.put("count", pendingReviewProducts);
            todoItems.add(pendingReview);

            // 待发货订单数量
            long pendingShipmentOrders = (long) orderService.getOrderStatistics(null)
                    .getOrDefault("pendingShipmentCount", 0);
            Map<String, Object> pendingShipment = new HashMap<>();
            pendingShipment.put("title", "待发货订单");
            pendingShipment.put("count", pendingShipmentOrders);
            todoItems.add(pendingShipment);

            // 待处理退款数量
            long pendingRefundsCount = refundService.getPendingRefundCount();
            Map<String, Object> pendingRefunds = new HashMap<>();
            pendingRefunds.put("title", "待处理退款");
            pendingRefunds.put("count", pendingRefundsCount);
            todoItems.add(pendingRefunds);

            // 库存预警商品数量
            long lowStockProducts = productService.count(new QueryWrapper<Product>()
                    .lt("stock", 10)); // 假设库存低于10为预警状态
            Map<String, Object> lowStock = new HashMap<>();
            lowStock.put("title", "库存预警商品");
            lowStock.put("count", lowStockProducts);
            todoItems.add(lowStock);

            return Result.success(todoItems);
        } catch (Exception e) {
            log.error("获取待处理事项数据失败", e);
            return Result.error("获取待处理事项数据失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户增长数据
     */
    @GetMapping("/user-growth")
    @Operation(summary = "获取用户增长数据")
    public Result<Map<String, Object>> getUserGrowth(
            @RequestParam(value = "months", defaultValue = "6") Integer months) {
        try {
            // 获取最近n个月的用户注册数据
            List<LocalDate> monthList = new ArrayList<>();
            for (int i = months - 1; i >= 0; i--) {
                monthList.add(LocalDate.now().withDayOfMonth(1).minusMonths(i));
            }

            // 格式化月份为展示格式
            List<String> formattedMonths = monthList.stream()
                    .map(month -> month.format(DateTimeFormatter.ofPattern("yyyy-MM")))
                    .collect(Collectors.toList());

            // 查询每月新增用户数
            List<Long> growthData = new ArrayList<>();

            for (LocalDate month : monthList) {
                LocalDateTime startOfMonth = month.atStartOfDay();
                LocalDateTime endOfMonth = month.plusMonths(1).atStartOfDay().minusNanos(1);

                long newUsers = userService.count(new QueryWrapper<User>()
                        .between("create_time", startOfMonth, endOfMonth));
                growthData.add(newUsers);
            }

            Map<String, Object> result = new HashMap<>();
            result.put("months", formattedMonths);
            result.put("growth", growthData);

            return Result.success(result);
        } catch (Exception e) {
            log.error("获取用户增长数据失败", e);
            return Result.error("获取用户增长数据失败: " + e.getMessage());
        }
    }

    /**
     * 获取退款趋势数据
     */
    @GetMapping("/refund-trend")
    @Operation(summary = "获取退款趋势数据")
    public Result<Map<String, Object>> getRefundTrend(
            @RequestParam(value = "days", defaultValue = "7") Integer days) {
        try {
            // 获取最近n天的日期列表
            List<LocalDate> dateList = new ArrayList<>();
            for (int i = days - 1; i >= 0; i--) {
                dateList.add(LocalDate.now().minusDays(i));
            }

            // 格式化日期为展示格式
            List<String> formattedDates = dateList.stream()
                    .map(date -> date.format(DateTimeFormatter.ofPattern("MM-dd")))
                    .collect(Collectors.toList());

            // 准备数据结构
            List<Long> refundCountData = new ArrayList<>();

            // 查询每天的退款数据
            for (LocalDate date : dateList) {
                LocalDateTime startOfDay = date.atStartOfDay();
                LocalDateTime endOfDay = date.plusDays(1).atStartOfDay().minusNanos(1);

                String startTime = startOfDay.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                String endTime = endOfDay.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

                Map<String, Object> refundStats = refundService.getRefundStatistics(startTime, endTime);
                long refundCount = (long) refundStats.getOrDefault("pendingCount", 0L) +
                        (long) refundStats.getOrDefault("approvedCount", 0L) +
                        (long) refundStats.getOrDefault("processingCount", 0L) +
                        (long) refundStats.getOrDefault("completedCount", 0L) +
                        (long) refundStats.getOrDefault("rejectedCount", 0L) +
                        (long) refundStats.getOrDefault("failedCount", 0L);

                refundCountData.add(refundCount);
            }

            Map<String, Object> result = new HashMap<>();
            result.put("dates", formattedDates);
            result.put("refundCounts", refundCountData);

            return Result.success(result);
        } catch (Exception e) {
            log.error("获取退款趋势数据失败", e);
            return Result.error("获取退款趋势数据失败: " + e.getMessage());
        }
    }

    /**
     * 计算总收入（已完成订单的总金额）
     */
    private BigDecimal calculateTotalIncome() {
        // 查询所有已完成订单
        QueryWrapper<Order> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", "completed"); // 假设status=completed表示已完成

        List<Order> completedOrders = orderService.list(queryWrapper);

        // 计算总金额
        return completedOrders.stream()
                .map(Order::getActualAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}