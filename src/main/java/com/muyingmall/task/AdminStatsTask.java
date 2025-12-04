package com.muyingmall.task;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.muyingmall.entity.Order;
import com.muyingmall.service.AdminLoginRecordService;
import com.muyingmall.service.AdminOperationLogService;
import com.muyingmall.service.OrderService;
import com.muyingmall.service.ProductService;
import com.muyingmall.service.RefundService;
import com.muyingmall.service.UserService;
import com.muyingmall.websocket.AdminStatsWebSocket;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 管理员统计数据定时任务
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AdminStatsTask {

    private final AdminLoginRecordService loginRecordService;
    private final AdminOperationLogService operationLogService;
    private final UserService userService;
    private final ProductService productService;
    private final OrderService orderService;
    private final RefundService refundService;

    /**
     * 每30秒推送一次统计数据
     * 推送业务核心统计数据：用户数、订单数、商品数、总收入
     */
    @Scheduled(fixedRate = 30000)
    public void pushStatsData() {
        try {
            // 检查是否有在线的管理员
            if (AdminStatsWebSocket.getOnlineCount() == 0) {
                return;
            }

            log.debug("开始推送统计数据，当前在线管理员数量：{}", AdminStatsWebSocket.getOnlineCount());

            // 获取业务核心统计数据（与DashboardController.getDashboardStats()保持一致）
            Map<String, Object> stats = new HashMap<>();
            
            // 查询用户总数
            long userCount = userService.count();
            stats.put("userCount", userCount);

            // 查询商品总数
            long productCount = productService.count();
            stats.put("productCount", productCount);

            // 查询订单统计
            Map<String, Object> orderStats = orderService.getOrderStatistics(null);
            long orderCount = (long) orderStats.getOrDefault("totalCount", 0);
            stats.put("orderCount", orderCount);

            // 计算总收入（已完成订单）
            BigDecimal totalIncome = calculateTotalIncome();
            stats.put("totalIncome", totalIncome);

            // 查询退款统计
            Map<String, Object> refundStats = refundService.getRefundStatistics(null, null);
            stats.put("refundStats", refundStats);

            // 添加额外的管理员相关统计
            Map<String, Object> loginStats = loginRecordService.getLoginStatistics(null, 30);
            stats.put("loginStats", loginStats);

            Map<String, Object> operationStats = operationLogService.getOperationStatistics(null, 30);
            stats.put("operationStats", operationStats);

            // 添加当前在线管理员数量
            stats.put("onlineAdmins", AdminStatsWebSocket.getOnlineCount());
            stats.put("updateTime", System.currentTimeMillis());

            // 广播统计数据到所有在线管理员
            AdminStatsWebSocket.broadcastStats(stats);

            log.debug("统计数据推送成功: userCount={}, orderCount={}, productCount={}, totalIncome={}", 
                    userCount, orderCount, productCount, totalIncome);

        } catch (Exception e) {
            log.error("推送统计数据失败", e);
        }
    }

    /**
     * 计算总收入（已完成订单的总金额）
     */
    private BigDecimal calculateTotalIncome() {
        try {
            // 查询所有已完成订单
            QueryWrapper<Order> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("status", "completed");

            List<Order> completedOrders = orderService.list(queryWrapper);

            // 计算总金额
            return completedOrders.stream()
                    .map(Order::getActualAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        } catch (Exception e) {
            log.error("计算总收入失败", e);
            return BigDecimal.ZERO;
        }
    }

    /**
     * 每5分钟推送一次详细统计数据
     */
    @Scheduled(fixedRate = 300000)
    public void pushDetailedStats() {
        try {
            // 检查是否有在线的管理员
            if (AdminStatsWebSocket.getOnlineCount() == 0) {
                return;
            }

            log.debug("开始推送详细统计数据");

            // 获取详细统计数据
            Map<String, Object> detailedStats = new HashMap<>();
            
            // 获取各种时间段的统计
            detailedStats.put("todayStats", getStatsForPeriod(1));
            detailedStats.put("weekStats", getStatsForPeriod(7));
            detailedStats.put("monthStats", getStatsForPeriod(30));

            // 获取模块操作统计
            detailedStats.put("moduleStats", operationLogService.getModuleOperationStats(null, 30));

            // 推送详细统计数据
            Map<String, Object> message = Map.of(
                "type", "detailedStats",
                "data", detailedStats,
                "timestamp", System.currentTimeMillis()
            );

            AdminStatsWebSocket.broadcast(
                new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(message)
            );

        } catch (Exception e) {
            log.error("推送详细统计数据失败", e);
        }
    }

    /**
     * 获取指定时间段的统计数据
     */
    private Map<String, Object> getStatsForPeriod(int days) {
        Map<String, Object> stats = new HashMap<>();
        
        // 登录统计
        Map<String, Object> loginStats = loginRecordService.getLoginStatistics(null, days);
        stats.put("login", loginStats);
        
        // 操作统计
        Map<String, Object> operationStats = operationLogService.getOperationStatistics(null, days);
        stats.put("operation", operationStats);
        
        return stats;
    }

    /**
     * 每小时清理一次离线连接
     */
    @Scheduled(fixedRate = 3600000)
    public void cleanupConnections() {
        try {
            int onlineCount = AdminStatsWebSocket.getOnlineCount();
            log.info("当前WebSocket连接数：{}，在线管理员：{}", 
                    onlineCount, AdminStatsWebSocket.getWebSocketMap().keySet());
        } catch (Exception e) {
            log.error("清理连接失败", e);
        }
    }
}
