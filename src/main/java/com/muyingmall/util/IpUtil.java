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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * IP 地址工具类。
 */
@Slf4j
public class IpUtil {

    private static final String UNKNOWN = "unknown";
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private static final ConcurrentHashMap<String, CacheEntry> locationCache = new ConcurrentHashMap<>();
    private static final long CACHE_EXPIRE_TIME = TimeUnit.HOURS.toMillis(24);

    private static final String[] IP_HEADER_CANDIDATES = {
            "X-Forwarded-For",
            "X-Real-IP",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_X_FORWARDED_FOR",
            "CF-Connecting-IP",
            "X-Client-IP",
            "True-Client-IP"
    };

    public static String getIpAddr(HttpServletRequest request) {
        return getRealIp(request);
    }

    /**
     * 从代理请求头中解析客户端真实 IP。
     * 优先返回转发链中的第一个公网 IP。
     */
    public static String getRealIp(HttpServletRequest request) {
        if (request == null) {
            return "127.0.0.1";
        }

        List<String> candidates = collectHeaderIpCandidates(request);

        for (String candidate : candidates) {
            if (isPublicIp(candidate)) {
                return candidate;
            }
        }

        // 若没有公网 IP，保留第一个可用地址用于排查链路问题。
        if (!candidates.isEmpty()) {
            return candidates.get(0);
        }

        String ip = normalizeIp(request.getRemoteAddr());
        if (isInvalidIp(ip)) {
            ip = "127.0.0.1";
        }

        if (isLoopbackIp(ip)) {
            try {
                InetAddress inetAddress = InetAddress.getLocalHost();
                String hostAddress = inetAddress.getHostAddress();
                if (!isInvalidIp(hostAddress) && !"127.0.0.1".equals(hostAddress)) {
                    ip = hostAddress;
                } else {
                    ip = "127.0.0.1";
                }
            } catch (Exception e) {
                ip = "127.0.0.1";
            }
        }

        return ip;
    }

    /**
     * 判断是否为可路由的公网 IP。
     */
    public static boolean isPublicIp(String ip) {
        if (isInvalidIp(ip)) {
            return false;
        }
        String normalized = normalizeIp(ip);
        if (isInvalidIp(normalized)) {
            return false;
        }

        if (normalized.contains(":")) {
            return isPublicIpv6(normalized);
        }
        return isPublicIpv4(normalized);
    }

    private static List<String> collectHeaderIpCandidates(HttpServletRequest request) {
        List<String> candidates = new ArrayList<>();
        for (String headerName : IP_HEADER_CANDIDATES) {
            String headerValue = request.getHeader(headerName);
            if (isInvalidIp(headerValue)) {
                continue;
            }
            String[] parts = headerValue.split(",");
            for (String part : parts) {
                String candidate = normalizeIp(part);
                if (!isInvalidIp(candidate)) {
                    candidates.add(candidate);
                }
            }
        }
        return candidates;
    }

    private static boolean isLoopbackIp(String ip) {
        if (ip == null) {
            return false;
        }
        return "127.0.0.1".equals(ip) || "::1".equals(ip) || "0:0:0:0:0:0:0:1".equals(ip);
    }

    private static String normalizeIp(String ip) {
        if (ip == null) {
            return null;
        }
        String trimmed = ip.trim();
        if (trimmed.isEmpty()) {
            return null;
        }

        // 处理 [IPv6]:port
        if (trimmed.startsWith("[")) {
            int endBracket = trimmed.indexOf(']');
            if (endBracket > 0) {
                return trimmed.substring(1, endBracket);
            }
        }

        // 处理 IPv4:port
        if (trimmed.indexOf(':') > 0
                && trimmed.indexOf(':') == trimmed.lastIndexOf(':')
                && trimmed.contains(".")) {
            String[] parts = trimmed.split(":");
            if (parts.length == 2 && parts[0].contains(".")) {
                return parts[0].trim();
            }
        }

        return trimmed;
    }

    private static boolean isInvalidIp(String ip) {
        return ip == null || ip.isEmpty() || UNKNOWN.equalsIgnoreCase(ip);
    }

