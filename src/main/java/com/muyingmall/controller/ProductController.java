package com.muyingmall.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.muyingmall.common.response.Result;
import com.muyingmall.entity.Product;
import com.muyingmall.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

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

        Page<Product> productPage = productService.getProductPage(page, size, categoryId, isHot, isNew, isRecommend,
                keyword);
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

    @GetMapping("/recommended")
    @Operation(summary = "获取推荐商品列表", description = "根据类型返回本店推荐或猜你喜欢商品")
    public Result<List<Product>> recommended(
            @RequestParam(required = false) Integer productId,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "shop") String type) {

        try {
            // 使用优化后的推荐服务方法获取推荐商品
            List<Product> recommendedProducts = productService.getRecommendedProducts(
                    productId, categoryId, limit, type);

            return Result.success(recommendedProducts);
        } catch (Exception e) {
            return Result.error("获取推荐商品失败: " + e.getMessage());
        }
    }
}