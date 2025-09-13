package com.muyingmall.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * 管理员统计数据WebSocket服务端点
 */
@Slf4j
@Component
@ServerEndpoint("/ws/admin/stats/{adminId}")
public class AdminStatsWebSocket {

    /**
     * 存放每个客户端对应的WebSocket对象
     */
    private static final CopyOnWriteArraySet<AdminStatsWebSocket> webSocketSet = new CopyOnWriteArraySet<>();

    /**
     * 与某个客户端的连接会话，需要通过它来给客户端发送数据
     */
    private Session session;

    /**
     * 接收adminId
     */
    private String adminId = "";

    /**
     * 用于存放每个管理员ID对应的WebSocket连接
     */
    private static final ConcurrentHashMap<String, AdminStatsWebSocket> webSocketMap = new ConcurrentHashMap<>();

    /**
     * JSON转换器
     */
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 连接建立成功调用的方法
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("adminId") String adminId) {
        this.session = session;
        this.adminId = adminId;

        // 加入set中
        webSocketSet.add(this);

        // 加入map中
        webSocketMap.put(adminId, this);

        log.info("管理员{}连接WebSocket成功，当前在线人数为：{}", adminId, getOnlineCount());

        try {
            sendMessage("连接成功");
        } catch (IOException e) {
            log.error("WebSocket IO异常", e);
        }
    }

    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose() {
        // 从set中删除
        webSocketSet.remove(this);

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
     */
    public static void broadcast(String message) {
        for (AdminStatsWebSocket item : webSocketSet) {
            try {
                item.sendMessage(message);
            } catch (IOException e) {
                log.error("广播消息失败", e);
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
     */
    public static synchronized int getOnlineCount() {
        return webSocketSet.size();
    }

    /**
     * 获取所有在线管理员ID
     */
    public static ConcurrentHashMap<String, AdminStatsWebSocket> getWebSocketMap() {
        return webSocketMap;
    }
}
