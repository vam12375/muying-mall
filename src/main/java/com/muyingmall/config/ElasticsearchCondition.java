package com.muyingmall.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * Elasticsearch可用性条件检查
 */
@Slf4j
public class ElasticsearchCondition implements Condition {

    @Override
    public boolean matches(@NotNull ConditionContext context, @NotNull AnnotatedTypeMetadata metadata) {
        try {
            String elasticsearchUrl = context.getEnvironment().getProperty(
                    "spring.data.elasticsearch.uris", "localhost:9200");
            
            // 解析URL
            String[] urlParts = elasticsearchUrl.replace("https://", "").replace("https://", "").split(":");
            String host = urlParts[0];
            int port = urlParts.length > 1 ? Integer.parseInt(urlParts[1]) : 9200;

            // 尝试连接Elasticsearch
            try (RestClient restClient = RestClient.builder(new HttpHost(host, port, "http"))
                    .setRequestConfigCallback(requestConfigBuilder ->
                            requestConfigBuilder
                                    .setConnectTimeout(1000)
                                    .setSocketTimeout(1000)
                    )
                    .build()) {
                
                // 执行一个简单的健康检查
                restClient.performRequest(new org.elasticsearch.client.Request("GET", "/_cluster/health"));
                log.info("Elasticsearch服务可用: {}:{}", host, port);
                return true;
                
            }
        } catch (Exception e) {
            log.warn("Elasticsearch服务不可用，将禁用搜索功能: {}", e.getMessage());
            return false;
        }
    }
}
