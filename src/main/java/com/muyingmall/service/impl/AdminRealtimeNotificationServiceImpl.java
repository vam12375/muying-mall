package com.muyingmall.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.muyingmall.entity.AdminLoginRecord;
import com.muyingmall.entity.AdminOperationLog;
import com.muyingmall.service.AdminLoginRecordService;
import com.muyingmall.service.AdminOperationLogService;
import com.muyingmall.service.AdminRealtimeNotificationService;
import com.muyingmall.websocket.AdminStatsWebSocket;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 管理员实时通知服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminRealtimeNotificationServiceImpl implements AdminRealtimeNotificationService {

    private final AdminLoginRecordService loginRecordService;
    private final AdminOperationLogService operationLogService;
    private final ObjectMapper objectMapper;

    @Override
    public void notifyNewLoginRecord(AdminLoginRecord loginRecord) {
        try {
            // 推送给所有在线管理员
            AdminStatsWebSocket.pushLoginRecordToAdmin("all", loginRecord);
            
            // 同时更新统计数据
            notifyStatsUpdate(null);
            
            log.debug("推送新登录记录：{}", loginRecord.getAdminName());
        } catch (Exception e) {
            log.error("推送登录记录失败", e);
        }
    }

    @Override
    public void notifyNewOperationLog(AdminOperationLog operationLog) {
        try {
            // 推送给所有在线管理员
            AdminStatsWebSocket.pushOperationLogToAdmin("all", operationLog);
            
            // 同时更新统计数据
            notifyStatsUpdate(null);
            
            log.debug("推送新操作日志：{} - {}", operationLog.getAdminName(), operationLog.getOperation());
        } catch (Exception e) {
            log.error("推送操作日志失败", e);
        }
    }

    @Override
    public void notifyStatsUpdate(Integer adminId) {
        try {
            // 获取最新统计数据
            Map<String, Object> stats = new HashMap<>();
            
            // 获取登录统计
            Map<String, Object> loginStats = loginRecordService.getLoginStatistics(adminId, 30);
            stats.putAll(loginStats);

            // 获取操作统计
            Map<String, Object> operationStats = operationLogService.getOperationStatistics(adminId, 30);
            stats.putAll(operationStats);

            // 获取24小时活跃度
            int[] activeHours = loginRecordService.getHourlyActiveStats(adminId, 7);
            stats.put("activeHours", activeHours);

            // 获取操作类型分布
            Map<String, Integer> operationTypes = operationLogService.getOperationTypeDistribution(adminId, 30);
            stats.put("operationTypes", operationTypes);

            // 添加当前在线管理员数量
            stats.put("onlineAdmins", AdminStatsWebSocket.getOnlineCount());
            stats.put("updateTime", System.currentTimeMillis());

            if (adminId != null) {
                // 推送给指定管理员
                AdminStatsWebSocket.pushStatsToAdmin(adminId.toString(), stats);
            } else {
                // 广播给所有在线管理员
                AdminStatsWebSocket.broadcastStats(stats);
            }
            
            log.debug("推送统计数据更新");
        } catch (Exception e) {
            log.error("推送统计数据更新失败", e);
        }
    }

    @Override
    public void notifyAdminOnline(Integer adminId, String adminName) {
        try {
            Map<String, Object> notification = Map.of(
                "type", "adminOnline",
                "data", Map.of(
                    "adminId", adminId,
                    "adminName", adminName,
                    "timestamp", System.currentTimeMillis()
                )
            );
            
            String message = objectMapper.writeValueAsString(notification);
            AdminStatsWebSocket.broadcast(message);
            
            log.info("管理员{}上线通知已发送", adminName);
        } catch (Exception e) {
            log.error("发送管理员上线通知失败", e);
        }
    }

    @Override
    public void notifyAdminOffline(Integer adminId, String adminName) {
        try {
            Map<String, Object> notification = Map.of(
                "type", "adminOffline",
                "data", Map.of(
                    "adminId", adminId,
                    "adminName", adminName,
                    "timestamp", System.currentTimeMillis()
                )
            );
            
            String message = objectMapper.writeValueAsString(notification);
            AdminStatsWebSocket.broadcast(message);
            
            log.info("管理员{}下线通知已发送", adminName);
        } catch (Exception e) {
            log.error("发送管理员下线通知失败", e);
        }
    }

    @Override
    public void sendSystemNotification(String message, Integer adminId) {
        try {
            Map<String, Object> notification = Map.of(
                "type", "systemNotification",
                "data", Map.of(
                    "message", message,
                    "timestamp", System.currentTimeMillis()
                )
            );
            
            String jsonMessage = objectMapper.writeValueAsString(notification);
            
            if (adminId != null) {
                // 发送给指定管理员
                AdminStatsWebSocket.sendInfo(jsonMessage, adminId.toString());
            } else {
                // 广播给所有在线管理员
                AdminStatsWebSocket.broadcast(jsonMessage);
            }
            
            log.info("系统通知已发送：{}", message);
        } catch (Exception e) {
            log.error("发送系统通知失败", e);
        }
    }
}
