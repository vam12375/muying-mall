package com.muyingmall.controller.admin;

import com.muyingmall.common.constants.CacheConstants;
import com.muyingmall.common.api.Result;
import com.muyingmall.util.CacheProtectionUtil;
import com.muyingmall.util.RedisUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.Callable;

/**
 * 数据分析控制器
 * 提供数据分析相关的API端点
 */
@RestController
@RequestMapping("/admin/analytics")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "数据分析", description = "提供数据分析相关的API")
public class AnalyticsController {

    private final RedisUtil redisUtil;
    private final CacheProtectionUtil cacheProtectionUtil;

    /**
     * 获取仪表盘汇总数据
     */
    @GetMapping("/dashboard-summary")
    @Operation(summary = "获取仪表盘汇总数据")
    public Result<Map<String, Object>> getDashboardSummary(
            @RequestParam(value = "timeRange", defaultValue = "today") @Parameter(description = "时间范围") String timeRange,
            @RequestParam(value = "startDate", required = false) @Parameter(description = "开始日期") String startDate,
            @RequestParam(value = "endDate", required = false) @Parameter(description = "结束日期") String endDate) {
        try {
            log.info("获取仪表盘汇总数据，时间范围：{}，开始日期：{}，结束日期：{}", timeRange, startDate, endDate);

            // 构建缓存键
            String cacheKey = CacheConstants.ANALYTICS_KEY_PREFIX + "dashboard:" + timeRange;
            if (startDate != null && endDate != null) {
                cacheKey += ":" + startDate + ":" + endDate;
            }

            // 使用缓存保护工具获取数据
            Callable<Map<String, Object>> dbFallback = () -> {
                // 模拟从数据库获取数据
                Map<String, Object> result = new HashMap<>();

                // 这里暂时返回模拟数据，后续可以接入真实数据源
                result.put("totalSales", 268452.65);
                result.put("dailySales", 12680.54);
                result.put("monthlySales", 86452.32);
                result.put("salesGrowth", 12.5);

                result.put("totalOrders", 8426);
                result.put("dailyOrders", 186);
                result.put("monthlyOrders", 1245);
                result.put("orderGrowth", 8.2);

                result.put("totalUsers", 3254);
                result.put("newUsers", 28);
                result.put("activeUsers", 452);
                result.put("userGrowth", 5.3);

                result.put("totalProducts", 1256);
                result.put("outOfStockProducts", 32);
                result.put("productGrowth", 2.1);

                return result;
            };

            // 使用缓存保护查询，缓存时间1小时
            Map<String, Object> result = cacheProtectionUtil.queryWithProtection(
                    cacheKey,
                    CacheConstants.ANALYTICS_EXPIRE_TIME,
                    dbFallback);

            return Result.success(result);
        } catch (Exception e) {
            log.error("获取仪表盘汇总数据失败", e);
            return Result.error("获取仪表盘汇总数据失败: " + e.getMessage());
        }
    }

    /**
     * 获取销售趋势数据
     */
    @GetMapping("/sales-trend")
    @Operation(summary = "获取销售趋势数据")
    public Result<List<Map<String, Object>>> getSalesTrend(
            @RequestParam(value = "timeRange", defaultValue = "week") @Parameter(description = "时间范围") String timeRange,
            @RequestParam(value = "startDate", required = false) @Parameter(description = "开始日期") String startDate,
            @RequestParam(value = "endDate", required = false) @Parameter(description = "结束日期") String endDate) {
        try {
            log.info("获取销售趋势数据，时间范围：{}，开始日期：{}，结束日期：{}", timeRange, startDate, endDate);

            // 构建缓存键
            String cacheKey = CacheConstants.ANALYTICS_KEY_PREFIX + "sales-trend:" + timeRange;
            if (startDate != null && endDate != null) {
                cacheKey += ":" + startDate + ":" + endDate;
            }

            // 从缓存获取数据
            Object cachedData = redisUtil.get(cacheKey);
            if (cachedData != null) {
                log.debug("从缓存获取销售趋势数据: {}", cacheKey);
                return Result.success((List<Map<String, Object>>) cachedData);
            }

            // 缓存未命中，模拟从数据库获取数据
            List<Map<String, Object>> trendData = new ArrayList<>();
            Random random = new Random();

            // 根据时间范围生成对应数量的数据点
            int dataPoints;
            switch (timeRange) {
                case "week":
                    dataPoints = 7;
                    break;
                case "month":
                    dataPoints = 30;
                    break;
                case "year":
                    dataPoints = 12;
                    break;
                default:
                    dataPoints = 7;
            }

            // 生成模拟数据
            for (int i = 0; i < dataPoints; i++) {
                LocalDate date = LocalDate.now().minusDays(dataPoints - i - 1);
                String dateString = date.format(DateTimeFormatter.ISO_LOCAL_DATE);

                Map<String, Object> dataPoint = new HashMap<>();
                dataPoint.put("date", dateString);
                dataPoint.put("sales", 5000 + random.nextDouble() * 10000);
                dataPoint.put("orders", 50 + random.nextDouble() * 100);

                trendData.add(dataPoint);
            }

            // 缓存结果，设置30分钟过期时间
            redisUtil.set(cacheKey, trendData, CacheConstants.ANALYTICS_EXPIRE_TIME);
            log.debug("缓存销售趋势数据: {}", cacheKey);

            return Result.success(trendData);
        } catch (Exception e) {
            log.error("获取销售趋势数据失败", e);
            return Result.error("获取销售趋势数据失败: " + e.getMessage());
        }
    }

