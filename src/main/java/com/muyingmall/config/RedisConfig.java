package com.muyingmall.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis配置类
 */
@Configuration
@EnableCaching
public class RedisConfig {

    /**
     * 创建db1的Redis连接工厂
     */
    @Bean
    public RedisConnectionFactory redisConnectionFactoryDb1() {
        RedisStandaloneConfiguration redisConfig = new RedisStandaloneConfiguration();
        redisConfig.setHostName("localhost");
        redisConfig.setPort(6379);
        redisConfig.setDatabase(1); // 使用db1
        return new LettuceConnectionFactory(redisConfig);
    }

    /**
     * 创建支持Java 8日期时间类型的ObjectMapper，专用于Redis序列化
     * 注意：此ObjectMapper包含类型信息，仅用于Redis，不会用于HTTP请求处理
     */
    @Bean("redisObjectMapper") // 明确指定bean名称，避免被自动注入到其他地方
    public ObjectMapper redisObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        // 注册Java 8日期时间模块
        objectMapper.registerModule(new JavaTimeModule());
        // 禁用将日期转换为时间戳的特性，使用ISO-8601格式
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        // 设置可见性
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        // 启用序列化类的类型信息（防止反序列化时出现错误）
        objectMapper.activateDefaultTyping(LaissezFaireSubTypeValidator.instance, 
                                ObjectMapper.DefaultTyping.NON_FINAL);
        return objectMapper;
    }

    /**
     * 默认RedisTemplate，使用db0
     */
    @Primary
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory, 
                                                      @Qualifier("redisObjectMapper") ObjectMapper redisObjectMapper) {
        return createRedisTemplate(factory, redisObjectMapper);
    }

    /**
     * 创建db1专用的RedisTemplate
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplateDb1(
            @Qualifier("redisConnectionFactoryDb1") RedisConnectionFactory factory,
            @Qualifier("redisObjectMapper") ObjectMapper redisObjectMapper) {
        return createRedisTemplate(factory, redisObjectMapper);
    }

    /**
     * 创建RedisTemplate的通用方法
     */
    private RedisTemplate<String, Object> createRedisTemplate(RedisConnectionFactory factory, 
                                                             ObjectMapper redisObjectMapper) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        // 配置连接工厂
        template.setConnectionFactory(factory);

        // 使用StringRedisSerializer来序列化和反序列化redis的key值
        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
        template.setKeySerializer(stringRedisSerializer);
        template.setHashKeySerializer(stringRedisSerializer);

        // 使用Jackson2JsonRedisSerializer来序列化和反序列化redis的value值
        Jackson2JsonRedisSerializer<Object> jsonRedisSerializer = 
            new Jackson2JsonRedisSerializer<>(redisObjectMapper, Object.class);
        template.setValueSerializer(jsonRedisSerializer);
        template.setHashValueSerializer(jsonRedisSerializer);

        template.afterPropertiesSet();

        return template;
    }
}