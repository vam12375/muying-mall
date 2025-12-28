package com.muyingmall.dto.amap;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;

import java.io.IOException;

/**
 * 高德地图逆地理编码API响应
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class RegeoCodeResponse {

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
     * 逆地理编码结果
     */
    @JsonProperty("regeocode")
    private RegeoCode regeoCode;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RegeoCode {
        /**
         * 格式化地址
         */
        @JsonProperty("formatted_address")
        private String formattedAddress;

        /**
         * 地址组件
         */
        @JsonProperty("addressComponent")
        private AddressComponent addressComponent;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AddressComponent {
        /**
         * 省份
         */
        private String province;

        /**
         * 城市（可能为空数组[]，使用自定义反序列化器处理）
         */
        @JsonDeserialize(using = CityDeserializer.class)
        private String city;

        /**
         * 区县
         */
        private String district;

        /**
         * 区域编码
         */
        @JsonProperty("adcode")
        private String adCode;
    }

    /**
     * 城市字段自定义反序列化器
     * 处理高德API返回的特殊情况：直辖市的city字段为空数组[]
     */
    public static class CityDeserializer extends JsonDeserializer<String> {
        @Override
        public String deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            JsonNode node = p.getCodec().readTree(p);
            
            // 如果是数组（空数组），返回空字符串
            if (node.isArray()) {
                return "";
            }
            
            // 如果是字符串，直接返回
            if (node.isTextual()) {
                return node.asText();
            }
            
            // 其他情况返回空字符串
            return "";
        }
    }
}
