package com.muyingmall.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.muyingmall.common.api.Result;
import com.muyingmall.dto.ProductSkuDTO;
import com.muyingmall.dto.SkuStockDTO;
import com.muyingmall.dto.SkuStockLogDTO;
import com.muyingmall.service.ProductSkuService;
import com.muyingmall.service.ProductSkuStockLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 商品SKU管理控制器（后台）
 * 
 * @author 青柠檬
 * @date 2024-11-24
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/products")
@RequiredArgsConstructor
@Tag(name = "商品SKU管理（后台）")
public class AdminProductSkuController {

    private final ProductSkuService productSkuService;
    private final ProductSkuStockLogService stockLogService;

    @GetMapping("/{productId}/skus")
    @Operation(summary = "获取商品的SKU列表")
    public Result<List<ProductSkuDTO>> getSkuList(
            @Parameter(description = "商品ID") @PathVariable Integer productId) {
        List<ProductSkuDTO> skuList = productSkuService.getSkuListByProductId(productId);
        return Result.success(skuList);
    }

    @GetMapping("/skus/{skuId}")
    @Operation(summary = "获取SKU详情")
    public Result<ProductSkuDTO> getSkuDetail(
            @Parameter(description = "SKU ID") @PathVariable Long skuId) {
        ProductSkuDTO sku = productSkuService.getSkuById(skuId);
        return Result.success(sku);
    }

    @GetMapping("/skus/code/{skuCode}")
    @Operation(summary = "根据SKU编码获取详情")
    public Result<ProductSkuDTO> getSkuByCode(
            @Parameter(description = "SKU编码") @PathVariable String skuCode) {
        ProductSkuDTO sku = productSkuService.getSkuByCode(skuCode);
        return Result.success(sku);
    }

    @PostMapping("/{productId}/skus")
    @Operation(summary = "批量保存或更新SKU")
    public Result<Boolean> saveOrUpdateSkus(
            @Parameter(description = "商品ID") @PathVariable Integer productId,
            @Parameter(description = "SKU列表") @RequestBody List<ProductSkuDTO> skuList) {
        boolean result = productSkuService.saveOrUpdateBatch(productId, skuList);
        return Result.success(result);
    }

    @DeleteMapping("/skus/{skuId}")
    @Operation(summary = "删除SKU")
    public Result<Boolean> deleteSku(
            @Parameter(description = "SKU ID") @PathVariable Long skuId) {
        boolean result = productSkuService.removeById(skuId);
        return Result.success(result);
    }

    @DeleteMapping("/{productId}/skus")
    @Operation(summary = "删除商品的所有SKU")
    public Result<Boolean> deleteProductSkus(
            @Parameter(description = "商品ID") @PathVariable Integer productId) {
        boolean result = productSkuService.deleteByProductId(productId);
        return Result.success(result);
    }

    @PutMapping("/skus/{skuId}/stock")
    @Operation(summary = "调整SKU库存")
    public Result<Boolean> adjustStock(
            @Parameter(description = "SKU ID") @PathVariable Long skuId,
            @Parameter(description = "新库存数量") @RequestParam Integer stock,
            @Parameter(description = "操作人") @RequestParam(required = false) String operator,
            @Parameter(description = "备注") @RequestParam(required = false) String remark) {
        boolean result = productSkuService.adjustStock(skuId, stock, operator, remark);
        return Result.success(result);
    }

    @PostMapping("/skus/stock/deduct")
    @Operation(summary = "批量扣减库存")
    public Result<Boolean> batchDeductStock(
            @Parameter(description = "库存扣减列表") @RequestBody List<SkuStockDTO> stockList) {
        boolean result = productSkuService.batchDeductStock(stockList);
        return Result.success(result);
    }

    @PostMapping("/skus/stock/restore")
    @Operation(summary = "批量恢复库存")
    public Result<Boolean> batchRestoreStock(
            @Parameter(description = "库存恢复列表") @RequestBody List<SkuStockDTO> stockList) {
        boolean result = productSkuService.batchRestoreStock(stockList);
        return Result.success(result);
    }

    @GetMapping("/skus/low-stock")
    @Operation(summary = "查询库存不足的SKU列表")
    public Result<List<ProductSkuDTO>> getLowStockSkus(
            @Parameter(description = "库存阈值") @RequestParam(defaultValue = "10") Integer threshold) {
        List<ProductSkuDTO> skuList = productSkuService.getLowStockSkus(threshold);
        return Result.success(skuList);
    }

    @GetMapping("/skus/{skuId}/stock/check")
    @Operation(summary = "检查SKU库存是否充足")
    public Result<Boolean> checkStock(
            @Parameter(description = "SKU ID") @PathVariable Long skuId,
            @Parameter(description = "需要数量") @RequestParam Integer quantity) {
        boolean result = productSkuService.checkStock(skuId, quantity);
        return Result.success(result);
    }

    @GetMapping("/skus/stock/logs")
    @Operation(summary = "分页查询库存日志")
    public Result<IPage<SkuStockLogDTO>> getStockLogs(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "SKU ID") @RequestParam(required = false) Long skuId,
            @Parameter(description = "订单ID") @RequestParam(required = false) Integer orderId,
            @Parameter(description = "变更类型") @RequestParam(required = false) String changeType,
            @Parameter(description = "开始时间") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @Parameter(description = "结束时间") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        IPage<SkuStockLogDTO> logs = stockLogService.getLogPage(page, size, skuId, orderId, changeType, startTime, endTime);
        return Result.success(logs);
    }

    @GetMapping("/skus/{skuId}/stock/logs")
    @Operation(summary = "查询SKU的库存日志")
    public Result<List<SkuStockLogDTO>> getSkuStockLogs(
            @Parameter(description = "SKU ID") @PathVariable Long skuId) {
        List<SkuStockLogDTO> logs = stockLogService.getLogsBySkuId(skuId);
        return Result.success(logs);
    }
}
