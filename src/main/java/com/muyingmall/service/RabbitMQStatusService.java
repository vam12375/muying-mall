package com.muyingmall.service;

import com.muyingmall.config.properties.RabbitMQProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * RabbitMQ状态检查服务
 * 提供RabbitMQ连接状态检查和配置信息查询
 * 
 * @author MuyingMall
 * @since 2025-09-18
 */
@Service
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(name = "rabbitmq.enabled", havingValue = "true", matchIfMissing = true)
public class RabbitMQStatusService {

    private final ConnectionFactory connectionFactory;
    private final RabbitMQProperties rabbitMQProperties;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 检查RabbitMQ连接状态
     * 
     * @return 连接状态信息
     */
    public Map<String, Object> checkConnectionStatus() {
        Map<String, Object> status = new HashMap<>();
        
        status.put("enabled", rabbitMQProperties.isEnabled());
        status.put("fallback-to-sync", rabbitMQProperties.isFallbackToSync());
        status.put("check-time", LocalDateTime.now().format(FORMATTER));
        
        if (!rabbitMQProperties.isEnabled()) {
            status.put("connection-status", "disabled");
            status.put("message", "RabbitMQ功能已通过配置禁用");
            return status;
        }

        try {
            // 尝试创建连接来测试连接状态
            var connection = connectionFactory.createConnection();
            
            if (connection.isOpen()) {
                connection.close();
                status.put("connection-status", "connected");
                status.put("message", "RabbitMQ连接正常");
            } else {
                status.put("connection-status", "failed");
                status.put("message", "无法建立RabbitMQ连接");
            }
            
        } catch (Exception e) {
            log.warn("RabbitMQ连接检查失败: {}", e.getMessage());
            
            status.put("connection-status", "error");
            status.put("message", "RabbitMQ连接异常: " + e.getMessage());
            status.put("error-class", e.getClass().getSimpleName());
            
            // 如果启用了降级模式，标记为可用
            if (rabbitMQProperties.isFallbackToSync()) {
                status.put("fallback-available", true);
                status.put("fallback-message", "已启用降级模式，系统可正常运行");
            }
        }
        
        return status;
    }

    /**
     * 获取RabbitMQ配置信息
     * 
     * @return 配置信息
     */
    public Map<String, Object> getConfigurationInfo() {
        Map<String, Object> config = new HashMap<>();
        
        config.put("enabled", rabbitMQProperties.isEnabled());
        config.put("fallback-to-sync", rabbitMQProperties.isFallbackToSync());
        config.put("config-description", rabbitMQProperties.getConfigDescription());
        config.put("config-valid", rabbitMQProperties.isValidConfiguration());
        
        // 错误处理配置
        Map<String, Object> errorHandling = new HashMap<>();
        errorHandling.put("max-retry-attempts", rabbitMQProperties.getErrorHandling().getMaxRetryAttempts());
        errorHandling.put("retry-interval", rabbitMQProperties.getErrorHandling().getRetryInterval());
        errorHandling.put("dead-letter-enabled", rabbitMQProperties.getErrorHandling().isDeadLetterEnabled());
        errorHandling.put("alert-enabled", rabbitMQProperties.getErrorHandling().isAlertEnabled());
        config.put("error-handling", errorHandling);
        
        // 监控配置
        Map<String, Object> monitoring = new HashMap<>();
        monitoring.put("enabled", rabbitMQProperties.getMonitoring().isEnabled());
        monitoring.put("health-check-interval", rabbitMQProperties.getMonitoring().getHealthCheckInterval());
        monitoring.put("metrics-collection-enabled", rabbitMQProperties.getMonitoring().isMetricsCollectionEnabled());
        monitoring.put("stats-retention-days", rabbitMQProperties.getMonitoring().getStatsRetentionDays());
        config.put("monitoring", monitoring);
        
        // 性能配置
        Map<String, Object> performance = new HashMap<>();
        performance.put("concurrent-consumers", rabbitMQProperties.getPerformance().getConcurrentConsumers());
        performance.put("max-concurrent-consumers", rabbitMQProperties.getPerformance().getMaxConcurrentConsumers());
        performance.put("prefetch-count", rabbitMQProperties.getPerformance().getPrefetchCount());
        config.put("performance", performance);
        
        return config;
    }

    /**
     * 获取RabbitMQ状态概览
     * 
     * @return 状态概览
     */
    public Map<String, Object> getStatusOverview() {
        Map<String, Object> overview = new HashMap<>();
        
        // 基本状态
        overview.put("enabled", rabbitMQProperties.isEnabled());
        overview.put("fallback-enabled", rabbitMQProperties.isFallbackToSync());
        overview.put("config-description", rabbitMQProperties.getConfigDescription());
        
        // 连接状态
        Map<String, Object> connectionStatus = checkConnectionStatus();
        overview.put("connection-status", connectionStatus.get("connection-status"));
        overview.put("connection-message", connectionStatus.get("message"));
        
        // 配置有效性
        overview.put("config-valid", rabbitMQProperties.isValidConfiguration());
        
        return overview;
    }
}