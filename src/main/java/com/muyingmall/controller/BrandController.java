package com.muyingmall.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.muyingmall.common.response.Result;
import com.muyingmall.entity.Brand;
import com.muyingmall.service.BrandService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 品牌控制器
 */
@RestController
@RequestMapping("/api/brands")
@RequiredArgsConstructor
@Tag(name = "品牌管理", description = "品牌查询、管理相关接口")
public class BrandController {

    private final BrandService brandService;

    /**
     * 获取品牌分页列表
     *
     * @param page    页码
     * @param size    每页大小
     * @param keyword 关键词
     * @return 品牌分页列表
     */
    @GetMapping
    @Operation(summary = "获取品牌分页列表")
    public Result<Page<Brand>> getBrandPage(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "keyword", required = false) String keyword) {
        try {
            Page<Brand> brandPage = brandService.getBrandPage(page, size, keyword);
            return Result.success(brandPage);
        } catch (Exception e) {
            return Result.error("获取品牌列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取所有品牌列表（不分页，用于下拉选择）
     *
     * @return 品牌列表
     */
    @GetMapping("/all")
    @Operation(summary = "获取所有品牌列表")
    public Result<List<Brand>> getAllBrands() {
        try {
            List<Brand> brands = brandService.list();
            return Result.success(brands);
        } catch (Exception e) {
            return Result.error("获取品牌列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取品牌详情
     *
     * @param id 品牌ID
     * @return 品牌详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取品牌详情")
    public Result<Brand> getBrandDetail(@PathVariable Integer id) {
        try {
            Brand brand = brandService.getBrandDetail(id);
            if (brand == null) {
                return Result.error("品牌不存在");
            }
            return Result.success(brand);
        } catch (Exception e) {
            return Result.error("获取品牌详情失败: " + e.getMessage());
        }
    }

    /**
     * 创建品牌
     *
     * @param brand 品牌信息
     * @return 创建结果
     */
    @PostMapping
    @Operation(summary = "创建品牌")
    public Result<Boolean> createBrand(@RequestBody Brand brand) {
        try {
            boolean result = brandService.createBrand(brand);
            if (result) {
                return Result.success(true, "创建品牌成功");
            } else {
                return Result.error("创建品牌失败");
            }
        } catch (Exception e) {
            return Result.error("创建品牌失败: " + e.getMessage());
        }
    }

    /**
     * 更新品牌
     *
     * @param id    品牌ID
     * @param brand 品牌信息
     * @return 更新结果
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新品牌")
    public Result<Boolean> updateBrand(@PathVariable Integer id, @RequestBody Brand brand) {
        try {
            brand.setBrandId(id);
            boolean result = brandService.updateBrand(brand);
            if (result) {
                return Result.success(true, "更新品牌成功");
            } else {
                return Result.error("更新品牌失败");
            }
        } catch (Exception e) {
            return Result.error("更新品牌失败: " + e.getMessage());
        }
    }

    /**
     * 删除品牌
     *
     * @param id 品牌ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除品牌")
    public Result<Boolean> deleteBrand(@PathVariable Integer id) {
        try {
            boolean result = brandService.deleteBrand(id);
            if (result) {
                return Result.success(true, "删除品牌成功");
            } else {
                return Result.error("删除品牌失败");
            }
        } catch (Exception e) {
            return Result.error("删除品牌失败: " + e.getMessage());
        }
    }
}