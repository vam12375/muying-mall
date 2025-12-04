package com.muyingmall.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.muyingmall.util.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 管理员统计数据WebSocket服务端点
 * 
 * 功能：
 * - 实时推送统计数据
 * - 实时推送登录记录
 * - 实时推送操作日志
 * - JWT认证保护
 * - 自动心跳保活
 */
@Slf4j
@Component
@ServerEndpoint("/ws/admin/stats/{adminId}")
public class AdminStatsWebSocket {

    /**
     * 用于存放每个管理员ID对应的WebSocket连接
     * 优化：移除冗余的CopyOnWriteArraySet，只使用Map管理连接
     */
    private static final ConcurrentHashMap<String, AdminStatsWebSocket> webSocketMap = new ConcurrentHashMap<>();

    /**
     * JWT工具类（静态注入）
     */
    private static JwtUtils jwtUtils;

    /**
     * 通过Spring注入JwtUtils（静态方法注入）
     */
    @Autowired
    public void setJwtUtils(JwtUtils jwtUtils) {
        AdminStatsWebSocket.jwtUtils = jwtUtils;
    }

    /**
     * 与某个客户端的连接会话，需要通过它来给客户端发送数据
     */
    private Session session;

    /**
     * 接收adminId
     */
    private String adminId = "";

