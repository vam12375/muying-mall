package com.muyingmall.listener;

import com.muyingmall.event.AdminLoginEvent;
import com.muyingmall.event.AdminOperationEvent;
import com.muyingmall.websocket.AdminStatsWebSocket;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 管理员事件监听器
 */
@Slf4j
@Component
public class AdminEventListener {

    /**
     * 监听登录事件
     */
    @Async
    @EventListener
    public void handleLoginEvent(AdminLoginEvent event) {
        try {
            // 推送登录记录到WebSocket
            AdminStatsWebSocket.pushLoginRecordToAdmin("all", event.getLoginRecord());
            log.debug("推送登录事件: {}", event.getLoginRecord().getAdminName());
        } catch (Exception e) {
            log.error("处理登录事件失败", e);
        }
    }

    /**
     * 监听操作事件
     */
    @Async
    @EventListener
    public void handleOperationEvent(AdminOperationEvent event) {
        try {
            // 推送操作日志到WebSocket
            AdminStatsWebSocket.pushOperationLogToAdmin("all", event.getOperationLog());
            log.debug("推送操作事件: {} - {}", 
                     event.getOperationLog().getAdminName(), 
                     event.getOperationLog().getOperation());
        } catch (Exception e) {
            log.error("处理操作事件失败", e);
        }
    }
}
