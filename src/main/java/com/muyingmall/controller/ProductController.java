package com.muyingmall.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.muyingmall.common.response.Result;
import com.muyingmall.entity.Product;
import com.muyingmall.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
@Tag(name = "商品管理", description = "商品查询、管理相关接口")
public class ProductController {

    private final ProductService productService;

    @GetMapping
    @Operation(summary = "获取商品列表")
    public Result<Page<Product>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) Boolean isHot,
            @RequestParam(required = false) Boolean isNew,
            @RequestParam(required = false) Boolean isRecommend,
            @RequestParam(required = false) String keyword) {
        
        Page<Product> productPage = productService.getProductPage(page, size, categoryId, isHot, isNew, isRecommend, keyword);
        return Result.success(productPage);
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取商品详情")
    public Result<Product> detail(@PathVariable("id") Integer id) {
        Product product = productService.getProductDetail(id);
        if (product == null) {
            return Result.error("商品不存在");
        }
        return Result.success(product);
    }
}