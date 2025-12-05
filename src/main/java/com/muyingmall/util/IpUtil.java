package com.muyingmall.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * IP地址工具类 - 获取客户端真实IP和地理位置
 */
@Slf4j
public class IpUtil {
    
    private static final String UNKNOWN = "unknown";
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    // IP地理位置缓存（避免频繁调用API）
    private static final ConcurrentHashMap<String, CacheEntry> locationCache = new ConcurrentHashMap<>();
    private static final long CACHE_EXPIRE_TIME = TimeUnit.HOURS.toMillis(24); // 缓存24小时
    
    /**
     * 获取客户端真实IP地址（考虑代理、负载均衡）
     * 别名方法，兼容不同命名习惯
     */
    public static String getIpAddr(HttpServletRequest request) {
        return getRealIp(request);
    }
    
    /**
     * 获取客户端真实IP地址（考虑代理、负载均衡）
     */
    public static String getRealIp(HttpServletRequest request) {
        if (request == null) return "127.0.0.1";
        
        String ip = request.getHeader("X-Forwarded-For");
        if (isInvalidIp(ip)) ip = request.getHeader("Proxy-Client-IP");
        if (isInvalidIp(ip)) ip = request.getHeader("WL-Proxy-Client-IP");
        if (isInvalidIp(ip)) ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        if (isInvalidIp(ip)) ip = request.getHeader("X-Real-IP");
        if (isInvalidIp(ip)) ip = request.getRemoteAddr();
        
        // 处理多个IP（取第一个）
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        
        // 处理 IPv6 本地回环地址
        if (ip != null && (ip.equals("0:0:0:0:0:0:0:1") || ip.equals("::1"))) {
            try {
                // 尝试获取本机的真实IP
                InetAddress inetAddress = InetAddress.getLocalHost();
                String hostAddress = inetAddress.getHostAddress();
                if (hostAddress != null && !hostAddress.equals("127.0.0.1")) {
                    ip = hostAddress;
                } else {
                    ip = "127.0.0.1";
                }
            } catch (Exception e) {
                ip = "127.0.0.1";
            }
        }
        
        return ip != null ? ip : "127.0.0.1";
    }
    
    private static boolean isInvalidIp(String ip) {
        return ip == null || ip.isEmpty() || UNKNOWN.equalsIgnoreCase(ip);
    }
    
    /**
     * 获取IP地理位置（使用免费API）
     */
    public static String getIpLocation(String ip) {
        if (ip == null || ip.isEmpty()) return "未知";
        
        // 本地地址
        if (ip.equals("127.0.0.1") || ip.equals("localhost") || 
            ip.equals("0:0:0:0:0:0:0:1") || ip.equals("::1")) {
            return "本地";
        }
        
        // 内网地址
        if (isInternalIp(ip)) {
            return "内网";
        }
        
        // 检查缓存
        CacheEntry cached = locationCache.get(ip);
        if (cached != null && !cached.isExpired()) {
            return cached.location;
        }
        
        // 调用API查询
        String location = queryIpLocation(ip);
        
        // 缓存结果
        if (location != null && !location.equals("未知")) {
            locationCache.put(ip, new CacheEntry(location));
        }
        
        return location != null ? location : "未知";
    }
    
    /**
     * 判断是否为内网IP
     */
    private static boolean isInternalIp(String ip) {
        if (ip == null || ip.isEmpty()) return false;
        
        String[] parts = ip.split("\\.");
        if (parts.length != 4) return false;
        
        try {
            int first = Integer.parseInt(parts[0]);
            int second = Integer.parseInt(parts[1]);
            
            // 10.0.0.0 - 10.255.255.255
            if (first == 10) return true;
            
            // 172.16.0.0 - 172.31.255.255
            if (first == 172 && second >= 16 && second <= 31) return true;
            
            // 192.168.0.0 - 192.168.255.255
            if (first == 192 && second == 168) return true;
            
        } catch (NumberFormatException e) {
            return false;
        }
        
        return false;
    }
    
