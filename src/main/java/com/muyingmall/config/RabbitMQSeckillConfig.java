package com.muyingmall.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 秒杀专用 RabbitMQ 配置
 * 实现异步削峰，保护数据库
 */
@Configuration
public class RabbitMQSeckillConfig {

    // 秒杀队列
    public static final String SECKILL_QUEUE = "seckill.order.queue";
    // 秒杀交换机
    public static final String SECKILL_EXCHANGE = "seckill.exchange";
    // 秒杀路由键
    public static final String SECKILL_ROUTING_KEY = "seckill.order";

    @Bean
    public Queue seckillQueue() {
        return QueueBuilder.durable(SECKILL_QUEUE)
                .maxLength(10000L) // 队列最大长度，防止内存溢出
                .build();
    }

    @Bean
    public DirectExchange seckillExchange() {
        return new DirectExchange(SECKILL_EXCHANGE, true, false);
    }

    @Bean
    public Binding seckillBinding() {
        return BindingBuilder.bind(seckillQueue())
                .to(seckillExchange())
                .with(SECKILL_ROUTING_KEY);
    }
}
