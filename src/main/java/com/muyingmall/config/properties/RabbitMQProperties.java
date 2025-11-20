package com.muyingmall.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * RabbitMQ配置属性类
 * 统一管理所有RabbitMQ相关的配置项
 * 
 * @author MuyingMall
 * @since 2025-09-18
 */
@Data
@Component
@ConfigurationProperties(prefix = "rabbitmq")
@Validated
public class RabbitMQProperties {

    /**
     * 是否启用RabbitMQ功能
     * 默认值：true
     */
    private boolean enabled = true;

    /**
     * RabbitMQ不可用时是否回退到同步处理模式
     * 默认值：true
     */
    private boolean fallbackToSync = true;

    /**
     * 错误处理配置
     */
    @Valid
    @NotNull
    private ErrorHandling errorHandling = new ErrorHandling();

    /**
     * 监控配置
     */
    @Valid
    @NotNull
    private Monitoring monitoring = new Monitoring();

    /**
     * 性能配置
     */
    @Valid
    @NotNull
    private Performance performance = new Performance();

    /**
     * 错误处理配置类
     */
    @Data
    public static class ErrorHandling {
        
        /**
         * 最大重试次数
         */
        @Min(0)
        private int maxRetryAttempts = 3;

        /**
         * 重试间隔（毫秒）
         */
        @Min(100)
        private long retryInterval = 1000L;

        /**
         * 是否启用死信队列
         */
        private boolean deadLetterEnabled = true;

        /**
         * 是否启用告警
         */
        private boolean alertEnabled = true;

        /**
         * 连接超时时间（毫秒）
         */
        @Min(1000)
        private long connectionTimeout = 15000L;

        /**
         * 消息发送超时时间（毫秒）
         */
        @Min(1000)
        private long sendTimeout = 5000L;
    }

    /**
     * 监控配置类
     */
    @Data
    public static class Monitoring {
        
        /**
         * 是否启用监控
         */
        private boolean enabled = true;

        /**
         * 健康检查间隔（毫秒）
         */
        @Min(5000)
        private long healthCheckInterval = 30000L;

        /**
         * 是否启用指标收集
         */
        private boolean metricsCollectionEnabled = true;

        /**
         * 统计数据保留天数
         */
        @Min(1)
        private int statsRetentionDays = 7;

        /**
         * 是否启用连接状态监控
         */
        private boolean connectionStatusEnabled = true;

        /**
         * 队列长度告警阈值
         */
        @Min(1)
        private int queueLengthAlertThreshold = 1000;
    }

    /**
     * 性能配置类
     */
    @Data
    public static class Performance {
        
        /**
         * 消费者并发数
         */
        @Min(1)
        private int concurrentConsumers = 5;

        /**
         * 最大消费者并发数
         */
        @Min(1)
        private int maxConcurrentConsumers = 10;

        /**
         * 预取数量
         */
        @Min(1)
        private int prefetchCount = 1;

        /**
         * 批量大小
         */
        @Min(1)
        private int batchSize = 100;

        /**
         * 是否启用批量处理
         */
        private boolean batchProcessingEnabled = false;

        /**
         * 批量处理超时时间（毫秒）
         */
        @Min(100)
        private long batchTimeout = 1000L;
    }

    /**
     * 获取完整的配置描述
     */
    public String getConfigDescription() {
        return String.format(
            "RabbitMQ配置 [启用: %s, 降级: %s, 重试次数: %d, 监控: %s, 并发数: %d]",
            enabled, fallbackToSync, errorHandling.maxRetryAttempts, 
            monitoring.enabled, performance.concurrentConsumers
        );
    }

    /**
     * 验证配置的有效性
     */
    public boolean isValidConfiguration() {
        return performance.maxConcurrentConsumers >= performance.concurrentConsumers;
    }
}