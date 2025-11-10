package com.muyingmall.config;

import com.rabbitmq.client.ShutdownSignalException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.Connection;
import org.springframework.amqp.rabbit.connection.ConnectionListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * RabbitMQ连接监听器
 * 用于监控连接状态并限制错误日志输出
 * 
 * @author MuyingMall
 * @since 2025-10-17
 */
@Slf4j
@Component
public class RabbitMQConnectionListener implements ConnectionListener {

    // 连接失败计数器
    private final AtomicInteger failureCount = new AtomicInteger(0);
    
    // 最大错误日志输出次数
    private static final int MAX_FAILURE_LOGS = 3;
    
    // 连接是否曾经成功过
    private volatile boolean hasConnectedBefore = false;

    @Override
    public void onCreate(Connection connection) {
        hasConnectedBefore = true;
        failureCount.set(0); // 重置计数器
        log.info("✓ RabbitMQ连接已建立: {}", connection);
    }

    @Override
    public void onClose(Connection connection) {
        if (hasConnectedBefore) {
            log.warn("RabbitMQ连接已关闭: {}", connection);
        }
    }

    @Override
    public void onShutDown(ShutdownSignalException signal) {
        int currentCount = failureCount.incrementAndGet();
        
        if (currentCount <= MAX_FAILURE_LOGS) {
            log.error("✗ RabbitMQ连接失败 [{}/{}]: {}", 
                    currentCount, MAX_FAILURE_LOGS, signal.getMessage());
            
            if (currentCount == MAX_FAILURE_LOGS) {
                log.warn("⚠ RabbitMQ连接失败已达到{}次，后续错误将不再输出到日志。" +
                        "应用将以降级模式运行（同步处理）。请检查 RabbitMQ 服务是否正常启动。", 
                        MAX_FAILURE_LOGS);
                log.warn("提示：如果不需要使用消息队列功能，可以在 application.yml 中设置 rabbitmq.enabled=false");
            }
        }
    }

    @Override
    public void onFailed(Exception exception) {
        int currentCount = failureCount.incrementAndGet();
        
        if (currentCount <= MAX_FAILURE_LOGS) {
            if (!hasConnectedBefore) {
                log.error("✗ RabbitMQ初始连接失败 [{}/{}]: {}", 
                        currentCount, MAX_FAILURE_LOGS, exception.getMessage());
            } else {
                log.error("✗ RabbitMQ连接异常 [{}/{}]: {}", 
                        currentCount, MAX_FAILURE_LOGS, exception.getMessage());
            }
            
            if (currentCount == MAX_FAILURE_LOGS) {
                log.warn("⚠ RabbitMQ连接失败已达到{}次，后续错误将不再输出到日志。" +
                        "应用将以降级模式运行（同步处理）。请检查 RabbitMQ 服务是否正常启动。", 
                        MAX_FAILURE_LOGS);
                log.warn("提示：如果不需要使用消息队列功能，可以在 application.yml 中设置 rabbitmq.enabled=false");
            }
        }
    }
}
