package com.muyingmall.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.ErrorHandler;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

/**
 * RabbitMQ错误处理器
 * 统一处理消息消费异常，记录错误信息
 * 
 * @author MuyingMall
 * @since 2025-09-18
 */
@Component
@Slf4j
public class RabbitMQErrorHandler implements ErrorHandler {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String ERROR_LOG_KEY_PREFIX = "rabbitmq:error:";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 处理监听器执行异常
     * 
     * @param t 异常信息
     */
    @Override
    public void handleError(Throwable t) {
        log.error("RabbitMQ消息处理异常: {}", t.getMessage(), t);
        
        try {
            // 记录错误信息到Redis
            recordErrorToRedis(t);
            
        } catch (Exception e) {
            log.error("错误处理器执行失败", e);
        }
    }

    /**
     * 记录错误信息到Redis
     */
    private void recordErrorToRedis(Throwable t) {
        try {
            String errorKey = ERROR_LOG_KEY_PREFIX + System.currentTimeMillis();
            
            ErrorRecord errorRecord = new ErrorRecord();
            errorRecord.setQueueName("unknown");
            errorRecord.setMessageId("unknown");
            errorRecord.setErrorMessage(t.getMessage());
            errorRecord.setErrorClass(t.getClass().getSimpleName());
            errorRecord.setTimestamp(LocalDateTime.now().format(FORMATTER));
            
            redisTemplate.opsForValue().set(errorKey, errorRecord, 24, TimeUnit.HOURS);
            
        } catch (Exception e) {
            log.error("记录错误信息失败", e);
        }
    }

    /**
     * 错误记录内部类
     */
    public static class ErrorRecord {
        private String queueName;
        private String messageId;
        private String errorMessage;
        private String errorClass;
        private String timestamp;

        // Getter和Setter方法
        public String getQueueName() { return queueName; }
        public void setQueueName(String queueName) { this.queueName = queueName; }
        
        public String getMessageId() { return messageId; }
        public void setMessageId(String messageId) { this.messageId = messageId; }
        
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
        
        public String getErrorClass() { return errorClass; }
        public void setErrorClass(String errorClass) { this.errorClass = errorClass; }
        
        public String getTimestamp() { return timestamp; }
        public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
    }
}