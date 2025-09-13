package com.muyingmall.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.config.AbstractElasticsearchConfiguration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

/**
 * Elasticsearch配置类
 * 配置Elasticsearch客户端连接和基本设置
 */
@Slf4j
@Configuration
@EnableElasticsearchRepositories(basePackages = "com.muyingmall.repository")
public class ElasticsearchConfig extends AbstractElasticsearchConfiguration {

    @Value("${spring.data.elasticsearch.uris:localhost:9200}")
    private String elasticsearchUrl;

    @Value("${spring.data.elasticsearch.username:}")
    private String username;

    @Value("${spring.data.elasticsearch.password:}")
    private String password;

    @Override
    @Bean
    public RestClient elasticsearchClient() {
        try {
            // 解析URL
            String[] urlParts = elasticsearchUrl.replace("http://", "").replace("https://", "").split(":");
            String host = urlParts[0];
            int port = urlParts.length > 1 ? Integer.parseInt(urlParts[1]) : 9200;

            RestClient.Builder builder = RestClient.builder(new HttpHost(host, port, "http"));

            // 设置连接超时和套接字超时
            builder.setRequestConfigCallback(requestConfigBuilder ->
                    requestConfigBuilder
                            .setConnectTimeout(5000)
                            .setSocketTimeout(30000)
            );

            // 设置HTTP客户端配置
            builder.setHttpClientConfigCallback(httpClientBuilder -> {
                // 如果有认证信息，可以在这里配置
                return httpClientBuilder;
            });

            RestClient restClient = builder.build();
            log.info("Elasticsearch客户端连接成功: {}:{}", host, port);
            return restClient;
        } catch (Exception e) {
            log.error("Elasticsearch客户端连接失败: {}", e.getMessage());
            throw new RuntimeException("Failed to create Elasticsearch client", e);
        }
    }

    /**
     * 创建Elasticsearch Java API客户端
     */
    @Bean
    public ElasticsearchClient elasticsearchJavaClient(RestClient restClient) {
        try {
            // 创建传输层
            ElasticsearchTransport transport = new RestClientTransport(
                    restClient, new JacksonJsonpMapper());

            // 创建API客户端
            ElasticsearchClient client = new ElasticsearchClient(transport);
            log.info("Elasticsearch Java API客户端创建成功");
            return client;
        } catch (Exception e) {
            log.error("创建Elasticsearch Java API客户端失败: {}", e.getMessage());
            throw new RuntimeException("Failed to create Elasticsearch Java API client", e);
        }
    }
}
