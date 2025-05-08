package com.muyingmall.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.muyingmall.common.api.CommonResult;
import com.muyingmall.entity.Category;
import com.muyingmall.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理后台分类管理控制器
 */
@RestController
@RequestMapping("/admin/categories")
@RequiredArgsConstructor
@Tag(name = "管理后台分类管理", description = "管理后台分类查询、管理相关接口")
public class AdminCategoryController {

    private final CategoryService categoryService;

    /**
     * 获取所有分类（树形结构）
     *
     * @return 分类列表
     */
    @GetMapping
    @Operation(summary = "获取所有分类（树形结构）")
    public CommonResult<List<Category>> getAllCategories() {
        try {
            List<Category> categories = categoryService.listWithTree();
            return CommonResult.success(categories);
        } catch (Exception e) {
            return CommonResult.failed("获取分类列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取所有分类（平铺结构）
     *
     * @return 分类列表
     */
    @GetMapping("/list")
    @Operation(summary = "获取所有分类（平铺结构）")
    public CommonResult<List<Category>> getAllCategoriesFlat() {
        try {
            List<Category> categories = categoryService.list();
            return CommonResult.success(categories);
        } catch (Exception e) {
            return CommonResult.failed("获取分类列表失败: " + e.getMessage());
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
    public CommonResult<Category> getCategoryDetail(@PathVariable Integer id) {
        try {
            Category category = categoryService.getById(id);
            if (category == null) {
                return CommonResult.failed("分类不存在");
            }
            return CommonResult.success(category);
        } catch (Exception e) {
            return CommonResult.failed("获取分类详情失败: " + e.getMessage());
        }
    }

    /**
     * 创建分类
     *
     * @param category 分类信息
     * @return 创建结果
     */
    @PostMapping
    @Operation(summary = "创建分类")
    public CommonResult<Boolean> createCategory(@RequestBody Category category) {
        try {
            boolean result = categoryService.save(category);
            if (result) {
                return CommonResult.success(true, "创建分类成功");
            } else {
                return CommonResult.failed("创建分类失败");
            }
        } catch (Exception e) {
            return CommonResult.failed("创建分类失败: " + e.getMessage());
        }
    }

    /**
     * 更新分类
     *
     * @param id       分类ID
     * @param category 分类信息
     * @return 更新结果
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新分类")
    public CommonResult<Boolean> updateCategory(@PathVariable Integer id, @RequestBody Category category) {
        try {
            category.setCategoryId(id);
            boolean result = categoryService.updateById(category);
            if (result) {
                return CommonResult.success(true, "更新分类成功");
            } else {
                return CommonResult.failed("更新分类失败");
            }
        } catch (Exception e) {
            return CommonResult.failed("更新分类失败: " + e.getMessage());
        }
    }

    /**
     * 删除分类
     *
     * @param id 分类ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除分类")
    public CommonResult<Boolean> deleteCategory(@PathVariable Integer id) {
        try {
            // 检查是否有子分类
            if (categoryService.hasChildren(id)) {
                return CommonResult.failed("该分类下有子分类，无法删除");
            }

            // 检查分类下是否有商品
            if (categoryService.hasProducts(id)) {
                return CommonResult.failed("该分类下有商品，无法删除");
            }

            boolean result = categoryService.removeById(id);
            if (result) {
                return CommonResult.success(true, "删除分类成功");
            } else {
                return CommonResult.failed("删除分类失败");
            }
        } catch (Exception e) {
            return CommonResult.failed("删除分类失败: " + e.getMessage());
        }
    }
}