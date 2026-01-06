package com.muyingmall.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.muyingmall.config.AMapConfig;
import com.muyingmall.dto.amap.DistanceResponse;
import com.muyingmall.dto.amap.DrivingRouteResponse;
import com.muyingmall.dto.amap.GeoCodeResponse;
import com.muyingmall.dto.amap.RegeoCodeResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * 高德地图API服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AMapService {

    private final AMapConfig amapConfig;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    private static final String GEO_CODE_URL = "https://restapi.amap.com/v3/geocode/geo";
    private static final String REGEO_CODE_URL = "https://restapi.amap.com/v3/geocode/regeo";
    private static final String DISTANCE_URL = "https://restapi.amap.com/v3/distance";
    private static final String DRIVING_ROUTE_URL = "https://restapi.amap.com/v3/direction/driving";
    private static final String IP_LOCATION_URL = "https://restapi.amap.com/v3/ip";

    /**
     * 地理编码：将地址转换为经纬度
     *
     * @param address 地址字符串
     * @return 地理编码响应
     */
    public GeoCodeResponse geoCode(String address) {
        try {
            String url = UriComponentsBuilder.fromHttpUrl(GEO_CODE_URL)
                    .queryParam("key", amapConfig.getWebKey())
                    .queryParam("address", URLEncoder.encode(address, StandardCharsets.UTF_8))
                    .build(false)
                    .toUriString();

            log.info("【高德API】调用地理编码API: address={}", address);
            log.info("【高德API】请求URL: {}", url);
            
            String response = restTemplate.getForObject(url, String.class);
            log.info("【高德API】原始响应: {}", response);
            
            GeoCodeResponse result = objectMapper.readValue(response, GeoCodeResponse.class);

            if (!"1".equals(result.getStatus())) {
                log.error("【高德API】地理编码失败: status={}, info={}, infocode={}", 
                        result.getStatus(), result.getInfo(), result.getInfoCode());
                return null;
            }

            if (result.getGeoCodes() == null || result.getGeoCodes().isEmpty()) {
                log.warn("【高德API】地理编码返回空结果: count={}", result.getCount());
                return null;
            }

            log.info("【高德API】地理编码成功: count={}, location={}", 
                    result.getCount(), result.getGeoCodes().get(0).getLocation());
            
            return result;
        } catch (Exception e) {
            log.error("【高德API】地理编码异常: address={}, error={}", address, e.getMessage(), e);
            return null;
        }
    }

    /**
     * 逆地理编码：将经纬度转换为地址
     *
     * @param longitude 经度
     * @param latitude  纬度
     * @return 逆地理编码响应
     */
    public RegeoCodeResponse regeoCode(Double longitude, Double latitude) {
        try {
            String location = longitude + "," + latitude;
            String url = UriComponentsBuilder.fromHttpUrl(REGEO_CODE_URL)
                    .queryParam("key", amapConfig.getWebKey())
                    .queryParam("location", location)
                    .queryParam("extensions", "base") // base-基本信息，all-详细信息
                    .build()
                    .toUriString();

            log.info("调用高德地图逆地理编码API: location={}", location);
            String response = restTemplate.getForObject(url, String.class);
            RegeoCodeResponse result = objectMapper.readValue(response, RegeoCodeResponse.class);

            if (!"1".equals(result.getStatus())) {
                log.error("逆地理编码失败: {}", result.getInfo());
                return null;
            }

            return result;
        } catch (Exception e) {
            log.error("逆地理编码异常: longitude={}, latitude={}", longitude, latitude, e);
            return null;
        }
    }

    /**
     * 计算两点之间的距离
     *
     * @param originLng 起点经度
     * @param originLat 起点纬度
     * @param destLng   终点经度
     * @param destLat   终点纬度
     * @return 距离（公里）
     */
    public Double calculateDistance(Double originLng, Double originLat, Double destLng, Double destLat) {
        try {
            String origins = originLng + "," + originLat;
            String destination = destLng + "," + destLat;

            String url = UriComponentsBuilder.fromHttpUrl(DISTANCE_URL)
                    .queryParam("key", amapConfig.getWebKey())
                    .queryParam("origins", origins)
                    .queryParam("destination", destination)
                    .queryParam("type", "1") // 1-直线距离，0-驾车距离
                    .build()
                    .toUriString();

            log.info("调用高德地图距离测量API: origins={}, destination={}", origins, destination);
            String response = restTemplate.getForObject(url, String.class);
            DistanceResponse result = objectMapper.readValue(response, DistanceResponse.class);

            if (!"1".equals(result.getStatus()) || result.getResults() == null || result.getResults().isEmpty()) {
                log.error("距离测量失败: {}", result.getInfo());
                return null;
            }

            // 转换为公里
            String distanceStr = result.getResults().get(0).getDistance();
            return Double.parseDouble(distanceStr) / 1000.0;
        } catch (Exception e) {
            log.error("距离测量异常", e);
            return null;
        }
    }

    /**
     * 从仓库到目标地址计算距离
     *
     * @param destLng 目标经度
     * @param destLat 目标纬度
     * @return 距离（公里）
     */
    public Double calculateDistanceFromWarehouse(Double destLng, Double destLat) {
        return calculateDistance(
                amapConfig.getWarehouse().getLongitude(),
                amapConfig.getWarehouse().getLatitude(),
                destLng,
                destLat
        );
    }

    /**
     * 【场景3：物流轨迹可视化】驾车路径规划
     * 从杭州仓库到目标地址的驾车路线规划
     *
     * @param destLng 目标经度
     * @param destLat 目标纬度
     * @return 驾车路径规划响应
     */
    public DrivingRouteResponse drivingRoute(Double destLng, Double destLat) {
        try {
            // 起点：杭州仓库坐标
            String origin = amapConfig.getWarehouse().getLongitude() + "," + amapConfig.getWarehouse().getLatitude();
            // 终点：收货地址坐标
            String destination = destLng + "," + destLat;

            String url = UriComponentsBuilder.fromHttpUrl(DRIVING_ROUTE_URL)
                    .queryParam("key", amapConfig.getWebKey())
                    .queryParam("origin", origin)
                    .queryParam("destination", destination)
                    .queryParam("extensions", "all") // all-返回全部信息（包含steps）
                    .queryParam("strategy", "10") // 10-躲避拥堵，路程较短
                    .build()
                    .toUriString();

            log.info("调用高德地图驾车路径规划API: origin={}, destination={}", origin, destination);
            String response = restTemplate.getForObject(url, String.class);
            DrivingRouteResponse result = objectMapper.readValue(response, DrivingRouteResponse.class);

            if (!"1".equals(result.getStatus())) {
                log.error("驾车路径规划失败: {}", result.getInfo());
                return null;
            }

            if (result.getRoute() == null || result.getRoute().getPaths() == null || result.getRoute().getPaths().isEmpty()) {
                log.error("驾车路径规划返回空结果");
                return null;
            }

            log.info("驾车路径规划成功: 方案数={}, 距离={}米, 时长={}秒",
                    result.getRoute().getPaths().size(),
                    result.getRoute().getPaths().get(0).getDistance(),
                    result.getRoute().getPaths().get(0).getDuration());

            return result;
        } catch (Exception e) {
            log.error("驾车路径规划异常: destLng={}, destLat={}", destLng, destLat, e);
            return null;
        }
    }

    /**
     * IP定位：根据IP地址获取城市信息
     * 若不传入IP，则自动使用请求来源IP
     *
     * @param ip IP地址（可选，不传则使用请求IP）
     * @return IP定位结果（包含省份、城市、adcode等）
     */
    public Map<String, Object> getLocationByIP(String ip) {
        try {
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(IP_LOCATION_URL)
                    .queryParam("key", amapConfig.getWebKey());
            
            // 如果传入了IP，则添加IP参数
            if (ip != null && !ip.isEmpty()) {
                builder.queryParam("ip", ip);
            }
            
            String url = builder.build().toUriString();

            log.info("【高德API】调用IP定位API: ip={}", ip != null ? ip : "自动检测");
            String response = restTemplate.getForObject(url, String.class);
            log.debug("【高德API】IP定位原始响应: {}", response);
            
            @SuppressWarnings("unchecked")
            Map<String, Object> result = objectMapper.readValue(response, Map.class);

            if (!"1".equals(result.get("status").toString())) {
                log.error("【高德API】IP定位失败: status={}, info={}", 
                        result.get("status"), result.get("info"));
                return null;
            }

            log.info("【高德API】IP定位成功: province={}, city={}, adcode={}", 
                    result.get("province"), result.get("city"), result.get("adcode"));
            
            return result;
        } catch (Exception e) {
            log.error("【高德API】IP定位异常: ip={}, error={}", ip, e.getMessage(), e);
            return null;
        }
    }
}