    /**
     * 调用免费IP查询API获取地理位置
     * 使用多个备用API以提高可用性
     */
    private static String queryIpLocation(String ip) {
        // 方案1: 使用 ip-api.com (免费，无需API key，限制45次/分钟)
        String location = queryIpApi(ip);
        if (location != null && !location.equals("未知")) {
            return location;
        }
        
        // 方案2: 使用 ipapi.co (免费，限制1000次/天)
        location = queryIpApiCo(ip);
        if (location != null && !location.equals("未知")) {
            return location;
        }
        
        return "未知";
    }
    
    /**
     * 使用 ip-api.com 查询
     */
    private static String queryIpApi(String ip) {
        try {
            String apiUrl = "http://ip-api.com/json/" + ip + "?lang=zh-CN&fields=status,country,regionName,city";
            String response = sendHttpGet(apiUrl);
            
            if (response != null) {
                JsonNode json = objectMapper.readTree(response);
                String status = json.get("status").asText();
                
                if ("success".equals(status)) {
                    String country = json.has("country") ? json.get("country").asText() : "";
                    String region = json.has("regionName") ? json.get("regionName").asText() : "";
                    String city = json.has("city") ? json.get("city").asText() : "";
                    
                    StringBuilder location = new StringBuilder();
                    if (!country.isEmpty()) location.append(country);
                    if (!region.isEmpty()) location.append(" ").append(region);
                    if (!city.isEmpty()) location.append(" ").append(city);
                    
                    return location.toString().trim();
                }
            }
        } catch (Exception e) {
            log.warn("IP地理位置查询失败(ip-api.com): {}", e.getMessage());
        }
        return null;
    }
    
    /**
     * 使用 ipapi.co 查询
     */
    private static String queryIpApiCo(String ip) {
        try {
            String apiUrl = "https://ipapi.co/" + ip + "/json/";
            String response = sendHttpGet(apiUrl);
            
            if (response != null) {
                JsonNode json = objectMapper.readTree(response);
                
                if (!json.has("error")) {
                    String country = json.has("country_name") ? json.get("country_name").asText() : "";
                    String region = json.has("region") ? json.get("region").asText() : "";
                    String city = json.has("city") ? json.get("city").asText() : "";
                    
                    StringBuilder location = new StringBuilder();
                    if (!country.isEmpty()) location.append(country);
                    if (!region.isEmpty()) location.append(" ").append(region);
                    if (!city.isEmpty()) location.append(" ").append(city);
                    
                    return location.toString().trim();
                }
            }
        } catch (Exception e) {
            log.warn("IP地理位置查询失败(ipapi.co): {}", e.getMessage());
        }
        return null;
    }
    
    /**
     * 发送HTTP GET请求
     */
    private static String sendHttpGet(String urlString) {
        HttpURLConnection connection = null;
        BufferedReader reader = null;
        
        try {
            URL url = new URL(urlString);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(3000);
            connection.setReadTimeout(3000);
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");
            
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                reader = new BufferedReader(new InputStreamReader(
                    connection.getInputStream(), StandardCharsets.UTF_8));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                return response.toString();
            }
        } catch (Exception e) {
            log.warn("HTTP请求失败: {}", e.getMessage());
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (Exception e) {
                    // ignore
                }
            }
            if (connection != null) {
                connection.disconnect();
            }
        }
        return null;
    }
    
    /**
     * 缓存条目
     */
    private static class CacheEntry {
        final String location;
        final long timestamp;
        
        CacheEntry(String location) {
            this.location = location;
            this.timestamp = System.currentTimeMillis();
        }
        
        boolean isExpired() {
            return System.currentTimeMillis() - timestamp > CACHE_EXPIRE_TIME;
        }
    }
    
    /**
     * 清理过期缓存
     */
    public static void cleanExpiredCache() {
        locationCache.entrySet().removeIf(entry -> entry.getValue().isExpired());
    }
}
