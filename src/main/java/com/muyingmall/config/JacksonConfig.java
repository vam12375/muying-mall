package com.muyingmall.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

/**
 * Jackson配置类
 * 用于配置HTTP请求处理的ObjectMapper
 */
@Configuration
public class JacksonConfig {

    /**
     * 配置主要的ObjectMapper，用于HTTP请求处理
     * 注意：此ObjectMapper不包含类型信息，适用于标准HTTP请求处理
     */
    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();

        // 注册Java 8日期时间模块
        objectMapper.registerModule(new JavaTimeModule());

        // 禁用将日期转换为时间戳的特性，使用ISO-8601格式
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // 忽略未知属性，避免反序列化失败
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        // 允许空对象序列化
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

        return objectMapper;
    }

    /**
     * 配置Jackson HTTP消息转换器
     * 使用标准配置，确保与Spring Boot默认行为一致
     */
    @Bean
    public MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter(ObjectMapper objectMapper) {
        return new MappingJackson2HttpMessageConverter(objectMapper);
    }
}