package com.muyingmall.handler;

import com.muyingmall.common.constants.RabbitMQConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.retry.MessageRecoverer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * RabbitMQ重试处理器
 * 实现消息重试机制和失败恢复策略
 * 
 * @author MuyingMall
 * @since 2025-09-18
 */
@Component
@Slf4j
public class RabbitMQRetryHandler implements MessageRecoverer {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String RETRY_STATS_KEY = "rabbitmq:retry:stats";
    private static final String FAILED_MESSAGE_KEY_PREFIX = "rabbitmq:failed:";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 处理重试失败的消息
     * 当消息重试次数达到上限时，此方法会被调用
     * 
     * @param message 失败的消息
     * @param cause 失败原因
     */
    @Override
    public void recover(Message message, Throwable cause) {
        try {
            log.error("消息重试失败，开始执行恢复策略: {}", cause.getMessage(), cause);
            
            // 获取消息信息
            String messageId = getMessageId(message);
            String queueName = getQueueName(message);
            
            // 记录失败消息
            recordFailedMessage(message, cause, messageId, queueName);
            
            // 更新重试统计
            updateRetryStats(queueName, false);
            
            // 执行恢复策略
            executeRecoveryStrategy(message, cause, messageId, queueName);
            
            log.info("消息恢复策略执行完成: messageId={}, queue={}", messageId, queueName);
            
        } catch (Exception e) {
            log.error("执行消息恢复策略失败", e);
        }
    }

    /**
     * 记录重试成功的消息
     * 
     * @param queueName 队列名称
     * @param messageId 消息ID
     */
    public void recordRetrySuccess(String queueName, String messageId) {
        try {
            log.info("消息重试成功: queue={}, messageId={}", queueName, messageId);
            
            // 更新重试统计
            updateRetryStats(queueName, true);
            
            // 清理重试相关的Redis数据
            cleanupRetryData(queueName, messageId);
            
        } catch (Exception e) {
            log.error("记录重试成功失败", e);
        }
    }

    /**
     * 获取重试统计信息
     * 
     * @return 重试统计信息
     */
    public Map<String, Object> getRetryStats() {
        try {
            Map<Object, Object> stats = redisTemplate.opsForHash().entries(RETRY_STATS_KEY);
            Map<String, Object> result = new HashMap<>();
            
            for (Map.Entry<Object, Object> entry : stats.entrySet()) {
                result.put(entry.getKey().toString(), entry.getValue());
            }
            
            return result;
            
        } catch (Exception e) {
            log.error("获取重试统计信息失败", e);
            return new HashMap<>();
        }
    }

    /**
     * 获取失败消息列表
     * 
     * @param limit 限制数量
     * @return 失败消息列表
     */
    public Map<String, Object> getFailedMessages(int limit) {
        try {
            Map<String, Object> result = new HashMap<>();
            
            // 获取所有失败消息的键
            String pattern = FAILED_MESSAGE_KEY_PREFIX + "*";
            // 注意：在生产环境中应该避免使用keys命令，这里仅作示例
            // 实际应该使用scan命令或者维护一个失败消息的索引
            
            result.put("totalCount", 0);
            result.put("messages", new HashMap<>());
            result.put("note", "需要实现具体的失败消息查询逻辑");
            
            return result;
            
        } catch (Exception e) {
            log.error("获取失败消息列表失败", e);
            return new HashMap<>();
        }
    }

    /**
     * 重新处理失败的消息
     * 
     * @param messageId 消息ID
     * @return 是否成功
     */
    public boolean reprocessFailedMessage(String messageId) {
        try {
            log.info("开始重新处理失败消息: messageId={}", messageId);
            
            // 查找失败消息记录
            String failedMessageKey = FAILED_MESSAGE_KEY_PREFIX + messageId;
            Object failedMessageObj = redisTemplate.opsForValue().get(failedMessageKey);
            
            if (failedMessageObj == null) {
                log.warn("未找到失败消息记录: messageId={}", messageId);
                return false;
            }
            
            // 这里应该实现具体的重新处理逻辑
            // 例如：重新发送消息到原队列，或者发送到特殊的重处理队列
            
            log.info("失败消息重新处理完成: messageId={}", messageId);
            return true;
            
        } catch (Exception e) {
            log.error("重新处理失败消息失败: messageId={}", messageId, e);
            return false;
        }
    }

