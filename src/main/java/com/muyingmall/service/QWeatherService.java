package com.muyingmall.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.muyingmall.config.QWeatherConfig;
import com.muyingmall.dto.qweather.QWeatherAirResponse;
import com.muyingmall.dto.qweather.QWeatherMinutelyResponse;
import com.muyingmall.dto.qweather.QWeatherNowResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.HashMap;
import java.util.Map;

/**
 * 和风天气服务
 * 官方文档：https://dev.qweather.com/docs/api/
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class QWeatherService {

    private final RestTemplate qweatherRestTemplate; // 使用支持Gzip的RestTemplate
    private final QWeatherConfig qweatherConfig;
    private final ObjectMapper objectMapper;

    /**
     * 获取实时天气
     * @param location 城市ID或经纬度（格式：116.41,39.92）
     * @return 实时天气数据
     */
    public QWeatherNowResponse getRealtimeWeather(String location) {
        if (!qweatherConfig.isEnabled()) {
            log.warn("【和风天气】服务未启用");
            return null;
        }

        try {
            // 构建请求URL（不包含key参数）
            String url = UriComponentsBuilder
                    .fromHttpUrl("https://" + qweatherConfig.getApiHost() + "/v7/weather/now")
                    .queryParam("location", location)
                    .build()
                    .toUriString();

            log.info("【和风天气】调用实时天气API: location={}", location);
            log.debug("【和风天气】完整请求URL: {}", url);
            
            // 使用请求标头传递API KEY
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.set("X-QW-Api-Key", qweatherConfig.getApiKey());
            
            org.springframework.http.HttpEntity<String> entity = new org.springframework.http.HttpEntity<>(headers);
            
            // 使用支持Gzip的RestTemplate，会自动解压响应
            org.springframework.http.ResponseEntity<String> responseEntity = qweatherRestTemplate.exchange(
                    url,
                    org.springframework.http.HttpMethod.GET,
                    entity,
                    String.class
            );
            
            String response = responseEntity.getBody();
            log.debug("【和风天气】实时天气原始响应: {}", response);
            
            QWeatherNowResponse result = objectMapper.readValue(response, QWeatherNowResponse.class);

            if (!"200".equals(result.getCode())) {
                log.error("【和风天气】实时天气查询失败: code={}", result.getCode());
                log.error("【和风天气】可能原因: 1) API Key无效 2) API Host配置错误 3) 配额不足");
                log.error("【和风天气】请检查: https://console.qweather.com/#/apps");
                return null;
            }

            log.info("【和风天气】实时天气查询成功: temp={}°C, text={}", 
                    result.getNow().getTemp(), result.getNow().getText());
            
            return result;
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            log.error("【和风天气】HTTP错误: status={}, message={}", e.getStatusCode(), e.getMessage());
            log.error("【和风天气】请检查API Key是否正确，访问 https://console.qweather.com/#/apps 确认");
            return null;
        } catch (Exception e) {
            log.error("【和风天气】实时天气查询异常: location={}, error={}", location, e.getMessage(), e);
            return null;
        }
    }

    /**
     * 城市ID到经纬度的映射表（从和风天气热门城市API获取）
     * 避免每次都调用GeoAPI，提升性能并减少API调用次数
     */
    private static final Map<String, double[]> CITY_COORDINATES = new HashMap<String, double[]>() {{
        put("101010100", new double[]{39.90499, 116.40529});  // 北京
        put("101020100", new double[]{31.23171, 121.47264});  // 上海
        put("101030100", new double[]{39.08333, 117.20000});  // 天津
        put("101040100", new double[]{29.56667, 106.55000});  // 重庆
        put("101210101", new double[]{30.28747, 120.15507});  // 杭州
        put("101280101", new double[]{23.12908, 113.26436});  // 广州
        put("101280601", new double[]{22.54700, 114.08595});  // 深圳
        put("101270101", new double[]{30.66667, 104.06667});  // 成都
        put("101110101", new double[]{34.34167, 108.94000});  // 西安
        put("101200101", new double[]{30.59333, 114.30500});  // 武汉
        put("101190101", new double[]{32.05840, 118.79647});  // 南京
        put("101190401", new double[]{31.30000, 120.58333});  // 苏州
        put("101180101", new double[]{34.74667, 113.62500});  // 郑州
        put("101250101", new double[]{28.22778, 112.93886});  // 长沙
        put("101070101", new double[]{41.80556, 123.43278});  // 沈阳
        put("101120201", new double[]{36.06694, 120.38264});  // 青岛
        put("101120101", new double[]{36.65167, 117.12000});  // 济南
        put("101050101", new double[]{45.80333, 126.53500});  // 哈尔滨
        put("101230101", new double[]{26.07421, 119.29647});  // 福州
        put("101230201", new double[]{24.47943, 118.08900});  // 厦门
        put("101290101", new double[]{25.04000, 102.71667});  // 昆明
        put("101240101", new double[]{28.68333, 115.85806});  // 南昌
        put("101260101", new double[]{26.58333, 106.71667});  // 贵阳
        put("101100101", new double[]{37.87059, 112.54861});  // 太原
        put("101160101", new double[]{36.06111, 103.83417});  // 兰州
        put("101090101", new double[]{38.05000, 114.51667});  // 石家庄
        put("101220101", new double[]{31.82057, 117.22901});  // 合肥
        put("101300101", new double[]{22.81667, 108.36667});  // 南宁
        put("101310101", new double[]{20.03167, 110.32000});  // 海口
        put("101170101", new double[]{38.46637, 106.27800});  // 银川
        put("101150101", new double[]{36.61670, 101.77820});  // 西宁
        put("101080101", new double[]{40.81831, 111.66035});  // 呼和浩特
        put("101130101", new double[]{43.82663, 87.61688});   // 乌鲁木齐
        put("101140101", new double[]{29.64415, 91.11450});   // 拉萨
    }};

    /**
     * 通过城市ID获取城市经纬度（使用本地映射表）
     * @param locationId 城市ID（如：101210101）
     * @return 经纬度数组 [纬度, 经度]，失败返回null
     */
    private double[] getLocationCoordinates(String locationId) {
        double[] coords = CITY_COORDINATES.get(locationId);
        if (coords != null) {
            log.debug("【和风天气】从本地映射表获取城市坐标: locationId={}, lat={}, lon={}", 
                    locationId, coords[0], coords[1]);
            return coords;
        }
        log.warn("【和风天气】未找到城市坐标映射: locationId={}", locationId);
        return null;
    }

    /**
     * 获取实时空气质量（使用经纬度API）
     * @param location 城市ID（如：101210101）
     * @return 空气质量数据
     */
    public QWeatherAirResponse getAirQuality(String location) {
        if (!qweatherConfig.isEnabled()) {
            log.warn("【和风天气】服务未启用");
            return null;
        }

        try {
            // 先通过本地映射表获取城市经纬度
            double[] coordinates = getLocationCoordinates(location);
            if (coordinates == null) {
                log.warn("【和风天气】无法获取城市坐标，跳过空气质量查询");
                return null;
            }
            
            double lat = coordinates[0];
            double lon = coordinates[1];
            
            // 使用经纬度调用空气质量API - 路径格式：/airquality/v1/current/{latitude}/{longitude}
            // 注意：空气质量API使用查询参数key而不是请求头
            String url = UriComponentsBuilder
                    .fromHttpUrl(String.format("https://%s/airquality/v1/current/%.2f/%.2f", 
                            qweatherConfig.getApiHost(), lat, lon))
                    .queryParam("key", qweatherConfig.getApiKey())
                    .build()
                    .toUriString();

            log.info("【和风天气】调用空气质量API: location={}, lat={}, lon={}", location, lat, lon);
            log.debug("【和风天气】完整请求URL: {}", url);
            
            org.springframework.http.ResponseEntity<String> responseEntity = qweatherRestTemplate.exchange(
                    url,
                    org.springframework.http.HttpMethod.GET,
                    null,
                    String.class
            );
            
            String response = responseEntity.getBody();
            log.debug("【和风天气】空气质量原始响应: {}", response);
            
            // 解析新的空气质量API响应格式
            com.fasterxml.jackson.databind.JsonNode root = objectMapper.readTree(response);
            
            // 构建兼容旧格式的响应对象
            QWeatherAirResponse result = new QWeatherAirResponse();
            result.setCode("200"); // 空气质量API成功返回即为200
            
            // 从indexes数组中获取第一个AQI（通常是当地标准）
            if (root.has("indexes") && root.path("indexes").isArray() && root.path("indexes").size() > 0) {
                com.fasterxml.jackson.databind.JsonNode firstIndex = root.path("indexes").get(0);
                
                QWeatherAirResponse.NowData nowData = new QWeatherAirResponse.NowData();
                nowData.setAqi(firstIndex.path("aqiDisplay").asText());
                nowData.setLevel(firstIndex.path("level").asText());
                nowData.setCategory(firstIndex.path("category").asText());
                
                // 从pollutants数组中获取PM2.5和PM10
                if (root.has("pollutants") && root.path("pollutants").isArray()) {
                    for (com.fasterxml.jackson.databind.JsonNode pollutant : root.path("pollutants")) {
                        String code = pollutant.path("code").asText();
                        String value = pollutant.path("concentration").path("value").asText();
                        
                        if ("pm2p5".equals(code)) {
                            nowData.setPm2p5(value);
                        } else if ("pm10".equals(code)) {
                            nowData.setPm10(value);
                        } else if ("no2".equals(code)) {
                            nowData.setNo2(value);
                        } else if ("so2".equals(code)) {
                            nowData.setSo2(value);
                        } else if ("co".equals(code)) {
                            nowData.setCo(value);
                        } else if ("o3".equals(code)) {
                            nowData.setO3(value);
                        }
                    }
                }
                
                // 设置首要污染物
                if (firstIndex.has("primaryPollutant") && !firstIndex.path("primaryPollutant").isNull()) {
                    nowData.setPrimary(firstIndex.path("primaryPollutant").path("name").asText());
                }
                
                result.setNow(nowData);
            }

            log.info("【和风天气】空气质量查询成功: aqi={}, level={}", 
                    result.getNow().getAqi(), result.getNow().getLevel());
            
            return result;
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            log.error("【和风天气】HTTP错误: status={}, message={}", e.getStatusCode(), e.getMessage());
            log.error("【和风天气】请检查API Key是否正确，访问 https://console.qweather.com/#/apps 确认");
            return null;
        } catch (Exception e) {
            log.error("【和风天气】空气质量查询异常: location={}, error={}", location, e.getMessage(), e);
            return null;
        }
    }

    /**
     * 获取综合天气信息（实时天气 + 空气质量）
     * @param location 城市ID或经纬度
     * @return 综合天气数据
     */
    public Map<String, Object> getWeatherInfo(String location) {
        Map<String, Object> result = new HashMap<>();

        // 获取实时天气
        QWeatherNowResponse weatherResponse = getRealtimeWeather(location);
        if (weatherResponse != null && weatherResponse.getNow() != null) {
            QWeatherNowResponse.NowData now = weatherResponse.getNow();
            
            result.put("temperature", now.getTemp());
            result.put("feelsLike", now.getFeelsLike());
            result.put("weather", now.getText());
            result.put("weatherIcon", now.getIcon());
            result.put("windDirection", now.getWindDir());
            result.put("windScale", now.getWindScale());
            result.put("windSpeed", now.getWindSpeed());
            result.put("humidity", now.getHumidity());
            result.put("pressure", now.getPressure());
            result.put("visibility", now.getVis());
            result.put("cloud", now.getCloud());
            result.put("dew", now.getDew());
            result.put("obsTime", now.getObsTime());
            result.put("updateTime", weatherResponse.getUpdateTime());
        }

        // 获取空气质量
        QWeatherAirResponse airResponse = getAirQuality(location);
        if (airResponse != null && airResponse.getNow() != null) {
            QWeatherAirResponse.NowData air = airResponse.getNow();
            
            result.put("aqi", air.getAqi());
            result.put("aqiLevel", air.getLevel());
            result.put("aqiCategory", air.getCategory());
            result.put("pm25", air.getPm2p5());
            result.put("pm10", air.getPm10());
        }

        return result.isEmpty() ? null : result;
    }

    /**
     * 获取7天天气预报
     * @param location 城市ID
     * @return 天气预报数据
     */
    public Map<String, Object> getWeatherForecast7d(String location) {
        if (!qweatherConfig.isEnabled()) {
            log.warn("【和风天气】服务未启用");
            return null;
        }

        try {
            String url = UriComponentsBuilder
                    .fromHttpUrl("https://" + qweatherConfig.getApiHost() + "/v7/weather/7d")
                    .queryParam("location", location)
                    .build()
                    .toUriString();

            log.info("【和风天气】调用7天预报API: location={}", location);
            
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.set("X-QW-Api-Key", qweatherConfig.getApiKey());
            
            org.springframework.http.HttpEntity<String> entity = new org.springframework.http.HttpEntity<>(headers);
            
            org.springframework.http.ResponseEntity<String> responseEntity = qweatherRestTemplate.exchange(
                    url,
                    org.springframework.http.HttpMethod.GET,
                    entity,
                    String.class
            );
            
            String response = responseEntity.getBody();
            
            @SuppressWarnings("unchecked")
            Map<String, Object> result = objectMapper.readValue(response, Map.class);

            if (!"200".equals(result.get("code"))) {
                log.error("【和风天气】7天预报查询失败: code={}", result.get("code"));
                return null;
            }

            log.info("【和风天气】7天预报查询成功");
            return result;
        } catch (Exception e) {
            log.error("【和风天气】7天预报查询异常: location={}, error={}", location, e.getMessage());
            return null;
        }
    }

    /**
     * 获取天气生活指数
     * @param location 城市ID
     * @param type 指数类型（1-运动指数，2-洗车指数，3-穿衣指数等，0-全部）
     * @return 生活指数数据
     */
    public Map<String, Object> getWeatherIndices(String location, String type) {
        if (!qweatherConfig.isEnabled()) {
            log.warn("【和风天气】服务未启用");
            return null;
        }

        try {
            String url = UriComponentsBuilder
                    .fromHttpUrl("https://" + qweatherConfig.getApiHost() + "/v7/indices/1d")
                    .queryParam("location", location)
                    .queryParam("type", type != null ? type : "0") // 0表示全部指数
                    .build()
                    .toUriString();

            log.info("【和风天气】调用生活指数API: location={}, type={}", location, type);
            
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.set("X-QW-Api-Key", qweatherConfig.getApiKey());
            
            org.springframework.http.HttpEntity<String> entity = new org.springframework.http.HttpEntity<>(headers);
            
            org.springframework.http.ResponseEntity<String> responseEntity = qweatherRestTemplate.exchange(
                    url,
                    org.springframework.http.HttpMethod.GET,
                    entity,
                    String.class
            );
            
            String response = responseEntity.getBody();
            
            @SuppressWarnings("unchecked")
            Map<String, Object> result = objectMapper.readValue(response, Map.class);

            if (!"200".equals(result.get("code"))) {
                log.error("【和风天气】生活指数查询失败: code={}", result.get("code"));
                return null;
            }

            log.info("【和风天气】生活指数查询成功");
            return result;
        } catch (Exception e) {
            log.error("【和风天气】生活指数查询异常: location={}, error={}", location, e.getMessage());
            return null;
        }
    }

    /**
     * 获取天气预警
     * @param location 城市ID
     * @return 预警数据
     */
    public Map<String, Object> getWeatherWarning(String location) {
        if (!qweatherConfig.isEnabled()) {
            log.warn("【和风天气】服务未启用");
            return null;
        }

        try {
            String url = UriComponentsBuilder
                    .fromHttpUrl("https://" + qweatherConfig.getApiHost() + "/v7/warning/now")
                    .queryParam("location", location)
                    .build()
                    .toUriString();

            log.info("【和风天气】调用预警API: location={}", location);
            
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.set("X-QW-Api-Key", qweatherConfig.getApiKey());
            
            org.springframework.http.HttpEntity<String> entity = new org.springframework.http.HttpEntity<>(headers);
            
            org.springframework.http.ResponseEntity<String> responseEntity = qweatherRestTemplate.exchange(
                    url,
                    org.springframework.http.HttpMethod.GET,
                    entity,
                    String.class
            );
            
            String response = responseEntity.getBody();
            
            @SuppressWarnings("unchecked")
            Map<String, Object> result = objectMapper.readValue(response, Map.class);

            if (!"200".equals(result.get("code"))) {
                log.error("【和风天气】预警查询失败: code={}", result.get("code"));
                return null;
            }

            log.info("【和风天气】预警查询成功");
            return result;
        } catch (Exception e) {
            log.error("【和风天气】预警查询异常: location={}, error={}", location, e.getMessage());
            return null;
        }
    }

    /**
     * 获取分钟级降水预报（未来2小时每5分钟）
     * @param location 经纬度（格式：116.41,39.92）或城市ID
     * @return 分钟级降水数据
     */
    public QWeatherMinutelyResponse getMinutelyPrecipitation(String location) {
        if (!qweatherConfig.isEnabled()) {
            log.warn("【和风天气】服务未启用");
            return null;
        }

        try {
            // 如果是城市ID，需要转换为经纬度
            String locationParam = location;
            if (location.length() == 9 && location.matches("\\d+")) {
                // 是城市ID，获取经纬度
                double[] coords = getLocationCoordinates(location);
                if (coords != null) {
                    locationParam = coords[1] + "," + coords[0]; // 经度,纬度
                } else {
                    log.warn("【和风天气】无法获取城市坐标，跳过分钟级降水查询");
                    return null;
                }
            }

            String url = UriComponentsBuilder
                    .fromHttpUrl("https://" + qweatherConfig.getApiHost() + "/v7/minutely/5m")
                    .queryParam("location", locationParam)
                    .build()
                    .toUriString();

            log.info("【和风天气】调用分钟级降水API: location={}", locationParam);
            
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.set("X-QW-Api-Key", qweatherConfig.getApiKey());
            
            org.springframework.http.HttpEntity<String> entity = new org.springframework.http.HttpEntity<>(headers);
            
            org.springframework.http.ResponseEntity<String> responseEntity = qweatherRestTemplate.exchange(
                    url,
                    org.springframework.http.HttpMethod.GET,
                    entity,
                    String.class
            );
            
            String response = responseEntity.getBody();
            log.debug("【和风天气】分钟级降水原始响应: {}", response);
            
            QWeatherMinutelyResponse result = objectMapper.readValue(response, QWeatherMinutelyResponse.class);

            if (!"200".equals(result.getCode())) {
                log.error("【和风天气】分钟级降水查询失败: code={}", result.getCode());
                return null;
            }

            log.info("【和风天气】分钟级降水查询成功: summary={}", result.getSummary());
            return result;
        } catch (Exception e) {
            log.error("【和风天气】分钟级降水查询异常: location={}, error={}", location, e.getMessage());
            return null;
        }
    }
}
