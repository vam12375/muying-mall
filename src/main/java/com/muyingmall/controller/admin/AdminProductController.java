package com.muyingmall.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.muyingmall.common.api.CommonResult;
import com.muyingmall.entity.Product;
import com.muyingmall.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理后台商品管理控制器
 */
@RestController
@RequestMapping("/admin/products")
@RequiredArgsConstructor
@Tag(name = "管理后台商品管理", description = "管理后台商品查询、管理相关接口")
public class AdminProductController {

    private final ProductService productService;

    /**
     * 分页获取商品列表
     *
     * @param page       页码
     * @param size       每页大小
     * @param keyword    关键词
     * @param categoryId 分类ID
     * @param brandId    品牌ID
     * @param status     状态
     * @return 商品分页列表
     */
    @GetMapping("/page")
    @Operation(summary = "分页获取商品列表")
    public CommonResult<Page<Product>> getProductPage(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "categoryId", required = false) Integer categoryId,
            @RequestParam(value = "brandId", required = false) Integer brandId,
            @RequestParam(value = "status", required = false) Integer status) {
        try {
            Page<Product> productPage = productService.getProductPage(page, size, categoryId, brandId, keyword, status);
            return CommonResult.success(productPage);
        } catch (Exception e) {
            return CommonResult.failed("获取商品列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取商品详情
     *
     * @param id 商品ID
     * @return 商品详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取商品详情")
    public CommonResult<Product> getProductDetail(@PathVariable Integer id) {
        try {
            Product product = productService.getProductDetail(id);
            if (product == null) {
                return CommonResult.failed("商品不存在");
            }
            return CommonResult.success(product);
        } catch (Exception e) {
            return CommonResult.failed("获取商品详情失败: " + e.getMessage());
        }
    }

    /**
     * 创建商品
     *
     * @param product 商品信息
     * @return 创建结果
     */
    @PostMapping
    @Operation(summary = "创建商品")
    public CommonResult<Boolean> createProduct(@RequestBody Product product) {
        try {
            boolean result = productService.createProduct(product);
            if (result) {
                return CommonResult.success(true, "创建商品成功");
            } else {
                return CommonResult.failed("创建商品失败");
            }
        } catch (Exception e) {
            return CommonResult.failed("创建商品失败: " + e.getMessage());
        }
    }

    /**
     * 更新商品
     *
     * @param id      商品ID
     * @param product 商品信息
     * @return 更新结果
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新商品")
    public CommonResult<Boolean> updateProduct(@PathVariable Integer id, @RequestBody Product product) {
        try {
            product.setProductId(id);
            boolean result = productService.updateProduct(product);
            if (result) {
                return CommonResult.success(true, "更新商品成功");
            } else {
                return CommonResult.failed("更新商品失败");
            }
        } catch (Exception e) {
            return CommonResult.failed("更新商品失败: " + e.getMessage());
        }
    }

    /**
     * 删除商品
     *
     * @param id 商品ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除商品")
    public CommonResult<Boolean> deleteProduct(@PathVariable Integer id) {
        try {
            boolean result = productService.deleteById(id);
            if (result) {
                return CommonResult.success(true, "删除商品成功");
            } else {
                return CommonResult.failed("删除商品失败");
            }
        } catch (Exception e) {
            return CommonResult.failed("删除商品失败: " + e.getMessage());
        }
    }

    /**
     * 更新商品状态
     *
     * @param id     商品ID
     * @param status 状态值（0-下架，1-上架）
     * @return 更新结果
     */
    @PutMapping("/{id}/status")
    @Operation(summary = "更新商品状态")
    public CommonResult<Boolean> updateProductStatus(
            @PathVariable Integer id,
            @RequestParam("status") Integer status) {
        try {
            boolean result = productService.updateStatus(id, status);
            String statusDesc = status == 1 ? "上架" : "下架";
            if (result) {
                return CommonResult.success(true, "商品" + statusDesc + "成功");
            } else {
                return CommonResult.failed("商品" + statusDesc + "失败");
            }
        } catch (Exception e) {
            return CommonResult.failed("更新商品状态失败: " + e.getMessage());
        }
    }
}