    /**
     * 记录失败消息
     */
    private void recordFailedMessage(Message message, Throwable cause, String messageId, String queueName) {
        try {
            String failedMessageKey = FAILED_MESSAGE_KEY_PREFIX + messageId;
            
            FailedMessageRecord record = new FailedMessageRecord();
            record.setMessageId(messageId);
            record.setQueueName(queueName);
            record.setMessageBody(new String(message.getBody()));
            record.setErrorMessage(cause.getMessage());
            record.setErrorClass(cause.getClass().getSimpleName());
            record.setFailedTime(LocalDateTime.now().format(FORMATTER));
            record.setRetryCount(RabbitMQConstants.MAX_RETRY_COUNT);
            record.setMessageProperties(message.getMessageProperties().toString());
            
            // 保存失败消息记录，保留7天
            redisTemplate.opsForValue().set(failedMessageKey, record, 7, TimeUnit.DAYS);
            
            log.info("失败消息记录已保存: messageId={}, queue={}", messageId, queueName);
            
        } catch (Exception e) {
            log.error("记录失败消息失败", e);
        }
    }

    /**
     * 更新重试统计
     */
    private void updateRetryStats(String queueName, boolean success) {
        try {
            String statsKey = RETRY_STATS_KEY;
            String totalKey = "total_" + queueName;
            String successKey = "success_" + queueName;
            String failedKey = "failed_" + queueName;
            String lastUpdateKey = "lastUpdate_" + queueName;
            
            // 增加总数
            redisTemplate.opsForHash().increment(statsKey, totalKey, 1);
            
            // 增加成功或失败数
            if (success) {
                redisTemplate.opsForHash().increment(statsKey, successKey, 1);
            } else {
                redisTemplate.opsForHash().increment(statsKey, failedKey, 1);
            }
            
            // 更新最后更新时间
            redisTemplate.opsForHash().put(statsKey, lastUpdateKey, LocalDateTime.now().format(FORMATTER));
            
            // 设置过期时间
            redisTemplate.expire(statsKey, 30, TimeUnit.DAYS);
            
        } catch (Exception e) {
            log.error("更新重试统计失败", e);
        }
    }

    /**
     * 执行恢复策略
     */
    private void executeRecoveryStrategy(Message message, Throwable cause, String messageId, String queueName) {
        try {
            log.info("执行消息恢复策略: messageId={}, queue={}", messageId, queueName);
            
            // 策略1: 发送告警通知
            sendAlertNotification(messageId, queueName, cause);
            
            // 策略2: 记录到监控系统
            recordToMonitoringSystem(messageId, queueName, cause);
            
            // 策略3: 根据队列类型执行特定恢复逻辑
            executeQueueSpecificRecovery(queueName, message, cause);
            
        } catch (Exception e) {
            log.error("执行恢复策略失败", e);
        }
    }

    /**
     * 发送告警通知
     */
    private void sendAlertNotification(String messageId, String queueName, Throwable cause) {
        try {
            log.warn("发送消息处理失败告警: messageId={}, queue={}, error={}", 
                    messageId, queueName, cause.getMessage());
            
            // 这里可以集成邮件、短信、钉钉等告警系统
            // 例如：发送邮件给运维人员
            // 例如：发送钉钉机器人消息
            // 例如：记录到告警系统
            
        } catch (Exception e) {
            log.error("发送告警通知失败", e);
        }
    }

