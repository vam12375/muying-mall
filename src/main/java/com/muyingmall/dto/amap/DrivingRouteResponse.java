package com.muyingmall.dto.amap;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * 高德地图驾车路径规划响应DTO
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true) // 忽略未知字段，避免反序列化错误
public class DrivingRouteResponse {

    /**
     * 返回状态：1-成功，0-失败
     */
    private String status;

    /**
     * 返回信息
     */
    private String info;

    /**
     * 返回结果总数
     */
    private String count;

    /**
     * 路线方案
     */
    private Route route;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true) // 忽略未知字段
    public static class Route {
        /**
         * 起点坐标
         */
        private String origin;

        /**
         * 终点坐标
         */
        private String destination;

        /**
         * 驾车方案列表
         */
        private List<Path> paths;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true) // 忽略未知字段
    public static class Path {
        /**
         * 行驶距离（米）
         */
        private String distance;

        /**
         * 预计行驶时间（秒）
         */
        private String duration;

        /**
         * 导航策略
         */
        private String strategy;

        /**
         * 道路收费（元）
         */
        private String tolls;

        /**
         * 收费路段距离（米）
         */
        @JsonProperty("toll_distance")
        private String tollDistance;

        /**
         * 红绿灯个数
         */
        @JsonProperty("traffic_lights")
        private String trafficLights;

        /**
         * 导航路段列表
         */
        private List<Step> steps;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true) // 忽略未知字段
    public static class Step {
        /**
         * 行驶指示
         */
        private String instruction;

        /**
         * 方向
         */
        private String orientation;

        /**
         * 道路名称
         */
        private String road;

        /**
         * 此路段距离（米）
         */
        private String distance;

        /**
         * 此路段收费（元）
         */
        private String tolls;

        /**
         * 收费路段距离（米）
         */
        @JsonProperty("toll_distance")
        private String tollDistance;

        /**
         * 此路段坐标点串（经度,纬度;经度,纬度...）
         */
        private String polyline;

        /**
         * 导航主要动作（可能是数组或字符串，使用Object类型兼容）
         */
        private Object action;

        /**
         * 导航辅助动作（可能是数组或字符串，使用Object类型兼容）
         */
        @JsonProperty("assistant_action")
        private Object assistantAction;

        /**
         * 预计行驶时间（秒）
         */
        private String duration;
    }
}