    private static boolean isPublicIpv4(String ip) {
        String[] parts = ip.split("\\.");
        if (parts.length != 4) {
            return false;
        }

        int[] nums = new int[4];
        try {
            for (int i = 0; i < 4; i++) {
                nums[i] = Integer.parseInt(parts[i]);
                if (nums[i] < 0 || nums[i] > 255) {
                    return false;
                }
            }
        } catch (NumberFormatException e) {
            return false;
        }

        int first = nums[0];
        int second = nums[1];
        int third = nums[2];

        // RFC1918 私网 + 回环 + 共享地址 + 链路本地地址
        if (first == 10) return false;
        if (first == 127) return false;
        if (first == 0) return false;
        if (first == 169 && second == 254) return false;
        if (first == 172 && second >= 16 && second <= 31) return false;
        if (first == 192 && second == 168) return false;
        if (first == 100 && second >= 64 && second <= 127) return false;

        // 保留网段/文档网段/压测网段
        if (first == 192 && second == 0) return false; // 192.0.0.0/24
        if (first == 192 && second == 0 && third == 2) return false; // 192.0.2.0/24
        if (first == 198 && second >= 18 && second <= 19) return false; // 198.18.0.0/15
        if (first == 198 && second == 51 && third == 100) return false; // 198.51.100.0/24
        if (first == 203 && second == 0 && third == 113) return false; // 203.0.113.0/24

        // 组播和未来保留地址
        if (first >= 224) return false;

        return true;
    }

    private static boolean isPublicIpv6(String ip) {
        try {
            InetAddress address = InetAddress.getByName(ip);
            byte[] bytes = address.getAddress();
            if (bytes == null || bytes.length != 16) {
                return false;
            }

            if (address.isAnyLocalAddress()
                    || address.isLoopbackAddress()
                    || address.isLinkLocalAddress()
                    || address.isSiteLocalAddress()
                    || address.isMulticastAddress()) {
                return false;
            }

            int first = bytes[0] & 0xFF;
            int second = bytes[1] & 0xFF;

            // fc00::/7（唯一本地地址）
            if ((first & 0xFE) == 0xFC) {
                return false;
            }
            // fe80::/10（链路本地地址）
            if (first == 0xFE && (second & 0xC0) == 0x80) {
                return false;
            }
            // 2001:db8::/32（文档地址）
            if (first == 0x20
                    && second == 0x01
                    && (bytes[2] & 0xFF) == 0x0D
                    && (bytes[3] & 0xFF) == 0xB8) {
                return false;
            }

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static String getIpLocation(String ip) {
        if (ip == null || ip.isEmpty()) {
            return "未知";
        }

        if (isLoopbackIp(ip) || "localhost".equalsIgnoreCase(ip)) {
            return "本地";
        }

        if (!isPublicIp(ip)) {
            return "内网";
        }

        CacheEntry cached = locationCache.get(ip);
        if (cached != null && !cached.isExpired()) {
            return cached.location;
        }

        String location = queryIpLocation(ip);
        if (location != null && !"未知".equals(location)) {
            locationCache.put(ip, new CacheEntry(location));
        }

        return location != null ? location : "未知";
    }

    private static String queryIpLocation(String ip) {
        String location = queryIpApi(ip);
        if (location != null && !"未知".equals(location)) {
            return location;
        }

        location = queryIpApiCo(ip);
        if (location != null && !"未知".equals(location)) {
            return location;
        }

        return "未知";
    }

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
            log.warn("IP location query failed (ip-api.com): {}", e.getMessage());
        }
        return null;
    }

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
            log.warn("IP location query failed (ipapi.co): {}", e.getMessage());
        }
        return null;
    }

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
            log.warn("HTTP request failed: {}", e.getMessage());
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (Exception e) {
                    // 忽略关闭异常
                }
            }
            if (connection != null) {
                connection.disconnect();
            }
        }
        return null;
    }

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

    public static void cleanExpiredCache() {
        locationCache.entrySet().removeIf(entry -> entry.getValue().isExpired());
    }
}
