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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
    private static final String GEO_API_HOST = "geoapi.qweather.com";

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
     * Load city coordinates from QWeather LocationList CSV.
     * Only Zhejiang and Jiangxi city-level records are cached locally.
     */
    private static final String CITY_COORDINATE_SOURCE_URL =
            "https://raw.githubusercontent.com/qwd/LocationList/master/China-City-List-latest.csv";
    private static final int CITY_COORDINATE_CONNECT_TIMEOUT_MS = 5000;
    private static final int CITY_COORDINATE_READ_TIMEOUT_MS = 10000;
    private static final Map<String, double[]> DEFAULT_CITY_COORDINATES = buildDefaultCityCoordinates();
    private static final Map<String, double[]> CITY_COORDINATES = loadCityCoordinatesFromCsv();


    private static final Map<String, double[]> DYNAMIC_CITY_COORDINATES = new ConcurrentHashMap<>();

    private static Map<String, double[]> loadCityCoordinatesFromCsv() {
        Map<String, double[]> cityCoordinates = new HashMap<>(DEFAULT_CITY_COORDINATES);
        HttpURLConnection connection = null;
        try {
            URL sourceUrl = new URL(CITY_COORDINATE_SOURCE_URL);
            connection = (HttpURLConnection) sourceUrl.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(CITY_COORDINATE_CONNECT_TIMEOUT_MS);
            connection.setReadTimeout(CITY_COORDINATE_READ_TIMEOUT_MS);
            connection.setDoInput(true);

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                // First line is version, second line is header
                reader.readLine();
                reader.readLine();

                String line;
                while ((line = reader.readLine()) != null) {
                    String[] columns = line.split(",", -1);
                    if (columns.length < 14) {
                        continue;
                    }

                    String locationId = columns[0].trim();
                    String adm1NameZh = columns[7].trim();
                    String adCode = columns[13].trim();
                    String latText = columns[11].trim();
                    String lonText = columns[12].trim();

                    if (locationId.isEmpty() || latText.isEmpty() || lonText.isEmpty()) {
                        continue;
                    }
                    if (!isTargetProvince(adm1NameZh) || !isCityLevelAdCode(adCode)) {
                        continue;
                    }

                    try {
                        double lat = Double.parseDouble(latText);
                        double lon = Double.parseDouble(lonText);
                        cityCoordinates.put(locationId, new double[]{lat, lon});
                    } catch (NumberFormatException ignore) {
                        // skip invalid coordinates
                    }
                }
            }

            log.info("[QWeather] loaded city coordinate mappings: totalCount={}, defaultCount={}, source={}, provinces=Zhejiang/Jiangxi",
                    cityCoordinates.size(), DEFAULT_CITY_COORDINATES.size(), CITY_COORDINATE_SOURCE_URL);
        } catch (Exception ex) {
            log.warn("[QWeather] failed to load remote city mappings, use built-in defaults only: defaultCount={}, source={}, error={}",
                    cityCoordinates.size(), CITY_COORDINATE_SOURCE_URL, ex.getMessage());
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return cityCoordinates;
    }

    private static Map<String, double[]> buildDefaultCityCoordinates() {
        Map<String, double[]> map = new HashMap<>();

        map.put("101010100", new double[]{39.90499, 116.40529});  // 北京
        map.put("101020100", new double[]{31.23171, 121.47264});  // 上海
        map.put("101030100", new double[]{39.08333, 117.20000});  // 天津
        map.put("101040100", new double[]{29.56667, 106.55000});  // 重庆
        map.put("101210101", new double[]{30.27415, 120.15515});  // 杭州
        map.put("101280101", new double[]{23.12908, 113.26436});  // 广州
        map.put("101280601", new double[]{22.54700, 114.08595});  // 深圳
        map.put("101270101", new double[]{30.66667, 104.06667});  // 成都
        map.put("101110101", new double[]{34.34167, 108.94000});  // 西安
        map.put("101200101", new double[]{30.59333, 114.30500});  // 武汉
        map.put("101190101", new double[]{32.05840, 118.79647});  // 南京
        map.put("101190401", new double[]{31.30000, 120.58333});  // 苏州
        map.put("101180101", new double[]{34.74667, 113.62500});  // 郑州
        map.put("101250101", new double[]{28.22778, 112.93886});  // 长沙
        map.put("101070101", new double[]{41.80556, 123.43278});  // 沈阳
        map.put("101120201", new double[]{36.06694, 120.38264});  // 青岛
        map.put("101120101", new double[]{36.65167, 117.12000});  // 济南
        map.put("101050101", new double[]{45.80333, 126.53500});  // 哈尔滨
        map.put("101230101", new double[]{26.07421, 119.29647});  // 福州
        map.put("101230201", new double[]{24.47943, 118.08900});  // 厦门
        map.put("101290101", new double[]{25.04000, 102.71667});  // 昆明
        map.put("101240101", new double[]{28.68333, 115.85806});  // 南昌
        map.put("101260101", new double[]{26.58333, 106.71667});  // 贵阳
        map.put("101100101", new double[]{37.87059, 112.54861});  // 太原
        map.put("101160101", new double[]{36.06111, 103.83417});  // 兰州
        map.put("101090101", new double[]{38.05000, 114.51667});  // 石家庄
        map.put("101220101", new double[]{31.82057, 117.22901});  // 合肥
        map.put("101300101", new double[]{22.81667, 108.36667});  // 南宁
        map.put("101310101", new double[]{20.03167, 110.32000});  // 海口
        map.put("101170101", new double[]{38.46637, 106.27800});  // 银川
        map.put("101150101", new double[]{36.61670, 101.77820});  // 西宁
        map.put("101080101", new double[]{40.81831, 111.66035});  // 呼和浩特
        map.put("101130101", new double[]{43.82663, 87.61688});   // 乌鲁木齐
        map.put("101140101", new double[]{29.64415, 91.11450});   // 拉萨
        map.put("101210401", new double[]{29.86833, 121.54400});  // 宁波
        map.put("101210601", new double[]{27.99492, 120.69939});  // 温州
        map.put("101190201", new double[]{31.49117, 120.31191});  // 无锡
        map.put("101191101", new double[]{31.81232, 119.97406});  // 常州
        map.put("101281601", new double[]{23.02067, 113.75179});  // 东莞
        map.put("101280800", new double[]{23.02185, 113.12192});  // 佛山
        map.put("101280801", new double[]{23.02185, 113.12192});  // 佛山（兼容ID）
        map.put("101280701", new double[]{22.27073, 113.57668});  // 珠海
        map.put("101281701", new double[]{22.51765, 113.39277});  // 中山
        map.put("101280301", new double[]{23.07940, 114.41260});  // 惠州
        map.put("101281101", new double[]{22.57865, 113.08161});  // 江门
        map.put("101070201", new double[]{38.91459, 121.61468});  // 大连
        map.put("101060101", new double[]{43.81707, 125.32357});  // 长春
        map.put("101120501", new double[]{37.46353, 121.44794});  // 烟台
        map.put("101121301", new double[]{37.50969, 122.11639});  // 威海
        map.put("101230501", new double[]{24.87389, 118.67587});  // 泉州

        // 浙江省地级市（补齐）
        map.put("101210201", new double[]{30.89250, 120.08682});  // 湖州
        map.put("101210301", new double[]{30.75220, 120.75550});  // 嘉兴
        map.put("101210501", new double[]{30.03033, 120.58020});  // 绍兴
        map.put("101210701", new double[]{28.46720, 119.92178});  // 丽水
        map.put("101210801", new double[]{28.65611, 121.42056});  // 台州
        map.put("101210901", new double[]{29.08952, 119.64951});  // 金华
        map.put("101211001", new double[]{28.93592, 118.87419});  // 衢州
        map.put("101211101", new double[]{29.98529, 122.20778});  // 舟山

        // 江西省地级市（补齐）
        map.put("101240201", new double[]{29.70577, 116.00193});  // 九江
        map.put("101240301", new double[]{28.45463, 117.94357});  // 上饶
        map.put("101240401", new double[]{27.94922, 116.35835});  // 抚州
        map.put("101240501", new double[]{27.80430, 114.38995});  // 宜春
        map.put("101240601", new double[]{27.11170, 114.98637});  // 吉安
        map.put("101240701", new double[]{25.83109, 114.93591});  // 赣州
        map.put("101240801", new double[]{29.26869, 117.17839});  // 景德镇
        map.put("101240901", new double[]{27.62289, 113.85427});  // 萍乡
        map.put("101241001", new double[]{27.81781, 114.91713});  // 新余
        map.put("101241101", new double[]{28.23864, 117.03953});  // 鹰潭

        return map;
    }

    private static boolean isTargetProvince(String adm1NameZh) {
        return adm1NameZh != null
                && (adm1NameZh.startsWith("\u6D59\u6C5F") || adm1NameZh.startsWith("\u6C5F\u897F"));
    }

    private static boolean isCityLevelAdCode(String adCode) {
        return adCode != null
                && adCode.length() == 6
                && !adCode.endsWith("0000")
                && adCode.endsWith("00");
    }

    /**
     * 通过城市ID获取城市经纬度（使用本地映射表）
     * @param locationId 城市ID（如：101210101）
     * @return 经纬度数组 [纬度, 经度]，失败返回null
     */
    private double[] getLocationCoordinates(String locationId) {
        if (locationId == null || locationId.isBlank()) {
            return null;
        }

        // 支持直接经纬度参数：lon,lat
        if (locationId.contains(",")) {
            String[] parts = locationId.split(",");
            if (parts.length == 2) {
                try {
                    double lon = Double.parseDouble(parts[0].trim());
                    double lat = Double.parseDouble(parts[1].trim());
                    return new double[]{lat, lon};
                } catch (NumberFormatException ignore) {
                    // ignore and continue
                }
            }
        }

        double[] coords = CITY_COORDINATES.get(locationId);
        if (coords != null) {
            log.debug("【和风天气】从本地映射表获取城市坐标: locationId={}, lat={}, lon={}",
                    locationId, coords[0], coords[1]);
            return coords;
        }

        double[] dynamicCoords = DYNAMIC_CITY_COORDINATES.get(locationId);
        if (dynamicCoords != null) {
            return dynamicCoords;
        }

        double[] resolvedCoords = resolveCoordinatesFromGeoApi(locationId);
        if (resolvedCoords != null) {
            DYNAMIC_CITY_COORDINATES.put(locationId, resolvedCoords);
            log.info("【和风天气】GeoAPI动态解析城市坐标成功: locationId={}, lat={}, lon={}",
                    locationId, resolvedCoords[0], resolvedCoords[1]);
            return resolvedCoords;
        }

        log.warn("【和风天气】未找到城市坐标映射: locationId={}", locationId);
        return null;
    }

    private double[] resolveCoordinatesFromGeoApi(String locationId) {
        try {
            String url = UriComponentsBuilder
                    .fromHttpUrl("https://" + GEO_API_HOST + "/v2/city/lookup")
                    .queryParam("location", locationId)
                    .queryParam("number", 1)
                    .build()
                    .toUriString();

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
            if (response == null || response.isBlank()) {
                return null;
            }

            com.fasterxml.jackson.databind.JsonNode root = objectMapper.readTree(response);
            if (!"200".equals(root.path("code").asText())) {
                return null;
            }

            com.fasterxml.jackson.databind.JsonNode locations = root.path("location");
            if (!locations.isArray() || locations.isEmpty()) {
                return null;
            }

            com.fasterxml.jackson.databind.JsonNode first = locations.get(0);
            String latText = first.path("lat").asText();
            String lonText = first.path("lon").asText();
            if (latText == null || latText.isBlank() || lonText == null || lonText.isBlank()) {
                return null;
            }

            return new double[]{Double.parseDouble(latText), Double.parseDouble(lonText)};
        } catch (Exception ex) {
            log.debug("【和风天气】GeoAPI解析城市坐标失败: locationId={}, error={}", locationId, ex.getMessage());
            return null;
        }
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
            double[] coordinates = getLocationCoordinates(location);
            if (coordinates == null) {
                log.warn("【和风天气】无法获取城市坐标，回退为按location查询空气质量: location={}", location);
                return getAirQualityByLocation(location);
            }

            double lat = coordinates[0];
            double lon = coordinates[1];

            String url = UriComponentsBuilder
                    .fromHttpUrl(String.format("https://%s/airquality/v1/current/%.2f/%.2f",
                            qweatherConfig.getApiHost(), lat, lon))
                    .queryParam("key", qweatherConfig.getApiKey())
                    .build()
                    .toUriString();

            log.info("【和风天气】调用空气质量API: location={}, lat={}, lon={}", location, lat, lon);

            org.springframework.http.ResponseEntity<String> responseEntity = qweatherRestTemplate.exchange(
                    url,
                    org.springframework.http.HttpMethod.GET,
                    null,
                    String.class
            );

            String response = responseEntity.getBody();
            com.fasterxml.jackson.databind.JsonNode root = objectMapper.readTree(response);

            QWeatherAirResponse result = new QWeatherAirResponse();
            result.setCode("200");

            if (root.has("indexes") && root.path("indexes").isArray() && root.path("indexes").size() > 0) {
                com.fasterxml.jackson.databind.JsonNode firstIndex = root.path("indexes").get(0);

                QWeatherAirResponse.NowData nowData = new QWeatherAirResponse.NowData();
                nowData.setAqi(firstIndex.path("aqiDisplay").asText());
                nowData.setLevel(firstIndex.path("level").asText());
                nowData.setCategory(firstIndex.path("category").asText());

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

                if (firstIndex.has("primaryPollutant") && !firstIndex.path("primaryPollutant").isNull()) {
                    nowData.setPrimary(firstIndex.path("primaryPollutant").path("name").asText());
                }

                result.setNow(nowData);
            }

            if (result.getNow() != null) {
                log.info("【和风天气】空气质量查询成功: aqi={}, level={}",
                        result.getNow().getAqi(), result.getNow().getLevel());
            } else {
                log.warn("【和风天气】空气质量查询成功但无有效指数数据: location={}", location);
            }
            return result;
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            log.error("【和风天气】HTTP错误: status={}, message={}", e.getStatusCode(), e.getMessage());
            log.error("【和风天气】请检查API Key是否正确，访问 https://console.qweather.com/#/apps 确认");
            return null;
        } catch (Exception e) {
            log.error("【和风天气】空气质量查询异常，尝试回退: location={}, error={}", location, e.getMessage());
            return getAirQualityByLocation(location);
        }
    }

    private QWeatherAirResponse getAirQualityByLocation(String location) {
        try {
            String url = UriComponentsBuilder
                    .fromHttpUrl("https://" + qweatherConfig.getApiHost() + "/v7/air/now")
                    .queryParam("location", location)
                    .build()
                    .toUriString();

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
            QWeatherAirResponse result = objectMapper.readValue(response, QWeatherAirResponse.class);
            if (!"200".equals(result.getCode())) {
                log.error("【和风天气】空气质量回退查询失败: location={}, code={}", location, result.getCode());
                return null;
            }

            log.info("【和风天气】空气质量回退查询成功: location={}", location);
            return result;
        } catch (org.springframework.web.client.HttpClientErrorException ex) {
            log.warn("【和风天气】空气质量回退查询被拒绝: location={}, status={}, error={}",
                    location, ex.getStatusCode(), ex.getMessage());
            return null;
        } catch (Exception ex) {
            log.error("【和风天气】空气质量回退查询异常: location={}, error={}", location, ex.getMessage());
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
            log.warn("[QWeather] service disabled");
            return null;
        }

        try {
            String locationParam = location;
            if (location != null && location.length() == 9 && location.matches("\\d+")) {
                double[] coords = getLocationCoordinates(location);
                if (coords != null) {
                    locationParam = coords[1] + "," + coords[0];
                } else {
                    log.warn("[QWeather] no coordinates for city id, fallback to location id for minutely: location={}", location);
                }
            }

            String url = UriComponentsBuilder
                    .fromHttpUrl("https://" + qweatherConfig.getApiHost() + "/v7/minutely/5m")
                    .queryParam("location", locationParam)
                    .build()
                    .toUriString();

            log.info("[QWeather] call minutely API: location={}", locationParam);

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
            QWeatherMinutelyResponse result = objectMapper.readValue(response, QWeatherMinutelyResponse.class);
            if (!"200".equals(result.getCode())) {
                log.error("[QWeather] minutely request failed: code={}", result.getCode());
                return null;
            }

            log.info("[QWeather] minutely request success: summary={}", result.getSummary());
            return result;
        } catch (Exception e) {
            log.error("[QWeather] minutely request error: location={}, error={}", location, e.getMessage());
            return null;
        }
    }
}
