package com.muyingmall.consumer;

import com.muyingmall.common.constants.RabbitMQConstants;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 死信消息消费者
 * 处理进入死信队列的消息，进行日志记录、告警和可能的恢复处理
 * 
 * @author MuyingMall
 * @since 2025-09-18
 */
@Component
@Slf4j
@org.springframework.context.annotation.DependsOn({"deadLetterQueue", "deadLetterExchange", "deadLetterBinding"})
public class DeadLetterMessageConsumer {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String DEAD_LETTER_LOG_KEY = "rabbitmq:deadletter:log";
    private static final String DEAD_LETTER_STATS_KEY = "rabbitmq:deadletter:stats";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 处理死信队列消息
     *
     * @param message 死信消息内容
     * @param amqpMessage AMQP消息对象
     * @param channel RabbitMQ通道
     * @param deliveryTag 消息投递标签
     */
    @RabbitListener(queues = RabbitMQConstants.DLX_QUEUE,
                   autoStartup = "true")
    public void handleDeadLetterMessage(@Payload Object message,
                                      org.springframework.amqp.core.Message amqpMessage,
                                      Channel channel,
                                      @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        
        log.warn("接收到死信消息: deliveryTag={}", deliveryTag);
        
        try {
            // 获取消息详细信息
            DeadLetterInfo deadLetterInfo = extractDeadLetterInfo(amqpMessage, message);
            
            // 记录死信消息
            recordDeadLetterMessage(deadLetterInfo);
            
            // 更新死信统计
            updateDeadLetterStats(deadLetterInfo);
            
            // 分析死信原因
            analyzeDeadLetterCause(deadLetterInfo);
            
            // 执行死信处理策略
            executeDeadLetterStrategy(deadLetterInfo);
            
            // 发送告警通知
            sendDeadLetterAlert(deadLetterInfo);
            
            // 手动确认消息（死信消息处理完成后确认）
            channel.basicAck(deliveryTag, false);
            
            log.debug("死信消息处理完成: messageId={}, originalQueue={}", 
                    deadLetterInfo.getMessageId(), deadLetterInfo.getOriginalQueue());
            
        } catch (Exception e) {
            log.error("处理死信消息失败: deliveryTag={}, error={}", deliveryTag, e.getMessage(), e);
            
            try {
                // 死信消息处理失败，拒绝消息但不重新入队（避免无限循环）
                channel.basicNack(deliveryTag, false, false);
            } catch (IOException ioException) {
                log.error("死信消息确认失败", ioException);
            }
        }
    }

    /**
     * 获取死信消息统计信息
     * 
     * @return 死信统计信息
     */
    public Map<String, Object> getDeadLetterStats() {
        try {
            Map<Object, Object> stats = redisTemplate.opsForHash().entries(DEAD_LETTER_STATS_KEY);
            Map<String, Object> result = new HashMap<>();
            
            for (Map.Entry<Object, Object> entry : stats.entrySet()) {
                result.put(entry.getKey().toString(), entry.getValue());
            }
            
            // 添加汇总信息
            long totalCount = stats.values().stream()
                    .filter(v -> v instanceof Number)
                    .mapToLong(v -> ((Number) v).longValue())
                    .sum();
            
            result.put("totalDeadLetters", totalCount);
            result.put("lastUpdate", LocalDateTime.now().format(FORMATTER));
            
            return result;
            
        } catch (Exception e) {
            log.error("获取死信统计信息失败", e);
            return new HashMap<>();
        }
    }

    /**
     * 获取最近的死信消息日志
     * 
     * @param limit 限制数量
     * @return 死信消息日志列表
     */
    public Map<String, Object> getRecentDeadLetterLogs(int limit) {
        try {
            // 获取最近的死信日志
            java.util.List<Object> logs = redisTemplate.opsForList().range(DEAD_LETTER_LOG_KEY, 0, limit - 1);
            
            Map<String, Object> result = new HashMap<>();
            result.put("logs", logs);
            result.put("count", logs != null ? logs.size() : 0);
            result.put("retrieveTime", LocalDateTime.now().format(FORMATTER));
            
            return result;
            
        } catch (Exception e) {
            log.error("获取死信日志失败", e);
            return new HashMap<>();
        }
    }

    /**
     * 清理过期的死信记录
     */
    public void cleanupExpiredDeadLetterRecords() {
        try {
            log.debug("开始清理过期的死信记录");
            
            // 保留最近1000条死信日志
            redisTemplate.opsForList().trim(DEAD_LETTER_LOG_KEY, 0, 999);
            
            log.debug("死信记录清理完成");
            
        } catch (Exception e) {
            log.error("清理死信记录失败", e);
        }
    }

