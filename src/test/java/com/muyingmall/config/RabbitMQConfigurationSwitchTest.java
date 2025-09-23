package com.muyingmall.config;

import com.muyingmall.config.properties.RabbitMQProperties;
import com.muyingmall.service.MessageProducerService;
import com.muyingmall.service.RabbitMQStatusService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.TestPropertySource;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * RabbitMQ配置开关测试
 * 验证RabbitMQ功能的启用/禁用机制
 * 
 * @author MuyingMall
 * @since 2025-09-18
 */
public class RabbitMQConfigurationSwitchTest {

    /**
     * 测试RabbitMQ启用时的配置
     */
    @SpringBootTest
    @TestPropertySource(properties = {
            "rabbitmq.enabled=true",
            "rabbitmq.fallback-to-sync=true",
            "rabbitmq.error-handling.max-retry-attempts=3",
            "rabbitmq.monitoring.enabled=true"
    })
    static class RabbitMQEnabledTest {

        @Test
        void testRabbitMQEnabledConfiguration(ApplicationContext context) {
            // 验证RabbitMQ相关Bean存在
            assertTrue(context.containsBean("rabbitMQConfig"));
            assertTrue(context.containsBean("messageProducerService"));
            assertTrue(context.containsBean("rabbitMQHealthIndicator"));
            
            // 验证配置属性
            RabbitMQProperties properties = context.getBean(RabbitMQProperties.class);
            assertTrue(properties.isEnabled());
            assertTrue(properties.isFallbackToSync());
            assertEquals(3, properties.getErrorHandling().getMaxRetryAttempts());
            assertTrue(properties.getMonitoring().isEnabled());
        }

        @Test
        void testMessageProducerServiceAvailable(ApplicationContext context) {
            // 验证MessageProducerService可用
            MessageProducerService messageProducerService = context.getBean(MessageProducerService.class);
            assertNotNull(messageProducerService);
            
            // 验证RabbitMQ可用性检查方法存在
            assertTrue(messageProducerService.isRabbitMQAvailable());
        }

        @Test
        void testStatusServiceAvailable(ApplicationContext context) {
            // 验证状态检查服务可用
            RabbitMQStatusService statusService = context.getBean(RabbitMQStatusService.class);
            assertNotNull(statusService);
            
            // 执行状态检查
            Map<String, Object> status = statusService.checkConnectionStatus();
            assertNotNull(status);
            assertTrue(status.containsKey("connection-status"));
        }
    }

    /**
     * 测试RabbitMQ禁用时的配置
     */
    @SpringBootTest
    @TestPropertySource(properties = {
            "rabbitmq.enabled=false",
            "rabbitmq.fallback-to-sync=true"
    })
    static class RabbitMQDisabledTest {

        @Test
        void testRabbitMQDisabledConfiguration(ApplicationContext context) {
            // 验证RabbitMQ相关Bean不存在
            assertFalse(context.containsBean("rabbitMQConfig"));
            assertFalse(context.containsBean("messageProducerService"));
            assertFalse(context.containsBean("rabbitMQStatusService"));
            
            // 验证配置属性仍然存在但已禁用
            RabbitMQProperties properties = context.getBean(RabbitMQProperties.class);
            assertFalse(properties.isEnabled());
            assertTrue(properties.isFallbackToSync());
        }

        @Test
        void testSystemStillWorksWithoutRabbitMQ(ApplicationContext context) {
            // 验证系统在没有RabbitMQ的情况下仍能正常启动
            assertNotNull(context);
            assertTrue(context.isActive());
            
            // 验证其他核心服务仍然可用
            assertTrue(context.containsBean("orderService"));
            assertTrue(context.containsBean("paymentService"));
        }
    }

    /**
     * 测试RabbitMQ配置属性验证
     */
    @SpringBootTest
    @TestPropertySource(properties = {
            "rabbitmq.enabled=true",
            "rabbitmq.error-handling.max-retry-attempts=5",
            "rabbitmq.error-handling.retry-interval=2000",
            "rabbitmq.monitoring.health-check-interval=60000",
            "rabbitmq.performance.concurrent-consumers=8",
            "rabbitmq.performance.max-concurrent-consumers=16"
    })
    static class RabbitMQPropertiesValidationTest {

        @Test
        void testConfigurationPropertiesBinding(ApplicationContext context) {
            RabbitMQProperties properties = context.getBean(RabbitMQProperties.class);
            
            // 验证错误处理配置
            assertEquals(5, properties.getErrorHandling().getMaxRetryAttempts());
            assertEquals(2000L, properties.getErrorHandling().getRetryInterval());
            
            // 验证监控配置
            assertEquals(60000L, properties.getMonitoring().getHealthCheckInterval());
            
            // 验证性能配置
            assertEquals(8, properties.getPerformance().getConcurrentConsumers());
            assertEquals(16, properties.getPerformance().getMaxConcurrentConsumers());
        }

        @Test
        void testConfigurationValidation(ApplicationContext context) {
            RabbitMQProperties properties = context.getBean(RabbitMQProperties.class);
            
            // 验证配置有效性
            assertTrue(properties.isValidConfiguration());
            
            // 验证配置描述
            String description = properties.getConfigDescription();
            assertNotNull(description);
            assertTrue(description.contains("启用: true"));
            assertTrue(description.contains("重试次数: 5"));
        }
    }

    /**
     * 测试降级模式配置
     */
    @SpringBootTest
    @TestPropertySource(properties = {
            "rabbitmq.enabled=true",
            "rabbitmq.fallback-to-sync=false",
            "spring.rabbitmq.host=invalid-host", // 故意设置无效主机
            "spring.rabbitmq.port=9999"
    })
    static class RabbitMQFallbackTest {

        @MockBean
        private MessageProducerService messageProducerService;

        @Test
        void testFallbackDisabledConfiguration(ApplicationContext context) {
            RabbitMQProperties properties = context.getBean(RabbitMQProperties.class);
            
            // 验证降级模式已禁用
            assertTrue(properties.isEnabled());
            assertFalse(properties.isFallbackToSync());
        }
    }

    /**
     * 测试监控配置开关
     */
    @SpringBootTest
    @TestPropertySource(properties = {
            "rabbitmq.enabled=true",
            "rabbitmq.monitoring.enabled=false"
    })
    static class RabbitMQMonitoringDisabledTest {

        @Test
        void testMonitoringDisabledConfiguration(ApplicationContext context) {
            RabbitMQProperties properties = context.getBean(RabbitMQProperties.class);
            
            // 验证监控已禁用
            assertTrue(properties.isEnabled());
            assertFalse(properties.getMonitoring().isEnabled());
            
            // 验证状态服务不存在
            assertFalse(context.containsBean("rabbitMQStatusService"));
        }
    }
}