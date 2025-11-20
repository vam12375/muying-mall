package com.muyingmall.controller.admin;

import com.muyingmall.common.api.Result;
import com.muyingmall.service.ProductSearchService;
import com.muyingmall.service.SearchIndexService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 管理员搜索控制器
 * 提供搜索索引管理功能
 */
@Slf4j
@RestController
@RequestMapping("/admin/search")
@RequiredArgsConstructor
@Tag(name = "管理员搜索管理", description = "搜索索引管理、数据同步等功能")
public class AdminSearchController {

    private final ProductSearchService productSearchService;
    private final SearchIndexService searchIndexService;

    @PostMapping("/index/create")
    @Operation(summary = "创建搜索索引", description = "创建商品搜索索引")
    public Result<Void> createIndex() {
        try {
            boolean success = searchIndexService.createProductIndex();
            if (success) {
                return Result.success(null, "索引创建成功");
            } else {
                return Result.error("索引创建失败");
            }
        } catch (Exception e) {
            log.error("创建搜索索引失败: {}", e.getMessage(), e);
            return Result.error("创建索引失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/index/delete")
    @Operation(summary = "删除搜索索引", description = "删除商品搜索索引")
    public Result<Void> deleteIndex() {
        try {
            boolean success = searchIndexService.deleteProductIndex();
            if (success) {
                return Result.success(null, "索引删除成功");
            } else {
                return Result.error("索引删除失败");
            }
        } catch (Exception e) {
            log.error("删除搜索索引失败: {}", e.getMessage(), e);
            return Result.error("删除索引失败: " + e.getMessage());
        }
    }

    @PostMapping("/index/rebuild")
    @Operation(summary = "重建搜索索引", description = "重新构建商品搜索索引")
    public Result<Void> rebuildIndex() {
        try {
            productSearchService.rebuildSearchIndex();
            return Result.success(null, "索引重建成功");
        } catch (Exception e) {
            log.error("重建搜索索引失败: {}", e.getMessage(), e);
            return Result.error("重建索引失败: " + e.getMessage());
        }
    }

    @GetMapping("/index/info")
    @Operation(summary = "获取索引信息", description = "获取搜索索引的详细信息")
    public Result<Map<String, Object>> getIndexInfo(
            @Parameter(description = "索引名称") @RequestParam(defaultValue = "products") String indexName) {
        try {
            Map<String, Object> info = searchIndexService.getIndexInfo(indexName);
            return Result.success(info);
        } catch (Exception e) {
            log.error("获取索引信息失败: {}", e.getMessage(), e);
            return Result.error("获取索引信息失败");
        }
    }

    @GetMapping("/index/stats")
    @Operation(summary = "获取索引统计", description = "获取搜索索引的统计信息")
    public Result<Map<String, Object>> getIndexStats(
            @Parameter(description = "索引名称") @RequestParam(defaultValue = "products") String indexName) {
        try {
            Map<String, Object> stats = searchIndexService.getIndexStats(indexName);
            return Result.success(stats);
        } catch (Exception e) {
            log.error("获取索引统计失败: {}", e.getMessage(), e);
            return Result.error("获取索引统计失败");
        }
    }

    @PostMapping("/index/refresh")
    @Operation(summary = "刷新索引", description = "刷新搜索索引")
    public Result<Void> refreshIndex(
            @Parameter(description = "索引名称") @RequestParam(defaultValue = "products") String indexName) {
        try {
            boolean success = searchIndexService.refreshIndex(indexName);
            if (success) {
                return Result.success(null, "索引刷新成功");
            } else {
                return Result.error("索引刷新失败");
            }
        } catch (Exception e) {
            log.error("刷新索引失败: {}", e.getMessage(), e);
            return Result.error("刷新索引失败: " + e.getMessage());
        }
    }

    @PostMapping("/index/optimize")
    @Operation(summary = "优化索引", description = "优化搜索索引性能")
    public Result<Void> optimizeIndex(
            @Parameter(description = "索引名称") @RequestParam(defaultValue = "products") String indexName) {
        try {
            boolean success = searchIndexService.optimizeIndex(indexName);
            if (success) {
                return Result.success(null, "索引优化成功");
            } else {
                return Result.error("索引优化失败");
            }
        } catch (Exception e) {
            log.error("优化索引失败: {}", e.getMessage(), e);
            return Result.error("优化索引失败: " + e.getMessage());
        }
    }

    @PostMapping("/sync/product/{productId}")
    @Operation(summary = "同步单个商品", description = "将指定商品同步到搜索索引")
    public Result<Void> syncProduct(
            @Parameter(description = "商品ID") @PathVariable Integer productId) {
        try {
            productSearchService.syncProductToIndex(productId);
            return Result.success(null, "商品同步成功");
        } catch (Exception e) {
            log.error("同步商品失败: {}", e.getMessage(), e);
            return Result.error("同步商品失败: " + e.getMessage());
        }
    }

    @PostMapping("/sync/products/batch")
    @Operation(summary = "批量同步商品", description = "批量将商品同步到搜索索引")
    public Result<Void> batchSyncProducts(
            @Parameter(description = "商品ID列表") @RequestBody List<Integer> productIds) {
        try {
            productSearchService.batchSyncProductsToIndex(productIds);
            return Result.success(null, "批量同步成功");
        } catch (Exception e) {
            log.error("批量同步商品失败: {}", e.getMessage(), e);
            return Result.error("批量同步失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/product/{productId}")
    @Operation(summary = "删除商品索引", description = "从搜索索引中删除指定商品")
    public Result<Void> deleteProductFromIndex(
            @Parameter(description = "商品ID") @PathVariable Integer productId) {
        try {
            productSearchService.deleteProductFromIndex(productId);
            return Result.success(null, "删除成功");
        } catch (Exception e) {
            log.error("删除商品索引失败: {}", e.getMessage(), e);
            return Result.error("删除失败: " + e.getMessage());
        }
    }

    @GetMapping("/health")
    @Operation(summary = "检查搜索健康状态", description = "检查Elasticsearch搜索服务的健康状态")
    public Result<Map<String, Object>> checkSearchHealth() {
        try {
            Map<String, Object> healthStatus = productSearchService.getIndexHealthStatus();
            return Result.success(healthStatus);
        } catch (Exception e) {
            log.error("检查搜索健康状态失败: {}", e.getMessage(), e);
            return Result.error("检查健康状态失败");
        }
    }

    @PostMapping("/alias/set")
    @Operation(summary = "设置索引别名", description = "为索引设置别名")
    public Result<Void> setIndexAlias(
            @Parameter(description = "索引名称") @RequestParam String indexName,
            @Parameter(description = "别名") @RequestParam String aliasName) {
        try {
            boolean success = searchIndexService.setIndexAlias(indexName, aliasName);
            if (success) {
                return Result.success(null, "设置别名成功");
            } else {
                return Result.error("设置别名失败");
            }
        } catch (Exception e) {
            log.error("设置索引别名失败: {}", e.getMessage(), e);
            return Result.error("设置别名失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/alias/remove")
    @Operation(summary = "删除索引别名", description = "删除索引别名")
    public Result<Void> removeIndexAlias(
            @Parameter(description = "索引名称") @RequestParam String indexName,
            @Parameter(description = "别名") @RequestParam String aliasName) {
        try {
            boolean success = searchIndexService.removeIndexAlias(indexName, aliasName);
            if (success) {
                return Result.success(null, "删除别名成功");
            } else {
                return Result.error("删除别名失败");
            }
        } catch (Exception e) {
            log.error("删除索引别名失败: {}", e.getMessage(), e);
            return Result.error("删除别名失败: " + e.getMessage());
        }
    }
}