    /**
     * 提取死信消息信息
     */
    private DeadLetterInfo extractDeadLetterInfo(org.springframework.amqp.core.Message amqpMessage, Object messagePayload) {
        DeadLetterInfo info = new DeadLetterInfo();
        
        try {
            // 基本信息
            info.setMessageId(getMessageId(amqpMessage));
            info.setMessageBody(messagePayload != null ? messagePayload.toString() : new String(amqpMessage.getBody()));
            info.setReceiveTime(LocalDateTime.now().format(FORMATTER));
            
            // 从消息属性中提取原始队列信息
            Map<String, Object> headers = amqpMessage.getMessageProperties().getHeaders();
            
            // 获取死信原因相关信息
            if (headers.containsKey("x-first-death-queue")) {
                info.setOriginalQueue(headers.get("x-first-death-queue").toString());
            }
            
            if (headers.containsKey("x-first-death-reason")) {
                info.setDeathReason(headers.get("x-first-death-reason").toString());
            }
            
            if (headers.containsKey("x-first-death-exchange")) {
                info.setOriginalExchange(headers.get("x-first-death-exchange").toString());
            }
            
            if (headers.containsKey("x-death")) {
                // x-death是一个包含死信历史的数组
                info.setDeathHistory(headers.get("x-death").toString());
            }
            
            // 消息属性
            info.setContentType(amqpMessage.getMessageProperties().getContentType());
            info.setTimestamp(amqpMessage.getMessageProperties().getTimestamp());
            
        } catch (Exception e) {
            log.error("提取死信消息信息失败", e);
            info.setOriginalQueue("unknown");
            info.setDeathReason("extraction_failed");
        }
        
        return info;
    }

    /**
     * 记录死信消息
     */
    private void recordDeadLetterMessage(DeadLetterInfo deadLetterInfo) {
        try {
            // 创建死信记录
            Map<String, Object> record = new HashMap<>();
            record.put("messageId", deadLetterInfo.getMessageId());
            record.put("originalQueue", deadLetterInfo.getOriginalQueue());
            record.put("deathReason", deadLetterInfo.getDeathReason());
            record.put("receiveTime", deadLetterInfo.getReceiveTime());
            record.put("messageBody", deadLetterInfo.getMessageBody());
            record.put("originalExchange", deadLetterInfo.getOriginalExchange());
            
            // 添加到死信日志列表
            redisTemplate.opsForList().leftPush(DEAD_LETTER_LOG_KEY, record);
            
            // 设置过期时间
            redisTemplate.expire(DEAD_LETTER_LOG_KEY, 7, TimeUnit.DAYS);
            
            log.debug("死信消息记录已保存: messageId={}, originalQueue={}", 
                    deadLetterInfo.getMessageId(), deadLetterInfo.getOriginalQueue());
            
        } catch (Exception e) {
            log.error("记录死信消息失败", e);
        }
    }

    /**
     * 更新死信统计
     */
    private void updateDeadLetterStats(DeadLetterInfo deadLetterInfo) {
        try {
            String queueKey = "queue_" + deadLetterInfo.getOriginalQueue();
            String reasonKey = "reason_" + deadLetterInfo.getDeathReason();
            String totalKey = "total";
            String todayKey = "today_" + LocalDateTime.now().toLocalDate().toString();
            
            // 按队列统计
            redisTemplate.opsForHash().increment(DEAD_LETTER_STATS_KEY, queueKey, 1);
            
            // 按原因统计
            redisTemplate.opsForHash().increment(DEAD_LETTER_STATS_KEY, reasonKey, 1);
            
            // 总数统计
            redisTemplate.opsForHash().increment(DEAD_LETTER_STATS_KEY, totalKey, 1);
            
            // 今日统计
            redisTemplate.opsForHash().increment(DEAD_LETTER_STATS_KEY, todayKey, 1);
            
            // 设置过期时间
            redisTemplate.expire(DEAD_LETTER_STATS_KEY, 30, TimeUnit.DAYS);
            
        } catch (Exception e) {
            log.error("更新死信统计失败", e);
        }
    }

    /**
     * 分析死信原因
     */
    private void analyzeDeadLetterCause(DeadLetterInfo deadLetterInfo) {
        try {
            String reason = deadLetterInfo.getDeathReason();
            String queue = deadLetterInfo.getOriginalQueue();
            
            log.warn("死信原因分析: queue={}, reason={}, messageId={}", 
                    queue, reason, deadLetterInfo.getMessageId());
            
            // 根据不同的死信原因进行分析
            switch (reason) {
                case "rejected":
                    log.warn("消息被拒绝: 可能是消费者处理异常或业务逻辑错误");
                    break;
                case "expired":
                    log.warn("消息过期: 消息在队列中停留时间过长");
                    break;
                case "maxlen":
                    log.warn("队列长度超限: 队列消息积压严重");
                    break;
                default:
                    log.warn("未知死信原因: {}", reason);
                    break;
            }
            
        } catch (Exception e) {
            log.error("分析死信原因失败", e);
        }
    }

