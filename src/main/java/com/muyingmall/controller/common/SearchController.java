package com.muyingmall.controller.common;

import com.muyingmall.common.api.Result;
import com.muyingmall.common.api.PageResult;
import com.muyingmall.document.ProductDocument;
import com.muyingmall.service.ProductSearchService;
import com.muyingmall.service.SearchStatisticsService;
import com.muyingmall.service.UserSearchHistoryService;
import com.muyingmall.util.JwtUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 搜索控制器
 * 提供搜索API接口，包括商品搜索、搜索建议、热门搜索等功能
 */
@Slf4j
@RestController
@RequestMapping("/search")
@RequiredArgsConstructor
@Tag(name = "搜索管理", description = "商品搜索、搜索建议、热门搜索等功能")
public class SearchController {

    private final ProductSearchService productSearchService;
    private final SearchStatisticsService searchStatisticsService;
    private final UserSearchHistoryService userSearchHistoryService;
    private final JwtUtils jwtUtils;

    @GetMapping("/products")
    @Operation(summary = "搜索商品", description = "根据关键词和筛选条件搜索商品")
    public Result<PageResult<ProductDocument>> searchProducts(
            @Parameter(description = "搜索关键词") @RequestParam(required = false) String keyword,
            @Parameter(description = "分类ID") @RequestParam(required = false) Integer categoryId,
            @Parameter(description = "品牌ID") @RequestParam(required = false) Integer brandId,
            @Parameter(description = "最低价格") @RequestParam(required = false) BigDecimal minPrice,
            @Parameter(description = "最高价格") @RequestParam(required = false) BigDecimal maxPrice,
            @Parameter(description = "排序字段") @RequestParam(defaultValue = "relevance") String sortBy,
            @Parameter(description = "排序方向") @RequestParam(defaultValue = "desc") String sortOrder,
            @Parameter(description = "页码") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "12") int size,
            HttpServletRequest request) {

        try {
            long startTime = System.currentTimeMillis();
            
            Page<ProductDocument> searchResult = productSearchService.searchProducts(
                    keyword, categoryId, brandId, minPrice, maxPrice,
                    sortBy, sortOrder, page, size);

            long responseTime = System.currentTimeMillis() - startTime;
            
            // 获取当前用户ID（如果已登录）
            Integer userId = getCurrentUserId(request);
            
            // 记录用户搜索历史（异步）
            if (keyword != null && !keyword.trim().isEmpty() && userId != null) {
                userSearchHistoryService.recordSearchHistory(userId, keyword);
            }
            
            // 记录搜索统计（异步）
            if (keyword != null && !keyword.trim().isEmpty()) {
                String ipAddress = getClientIpAddress(request);
                String userAgent = request.getHeader("User-Agent");
                searchStatisticsService.recordSearch(keyword, searchResult.getTotalElements(), 
                        userId, "web", ipAddress, userAgent, responseTime);
            }

            // 转换为可序列化的分页结果
            PageResult<ProductDocument> result = PageResult.from(searchResult);
            return Result.success(result);
        } catch (Exception e) {
            log.error("搜索商品失败: {}", e.getMessage(), e);
            return Result.error("搜索失败，请稍后重试");
        }
    }

    @GetMapping("/suggestions")
    @Operation(summary = "获取搜索建议", description = "根据关键词前缀获取搜索建议")
    public Result<List<String>> getSearchSuggestions(
            @Parameter(description = "关键词前缀") @RequestParam String keyword,
            @Parameter(description = "建议数量") @RequestParam(defaultValue = "10") int limit) {

        try {
            List<String> suggestions = productSearchService.getSearchSuggestions(keyword, limit);
            return Result.success(suggestions);
        } catch (Exception e) {
            log.error("获取搜索建议失败: {}", e.getMessage(), e);
            return Result.error("获取搜索建议失败");
        }
    }

    @GetMapping("/hot-keywords")
    @Operation(summary = "获取热门搜索词", description = "获取当前热门搜索关键词列表")
    public Result<List<String>> getHotSearchKeywords(
            @Parameter(description = "返回数量") @RequestParam(defaultValue = "10") int limit) {

        try {
            List<String> hotKeywords = productSearchService.getHotSearchKeywords(limit);
            return Result.success(hotKeywords);
        } catch (Exception e) {
            log.error("获取热门搜索词失败: {}", e.getMessage(), e);
            return Result.error("获取热门搜索词失败");
        }
    }

    @GetMapping("/aggregations")
    @Operation(summary = "获取搜索聚合信息", description = "获取搜索结果的聚合统计信息")
    public Result<Map<String, Object>> getSearchAggregations(
            @Parameter(description = "搜索关键词") @RequestParam(required = false) String keyword) {

        try {
            Map<String, Object> aggregations = productSearchService.getSearchAggregations(keyword);
            return Result.success(aggregations);
        } catch (Exception e) {
            log.error("获取搜索聚合信息失败: {}", e.getMessage(), e);
            return Result.error("获取聚合信息失败");
        }
    }

    @GetMapping("/similar/{productId}")
    @Operation(summary = "获取相似商品", description = "根据商品ID获取相似商品推荐")
    public Result<List<ProductDocument>> getSimilarProducts(
            @Parameter(description = "商品ID") @PathVariable Integer productId,
            @Parameter(description = "推荐数量") @RequestParam(defaultValue = "10") int limit) {

        try {
            List<ProductDocument> similarProducts = productSearchService.getSimilarProducts(productId, limit);
            return Result.success(similarProducts);
        } catch (Exception e) {
            log.error("获取相似商品失败: {}", e.getMessage(), e);
            return Result.error("获取相似商品失败");
        }
    }

    @PostMapping("/sync/{productId}")
    @Operation(summary = "同步商品到搜索索引", description = "将指定商品同步到Elasticsearch索引")
    public Result<Void> syncProductToIndex(
            @Parameter(description = "商品ID") @PathVariable Integer productId) {

        try {
            productSearchService.syncProductToIndex(productId);
            return Result.success(null, "商品同步成功");
        } catch (Exception e) {
            log.error("同步商品到搜索索引失败: {}", e.getMessage(), e);
            return Result.error("同步失败");
        }
    }

    @PostMapping("/sync/batch")
    @Operation(summary = "批量同步商品到搜索索引", description = "批量将商品同步到Elasticsearch索引")
    public Result<Void> batchSyncProductsToIndex(
            @Parameter(description = "商品ID列表") @RequestBody List<Integer> productIds) {

        try {
            productSearchService.batchSyncProductsToIndex(productIds);
            return Result.success(null, "批量同步成功");
        } catch (Exception e) {
            log.error("批量同步商品到搜索索引失败: {}", e.getMessage(), e);
            return Result.error("批量同步失败");
        }
    }

    @DeleteMapping("/index/{productId}")
    @Operation(summary = "从搜索索引删除商品", description = "从Elasticsearch索引中删除指定商品")
    public Result<Void> deleteProductFromIndex(
            @Parameter(description = "商品ID") @PathVariable Integer productId) {

        try {
            productSearchService.deleteProductFromIndex(productId);
            return Result.success(null, "删除成功");
        } catch (Exception e) {
            log.error("从搜索索引删除商品失败: {}", e.getMessage(), e);
            return Result.error("删除失败");
        }
    }

    @PostMapping("/reindex")
    @Operation(summary = "重建搜索索引", description = "重新构建Elasticsearch搜索索引")
    public Result<Void> rebuildSearchIndex() {

        try {
            productSearchService.rebuildSearchIndex();
            return Result.success(null, "重建索引成功");
        } catch (Exception e) {
            log.error("重建搜索索引失败: {}", e.getMessage(), e);
            return Result.error("重建索引失败");
        }
    }

    @GetMapping("/health")
    @Operation(summary = "检查搜索索引健康状态", description = "检查Elasticsearch索引的健康状态")
    public Result<Map<String, Object>> getIndexHealthStatus() {

        try {
            Map<String, Object> healthStatus = productSearchService.getIndexHealthStatus();
            return Result.success(healthStatus);
        } catch (Exception e) {
            log.error("检查搜索索引健康状态失败: {}", e.getMessage(), e);
            return Result.error("检查健康状态失败");
        }
    }

    // ==================== 用户搜索历史相关接口 ====================

    @GetMapping("/history")
    @Operation(summary = "获取用户搜索历史", description = "获取当前登录用户的搜索历史记录")
    public Result<List<String>> getUserSearchHistory(
            @Parameter(description = "返回数量") @RequestParam(defaultValue = "10") int limit,
            HttpServletRequest request) {

        try {
            Integer userId = getCurrentUserId(request);
            if (userId == null) {
                return Result.error("请先登录");
            }

            List<String> history = userSearchHistoryService.getUserSearchHistory(userId, limit);
            return Result.success(history);
        } catch (Exception e) {
            log.error("获取用户搜索历史失败: {}", e.getMessage(), e);
            return Result.error("获取搜索历史失败");
        }
    }

    @DeleteMapping("/history")
    @Operation(summary = "删除用户搜索历史", description = "删除指定的搜索历史记录")
    public Result<Void> deleteSearchHistory(
            @Parameter(description = "搜索关键词") @RequestParam String keyword,
            HttpServletRequest request) {

        try {
            Integer userId = getCurrentUserId(request);
            if (userId == null) {
                return Result.error("请先登录");
            }

            boolean success = userSearchHistoryService.deleteSearchHistory(userId, keyword);
            return success ? Result.success(null, "删除成功") : Result.error("删除失败");
        } catch (Exception e) {
            log.error("删除用户搜索历史失败: {}", e.getMessage(), e);
            return Result.error("删除搜索历史失败");
        }
    }

    @DeleteMapping("/history/clear")
    @Operation(summary = "清空用户搜索历史", description = "清空当前用户的所有搜索历史记录")
    public Result<Integer> clearSearchHistory(HttpServletRequest request) {

        try {
            Integer userId = getCurrentUserId(request);
            if (userId == null) {
                return Result.error("请先登录");
            }

            int count = userSearchHistoryService.clearSearchHistory(userId);
            return Result.success(count, "清空成功，共删除 " + count + " 条记录");
        } catch (Exception e) {
            log.error("清空用户搜索历史失败: {}", e.getMessage(), e);
            return Result.error("清空搜索历史失败");
        }
    }

    // ==================== 搜索点击记录接口 ====================

    @PostMapping("/click")
    @Operation(summary = "记录搜索点击", description = "记录用户点击搜索结果中的商品")
    public Result<Void> recordSearchClick(
            @Parameter(description = "搜索关键词") @RequestParam String keyword,
            @Parameter(description = "点击的商品ID") @RequestParam Integer productId,
            @Parameter(description = "会话ID") @RequestParam(required = false) String sessionId,
            HttpServletRequest request) {

        try {
            Integer userId = getCurrentUserId(request);
            searchStatisticsService.recordSearchClick(keyword, productId, userId, sessionId);
            return Result.success(null, "记录成功");
        } catch (Exception e) {
            log.error("记录搜索点击失败: {}", e.getMessage(), e);
            return Result.error("记录失败");
        }
    }

    // ==================== 辅助方法 ====================

    /**
     * 获取当前登录用户ID
     * @param request HTTP请求
     * @return 用户ID，未登录返回null
     */
    private Integer getCurrentUserId(HttpServletRequest request) {
        try {
            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
                if (jwtUtils.validateToken(token)) {
                    return jwtUtils.getClaimsFromToken(token).get("userId", Integer.class);
                }
            }
        } catch (Exception e) {
            log.debug("获取用户ID失败: {}", e.getMessage());
        }
        return null;
    }

    /**
     * 获取客户端IP地址
     * @param request HTTP请求
     * @return IP地址
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 多个代理时，取第一个IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
