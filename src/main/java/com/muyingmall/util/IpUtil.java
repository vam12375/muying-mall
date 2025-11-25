package com.muyingmall.util;

import javax.servlet.http.HttpServletRequest;
import java.net.InetAddress;

/**
 * IP地址工具类 - 获取客户端真实IP
 */
public class IpUtil {
    
    private static final String UNKNOWN = "unknown";
    
    /**
     * 获取客户端真实IP地址（考虑代理、负载均衡）
     */
    public static String getRealIp(HttpServletRequest request) {
        if (request == null) return UNKNOWN;
        
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
        
        return ip;
    }
    
    private static boolean isInvalidIp(String ip) {
        return ip == null || ip.isEmpty() || UNKNOWN.equalsIgnoreCase(ip);
    }
    
    /**
     * 获取IP地理位置（简化版）
     */
    public static String getIpLocation(String ip) {
        if (ip == null || ip.isEmpty()) return "未知地区";
        if (ip.startsWith("127.") || ip.startsWith("192.168.") || ip.startsWith("10.")) {
            return "本地";
        }
        // TODO: 集成第三方IP库
        return "未知地区";
    }
}
