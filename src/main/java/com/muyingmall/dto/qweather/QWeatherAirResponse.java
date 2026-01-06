package com.muyingmall.dto.qweather;

import lombok.Data;

import java.util.List;

/**
 * 和风天气空气质量响应
 */
@Data
public class QWeatherAirResponse {
    
    /**
     * 状态码：200-成功
     */
    private String code;
    
    /**
     * 数据更新时间
     */
    private String updateTime;
    
    /**
     * 和风天气网页链接
     */
    private String fxLink;
    
    /**
     * 实时空气质量数据
     */
    private NowData now;
    
    /**
     * 数据来源
     */
    private Refer refer;
    
    @Data
    public static class NowData {
        /**
         * 数据发布时间
         */
        private String pubTime;
        
        /**
         * 空气质量指数
         */
        private String aqi;
        
        /**
         * 空气质量等级
         */
        private String level;
        
        /**
         * 空气质量类别
         */
        private String category;
        
        /**
         * 主要污染物
         */
        private String primary;
        
        /**
         * PM10浓度
         */
        private String pm10;
        
        /**
         * PM2.5浓度
         */
        private String pm2p5;
        
        /**
         * 二氧化氮浓度
         */
        private String no2;
        
        /**
         * 二氧化硫浓度
         */
        private String so2;
        
        /**
         * 一氧化碳浓度
         */
        private String co;
        
        /**
         * 臭氧浓度
         */
        private String o3;
    }
    
    @Data
    public static class Refer {
        private List<String> sources;
        private List<String> license;
    }
}
