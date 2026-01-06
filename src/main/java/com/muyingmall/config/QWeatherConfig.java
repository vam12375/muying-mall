package com.muyingmall.config;

import lombok.Data;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.io.HttpClientConnectionManager;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * 和风天气API配置类
 * 官方文档：https://dev.qweather.com/docs/api/
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "qweather")
public class QWeatherConfig {

    /**
     * 和风天气 API Key
     * 获取方式：https://console.qweather.com/#/apps
     * 免费订阅：每天1000次调用
     */
    private String apiKey;

    /**
     * API Host
     * 查看方式：https://console.qweather.com/setting
     * 免费版通常是自定义域名，例如：xxx.qweatherapi.com
     * 注意：不要使用 devapi.qweather.com，请使用控制台显示的实际域名
     */
    private String apiHost = "devapi.qweather.com";

    /**
     * 是否启用和风天气服务
     */
    private boolean enabled = true;

    /**
     * 配置支持Gzip解压的RestTemplate
     * 和风天气API返回Gzip压缩数据，需要使用Apache HttpClient自动解压
     */
    @Bean(name = "qweatherRestTemplate")
    public RestTemplate qweatherRestTemplate() {
        // 创建支持Gzip的HttpClient连接管理器
        HttpClientConnectionManager connectionManager = PoolingHttpClientConnectionManagerBuilder.create()
                .setMaxConnTotal(100)
                .setMaxConnPerRoute(20)
                .build();

        // 创建HttpClient，默认支持Gzip解压
        HttpClient httpClient = HttpClientBuilder.create()
                .setConnectionManager(connectionManager)
                .build();

        // 创建HttpComponentsClientHttpRequestFactory
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);
        factory.setConnectTimeout(5000);
        factory.setConnectionRequestTimeout(5000);

        return new RestTemplate(factory);
    }
}