    /**
     * 执行死信处理策略
     */
    private void executeDeadLetterStrategy(DeadLetterInfo deadLetterInfo) {
        try {
            String queue = deadLetterInfo.getOriginalQueue();
            
            // 根据原始队列执行不同的处理策略
            switch (queue) {
                case RabbitMQConstants.ORDER_CREATE_QUEUE:
                    handleOrderCreateDeadLetter(deadLetterInfo);
                    break;
                case RabbitMQConstants.PAYMENT_SUCCESS_QUEUE:
                    handlePaymentSuccessDeadLetter(deadLetterInfo);
                    break;
                case RabbitMQConstants.PAYMENT_FAILED_QUEUE:
                    handlePaymentFailedDeadLetter(deadLetterInfo);
                    break;
                default:
                    handleGenericDeadLetter(deadLetterInfo);
                    break;
            }
            
        } catch (Exception e) {
            log.error("执行死信处理策略失败", e);
        }
    }

    /**
     * 处理订单创建死信
     */
    private void handleOrderCreateDeadLetter(DeadLetterInfo deadLetterInfo) {
        log.warn("处理订单创建死信消息: messageId={}", deadLetterInfo.getMessageId());
        // 可以发送补偿消息，或者标记订单需要人工处理
    }

    /**
     * 处理支付成功死信
     */
    private void handlePaymentSuccessDeadLetter(DeadLetterInfo deadLetterInfo) {
        log.warn("处理支付成功死信消息: messageId={}", deadLetterInfo.getMessageId());
        // 支付成功消息进入死信队列是严重问题，需要立即人工介入
    }

    /**
     * 处理支付失败死信
     */
    private void handlePaymentFailedDeadLetter(DeadLetterInfo deadLetterInfo) {
        log.warn("处理支付失败死信消息: messageId={}", deadLetterInfo.getMessageId());
        // 可能需要手动处理退款或订单状态
    }

    /**
     * 处理通用死信
     */
    private void handleGenericDeadLetter(DeadLetterInfo deadLetterInfo) {
        log.warn("处理通用死信消息: messageId={}, queue={}", 
                deadLetterInfo.getMessageId(), deadLetterInfo.getOriginalQueue());
    }

    /**
     * 发送死信告警
     */
    private void sendDeadLetterAlert(DeadLetterInfo deadLetterInfo) {
        try {
            log.error("发送死信告警: queue={}, reason={}, messageId={}", 
                    deadLetterInfo.getOriginalQueue(), 
                    deadLetterInfo.getDeathReason(), 
                    deadLetterInfo.getMessageId());
            
            // 这里可以集成各种告警系统
            // 例如：邮件、短信、钉钉、企业微信等
            
        } catch (Exception e) {
            log.error("发送死信告警失败", e);
        }
    }

    /**
     * 获取消息ID
     */
    private String getMessageId(Message message) {
        try {
            String messageId = message.getMessageProperties().getMessageId();
            if (messageId == null) {
                messageId = String.valueOf(message.hashCode());
            }
            return messageId;
        } catch (Exception e) {
            return String.valueOf(System.currentTimeMillis());
        }
    }

    /**
     * 死信信息内部类
     */
    public static class DeadLetterInfo {
        private String messageId;
        private String originalQueue;
        private String originalExchange;
        private String deathReason;
        private String deathHistory;
        private String messageBody;
        private String contentType;
        private java.util.Date timestamp;
        private String receiveTime;

        // Getter和Setter方法
        public String getMessageId() { return messageId; }
        public void setMessageId(String messageId) { this.messageId = messageId; }
        
        public String getOriginalQueue() { return originalQueue; }
        public void setOriginalQueue(String originalQueue) { this.originalQueue = originalQueue; }
        
        public String getOriginalExchange() { return originalExchange; }
        public void setOriginalExchange(String originalExchange) { this.originalExchange = originalExchange; }
        
        public String getDeathReason() { return deathReason; }
        public void setDeathReason(String deathReason) { this.deathReason = deathReason; }
        
        public String getDeathHistory() { return deathHistory; }
        public void setDeathHistory(String deathHistory) { this.deathHistory = deathHistory; }
        
        public String getMessageBody() { return messageBody; }
        public void setMessageBody(String messageBody) { this.messageBody = messageBody; }
        
        public String getContentType() { return contentType; }
        public void setContentType(String contentType) { this.contentType = contentType; }
        
        public java.util.Date getTimestamp() { return timestamp; }
        public void setTimestamp(java.util.Date timestamp) { this.timestamp = timestamp; }
        
        public String getReceiveTime() { return receiveTime; }
        public void setReceiveTime(String receiveTime) { this.receiveTime = receiveTime; }
    }
}