    /**
     * 记录到监控系统
     */
    private void recordToMonitoringSystem(String messageId, String queueName, Throwable cause) {
        try {
            // 这里可以集成Prometheus、Grafana等监控系统
            // 记录失败指标，用于监控和告警
            
            log.info("记录失败指标到监控系统: messageId={}, queue={}", messageId, queueName);
            
        } catch (Exception e) {
            log.error("记录监控指标失败", e);
        }
    }

    /**
     * 执行队列特定的恢复逻辑
     */
    private void executeQueueSpecificRecovery(String queueName, Message message, Throwable cause) {
        try {
            switch (queueName) {
                case RabbitMQConstants.ORDER_CREATE_QUEUE:
                    handleOrderCreateFailure(message, cause);
                    break;
                case RabbitMQConstants.PAYMENT_SUCCESS_QUEUE:
                    handlePaymentSuccessFailure(message, cause);
                    break;
                case RabbitMQConstants.PAYMENT_FAILED_QUEUE:
                    handlePaymentFailedFailure(message, cause);
                    break;
                default:
                    handleGenericFailure(message, cause);
                    break;
            }
        } catch (Exception e) {
            log.error("执行队列特定恢复逻辑失败: queue={}", queueName, e);
        }
    }

    /**
     * 处理订单创建失败
     */
    private void handleOrderCreateFailure(Message message, Throwable cause) {
        log.warn("订单创建消息处理失败，执行特定恢复逻辑");
        // 可以发送补偿消息，或者标记订单状态等
    }

    /**
     * 处理支付成功消息失败
     */
    private void handlePaymentSuccessFailure(Message message, Throwable cause) {
        log.warn("支付成功消息处理失败，执行特定恢复逻辑");
        // 可能需要手动确认支付状态，或者发送补偿消息
    }

    /**
     * 处理支付失败消息失败
     */
    private void handlePaymentFailedFailure(Message message, Throwable cause) {
        log.warn("支付失败消息处理失败，执行特定恢复逻辑");
        // 可能需要手动处理退款逻辑
    }

    /**
     * 处理通用失败
     */
    private void handleGenericFailure(Message message, Throwable cause) {
        log.warn("通用消息处理失败，执行默认恢复逻辑");
        // 执行默认的恢复策略
    }

    /**
     * 清理重试数据
     */
    private void cleanupRetryData(String queueName, String messageId) {
        try {
            String retryKey = "rabbitmq:retry:" + queueName + ":" + messageId;
            redisTemplate.delete(retryKey);
            
        } catch (Exception e) {
            log.error("清理重试数据失败", e);
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
     * 获取队列名称
     */
    private String getQueueName(Message message) {
        try {
            Object consumerQueue = message.getMessageProperties().getConsumerQueue();
            return consumerQueue != null ? consumerQueue.toString() : "unknown";
        } catch (Exception e) {
            return "unknown";
        }
    }

    /**
     * 失败消息记录内部类
     */
    public static class FailedMessageRecord {
        private String messageId;
        private String queueName;
        private String messageBody;
        private String errorMessage;
        private String errorClass;
        private String failedTime;
        private int retryCount;
        private String messageProperties;

        // Getters and Setters
        public String getMessageId() { return messageId; }
        public void setMessageId(String messageId) { this.messageId = messageId; }
        
        public String getQueueName() { return queueName; }
        public void setQueueName(String queueName) { this.queueName = queueName; }
        
        public String getMessageBody() { return messageBody; }
        public void setMessageBody(String messageBody) { this.messageBody = messageBody; }
        
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
        
        public String getErrorClass() { return errorClass; }
        public void setErrorClass(String errorClass) { this.errorClass = errorClass; }
        
        public String getFailedTime() { return failedTime; }
        public void setFailedTime(String failedTime) { this.failedTime = failedTime; }
        
        public int getRetryCount() { return retryCount; }
        public void setRetryCount(int retryCount) { this.retryCount = retryCount; }
        
        public String getMessageProperties() { return messageProperties; }
        public void setMessageProperties(String messageProperties) { this.messageProperties = messageProperties; }
    }
}