    /**
     * JSON转换器
     */
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 连接建立成功调用的方法
     * 
     * 安全增强：
     * 1. JWT Token认证
     * 2. 验证adminId与token匹配
     * 3. 限制同一管理员只能有一个活跃连接
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("adminId") String adminId) {
        try {
            // 1. 从查询参数获取token
            Map<String, List<String>> params = session.getRequestParameterMap();
            if (!params.containsKey("token") || params.get("token").isEmpty()) {
                log.warn("管理员{}尝试连接WebSocket但未提供token", adminId);
                session.close(new CloseReason(
                    CloseReason.CloseCodes.VIOLATED_POLICY, 
                    "未提供认证token"
                ));
                return;
            }

            String token = params.get("token").get(0);

            // 2. 验证token有效性
            if (jwtUtils == null || !jwtUtils.validateToken(token)) {
                log.warn("管理员{}的token验证失败", adminId);
                session.close(new CloseReason(
                    CloseReason.CloseCodes.VIOLATED_POLICY, 
                    "token无效或已过期"
                ));
                return;
            }

            // 3. 验证adminId与token是否匹配
            String tokenAdminId = jwtUtils.getAdminIdFromToken(token);
            if (!adminId.equals(tokenAdminId)) {
                log.warn("管理员{}的adminId与token不匹配，token中的adminId为{}", adminId, tokenAdminId);
                session.close(new CloseReason(
                    CloseReason.CloseCodes.VIOLATED_POLICY, 
                    "adminId与token不匹配"
                ));
                return;
            }

            // 4. 检查是否已有连接，如有则关闭旧连接
            AdminStatsWebSocket existingWs = webSocketMap.get(adminId);
            if (existingWs != null) {
                log.info("管理员{}已有连接，关闭旧连接", adminId);
                try {
                    existingWs.session.close(new CloseReason(
                        CloseReason.CloseCodes.NORMAL_CLOSURE, 
                        "新连接已建立"
                    ));
                } catch (IOException e) {
                    log.error("关闭旧连接失败", e);
                }
            }

            // 5. 建立新连接
            this.session = session;
            this.adminId = adminId;
            webSocketMap.put(adminId, this);

            log.info("管理员{}连接WebSocket成功，当前在线人数为：{}", adminId, getOnlineCount());

            // 发送连接成功消息
            Map<String, Object> successMessage = Map.of(
                "type", "connected",
                "message", "连接成功",
                "timestamp", System.currentTimeMillis()
            );
            sendMessage(objectMapper.writeValueAsString(successMessage));

        } catch (Exception e) {
            log.error("建立WebSocket连接失败", e);
            try {
                session.close(new CloseReason(
                    CloseReason.CloseCodes.UNEXPECTED_CONDITION, 
                    "服务器内部错误"
                ));
            } catch (IOException ioException) {
                log.error("关闭连接失败", ioException);
            }
        }
    }

    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose() {
        // 从map中删除
        webSocketMap.remove(adminId);

        log.info("管理员{}断开WebSocket连接，当前在线人数为：{}", adminId, getOnlineCount());
    }

    /**
     * 收到客户端消息后调用的方法
     *
     * @param message 客户端发送过来的消息
     */
    @OnMessage
    public void onMessage(String message, Session session) {
        log.info("收到来自管理员{}的信息：{}", adminId, message);

        try {
            // 解析消息
            Map<String, Object> messageMap = objectMapper.readValue(message, Map.class);
            String type = (String) messageMap.get("type");

            if ("heartbeat".equals(type)) {
                // 心跳消息
                sendMessage("{\"type\":\"heartbeat\",\"data\":\"pong\"}");
            } else if ("requestStats".equals(type)) {
                // 请求统计数据
                // 这里可以触发统计数据的推送
                log.info("管理员{}请求统计数据", adminId);
            }
        } catch (Exception e) {
            log.error("处理WebSocket消息失败", e);
        }
    }

    /**
     * 发生错误时调用
     */
    @OnError
    public void onError(Session session, Throwable error) {
        log.error("管理员{}的WebSocket发生错误", adminId, error);
    }

    /**
     * 实现服务器主动推送
     */
    public void sendMessage(String message) throws IOException {
        this.session.getBasicRemote().sendText(message);
    }

    /**
     * 群发自定义消息
     */
    public static void sendInfo(String message, @PathParam("adminId") String adminId) throws IOException {
        log.info("推送消息到管理员{}，推送内容：{}", adminId, message);

        AdminStatsWebSocket webSocket = webSocketMap.get(adminId);
        if (webSocket != null) {
            webSocket.sendMessage(message);
        }
    }

    /**
     * 推送统计数据到指定管理员
     */
    public static void pushStatsToAdmin(String adminId, Map<String, Object> stats) {
        try {
            AdminStatsWebSocket webSocket = webSocketMap.get(adminId);
            if (webSocket != null) {
                Map<String, Object> message = Map.of(
                        "type", "stats",
                        "data", stats,
                        "timestamp", System.currentTimeMillis());
                String jsonMessage = objectMapper.writeValueAsString(message);
                webSocket.sendMessage(jsonMessage);
                log.debug("推送统计数据到管理员{}", adminId);
            }
        } catch (Exception e) {
            log.error("推送统计数据失败", e);
        }
    }

    /**
     * 推送登录记录到指定管理员
     */
    public static void pushLoginRecordToAdmin(String adminId, Object loginRecord) {
        try {
            AdminStatsWebSocket webSocket = webSocketMap.get(adminId);
            if (webSocket != null) {
                Map<String, Object> message = Map.of(
                        "type", "loginRecord",
                        "data", loginRecord,
                        "timestamp", System.currentTimeMillis());
                String jsonMessage = objectMapper.writeValueAsString(message);
                webSocket.sendMessage(jsonMessage);
                log.debug("推送登录记录到管理员{}", adminId);
            }
        } catch (Exception e) {
            log.error("推送登录记录失败", e);
        }
    }

    /**
     * 推送操作日志到指定管理员
     */
    public static void pushOperationLogToAdmin(String adminId, Object operationLog) {
        try {
            AdminStatsWebSocket webSocket = webSocketMap.get(adminId);
            if (webSocket != null) {
                Map<String, Object> message = Map.of(
                        "type", "operationLog",
                        "data", operationLog,
                        "timestamp", System.currentTimeMillis());
                String jsonMessage = objectMapper.writeValueAsString(message);
                webSocket.sendMessage(jsonMessage);
                log.debug("推送操作日志到管理员{}", adminId);
            }
        } catch (Exception e) {
            log.error("推送操作日志失败", e);
        }
    }

    /**
     * 广播消息到所有在线管理员
     * 优化：直接使用Map的values()遍历
     */
    public static void broadcast(String message) {
        for (AdminStatsWebSocket ws : webSocketMap.values()) {
            try {
                ws.sendMessage(message);
            } catch (IOException e) {
                log.error("广播消息到管理员{}失败", ws.adminId, e);
            }
        }
    }

    /**
     * 广播统计数据到所有在线管理员
     */
    public static void broadcastStats(Map<String, Object> stats) {
        try {
            Map<String, Object> message = Map.of(
                    "type", "stats",
                    "data", stats,
                    "timestamp", System.currentTimeMillis());
            String jsonMessage = objectMapper.writeValueAsString(message);
            broadcast(jsonMessage);
            log.debug("广播统计数据到所有在线管理员");
        } catch (Exception e) {
            log.error("广播统计数据失败", e);
        }
    }

    /**
     * 获取当前在线连接数
     * 优化：直接使用Map的size()
     */
    public static int getOnlineCount() {
        return webSocketMap.size();
    }

    /**
     * 获取所有在线管理员ID
     */
    public static ConcurrentHashMap<String, AdminStatsWebSocket> getWebSocketMap() {
        return webSocketMap;
    }
}
