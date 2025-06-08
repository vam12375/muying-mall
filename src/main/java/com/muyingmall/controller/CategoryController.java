package com.muyingmall.controller;

import com.muyingmall.common.response.Result;
import com.muyingmall.entity.Category;
import com.muyingmall.entity.Product;
import com.muyingmall.service.CategoryService;
import com.muyingmall.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 商品分类控制器（公共API）
 */
@Slf4j
@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
@Tag(name = "商品分类", description = "商品分类查询相关接口")
public class CategoryController {

    private final CategoryService categoryService;
    private final ProductService productService;

    /**
     * 获取所有分类列表
     *
     * @return 分类列表
     */
    @GetMapping
    @Operation(summary = "获取所有分类列表")
    public Result<List<Category>> getAllCategories() {
        try {
            log.info("获取所有分类列表");
            // 获取所有状态正常的分类
            List<Category> categories = categoryService.list()
                    .stream()
                    .filter(category -> category.getStatus() != null && category.getStatus() == 1)
                    .collect(Collectors.toList());

            log.info("分类列表获取成功: 分类数量={}", categories.size());
            return Result.success(categories);
        } catch (Exception e) {
            log.error("获取分类列表失败: {}", e.getMessage(), e);
            return Result.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "获取分类列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取分类详情
     *
     * @param id 分类ID
     * @return 分类详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取分类详情")
    public Result<Category> getCategoryDetail(@Parameter(description = "分类ID") @PathVariable Integer id) {
        try {
            log.info("获取分类详情: id={}", id);
            if (id == null || id <= 0) {
                log.warn("无效的分类ID: {}", id);
                return Result.error(HttpStatus.BAD_REQUEST.value(), "无效的分类ID");
            }

            Category category = categoryService.getById(id);
            if (category == null) {
                log.warn("分类不存在: id={}", id);
                return Result.error(HttpStatus.NOT_FOUND.value(), "分类不存在");
            }

            if (category.getStatus() != null && category.getStatus() != 1) {
                log.warn("分类已禁用: id={}", id);
                return Result.error(HttpStatus.BAD_REQUEST.value(), "分类已禁用");
            }

            log.info("分类详情获取成功: id={}, name={}", id, category.getName());
            return Result.success(category);
        } catch (Exception e) {
            log.error("获取分类详情失败: id={}, 错误: {}", id, e.getMessage(), e);
            return Result.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "获取分类详情失败: " + e.getMessage());
        }
    }

    /**
     * 获取分类下的商品
     *
     * @param id   分类ID
     * @param page 页码
     * @param size 每页大小
     * @return 商品列表
     */
    @GetMapping("/{id}/products")
    @Operation(summary = "获取分类下的商品")
    public Result<Object> getCategoryProducts(
            @Parameter(description = "分类ID") @PathVariable Integer id,
            @Parameter(description = "页码，默认1") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页大小，默认10") @RequestParam(defaultValue = "10") int size) {
        try {
            log.info("获取分类商品: categoryId={}, page={}, size={}", id, page, size);

            if (id == null || id <= 0) {
                log.warn("无效的分类ID: {}", id);
                return Result.error(HttpStatus.BAD_REQUEST.value(), "无效的分类ID");
            }

            // 检查分类是否存在
            Category category = categoryService.getById(id);
            if (category == null) {
                log.warn("分类不存在: id={}", id);
                return Result.error(HttpStatus.NOT_FOUND.value(), "分类不存在");
            }

            if (category.getStatus() != null && category.getStatus() != 1) {
                log.warn("分类已禁用: id={}", id);
                return Result.error(HttpStatus.BAD_REQUEST.value(), "分类已禁用");
            }

            // 获取分类下的商品
            return Result.success(productService.getProductPage(page, size, id, null, null, null, null));
        } catch (Exception e) {
            log.error("获取分类商品失败: categoryId={}, 错误: {}", id, e.getMessage(), e);
            return Result.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "获取分类商品失败: " + e.getMessage());
        }
    }

    /**
     * 获取子分类列表
     *
     * @param parentId 父分类ID
     * @return 子分类列表
     */
    @GetMapping("/sub")
    @Operation(summary = "获取子分类列表")
    public Result<List<Category>> getSubCategories(
            @Parameter(description = "父分类ID") @RequestParam Integer parentId) {
        try {
            log.info("获取子分类列表: parentId={}", parentId);

            if (parentId == null) {
                log.warn("无效的父分类ID: null");
                return Result.error(HttpStatus.BAD_REQUEST.value(), "父分类ID不能为空");
            }

            // 获取指定父分类下的子分类
            List<Category> subCategories = categoryService.list()
                    .stream()
                    .filter(category -> category.getParentId() != null &&
                            category.getParentId().equals(parentId) &&
                            category.getStatus() != null &&
                            category.getStatus() == 1)
                    .collect(Collectors.toList());

            log.info("子分类列表获取成功: parentId={}, 子分类数量={}", parentId, subCategories.size());
            return Result.success(subCategories);
        } catch (Exception e) {
            log.error("获取子分类列表失败: parentId={}, 错误: {}", parentId, e.getMessage(), e);
            return Result.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "获取子分类列表失败: " + e.getMessage());
        }
    }
}