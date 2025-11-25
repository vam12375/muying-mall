package com.muyingmall.controller.admin;

import com.muyingmall.common.api.CommonResult;
import com.muyingmall.util.IpUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * IP地址测试控制器
 * 用于测试IP地址获取和地理位置查询功能
 */
@Slf4j
@RestController
@RequestMapping("/admin/ip-test")
@Tag(name = "IP测试", description = "IP地址和地理位置查询测试")
public class IpTestController {
    
    /**
     * 获取当前请求的IP信息
     */
    @GetMapping("/current")
    @Operation(summary = "获取当前IP信息")
    public CommonResult<Map<String, Object>> getCurrentIpInfo(HttpServletRequest request) {
        try {
            String ip = IpUtil.getRealIp(request);
            String location = IpUtil.getIpLocation(ip);
            
            Map<String, Object> result = new HashMap<>();
            result.put("ip", ip);
            result.put("location", location);
            result.put("userAgent", request.getHeader("User-Agent"));
            result.put("headers", getRequestHeaders(request));
            
            return CommonResult.success(result);
        } catch (Exception e) {
            log.error("获取IP信息失败", e);
            return CommonResult.failed("获取IP信息失败: " + e.getMessage());
        }
    }
    
    /**
     * 查询指定IP的地理位置
     */
    @GetMapping("/query")
    @Operation(summary = "查询指定IP地理位置")
    public CommonResult<Map<String, String>> queryIpLocation(@RequestParam String ip) {
        try {
            String location = IpUtil.getIpLocation(ip);
            
            Map<String, String> result = new HashMap<>();
            result.put("ip", ip);
            result.put("location", location);
            
            return CommonResult.success(result);
        } catch (Exception e) {
            log.error("查询IP地理位置失败", e);
            return CommonResult.failed("查询失败: " + e.getMessage());
        }
    }
    
    /**
     * 清理IP地理位置缓存
     */
    @PostMapping("/clear-cache")
    @Operation(summary = "清理IP缓存")
    public CommonResult<Void> clearCache() {
        try {
            IpUtil.cleanExpiredCache();
            return CommonResult.success(null, "缓存清理成功");
        } catch (Exception e) {
            log.error("清理缓存失败", e);
            return CommonResult.failed("清理缓存失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取所有请求头
     */
    private Map<String, String> getRequestHeaders(HttpServletRequest request) {
        Map<String, String> headers = new HashMap<>();
        java.util.Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            headers.put(headerName, request.getHeader(headerName));
        }
        return headers;
    }
}
