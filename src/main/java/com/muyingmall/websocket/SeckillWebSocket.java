package com.muyingmall.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 秒杀 WebSocket 端点
 * 用于实时推送秒杀结果给用户
 */
@Slf4j
@Component
@ServerEndpoint("/ws/seckill/{userId}")
public class SeckillWebSocket {

    /**
     * 存储所有在线用户的 WebSocket 连接
     * Key: userId, Value: Session
     */
    private static final Map<String, Session> ONLINE_USERS = new ConcurrentHashMap<>();

    /**
     * JSON 序列化工具
     */
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     * 连接建立成功调用的方法
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("userId") String userId) {
        ONLINE_USERS.put(userId, session);
        log.info("秒杀WebSocket连接建立: userId={}, 当前在线人数={}", userId, ONLINE_USERS.size());

        // 发送连接成功消息
        sendMessage(userId, createMessage("CONNECTED", "连接成功", null));
    }

    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose(@PathParam("userId") String userId) {
        ONLINE_USERS.remove(userId);
        log.info("秒杀WebSocket连接关闭: userId={}, 当前在线人数={}", userId, ONLINE_USERS.size());
    }

    /**
     * 发生错误时调用
     */
    @OnError
    public void onError(Session session, Throwable error, @PathParam("userId") String userId) {
        log.error("秒杀WebSocket发生错误: userId={}", userId, error);
        ONLINE_USERS.remove(userId);
    }

    /**
     * 收到客户端消息后调用的方法
     */
    @OnMessage
    public void onMessage(String message, @PathParam("userId") String userId) {
        log.debug("收到秒杀WebSocket消息: userId={}, message={}", userId, message);
        // 可以处理客户端发送的心跳消息等
    }

    /**
     * 发送消息给指定用户
     *
     * @param userId  用户ID
     * @param message 消息内容
     */
    public static void sendMessage(String userId, String message) {
        Session session = ONLINE_USERS.get(userId);
        if (session != null && session.isOpen()) {
            try {
                session.getBasicRemote().sendText(message);
                log.debug("发送秒杀WebSocket消息成功: userId={}", userId);
            } catch (IOException e) {
                log.error("发送秒杀WebSocket消息失败: userId={}", userId, e);
                ONLINE_USERS.remove(userId);
            }
        } else {
            log.debug("用户未连接或连接已关闭: userId={}", userId);
        }
    }

    /**
     * 发送秒杀成功消息
     *
     * @param userId  用户ID
     * @param orderId 订单ID
     */
    public static void sendSeckillSuccess(Integer userId, Long orderId) {
        String message = createMessage("SUCCESS", "秒杀成功", Map.of("orderId", orderId));
        sendMessage(String.valueOf(userId), message);
    }

    /**
     * 发送秒杀失败消息
     *
     * @param userId 用户ID
     * @param reason 失败原因
     */
    public static void sendSeckillFailure(Integer userId, String reason) {
        String message = createMessage("FAILURE", reason, null);
        sendMessage(String.valueOf(userId), message);
    }

    /**
     * 创建消息JSON
     *
     * @param type    消息类型
     * @param message 消息内容
     * @param data    附加数据
     * @return JSON字符串
     */
    private static String createMessage(String type, String message, Object data) {
        try {
            Map<String, Object> messageMap = Map.of(
                    "type", type,
                    "message", message,
                    "data", data != null ? data : Map.of(),
                    "timestamp", System.currentTimeMillis());
            return OBJECT_MAPPER.writeValueAsString(messageMap);
        } catch (Exception e) {
            log.error("创建WebSocket消息失败", e);
            return "{\"type\":\"ERROR\",\"message\":\"消息创建失败\"}";
        }
    }

    /**
     * 获取当前在线人数
     */
    public static int getOnlineCount() {
        return ONLINE_USERS.size();
    }
}
