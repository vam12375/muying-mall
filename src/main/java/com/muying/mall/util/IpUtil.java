package com.muying.mall.util;

import javax.servlet.http.HttpServletRequest;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * IP地址工具类
 * 用于获取客户端真实IP地址
 */
public class IpUtil {
    
    private static final String UNKNOWN = "unknown";
    private static final String LOCALHOST_IP = "127.0.0.1";
    private static final String LOCALHOST_IPV6 = "0:0:0:0:0:0:0:1";
    
    /**
     * 获取客户端真实IP地址
     * 考虑了代理、负载均衡等情况
     */
    public static String getRealIp(HttpServletRequest request) {
        if (request == null) {
            return UNKNOWN;
        }
        
        String ip = request.getHeader("X-Forwarded-For");
        
        if (isInvalidIp(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (isInvalidIp(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (isInvalidIp(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (isInvalidIp(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (isInvalidIp(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (isInvalidIp(ip)) {
            ip = request.getRemoteAddr();
            // 如果是本地访问，获取本机真实IP
            if (LOCALHOST_IP.equals(ip) || LOCALHOST_IPV6.equals(ip)) {
                try {
                    InetAddress inetAddress = InetAddress.getLocalHost();
                    ip = inetAddress.getHostAddress();
                } catch (UnknownHostException e) {
                    // 保持原IP
                }
            }
        }
        
        // 处理多个IP的情况（取第一个）
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        
        return ip;
    }
    
    /**
     * 判断IP是否无效
     */
    private static boolean isInvalidIp(String ip) {
        return ip == null || ip.isEmpty() || UNKNOWN.equalsIgnoreCase(ip);
    }
    
    /**
     * 获取IP地理位置（简化版本）
     * 实际项目中可以集成第三方IP库如 IP2Location、GeoIP2 等
     */
    public static String getIpLocation(String ip) {
        if (ip == null || ip.isEmpty()) {
            return "未知地区";
        }
        
        // 本地IP
        if (LOCALHOST_IP.equals(ip) || LOCALHOST_IPV6.equals(ip) || ip.startsWith("192.168.") || ip.startsWith("10.")) {
            return "本地";
        }
        
        // TODO: 集成第三方IP地理位置服务
        // 这里返回示例，实际应该调用IP库或API
        return "未知地区";
    }
}
