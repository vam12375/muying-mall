package com.muyingmall.controller.user;

import com.muyingmall.common.api.Result;
import com.muyingmall.service.QWeatherService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 和风天气API控制器
 * 官方文档：https://dev.qweather.com/docs/api/
 */
@Tag(name = "和风天气", description = "和风天气相关接口")
@RestController
@RequestMapping("/qweather")
@RequiredArgsConstructor
public class QWeatherController {

    private final QWeatherService qweatherService;

    /**
     * 获取实时天气信息（包含空气质量）
     * @param location 城市ID或经纬度（格式：116.41,39.92）
     *                 城市ID可通过和风天气城市搜索API获取
     *                 常用城市ID：北京=101010100，上海=101020100，杭州=101210101
     * @return 实时天气数据
     */
    @Operation(summary = "获取实时天气", description = "获取指定位置的实时天气和空气质量数据")
    @GetMapping("/weather/now")
    public Result<Map<String, Object>> getWeatherNow(@RequestParam String location) {
        try {
            Map<String, Object> weather = qweatherService.getWeatherInfo(location);
            
            if (weather == null || weather.isEmpty()) {
                return Result.error("获取天气信息失败，请检查location参数是否正确");
            }
            
            return Result.success(weather);
        } catch (Exception e) {
            return Result.error("获取天气信息异常: " + e.getMessage());
        }
    }

    /**
     * 获取7天天气预报
     * @param location 城市ID
     * @return 7天天气预报数据
     */
    @Operation(summary = "获取7天天气预报", description = "获取指定位置的7天天气预报")
    @GetMapping("/weather/7d")
    public Result<Map<String, Object>> getWeatherForecast7d(@RequestParam String location) {
        try {
            Map<String, Object> forecast = qweatherService.getWeatherForecast7d(location);
            
            if (forecast == null) {
                return Result.error("获取天气预报失败");
            }
            
            return Result.success(forecast);
        } catch (Exception e) {
            return Result.error("获取天气预报异常: " + e.getMessage());
        }
    }

    /**
     * 获取天气生活指数
     * @param location 城市ID
     * @param type 指数类型（1-运动指数，2-洗车指数，3-穿衣指数等，0或不传-全部）
     * @return 生活指数数据
     */
    @Operation(summary = "获取天气生活指数", description = "获取指定位置的天气生活指数（运动、洗车、穿衣等）")
    @GetMapping("/indices/1d")
    public Result<Map<String, Object>> getWeatherIndices(
            @RequestParam String location,
            @RequestParam(required = false, defaultValue = "0") String type) {
        try {
            Map<String, Object> indices = qweatherService.getWeatherIndices(location, type);
            
            if (indices == null) {
                return Result.error("获取生活指数失败");
            }
            
            return Result.success(indices);
        } catch (Exception e) {
            return Result.error("获取生活指数异常: " + e.getMessage());
        }
    }

    /**
     * 获取天气预警
     * @param location 城市ID
     * @return 预警数据
     */
    @Operation(summary = "获取天气预警", description = "获取指定位置的天气预警信息")
    @GetMapping("/warning/now")
    public Result<Map<String, Object>> getWeatherWarning(@RequestParam String location) {
        try {
            Map<String, Object> warning = qweatherService.getWeatherWarning(location);
            
            if (warning == null) {
                return Result.error("获取天气预警失败");
            }
            
            return Result.success(warning);
        } catch (Exception e) {
            return Result.error("获取天气预警异常: " + e.getMessage());
        }
    }

    /**
     * 获取分钟级降水预报
     * @param location 城市ID或经纬度（格式：116.41,39.92）
     * @return 未来2小时每5分钟的降水预报
     */
    @Operation(summary = "获取分钟级降水预报", description = "获取指定位置未来2小时每5分钟的降水预报")
    @GetMapping("/minutely/5m")
    public Result<Object> getMinutelyPrecipitation(@RequestParam String location) {
        try {
            Object minutely = qweatherService.getMinutelyPrecipitation(location);
            
            if (minutely == null) {
                return Result.error("获取分钟级降水预报失败");
            }
            
            return Result.success(minutely);
        } catch (Exception e) {
            return Result.error("获取分钟级降水预报异常: " + e.getMessage());
        }
    }
}
