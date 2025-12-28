package com.muyingmall.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 高德地图API配置类
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "amap")
public class AMapConfig {

    /**
     * Web服务API Key（后端调用）
     */
    private String webKey;

    /**
     * JS API Key（前端地图展示）
     */
    private String jsKey;

    /**
     * 安全密钥
     */
    private String secret;

    /**
     * 仓库配置
     */
    private Warehouse warehouse = new Warehouse();

    /**
     * 超时配置
     */
    private Timeout timeout = new Timeout();

    /**
     * 配送配置
     */
    private Delivery delivery = new Delivery();

    @Data
    public static class Warehouse {
        private Double longitude = 116.397428;
        private Double latitude = 39.90923;
        private String name = "主仓库";
    }

    @Data
    public static class Timeout {
        private Integer connect = 5000;
        private Integer read = 10000;
    }

    @Data
    public static class Delivery {
        private Double maxDistance = 50.0;
        private Double minDistance = 0.0;
    }
}
