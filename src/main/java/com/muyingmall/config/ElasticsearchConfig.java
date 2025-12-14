package com.muyingmall.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest5_client.Rest5ClientTransport;
import co.elastic.clients.transport.rest5_client.low_level.Rest5Client;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.async.HttpAsyncClientBuilder;
import org.apache.hc.client5.http.impl.nio.PoolingAsyncClientConnectionManager;
import org.apache.hc.client5.http.impl.nio.PoolingAsyncClientConnectionManagerBuilder;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.reactor.IOReactorConfig;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * Elasticsearch 9.x 配置类
 * 使用新的 Rest5ClientTransport
 *
 * 性能优化：
 * 1. 配置连接池参数，支持高并发场景
 * 2. 优化IO线程数和超时设置
 * 3. 启用连接保活机制
 */
@Slf4j
@Configuration
public class ElasticsearchConfig {

    @Value("${spring.elasticsearch.uris:localhost:9200}")
    private String elasticsearchUrl;

    @Value("${spring.elasticsearch.username:}")
    private String username;

    @Value("${spring.elasticsearch.password:}")
    private String password;

    // ES连接池配置参数
    @Value("${spring.elasticsearch.pool.max-total:100}")
    private int maxTotal;

    @Value("${spring.elasticsearch.pool.max-per-route:50}")
    private int maxPerRoute;

    @Value("${spring.elasticsearch.pool.connection-timeout:5000}")
    private int connectionTimeout;

    @Value("${spring.elasticsearch.pool.socket-timeout:30000}")
    private int socketTimeout;

    /**
     * 创建 Elasticsearch 客户端
     * ES 9.x 使用 Rest5ClientTransport
     *
     * 性能优化配置：
     * - 连接池最大连接数: 100
     * - 每个路由最大连接数: 50
     * - IO线程数: CPU核心数 * 2
     * - 连接超时: 5秒
     * - Socket超时: 30秒
     */
    @Bean
    public ElasticsearchClient elasticsearchClient() {
        try {
            // 解析URL
            String[] urlParts = elasticsearchUrl.replace("http://", "").replace("https://", "").split(":");
            String host = urlParts[0];
            int port = urlParts.length > 1 ? Integer.parseInt(urlParts[1]) : 9200;

            // 计算最优IO线程数
            int ioThreadCount = Math.max(2, Runtime.getRuntime().availableProcessors() * 2);

            // 配置IO Reactor - 优化IO线程和超时
            IOReactorConfig ioReactorConfig = IOReactorConfig.custom()
                    .setIoThreadCount(ioThreadCount)
                    .setSoTimeout(Timeout.ofMilliseconds(socketTimeout))
                    .setSoKeepAlive(true)  // 启用TCP Keep-Alive
                    .build();

            // 配置连接池
            PoolingAsyncClientConnectionManager connectionManager = PoolingAsyncClientConnectionManagerBuilder.create()
                    .setMaxConnTotal(maxTotal)           // 最大连接数
                    .setMaxConnPerRoute(maxPerRoute)     // 每个路由最大连接数
                    .setDefaultConnectionConfig(ConnectionConfig.custom()
                            .setConnectTimeout(Timeout.ofMilliseconds(connectionTimeout))
                            .setSocketTimeout(Timeout.ofMilliseconds(socketTimeout))
                            .setValidateAfterInactivity(TimeValue.ofSeconds(10))  // 空闲连接验证
                            .build())
                    .build();

            // 配置请求参数
            RequestConfig requestConfig = RequestConfig.custom()
                    .setConnectionRequestTimeout(Timeout.ofMilliseconds(connectionTimeout))
                    .setResponseTimeout(Timeout.ofMilliseconds(socketTimeout))
                    .build();

            // 创建 Rest5Client - 带连接池和性能优化配置
            Rest5Client restClient = Rest5Client.builder(new HttpHost("http", host, port))
                    .setHttpClientConfigCallback(httpClientBuilder -> httpClientBuilder
                            .setIOReactorConfig(ioReactorConfig)
                            .setConnectionManager(connectionManager)
                            .setDefaultRequestConfig(requestConfig)
                            .evictExpiredConnections()  // 自动清除过期连接
                            .evictIdleConnections(TimeValue.ofMinutes(1)))  // 清除空闲超过1分钟的连接
                    .build();

            // 创建支持Java 8日期时间的ObjectMapper
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

            // 创建传输层 - ES 9.x 使用 Rest5ClientTransport
            ElasticsearchTransport transport = new Rest5ClientTransport(restClient, new JacksonJsonpMapper(objectMapper));

            // 创建API客户端
            ElasticsearchClient client = new ElasticsearchClient(transport);
            log.info("Elasticsearch 9.x 客户端连接成功: {}:{}, 连接池配置[maxTotal={}, maxPerRoute={}, ioThreads={}]",
                    host, port, maxTotal, maxPerRoute, ioThreadCount);
            return client;
        } catch (Exception e) {
            log.error("Elasticsearch客户端创建失败: {}", e.getMessage(), e);
            throw new RuntimeException("无法创建Elasticsearch客户端", e);
        }
    }
}
