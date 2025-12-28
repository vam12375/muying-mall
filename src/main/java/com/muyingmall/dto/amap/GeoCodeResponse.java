package com.muyingmall.dto.amap;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * 高德地图地理编码API响应
 */
@Data
public class GeoCodeResponse {

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
    @JsonProperty("infocode")
    private String infoCode;

    /**
     * 地理编码结果数量
     */
    private String count;

    /**
     * 地理编码结果列表
     */
    @JsonProperty("geocodes")
    private List<GeoCodes> geoCodes;

    @Data
    public static class GeoCodes {
        /**
         * 格式化地址
         */
        @JsonProperty("formatted_address")
        private String formattedAddress;

        /**
         * 省份
         */
        private String province;

        /**
         * 城市
         */
        private String city;

        /**
         * 区县
         */
        private String district;

        /**
         * 街道
         */
        private String street;

        /**
         * 门牌号
         */
        private String number;

        /**
         * 区域编码
         */
        @JsonProperty("adcode")
        private String adCode;

        /**
         * 坐标（经度,纬度）
         */
        private String location;

        /**
         * 匹配级别
         */
        private String level;
    }
}
