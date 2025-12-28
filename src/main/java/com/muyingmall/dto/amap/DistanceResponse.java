package com.muyingmall.dto.amap;

import lombok.Data;

import java.util.List;

/**
 * 高德地图距离测量API响应
 */
@Data
public class DistanceResponse {

    /**
     * 返回状态：1-成功，0-失败
     */
    private String status;

    /**
     * 返回信息
     */
    private String info;

    /**
     * 状态码
     */
    private String infocode;

    /**
     * 距离结果数量
     */
    private String count;

    /**
     * 距离结果列表
     */
    private List<DistanceResult> results;

    @Data
    public static class DistanceResult {
        /**
         * 起点坐标
         */
        private String origin;

        /**
         * 终点坐标
         */
        private String dest;

        /**
         * 距离（米）
         */
        private String distance;

        /**
         * 预计时间（秒）
         */
        private String duration;

        /**
         * 状态码
         */
        private String code;
    }
}
