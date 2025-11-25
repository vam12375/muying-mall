package com.muyingmall.controller;

import com.muyingmall.common.Result;
import com.muyingmall.dto.ProductSkuDTO;
import com.muyingmall.service.ProductSkuService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 商品SKU控制器（前台）
 * 
 * @author AI Assistant
 * @date 2024-11-24
 */
@Slf4j
@RestController
@RequestMapping("/api/products")
@Api(tags = "商品SKU（前台）")
public class ProductSkuController {

    @Resource
    private ProductSkuService productSkuService;

    @GetMapping("/{productId}/skus")
    @ApiOperation("获取商品的SKU列表")
    public Result<List<ProductSkuDTO>> getProductSkus(
            @ApiParam("商品ID") @PathVariable Integer productId) {
        List<ProductSkuDTO> skuList = productSkuService.getSkuListByProductId(productId);
        // 只返回启用状态的SKU
        List<ProductSkuDTO> enabledSkus = skuList.stream()
                .filter(sku -> sku.getStatus() == 1)
                .collect(java.util.stream.Collectors.toList());
        return Result.success(enabledSkus);
    }

    @GetMapping("/skus/{skuId}")
    @ApiOperation("获取SKU详情")
    public Result<ProductSkuDTO> getSkuDetail(
            @ApiParam("SKU ID") @PathVariable Long skuId) {
        ProductSkuDTO sku = productSkuService.getSkuById(skuId);
        return Result.success(sku);
    }

    @GetMapping("/skus/{skuId}/stock")
    @ApiOperation("检查SKU库存")
    public Result<Integer> getSkuStock(
            @ApiParam("SKU ID") @PathVariable Long skuId) {
        ProductSkuDTO sku = productSkuService.getSkuById(skuId);
        return Result.success(sku.getStock());
    }

    @GetMapping("/skus/{skuId}/check-stock")
    @ApiOperation("检查SKU库存是否充足")
    public Result<Boolean> checkStock(
            @ApiParam("SKU ID") @PathVariable Long skuId,
            @ApiParam("需要数量") @RequestParam Integer quantity) {
        boolean result = productSkuService.checkStock(skuId, quantity);
        return Result.success(result);
    }
}
