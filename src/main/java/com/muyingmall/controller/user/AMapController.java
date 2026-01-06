package com.muyingmall.controller.user;

import com.muyingmall.common.api.Result;
import com.muyingmall.common.result.ResultCode;
import com.muyingmall.config.AMapConfig;
import com.muyingmall.dto.amap.GeoCodeResponse;
import com.muyingmall.dto.amap.RegeoCodeResponse;
import com.muyingmall.service.AMapService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 高德地图API控制器
 */
@Tag(name = "高德地图", description = "高德地图相关接口")
@RestController
@RequestMapping("/amap")
@RequiredArgsConstructor
public class AMapController {

    private final AMapService amapService;
    private final AMapConfig amapConfig;

    /**
     * 【场景1：地址智能输入】地理编码：地址转坐标
     */
    @Operation(summary = "地理编码", description = "将地址转换为经纬度坐标")
    @GetMapping("/geocode")
    public Result<GeoCodeResponse> geoCode(@RequestParam String address) {
        GeoCodeResponse response = amapService.geoCode(address);
        if (response == null) {
            return Result.error("地理编码失败");
        }
        return Result.success(response);
    }

    /**
     * 【场景1：地址智能输入】逆地理编码：坐标转地址
     */
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

    /**
     * 获取前端JS API Key
     */
    @Operation(summary = "获取JS API Key", description = "获取前端地图展示所需的API Key")
    @GetMapping("/config")
    public Result<Map<String, Object>> getConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("jsKey", amapConfig.getJsKey());
        config.put("warehouseLongitude", amapConfig.getWarehouse().getLongitude());
        config.put("warehouseLatitude", amapConfig.getWarehouse().getLatitude());
        config.put("warehouseName", amapConfig.getWarehouse().getName());
        return Result.success(config);
    }

    /**
     * IP定位：根据IP地址获取城市信息
     */
    @Operation(summary = "IP定位", description = "根据IP地址获取城市信息，不传IP则自动使用请求来源IP")
    @GetMapping("/ip")
    public Result<Map<String, Object>> getLocationByIP(
            @RequestParam(required = false) String ip) {
        Map<String, Object> location = amapService.getLocationByIP(ip);
        if (location == null) {
            return Result.error("IP定位失败");
        }
        return Result.success(location);
    }
}