    /**
     * 获取分类销售数据
     */
    @GetMapping("/category-sales")
    @Operation(summary = "获取分类销售数据")
    public Result<List<Map<String, Object>>> getCategorySales(
            @RequestParam(value = "timeRange", defaultValue = "month") @Parameter(description = "时间范围") String timeRange) {
        try {
            log.info("获取分类销售数据，时间范围：{}", timeRange);

            // 构建缓存键
            String cacheKey = CacheConstants.ANALYTICS_KEY_PREFIX + "category-sales:" + timeRange;

            // 使用缓存穿透保护工具获取数据
            Callable<List<Map<String, Object>>> dbFallback = () -> {
                // 模拟从数据库获取数据
                List<Map<String, Object>> categorySales = new ArrayList<>();

                // 模拟数据
                Map<String, Object> category1 = new HashMap<>();
                category1.put("category", "奶粉");
                category1.put("sales", 68452.32);
                category1.put("percentage", 28);
                categorySales.add(category1);

                Map<String, Object> category2 = new HashMap<>();
                category2.put("category", "尿布");
                category2.put("sales", 52145.65);
                category2.put("percentage", 22);
                categorySales.add(category2);

                Map<String, Object> category3 = new HashMap<>();
                category3.put("category", "玩具");
                category3.put("sales", 35624.78);
                category3.put("percentage", 15);
                categorySales.add(category3);

                Map<String, Object> category4 = new HashMap<>();
                category4.put("category", "辅食");
                category4.put("sales", 28945.12);
                category4.put("percentage", 12);
                categorySales.add(category4);

                Map<String, Object> category5 = new HashMap<>();
                category5.put("category", "童装");
                category5.put("sales", 26321.45);
                category5.put("percentage", 11);
                categorySales.add(category5);

                Map<String, Object> category6 = new HashMap<>();
                category6.put("category", "洗护");
                category6.put("sales", 18745.23);
                category6.put("percentage", 8);
                categorySales.add(category6);

                Map<String, Object> category7 = new HashMap<>();
                category7.put("category", "其它");
                category7.put("sales", 9562.15);
                category7.put("percentage", 4);
                categorySales.add(category7);

                return categorySales;
            };

            // 使用缓存保护查询，缓存时间1小时
            List<Map<String, Object>> result = cacheProtectionUtil.queryWithProtection(
                    cacheKey,
                    CacheConstants.ANALYTICS_EXPIRE_TIME,
                    dbFallback);

            return Result.success(result);
        } catch (Exception e) {
            log.error("获取分类销售数据失败", e);
            return Result.error("获取分类销售数据失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户地域分布
     */
    @GetMapping("/user-regions")
    @Operation(summary = "获取用户地域分布")
    public Result<List<Map<String, Object>>> getUserRegions() {
        try {
            log.info("获取用户地域分布");

            // 构建缓存键
            String cacheKey = CacheConstants.ANALYTICS_KEY_PREFIX + "user-regions";

            // 使用分布式锁保护的缓存查询
            String lockKey = cacheKey + ":lock";

            Callable<List<Map<String, Object>>> dbFallback = () -> {
                // 模拟从数据库获取数据
                List<Map<String, Object>> regionData = new ArrayList<>();

                // 模拟数据
                Map<String, Object> region1 = new HashMap<>();
                region1.put("region", "华东");
                region1.put("users", 1245);
                region1.put("percentage", 32);
                regionData.add(region1);

                Map<String, Object> region2 = new HashMap<>();
                region2.put("region", "华南");
                region2.put("users", 986);
                region2.put("percentage", 25);
                regionData.add(region2);

                Map<String, Object> region3 = new HashMap<>();
                region3.put("region", "华北");
                region3.put("users", 756);
                region3.put("percentage", 19);
                regionData.add(region3);

                Map<String, Object> region4 = new HashMap<>();
                region4.put("region", "西南");
                region4.put("users", 452);
                region4.put("percentage", 12);
                regionData.add(region4);

                Map<String, Object> region5 = new HashMap<>();
                region5.put("region", "东北");
                region5.put("users", 325);
                region5.put("percentage", 8);
                regionData.add(region5);

                Map<String, Object> region6 = new HashMap<>();
                region6.put("region", "西北");
                region6.put("users", 156);
                region6.put("percentage", 4);
                regionData.add(region6);

                return regionData;
            };

            // 使用带互斥锁的缓存查询，防止缓存击穿
            List<Map<String, Object>> result = cacheProtectionUtil.queryWithMutex(
                    cacheKey,
                    lockKey,
                    CacheConstants.ANALYTICS_EXPIRE_TIME,
                    dbFallback);

            return Result.success(result);
        } catch (Exception e) {
            log.error("获取用户地域分布失败", e);
            return Result.error("获取用户地域分布失败: " + e.getMessage());
        }
    }

    /**
     * 获取报表模板列表
     */
    @GetMapping("/report-templates")
    @Operation(summary = "获取报表模板列表")
    public Result<List<Map<String, Object>>> getReportTemplates() {
        try {
            log.info("获取报表模板列表");

            // 构建缓存键
            String cacheKey = CacheConstants.ANALYTICS_KEY_PREFIX + "report-templates";

            // 使用缓存保护工具获取数据
            Callable<List<Map<String, Object>>> dbFallback = () -> {
                // 模拟从数据库获取数据
                List<Map<String, Object>> templates = new ArrayList<>();

                // 模拟数据
                Map<String, Object> template1 = new HashMap<>();
                template1.put("id", "1");
                template1.put("name", "销售概览报表");
                template1.put("description", "展示销售额、订单数等关键指标的概览报表");
                template1.put("type", "sales");
                template1.put("config", new HashMap<>());
                template1.put("createdBy", "admin");
                template1.put("createdAt", "2023-01-01T00:00:00Z");
                template1.put("updatedAt", "2023-01-01T00:00:00Z");
                templates.add(template1);

                Map<String, Object> template2 = new HashMap<>();
                template2.put("id", "2");
                template2.put("name", "用户增长报表");
                template2.put("description", "展示用户注册、活跃等数据的报表");
                template2.put("type", "users");
                template2.put("config", new HashMap<>());
                template2.put("createdBy", "admin");
                template2.put("createdAt", "2023-01-02T00:00:00Z");
                template2.put("updatedAt", "2023-01-02T00:00:00Z");
                templates.add(template2);

                Map<String, Object> template3 = new HashMap<>();
                template3.put("id", "3");
                template3.put("name", "商品销量报表");
                template3.put("description", "展示各商品销量、库存等数据的报表");
                template3.put("type", "products");
                template3.put("config", new HashMap<>());
                template3.put("createdBy", "admin");
                template3.put("createdAt", "2023-01-03T00:00:00Z");
                template3.put("updatedAt", "2023-01-03T00:00:00Z");
                templates.add(template3);

                return templates;
            };

            // 使用缓存保护查询，缓存时间较长（1天），因为报表模板不常变化
            List<Map<String, Object>> result = cacheProtectionUtil.queryWithProtection(
                    cacheKey,
                    CacheConstants.LONG_EXPIRE_TIME,
                    dbFallback);

            return Result.success(result);
        } catch (Exception e) {
            log.error("获取报表模板列表失败", e);
            return Result.error("获取报表模板列表失败: " + e.getMessage());
        }
    }

    /**
     * 清除分析数据缓存
     * 管理员用于手动刷新缓存数据
     */
    @PostMapping("/cache/clear")
    @Operation(summary = "清除分析数据缓存")
    public Result<Boolean> clearAnalyticsCache(
            @RequestParam(value = "type", required = false) @Parameter(description = "缓存类型") String type) {
        try {
            String pattern;
            if (type != null && !type.isEmpty()) {
                pattern = CacheConstants.ANALYTICS_KEY_PREFIX + type + "*";
            } else {
                pattern = CacheConstants.ANALYTICS_KEY_PREFIX + "*";
            }

            // 使用scan命令清除匹配的缓存
            long count = redisUtil.deleteByScan(pattern);

            log.info("清除分析数据缓存成功，共清除{}个键", count);
            return Result.success(true);
        } catch (Exception e) {
            log.error("清除分析数据缓存失败", e);
            return Result.error("清除分析数据缓存失败: " + e.getMessage());
        }
    }
}