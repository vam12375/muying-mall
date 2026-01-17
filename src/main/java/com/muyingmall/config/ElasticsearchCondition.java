package com.muyingmall.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.util.StringUtils;

/**
 * Elasticsearch可用性条件检查
 */
@Slf4j
public class ElasticsearchCondition implements Condition {

    @Override
    public boolean matches(@NotNull ConditionContext context, @NotNull AnnotatedTypeMetadata metadata) {
        try {
            // 修正属性键名，使用 spring.elasticsearch.uris
            String elasticsearchUrl = context.getEnvironment().getProperty(
                    "spring.elasticsearch.uris", "localhost:9200");
            String username = context.getEnvironment().getProperty("spring.elasticsearch.username");
            String password = context.getEnvironment().getProperty("spring.elasticsearch.password");
            
            // 解析URL和协议
            String scheme = "http";
            if (elasticsearchUrl.contains("https://")) {
                scheme = "https";
            }
            
            String[] urlParts = elasticsearchUrl.replace("http://", "").replace("https://", "").split(":");
            String host = urlParts[0];
            int port = urlParts.length > 1 ? Integer.parseInt(urlParts[1]) : 9200;

            // 配置认证
            final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            if (StringUtils.hasText(username) && StringUtils.hasText(password)) {
                credentialsProvider.setCredentials(AuthScope.ANY,
                        new UsernamePasswordCredentials(username, password));
            }

            // 尝试连接Elasticsearch
            try (RestClient restClient = RestClient.builder(new HttpHost(host, port, scheme))
                    .setRequestConfigCallback(requestConfigBuilder ->
                            requestConfigBuilder
                                    .setConnectTimeout(1000)
                                    .setSocketTimeout(1000)
                    )
                    .setHttpClientConfigCallback(httpClientBuilder -> {
                        if (StringUtils.hasText(username) && StringUtils.hasText(password)) {
                            httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
                        }
                        return httpClientBuilder;
                    })
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
