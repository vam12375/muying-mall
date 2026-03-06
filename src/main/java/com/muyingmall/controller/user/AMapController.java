package com.muyingmall.controller.user;

import com.muyingmall.common.api.Result;
import com.muyingmall.config.AMapConfig;
import com.muyingmall.dto.amap.GeoCodeResponse;
import com.muyingmall.dto.amap.RegeoCodeResponse;
import com.muyingmall.service.AMapService;
import com.muyingmall.util.IpUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 高德地图接口控制器。
 */
@Tag(name = "高德地图", description = "高德地图相关接口")
@RestController
@RequestMapping("/amap")
@Slf4j
@RequiredArgsConstructor
public class AMapController {

    private final AMapService amapService;
    private final AMapConfig amapConfig;

    @Operation(summary = "地理编码", description = "将地址转换为经纬度坐标")
    @GetMapping("/geocode")
    public Result<GeoCodeResponse> geoCode(@RequestParam String address) {
        GeoCodeResponse response = amapService.geoCode(address);
        if (response == null) {
            return Result.error("地理编码失败");
        }
        return Result.success(response);
    }

    @Operation(summary = "逆地理编码", description = "将经纬度坐标转换为地址")
    @GetMapping("/regeocode")
    public Result<RegeoCodeResponse> regeoCode(
            @RequestParam Double longitude,
            @RequestParam Double latitude) {
        RegeoCodeResponse response = amapService.regeoCode(longitude, latitude);
        if (response == null) {
            return Result.error("逆地理编码失败");
        }
        return Result.success(response);
    }

    @Operation(summary = "获取JS API Key", description = "获取前端地图展示所需配置")
    @GetMapping("/config")
    public Result<Map<String, Object>> getConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("jsKey", amapConfig.getJsKey());
        config.put("warehouseLongitude", amapConfig.getWarehouse().getLongitude());
        config.put("warehouseLatitude", amapConfig.getWarehouse().getLatitude());
        config.put("warehouseName", amapConfig.getWarehouse().getName());
        return Result.success(config);
    }

    @Operation(summary = "IP定位", description = "根据IP地址获取城市信息，不传ip时自动使用请求来源IP")
    @GetMapping("/ip")
    public Result<Map<String, Object>> getLocationByIP(
            @RequestParam(required = false) String ip,
            HttpServletRequest request) {
        String queryIp = ip == null ? null : ip.trim();
        String headerIp = request == null ? null : request.getHeader("X-Client-Public-IP");
        if (headerIp != null) {
            headerIp = headerIp.trim();
        }
        String requestIp = IpUtil.getRealIp(request);

        String resolvedIp = null;
        String ipSource = "amap-auto";

        if (IpUtil.isPublicIp(queryIp)) {
            resolvedIp = queryIp;
            ipSource = "query-ip";
        } else if (IpUtil.isPublicIp(headerIp)) {
            resolvedIp = headerIp;
            ipSource = "header-ip";
        } else if (IpUtil.isPublicIp(requestIp)) {
            resolvedIp = requestIp;
            ipSource = "request-ip";
        }

        if (queryIp != null && !queryIp.isBlank() && !IpUtil.isPublicIp(queryIp)) {
            log.warn("【高德API】忽略非公网query ip: {}", queryIp);
        }
        if (headerIp != null && !headerIp.isBlank() && !IpUtil.isPublicIp(headerIp)) {
            log.warn("【高德API】忽略非公网header ip: {}", headerIp);
        }
        if (resolvedIp == null) {
            log.warn("【高德API】未获取到公网客户端IP，将使用高德自动检测出口IP，结果可能偏向服务器所在城市");
        }

        log.info("【高德API】IP定位请求: queryIp={}, headerIp={}, requestIp={}, resolvedIp={}, ipSource={}, remoteAddr={}",
                queryIp,
                headerIp,
                requestIp,
                resolvedIp,
                ipSource,
                request != null ? request.getRemoteAddr() : "unknown");

        Map<String, Object> location = amapService.getLocationByIP(resolvedIp);
        if (location == null) {
            return Result.error("IP定位失败");
        }
        location.put("_ipSource", ipSource);
        location.put("_resolvedIp", resolvedIp);
        return Result.success(location);
    }
}
