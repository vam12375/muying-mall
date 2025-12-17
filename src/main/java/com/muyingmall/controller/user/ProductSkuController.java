package com.muyingmall.controller.user;

import com.muyingmall.common.api.Result;
import com.muyingmall.dto.ProductSkuDTO;
import com.muyingmall.service.ProductSkuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 商品SKU控制器（前台）
 * 
 * @author 青柠檬
 * @date 2024-11-24
 */
@Slf4j
@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
@Tag(name = "商品SKU（前台）")
public class ProductSkuController {

    private final ProductSkuService productSkuService;

    @GetMapping("/{productId}/skus")
    @Operation(summary = "获取商品的SKU列表")
    public Result<List<ProductSkuDTO>> getProductSkus(
            @Parameter(description = "商品ID") @PathVariable Integer productId) {
        try {
            log.debug("开始获取商品SKU列表，商品ID: {}", productId);
            List<ProductSkuDTO> skuList = productSkuService.getSkuListByProductId(productId);
            log.debug("查询到SKU数量: {}", skuList != null ? skuList.size() : 0);
            
            // 只返回启用状态的SKU
            List<ProductSkuDTO> enabledSkus = skuList.stream()
                    .filter(sku -> sku.getStatus() == 1)
                    .collect(Collectors.toList());
            log.debug("启用状态的SKU数量: {}", enabledSkus.size());
            return Result.success(enabledSkus);
        } catch (Exception e) {
            log.error("获取商品SKU列表失败，商品ID: {}", productId, e);
            throw e;
        }
    }

    @GetMapping("/skus/{skuId}")
    @Operation(summary = "获取SKU详情")
    public Result<ProductSkuDTO> getSkuDetail(
            @Parameter(description = "SKU ID") @PathVariable Long skuId) {
        ProductSkuDTO sku = productSkuService.getSkuById(skuId);
        return Result.success(sku);
    }

    @GetMapping("/skus/{skuId}/stock")
    @Operation(summary = "检查SKU库存")
    public Result<Integer> getSkuStock(
            @Parameter(description = "SKU ID") @PathVariable Long skuId) {
        ProductSkuDTO sku = productSkuService.getSkuById(skuId);
        return Result.success(sku.getStock());
    }

    @GetMapping("/skus/{skuId}/check-stock")
    @Operation(summary = "检查SKU库存是否充足")
    public Result<Boolean> checkStock(
            @Parameter(description = "SKU ID") @PathVariable Long skuId,
            @Parameter(description = "需要数量") @RequestParam Integer quantity) {
        boolean result = productSkuService.checkStock(skuId, quantity);
        return Result.success(result);
    }
}
