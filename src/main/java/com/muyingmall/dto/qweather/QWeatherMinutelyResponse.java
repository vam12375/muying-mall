package com.muyingmall.dto.qweather;

import lombok.Data;

import java.util.List;

/**
 * 和风天气分钟级降水响应
 * 官方文档：https://dev.qweather.com/docs/api/minutely/
 */
@Data
public class QWeatherMinutelyResponse {
    
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
     * 分钟降水描述
     */
    private String summary;
    
    /**
     * 分钟降水数据集合
     */
    private List<MinutelyData> minutely;
    
    /**
     * 数据来源
     */
    private Refer refer;
    
    @Data
    public static class MinutelyData {
        /**
         * 预报时间
         */
        private String fxTime;
        
        /**
         * 5分钟累计降水量，单位毫米
         */
        private String precip;
        
        /**
         * 降水类型：rain-雨，snow-雪
         */
        private String type;
    }
    
    @Data
    public static class Refer {
        private List<String> sources;
        private List<String> license;
    }
}
