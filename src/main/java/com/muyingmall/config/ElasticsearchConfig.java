package com.muyingmall.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest5_client.Rest5ClientTransport;
import co.elastic.clients.transport.rest5_client.low_level.Rest5Client;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.core5.http.HttpHost;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Elasticsearch 9.x 配置类
 * 使用新的 Rest5ClientTransport
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

    /**
     * 创建 Elasticsearch 客户端
     * ES 9.x 使用 Rest5ClientTransport
     */
    @Bean
    public ElasticsearchClient elasticsearchClient() {
        try {
            // 解析URL
            String[] urlParts = elasticsearchUrl.replace("http://", "").replace("https://", "").split(":");
            String host = urlParts[0];
            int port = urlParts.length > 1 ? Integer.parseInt(urlParts[1]) : 9200;

            // 创建 Rest5Client - ES 9.x 新的低级客户端
            Rest5Client restClient = Rest5Client.builder(new HttpHost("http", host, port)).build();

            // 创建传输层 - ES 9.x 使用 Rest5ClientTransport
            ElasticsearchTransport transport = new Rest5ClientTransport(restClient, new JacksonJsonpMapper());

            // 创建API客户端
            ElasticsearchClient client = new ElasticsearchClient(transport);
            log.info("Elasticsearch 9.x 客户端连接成功: {}:{}", host, port);
            return client;
        } catch (Exception e) {
            log.error("Elasticsearch客户端创建失败: {}", e.getMessage(), e);
            throw new RuntimeException("无法创建Elasticsearch客户端", e);
        }
    }
}
