package com.muyingmall.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.muyingmall.annotation.AdminOperationLog;
import com.muyingmall.common.api.Result;
import com.muyingmall.dto.SeckillOrderDTO;
import com.muyingmall.entity.SeckillOrder;
import com.muyingmall.service.SeckillOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 秒杀订单管理控制器（管理员权限）
 * 
 * @author MuyingMall
 */
@Slf4j
@RestController
@RequestMapping("/admin/seckill/orders")
@RequiredArgsConstructor
@Tag(name = "秒杀订单管理", description = "秒杀订单管理相关接口，仅管理员可访问")
public class SeckillOrderAdminController {

    private final SeckillOrderService seckillOrderService;

    /**
     * 分页获取秒杀订单列表
     */
    @GetMapping("/page")
    @Operation(summary = "分页获取秒杀订单列表")
    @PreAuthorize("hasAuthority('admin')")
    @AdminOperationLog(operation = "查询秒杀订单列表", module = "秒杀管理", operationType = "READ")
    public Result<IPage<SeckillOrderDTO>> getOrderPage(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "活动ID") @RequestParam(required = false) Long activityId,
            @Parameter(description = "用户ID") @RequestParam(required = false) Integer userId,
            @Parameter(description = "订单状态") @RequestParam(required = false) String status,
            @Parameter(description = "开始时间") @RequestParam(required = false) String startTime,
            @Parameter(description = "结束时间") @RequestParam(required = false) String endTime) {
        
        log.debug("查询秒杀订单列表 - page: {}, size: {}, activityId: {}, userId: {}, status: {}", 
                page, size, activityId, userId, status);
        
        try {
            // 解析时间参数
            LocalDateTime startDateTime = null;
            LocalDateTime endDateTime = null;
            
            if (startTime != null && !startTime.isEmpty()) {
                startDateTime = LocalDateTime.parse(startTime + "T00:00:00");
            }
            if (endTime != null && !endTime.isEmpty()) {
                endDateTime = LocalDateTime.parse(endTime + "T23:59:59");
            }
            
            Page<SeckillOrderDTO> pageParam = new Page<>(page, size);
            IPage<SeckillOrderDTO> orderPage = seckillOrderService.getOrderPage(
                    pageParam, activityId, userId, status, startDateTime, endDateTime);
            
            log.debug("查询成功，共 {} 条记录", orderPage.getTotal());
            return Result.success(orderPage);
        } catch (Exception e) {
            log.error("查询秒杀订单列表失败", e);
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 获取秒杀订单详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取秒杀订单详情")
    @PreAuthorize("hasAuthority('admin')")
    @AdminOperationLog(operation = "查看秒杀订单详情", module = "秒杀管理", operationType = "READ")
    public Result<SeckillOrderDTO> getOrderById(@PathVariable Long id) {
        log.debug("查询秒杀订单详情 - id: {}", id);
        
        try {
            SeckillOrderDTO order = seckillOrderService.getOrderDetailById(id);
            if (order == null) {
                return Result.error(404, "订单不存在");
            }
            return Result.success(order);
        } catch (Exception e) {
            log.error("查询秒杀订单详情失败 - id: {}", id, e);
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 根据活动ID获取订单统计
     */
    @GetMapping("/activity/{activityId}/statistics")
    @Operation(summary = "根据活动ID获取订单统计")
    @PreAuthorize("hasAuthority('admin')")
    @AdminOperationLog(operation = "查看活动订单统计", module = "秒杀管理", operationType = "READ")
    public Result<Map<String, Object>> getOrderStatisticsByActivity(@PathVariable Long activityId) {
        log.debug("查询活动订单统计 - activityId: {}", activityId);
        
        try {
            Map<String, Object> statistics = new HashMap<>();
            
            // 总订单数
            long totalOrders = seckillOrderService.countByActivityId(activityId);
            statistics.put("totalOrders", totalOrders);
            
            // 成功订单数
            long successOrders = seckillOrderService.countByActivityIdAndStatus(activityId, "SUCCESS");
            statistics.put("successOrders", successOrders);
            
            // 待支付订单数
            long pendingOrders = seckillOrderService.countByActivityIdAndStatus(activityId, "PENDING");
            statistics.put("pendingOrders", pendingOrders);
            
            // 已取消订单数
            long cancelledOrders = seckillOrderService.countByActivityIdAndStatus(activityId, "CANCELLED");
            statistics.put("cancelledOrders", cancelledOrders);
            
            // 总销售额
            java.math.BigDecimal totalAmount = seckillOrderService.sumAmountByActivityId(activityId);
            statistics.put("totalAmount", totalAmount != null ? totalAmount : java.math.BigDecimal.ZERO);
            
            log.debug("查询活动订单统计成功 - activityId: {}, totalOrders: {}", activityId, totalOrders);
            return Result.success(statistics);
        } catch (Exception e) {
            log.error("查询活动订单统计失败 - activityId: {}", activityId, e);
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 根据商品ID获取订单统计
     */
    @GetMapping("/product/{productId}/statistics")
    @Operation(summary = "根据商品ID获取订单统计")
    @PreAuthorize("hasAuthority('admin')")
    @AdminOperationLog(operation = "查看商品订单统计", module = "秒杀管理", operationType = "READ")
    public Result<Map<String, Object>> getOrderStatisticsByProduct(@PathVariable Long productId) {
        log.debug("查询商品订单统计 - productId: {}", productId);
        
        try {
            Map<String, Object> statistics = new HashMap<>();
            
            // 总订单数
            long totalOrders = seckillOrderService.countByProductId(productId);
            statistics.put("totalOrders", totalOrders);
            
            // 成功订单数
            long successOrders = seckillOrderService.countByProductIdAndStatus(productId, "SUCCESS");
            statistics.put("successOrders", successOrders);
            
            // 总销售数量
            Integer totalQuantity = seckillOrderService.sumQuantityByProductId(productId);
            statistics.put("totalQuantity", totalQuantity != null ? totalQuantity : 0);
            
            // 总销售额
            java.math.BigDecimal totalAmount = seckillOrderService.sumAmountByProductId(productId);
            statistics.put("totalAmount", totalAmount != null ? totalAmount : java.math.BigDecimal.ZERO);
            
            log.debug("查询商品订单统计成功 - productId: {}, totalOrders: {}", productId, totalOrders);
            return Result.success(statistics);
        } catch (Exception e) {
            log.error("查询商品订单统计失败 - productId: {}", productId, e);
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 获取秒杀订单总体统计
     */
    @GetMapping("/statistics")
    @Operation(summary = "获取秒杀订单总体统计")
    @PreAuthorize("hasAuthority('admin')")
    @AdminOperationLog(operation = "查看秒杀订单统计", module = "秒杀管理", operationType = "READ")
    public Result<Map<String, Object>> getOrderStatistics(
            @Parameter(description = "统计天数") @RequestParam(defaultValue = "7") int days) {
        
        log.debug("查询秒杀订单总体统计 - days: {}", days);
        
        try {
            Map<String, Object> statistics = new HashMap<>();
            
            // 计算时间范围
            LocalDateTime startTime = LocalDateTime.now().minusDays(days);
            
            // 总订单数
            long totalOrders = seckillOrderService.countByTimeRange(startTime, null);
            statistics.put("totalOrders", totalOrders);
            
            // 成功订单数
            long successOrders = seckillOrderService.countByTimeRangeAndStatus(startTime, null, "SUCCESS");
            statistics.put("successOrders", successOrders);
            
            // 待支付订单数
            long pendingOrders = seckillOrderService.countByTimeRangeAndStatus(startTime, null, "PENDING");
            statistics.put("pendingOrders", pendingOrders);
            
            // 已取消订单数
            long cancelledOrders = seckillOrderService.countByTimeRangeAndStatus(startTime, null, "CANCELLED");
            statistics.put("cancelledOrders", cancelledOrders);
            
            // 总销售额
            java.math.BigDecimal totalAmount = seckillOrderService.sumAmountByTimeRange(startTime, null);
            statistics.put("totalAmount", totalAmount != null ? totalAmount : java.math.BigDecimal.ZERO);
            
            // 成功率
            double successRate = totalOrders > 0 ? (double) successOrders / totalOrders * 100 : 0;
            statistics.put("successRate", String.format("%.2f", successRate));
            
            log.debug("查询秒杀订单总体统计成功 - totalOrders: {}, successOrders: {}", totalOrders, successOrders);
            return Result.success(statistics);
        } catch (Exception e) {
            log.error("查询秒杀订单总体统计失败", e);
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 导出秒杀订单
     */
    @GetMapping("/export")
    @Operation(summary = "导出秒杀订单")
    @PreAuthorize("hasAuthority('admin')")
    @AdminOperationLog(operation = "导出秒杀订单", module = "秒杀管理", operationType = "EXPORT")
    public Result<String> exportOrders(
            @Parameter(description = "活动ID") @RequestParam(required = false) Long activityId,
            @Parameter(description = "开始时间") @RequestParam(required = false) String startTime,
            @Parameter(description = "结束时间") @RequestParam(required = false) String endTime) {
        
        log.debug("导出秒杀订单 - activityId: {}, startTime: {}, endTime: {}", activityId, startTime, endTime);
        
        try {
            // 解析时间参数
            LocalDateTime startDateTime = null;
            LocalDateTime endDateTime = null;
            
            if (startTime != null && !startTime.isEmpty()) {
                startDateTime = LocalDateTime.parse(startTime + "T00:00:00");
            }
            if (endTime != null && !endTime.isEmpty()) {
                endDateTime = LocalDateTime.parse(endTime + "T23:59:59");
            }
            
            // TODO: 实现导出逻辑
            // String filePath = seckillOrderService.exportOrders(activityId, startDateTime, endDateTime);
            
            log.info("导出秒杀订单成功");
            return Result.success("", "导出功能开发中");
        } catch (Exception e) {
            log.error("导出秒杀订单失败", e);
            return Result.error("导出失败：" + e.getMessage());
        }
    }
}
