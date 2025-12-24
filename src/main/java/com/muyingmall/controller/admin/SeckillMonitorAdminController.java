package com.muyingmall.controller.admin;

import com.muyingmall.annotation.AdminOperationLog;
import com.muyingmall.common.api.Result;
import com.muyingmall.service.SeckillActivityService;
import com.muyingmall.service.SeckillOrderService;
import com.muyingmall.service.SeckillProductService;
import com.muyingmall.service.SeckillService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 秒杀监控统计控制器（管理员权限）
 * 
 * @author MuyingMall
 */
@Slf4j
@RestController
@RequestMapping("/admin/seckill/monitor")
@RequiredArgsConstructor
@Tag(name = "秒杀监控统计", description = "秒杀监控统计相关接口，仅管理员可访问")
public class SeckillMonitorAdminController {

    private final SeckillActivityService seckillActivityService;
    private final SeckillProductService seckillProductService;
    private final SeckillOrderService seckillOrderService;
    private final SeckillService seckillService;
    private final StringRedisTemplate redisTemplate;

    /**
     * 获取秒杀系统总览数据
     */
    @GetMapping("/overview")
    @Operation(summary = "获取秒杀系统总览数据")
    @PreAuthorize("hasAuthority('admin')")
    @AdminOperationLog(operation = "查看秒杀系统总览", module = "秒杀管理", operationType = "READ")
    public Result<Map<String, Object>> getSystemOverview() {
        log.debug("查询秒杀系统总览数据");
        
        try {
            Map<String, Object> overview = new HashMap<>();
            
            // 活动统计
            long totalActivities = seckillActivityService.count();
            long ongoingActivities = seckillActivityService.countByStatus(1);
            long upcomingActivities = seckillActivityService.countByStatus(0);
            
            overview.put("totalActivities", totalActivities);
            overview.put("ongoingActivities", ongoingActivities);
            overview.put("upcomingActivities", upcomingActivities);
            
            // 商品统计
            long totalProducts = seckillProductService.count();
            overview.put("totalProducts", totalProducts);
            
            // 订单统计（最近7天）
            LocalDateTime startTime = LocalDateTime.now().minusDays(7);
            long recentOrders = seckillOrderService.countByTimeRange(startTime, null);
            long successOrders = seckillOrderService.countByTimeRangeAndStatus(startTime, null, "SUCCESS");
            java.math.BigDecimal recentSales = seckillOrderService.sumAmountByTimeRange(startTime, null);
            
            overview.put("recentOrders", recentOrders);
            overview.put("successOrders", successOrders);
            overview.put("recentSales", recentSales != null ? recentSales : java.math.BigDecimal.ZERO);
            
            // 成功率
            double successRate = recentOrders > 0 ? (double) successOrders / recentOrders * 100 : 0;
            overview.put("successRate", String.format("%.2f", successRate));
            
            log.debug("查询秒杀系统总览成功");
            return Result.success(overview);
        } catch (Exception e) {
            log.error("查询秒杀系统总览失败", e);
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 获取实时活动状态
     */
    @GetMapping("/activities/realtime")
    @Operation(summary = "获取实时活动状态")
    @PreAuthorize("hasAuthority('admin')")
    @AdminOperationLog(operation = "查看实时活动状态", module = "秒杀管理", operationType = "READ")
    public Result<List<Map<String, Object>>> getRealtimeActivityStatus() {
        log.debug("查询实时活动状态");
        
        try {
            List<Map<String, Object>> activityStatusList = new ArrayList<>();
            
            // 获取所有进行中的活动
            List<com.muyingmall.entity.SeckillActivity> ongoingActivities = 
                    seckillActivityService.getActivitiesByStatus(1);
            
            for (com.muyingmall.entity.SeckillActivity activity : ongoingActivities) {
                Map<String, Object> status = new HashMap<>();
                status.put("activityId", activity.getId());
                status.put("activityName", activity.getName());
                status.put("startTime", activity.getStartTime());
                status.put("endTime", activity.getEndTime());
                
                // 获取活动下的商品数量
                long productCount = seckillProductService.countByActivityId(activity.getId());
                status.put("productCount", productCount);
                
                // 获取订单统计
                long orderCount = seckillOrderService.countByActivityId(activity.getId());
                long successCount = seckillOrderService.countByActivityIdAndStatus(activity.getId(), "SUCCESS");
                java.math.BigDecimal salesAmount = seckillOrderService.sumAmountByActivityId(activity.getId());
                
                status.put("orderCount", orderCount);
                status.put("successCount", successCount);
                status.put("salesAmount", salesAmount != null ? salesAmount : java.math.BigDecimal.ZERO);
                
                activityStatusList.add(status);
            }
            
            log.debug("查询实时活动状态成功，共 {} 个活动", activityStatusList.size());
            return Result.success(activityStatusList);
        } catch (Exception e) {
            log.error("查询实时活动状态失败", e);
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 获取库存预警列表
     */
    @GetMapping("/stock/warning")
    @Operation(summary = "获取库存预警列表")
    @PreAuthorize("hasAuthority('admin')")
    @AdminOperationLog(operation = "查看库存预警", module = "秒杀管理", operationType = "READ")
    public Result<List<Map<String, Object>>> getStockWarning(
            @Parameter(description = "预警阈值百分比") @RequestParam(defaultValue = "20") int threshold) {
        
        log.debug("查询库存预警列表 - threshold: {}%", threshold);
        
        try {
            List<Map<String, Object>> warningList = seckillProductService.getStockWarningList(threshold);
            log.debug("查询库存预警成功，共 {} 个商品", warningList.size());
            return Result.success(warningList);
        } catch (Exception e) {
            log.error("查询库存预警失败", e);
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 获取热门商品排行
     */
    @GetMapping("/products/hot")
    @Operation(summary = "获取热门商品排行")
    @PreAuthorize("hasAuthority('admin')")
    @AdminOperationLog(operation = "查看热门商品排行", module = "秒杀管理", operationType = "READ")
    public Result<List<Map<String, Object>>> getHotProducts(
            @Parameter(description = "排行数量") @RequestParam(defaultValue = "10") int limit,
            @Parameter(description = "统计天数") @RequestParam(defaultValue = "7") int days) {
        
        log.debug("查询热门商品排行 - limit: {}, days: {}", limit, days);
        
        try {
            LocalDateTime startTime = LocalDateTime.now().minusDays(days);
            List<Map<String, Object>> hotProducts = seckillProductService.getHotProductsRanking(limit, startTime);
            
            log.debug("查询热门商品排行成功，共 {} 个商品", hotProducts.size());
            return Result.success(hotProducts);
        } catch (Exception e) {
            log.error("查询热门商品排行失败", e);
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 获取销售趋势数据
     */
    @GetMapping("/sales/trend")
    @Operation(summary = "获取销售趋势数据")
    @PreAuthorize("hasAuthority('admin')")
    @AdminOperationLog(operation = "查看销售趋势", module = "秒杀管理", operationType = "READ")
    public Result<Map<String, Object>> getSalesTrend(
            @Parameter(description = "统计天数") @RequestParam(defaultValue = "7") int days) {
        
        log.debug("查询销售趋势数据 - days: {}", days);
        
        try {
            Map<String, Object> trend = seckillOrderService.getSalesTrend(days);
            log.debug("查询销售趋势成功");
            return Result.success(trend);
        } catch (Exception e) {
            log.error("查询销售趋势失败", e);
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 获取用户参与度统计
     */
    @GetMapping("/users/participation")
    @Operation(summary = "获取用户参与度统计")
    @PreAuthorize("hasAuthority('admin')")
    @AdminOperationLog(operation = "查看用户参与度", module = "秒杀管理", operationType = "READ")
    public Result<Map<String, Object>> getUserParticipation(
            @Parameter(description = "统计天数") @RequestParam(defaultValue = "7") int days) {
        
        log.debug("查询用户参与度统计 - days: {}", days);
        
        try {
            LocalDateTime startTime = LocalDateTime.now().minusDays(days);
            Map<String, Object> participation = new HashMap<>();
            
            // 参与用户数
            long participantCount = seckillOrderService.countDistinctUsersByTimeRange(startTime, null);
            participation.put("participantCount", participantCount);
            
            // 人均订单数
            long totalOrders = seckillOrderService.countByTimeRange(startTime, null);
            double avgOrdersPerUser = participantCount > 0 ? (double) totalOrders / participantCount : 0;
            participation.put("avgOrdersPerUser", String.format("%.2f", avgOrdersPerUser));
            
            // 人均消费金额
            java.math.BigDecimal totalAmount = seckillOrderService.sumAmountByTimeRange(startTime, null);
            double avgAmountPerUser = participantCount > 0 && totalAmount != null ? 
                    totalAmount.doubleValue() / participantCount : 0;
            participation.put("avgAmountPerUser", String.format("%.2f", avgAmountPerUser));
            
            log.debug("查询用户参与度成功");
            return Result.success(participation);
        } catch (Exception e) {
            log.error("查询用户参与度失败", e);
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 获取Redis缓存状态
     */
    @GetMapping("/redis/status")
    @Operation(summary = "获取Redis缓存状态")
    @PreAuthorize("hasAuthority('admin')")
    @AdminOperationLog(operation = "查看Redis状态", module = "秒杀管理", operationType = "READ")
    public Result<Map<String, Object>> getRedisStatus() {
        log.debug("查询Redis缓存状态");
        
        try {
            Map<String, Object> status = new HashMap<>();
            
            // 获取秒杀相关的Redis key数量
            java.util.Set<String> stockKeys = redisTemplate.keys("seckill:stock:*");
            java.util.Set<String> orderKeys = redisTemplate.keys("seckill:order:*");
            java.util.Set<String> userKeys = redisTemplate.keys("seckill:user:*");
            
            status.put("stockKeyCount", stockKeys != null ? stockKeys.size() : 0);
            status.put("orderKeyCount", orderKeys != null ? orderKeys.size() : 0);
            status.put("userKeyCount", userKeys != null ? userKeys.size() : 0);
            
            log.debug("查询Redis缓存状态成功");
            return Result.success(status);
        } catch (Exception e) {
            log.error("查询Redis缓存状态失败", e);
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 清理Redis缓存
     */
    @DeleteMapping("/redis/clear")
    @Operation(summary = "清理Redis缓存")
    @PreAuthorize("hasAuthority('admin')")
    @AdminOperationLog(operation = "清理Redis缓存", module = "秒杀管理", operationType = "DELETE")
    public Result<Map<String, Object>> clearRedisCache(
            @Parameter(description = "缓存类型：stock-库存，order-订单，user-用户，all-全部") 
            @RequestParam(defaultValue = "all") String type) {
        
        log.debug("清理Redis缓存 - type: {}", type);
        
        try {
            int clearedCount = 0;
            
            switch (type) {
                case "stock":
                    java.util.Set<String> stockKeys = redisTemplate.keys("seckill:stock:*");
                    if (stockKeys != null && !stockKeys.isEmpty()) {
                        clearedCount = stockKeys.size();
                        redisTemplate.delete(stockKeys);
                    }
                    break;
                case "order":
                    java.util.Set<String> orderKeys = redisTemplate.keys("seckill:order:*");
                    if (orderKeys != null && !orderKeys.isEmpty()) {
                        clearedCount = orderKeys.size();
                        redisTemplate.delete(orderKeys);
                    }
                    break;
                case "user":
                    java.util.Set<String> userKeys = redisTemplate.keys("seckill:user:*");
                    if (userKeys != null && !userKeys.isEmpty()) {
                        clearedCount = userKeys.size();
                        redisTemplate.delete(userKeys);
                    }
                    break;
                case "all":
                    java.util.Set<String> allKeys = redisTemplate.keys("seckill:*");
                    if (allKeys != null && !allKeys.isEmpty()) {
                        clearedCount = allKeys.size();
                        redisTemplate.delete(allKeys);
                    }
                    break;
                default:
                    return Result.error("无效的缓存类型");
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("clearedCount", clearedCount);
            result.put("type", type);
            
            log.info("清理Redis缓存成功 - type: {}, count: {}", type, clearedCount);
            return Result.success(result, "清理成功");
        } catch (Exception e) {
            log.error("清理Redis缓存失败", e);
            return Result.error("清理失败：" + e.getMessage());
        }
    }

    /**
     * 刷新活动状态
     */
    @PostMapping("/activities/refresh")
    @Operation(summary = "刷新活动状态")
    @PreAuthorize("hasAuthority('admin')")
    @AdminOperationLog(operation = "刷新活动状态", module = "秒杀管理", operationType = "UPDATE")
    public Result<Map<String, Object>> refreshActivityStatus() {
        log.debug("刷新活动状态");
        
        try {
            int updatedCount = seckillActivityService.updateActivityStatus();
            
            Map<String, Object> result = new HashMap<>();
            result.put("updatedCount", updatedCount);
            
            log.info("刷新活动状态成功 - 更新数量: {}", updatedCount);
            return Result.success(result, "刷新成功");
        } catch (Exception e) {
            log.error("刷新活动状态失败", e);
            return Result.error("刷新失败：" + e.getMessage());
        }
    }

    /**
     * 同步Redis库存到数据库
     */
    @PostMapping("/stock/sync")
    @Operation(summary = "同步Redis库存到数据库")
    @PreAuthorize("hasAuthority('admin')")
    @AdminOperationLog(operation = "同步库存", module = "秒杀管理", operationType = "UPDATE")
    public Result<Map<String, Object>> syncStockToDatabase() {
        log.debug("同步Redis库存到数据库");
        
        try {
            int syncedCount = seckillService.syncRedisStockToDatabase();
            
            Map<String, Object> result = new HashMap<>();
            result.put("syncedCount", syncedCount);
            
            log.info("同步库存成功 - 同步数量: {}", syncedCount);
            return Result.success(result, "同步成功");
        } catch (Exception e) {
            log.error("同步库存失败", e);
            return Result.error("同步失败：" + e.getMessage());
        }
    }
}
