package com.muyingmall.config;

import com.muyingmall.common.constants.RabbitMQConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

/**
 * RabbitMQ配置类
 * 定义交换机、队列和绑定关系
 * 
 * @author MuyingMall
 * @since 2025-09-18
 */
@Configuration
@EnableRabbit
@ConditionalOnProperty(name = "rabbitmq.enabled", havingValue = "true", matchIfMissing = true)
@org.springframework.core.annotation.Order(1)
@Slf4j
public class RabbitMQConfig {

    /**
     * 配置消息转换器，使用JSON格式
     */
    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * 配置RabbitTemplate
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        
        // 配置消息确认回调
        template.setConfirmCallback((correlationData, ack, cause) -> {
            if (ack) {
                log.debug("消息发送成功: {}", correlationData);
            } else {
                log.error("消息发送失败: {}, 原因: {}", correlationData, cause);
            }
        });
        
        // 配置消息返回回调
        template.setReturnsCallback(returned -> {
            log.error("消息被退回: {}, 退回原因: {}, 路由键: {}", 
                returned.getMessage(), returned.getReplyText(), returned.getRoutingKey());
        });
        
        return template;
    }

    /**
     * 配置监听器容器工厂
     */
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter());
        
        // 设置并发消费者数量
        factory.setConcurrentConsumers(5);
        factory.setMaxConcurrentConsumers(10);
        
        // 设置预取数量
        factory.setPrefetchCount(1);
        
        // 设置确认模式
        factory.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        
        // 配置重试机制
        factory.setRetryTemplate(retryTemplate());
        
        return factory;
    }

    /**
     * 配置重试模板
     */
    @Bean
    public RetryTemplate retryTemplate() {
        RetryTemplate retryTemplate = new RetryTemplate();
        
        // 配置重试策略
        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
        retryPolicy.setMaxAttempts(RabbitMQConstants.MAX_RETRY_COUNT);
        retryTemplate.setRetryPolicy(retryPolicy);
        
        // 配置退避策略
        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(1000); // 初始间隔1秒
        backOffPolicy.setMultiplier(2.0); // 倍数
        backOffPolicy.setMaxInterval(10000); // 最大间隔10秒
        retryTemplate.setBackOffPolicy(backOffPolicy);
        
        return retryTemplate;
    }



    // ==================== 订单相关配置 ====================

    /**
     * 订单交换机
     */
    @Bean
    public TopicExchange orderExchange() {
        return ExchangeBuilder
                .topicExchange(RabbitMQConstants.ORDER_EXCHANGE)
                .durable(true)
                .build();
    }

    /**
     * 订单创建队列
     */
    @Bean
    public Queue orderCreateQueue() {
        return QueueBuilder
                .durable(RabbitMQConstants.ORDER_CREATE_QUEUE)
                .ttl((int) RabbitMQConstants.DEFAULT_MESSAGE_TTL)
                .maxLength(RabbitMQConstants.MAX_QUEUE_LENGTH)
                .deadLetterExchange(RabbitMQConstants.DLX_EXCHANGE)
                .deadLetterRoutingKey(RabbitMQConstants.DLX_ROUTING_KEY)
                .build();
    }

    /**
     * 订单状态变更队列
     */
    @Bean
    public Queue orderStatusQueue() {
        return QueueBuilder
                .durable(RabbitMQConstants.ORDER_STATUS_QUEUE)
                .ttl((int) RabbitMQConstants.DEFAULT_MESSAGE_TTL)
                .maxLength(RabbitMQConstants.MAX_QUEUE_LENGTH)
                .deadLetterExchange(RabbitMQConstants.DLX_EXCHANGE)
                .deadLetterRoutingKey(RabbitMQConstants.DLX_ROUTING_KEY)
                .build();
    }

    /**
     * 订单取消队列
     */
    @Bean
    public Queue orderCancelQueue() {
        return QueueBuilder
                .durable(RabbitMQConstants.ORDER_CANCEL_QUEUE)
                .ttl((int) RabbitMQConstants.DEFAULT_MESSAGE_TTL)
                .maxLength(RabbitMQConstants.MAX_QUEUE_LENGTH)
                .deadLetterExchange(RabbitMQConstants.DLX_EXCHANGE)
                .deadLetterRoutingKey(RabbitMQConstants.DLX_ROUTING_KEY)
                .build();
    }

    /**
     * 订单完成队列
     */
    @Bean
    public Queue orderCompleteQueue() {
        return QueueBuilder
                .durable(RabbitMQConstants.ORDER_COMPLETE_QUEUE)
                .ttl((int) RabbitMQConstants.DEFAULT_MESSAGE_TTL)
                .maxLength(RabbitMQConstants.MAX_QUEUE_LENGTH)
                .deadLetterExchange(RabbitMQConstants.DLX_EXCHANGE)
                .deadLetterRoutingKey(RabbitMQConstants.DLX_ROUTING_KEY)
                .build();
    }

    // ==================== 支付相关配置 ====================

    /**
     * 支付交换机
     */
    @Bean
    public TopicExchange paymentExchange() {
        return ExchangeBuilder
                .topicExchange(RabbitMQConstants.PAYMENT_EXCHANGE)
                .durable(true)
                .build();
    }

    /**
     * 支付成功队列
     */
    @Bean
    public Queue paymentSuccessQueue() {
        return QueueBuilder
                .durable(RabbitMQConstants.PAYMENT_SUCCESS_QUEUE)
                .ttl((int) RabbitMQConstants.DEFAULT_MESSAGE_TTL)
                .maxLength(RabbitMQConstants.MAX_QUEUE_LENGTH)
                .deadLetterExchange(RabbitMQConstants.DLX_EXCHANGE)
                .deadLetterRoutingKey(RabbitMQConstants.DLX_ROUTING_KEY)
                .build();
    }

    /**
     * 支付失败队列
     */
    @Bean
    public Queue paymentFailedQueue() {
        return QueueBuilder
                .durable(RabbitMQConstants.PAYMENT_FAILED_QUEUE)
                .ttl((int) RabbitMQConstants.DEFAULT_MESSAGE_TTL)
                .maxLength(RabbitMQConstants.MAX_QUEUE_LENGTH)
                .deadLetterExchange(RabbitMQConstants.DLX_EXCHANGE)
                .deadLetterRoutingKey(RabbitMQConstants.DLX_ROUTING_KEY)
                .build();
    }

    /**
     * 退款处理队列
     */
    @Bean
    public Queue paymentRefundQueue() {
        return QueueBuilder
                .durable(RabbitMQConstants.PAYMENT_REFUND_QUEUE)
                .ttl((int) RabbitMQConstants.DEFAULT_MESSAGE_TTL)
                .maxLength(RabbitMQConstants.MAX_QUEUE_LENGTH)
                .deadLetterExchange(RabbitMQConstants.DLX_EXCHANGE)
                .deadLetterRoutingKey(RabbitMQConstants.DLX_ROUTING_KEY)
                .build();
    }

    // ==================== 死信队列配置 ====================

    /**
     * 死信交换机
     */
    @Bean
    public DirectExchange deadLetterExchange() {
        return ExchangeBuilder
                .directExchange(RabbitMQConstants.DLX_EXCHANGE)
                .durable(true)
                .build();
    }

    /**
     * 死信队列
     */
    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder
                .durable(RabbitMQConstants.DLX_QUEUE)
                .maxLength(RabbitMQConstants.MAX_QUEUE_LENGTH)
                .build();
    }

    /**
     * 死信队列绑定
     */
    @Bean
    public Binding deadLetterBinding() {
        return BindingBuilder
                .bind(deadLetterQueue())
                .to(deadLetterExchange())
                .with(RabbitMQConstants.DLX_ROUTING_KEY);
    }

    // ==================== 队列绑定关系配置 ====================

    /**
     * 订单创建队列绑定
     */
    @Bean
    public Binding orderCreateBinding() {
        return BindingBuilder
                .bind(orderCreateQueue())
                .to(orderExchange())
                .with(RabbitMQConstants.ORDER_CREATE_KEY);
    }

    /**
     * 订单状态变更队列绑定
     */
    @Bean
    public Binding orderStatusBinding() {
        return BindingBuilder
                .bind(orderStatusQueue())
                .to(orderExchange())
                .with(RabbitMQConstants.ORDER_STATUS_KEY);
    }

    /**
     * 订单取消队列绑定
     */
    @Bean
    public Binding orderCancelBinding() {
        return BindingBuilder
                .bind(orderCancelQueue())
                .to(orderExchange())
                .with(RabbitMQConstants.ORDER_CANCEL_KEY);
    }

    /**
     * 订单完成队列绑定
     */
    @Bean
    public Binding orderCompleteBinding() {
        return BindingBuilder
                .bind(orderCompleteQueue())
                .to(orderExchange())
                .with(RabbitMQConstants.ORDER_COMPLETE_KEY);
    }

    /**
     * 支付成功队列绑定
     */
    @Bean
    public Binding paymentSuccessBinding() {
        return BindingBuilder
                .bind(paymentSuccessQueue())
                .to(paymentExchange())
                .with(RabbitMQConstants.PAYMENT_SUCCESS_KEY);
    }

    /**
     * 支付失败队列绑定
     */
    @Bean
    public Binding paymentFailedBinding() {
        return BindingBuilder
                .bind(paymentFailedQueue())
                .to(paymentExchange())
                .with(RabbitMQConstants.PAYMENT_FAILED_KEY);
    }

    /**
     * 退款处理队列绑定
     */
    @Bean
    public Binding paymentRefundBinding() {
        return BindingBuilder
                .bind(paymentRefundQueue())
                .to(paymentExchange())
                .with(RabbitMQConstants.PAYMENT_REFUND_KEY);
    }

    // ==================== 高级配置 ====================

    // 注意: 延迟队列需要安装 rabbitmq_delayed_message_exchange 插件
    // 如果没有安装插件，请注释掉以下配置或安装插件后再启用

    /*
    /**
     * 延迟队列交换机（用于延迟消息处理）
     * 需要安装 rabbitmq_delayed_message_exchange 插件
     */
    /*
    @Bean
    public TopicExchange delayExchange() {
        return ExchangeBuilder
                .topicExchange("delay.exchange")
                .durable(true)
                .delayed()
                .build();
    }

    /**
     * 延迟队列（用于订单超时处理等场景）
     */
    /*
    @Bean
    public Queue delayQueue() {
        return QueueBuilder
                .durable("delay.queue")
                .ttl(30 * 60 * 1000) // 30分钟TTL
                .maxLength(5000)
                .deadLetterExchange(RabbitMQConstants.DLX_EXCHANGE)
                .deadLetterRoutingKey(RabbitMQConstants.DLX_ROUTING_KEY)
                .build();
    }

    /**
     * 延迟队列绑定
     */
    /*
    @Bean
    public Binding delayBinding() {
        return BindingBuilder
                .bind(delayQueue())
                .to(delayExchange())
                .with("delay.#");
    }
    */

    /**
     * 配置初始化日志
     */
    @Bean
    public String rabbitMQConfigInitLog() {
        log.info("=== RabbitMQ配置初始化完成 ===");
        log.info("订单交换机: {}", RabbitMQConstants.ORDER_EXCHANGE);
        log.info("支付交换机: {}", RabbitMQConstants.PAYMENT_EXCHANGE);
        log.info("死信交换机: {}", RabbitMQConstants.DLX_EXCHANGE);
        log.info("消息TTL: {} 毫秒", RabbitMQConstants.DEFAULT_MESSAGE_TTL);
        log.info("队列最大长度: {}", RabbitMQConstants.MAX_QUEUE_LENGTH);
        log.info("最大重试次数: {}", RabbitMQConstants.MAX_RETRY_COUNT);
        log.info("=== RabbitMQ配置初始化完成 ===");
        return "RabbitMQ配置初始化完成";
    }
}