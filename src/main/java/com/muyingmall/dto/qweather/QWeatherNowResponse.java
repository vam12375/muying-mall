package com.muyingmall.dto.qweather;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * 和风天气实时天气响应
 */
@Data
public class QWeatherNowResponse {
    
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
     * 实时天气数据
     */
    private NowData now;
    
    /**
     * 数据来源
     */
    private Refer refer;
    
    @Data
    public static class NowData {
        /**
         * 数据观测时间
         */
        private String obsTime;
        
        /**
         * 温度（摄氏度）
         */
        private String temp;
        
        /**
         * 体感温度（摄氏度）
         */
        private String feelsLike;
        
        /**
         * 天气图标代码
         */
        private String icon;
        
        /**
         * 天气状况文字描述
         */
        private String text;
        
        /**
         * 风向360角度
         */
        private String wind360;
        
        /**
         * 风向文字描述
         */
        private String windDir;
        
        /**
         * 风力等级
         */
        private String windScale;
        
        /**
         * 风速（公里/小时）
         */
        private String windSpeed;
        
        /**
         * 相对湿度（百分比）
         */
        private String humidity;
        
        /**
         * 当前小时累计降水量（毫米）
         */
        private String precip;
        
        /**
         * 大气压强（百帕）
         */
        private String pressure;
        
        /**
         * 能见度（公里）
         */
        private String vis;
        
        /**
         * 云量（百分比）
         */
        private String cloud;
        
        /**
         * 露点温度（摄氏度）
         */
        private String dew;
    }
    
    @Data
    public static class Refer {
        private List<String> sources;
        private List<String> license;
    }
}
