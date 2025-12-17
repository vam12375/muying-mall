package com.muyingmall.controller.common;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.muyingmall.common.api.Result;
import com.muyingmall.entity.Brand;
import com.muyingmall.service.BrandService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 品牌控制器
 */
@Slf4j
@RestController
@RequestMapping("/brands")
@RequiredArgsConstructor
@Tag(name = "品牌管理", description = "品牌查询相关接口")
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
            @Parameter(description = "页码，默认1") @RequestParam(value = "page", defaultValue = "1") int page,
            @Parameter(description = "每页大小，默认10") @RequestParam(value = "size", defaultValue = "10") int size,
            @Parameter(description = "搜索关键词") @RequestParam(value = "keyword", required = false) String keyword) {
        try {
            log.debug("获取品牌分页列表: page={}, size={}, keyword={}", page, size, keyword);
            Page<Brand> brandPage = brandService.getBrandPage(page, size, keyword);

            // 打印结果统计信息
            log.debug("品牌列表获取成功: 总数={}, 当前页数据数量={}",
                    brandPage.getTotal(), brandPage.getRecords().size());

            return Result.success(brandPage);
        } catch (Exception e) {
            log.error("获取品牌列表失败: page={}, size={}, keyword={}, 错误: {}",
                    page, size, keyword, e.getMessage(), e);
            return Result.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "获取品牌列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取所有品牌列表（不分页，用于下拉选择）
     *
     * @return 品牌列表
     */
    @GetMapping("/all")
    @Operation(summary = "获取所有品牌列表", description = "不分页，用于下拉菜单选择")
    public Result<List<Brand>> getAllBrands() {
        try {
            log.debug("获取所有品牌列表");
            List<Brand> brands = brandService.list();
            log.debug("所有品牌列表获取成功: 品牌数量={}", brands.size());
            return Result.success(brands);
        } catch (Exception e) {
            log.error("获取所有品牌列表失败: {}", e.getMessage(), e);
            return Result.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "获取品牌列表失败: " + e.getMessage());
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
    public Result<Brand> getBrandDetail(@Parameter(description = "品牌ID") @PathVariable Integer id) {
        try {
            log.debug("获取品牌详情: id={}", id);
            Brand brand = brandService.getBrandDetail(id);
            if (brand == null) {
                log.warn("品牌不存在: id={}", id);
                return Result.error(HttpStatus.NOT_FOUND.value(), "品牌不存在");
            }
            log.debug("品牌详情获取成功: id={}, name={}", id, brand.getName());
            return Result.success(brand);
        } catch (Exception e) {
            log.error("获取品牌详情失败: id={}, 错误: {}", id, e.getMessage(), e);
            return Result.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "获取品牌详情失败: " + e.getMessage());
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
            log.debug("创建品牌: {}", brand);
            boolean result = brandService.createBrand(brand);
            if (result) {
                log.debug("创建品牌成功: id={}, name={}", brand.getBrandId(), brand.getName());
                return Result.success(true, "创建品牌成功");
            } else {
                log.warn("创建品牌失败: {}", brand);
                return Result.error(HttpStatus.BAD_REQUEST.value(), "创建品牌失败");
            }
        } catch (Exception e) {
            log.error("创建品牌失败: {}, 错误: {}", brand, e.getMessage(), e);
            return Result.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "创建品牌失败: " + e.getMessage());
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
    public Result<Boolean> updateBrand(
            @Parameter(description = "品牌ID") @PathVariable Integer id,
            @RequestBody Brand brand) {
        try {
            log.debug("更新品牌: id={}, brand={}", id, brand);
            brand.setBrandId(id);
            boolean result = brandService.updateBrand(brand);
            if (result) {
                log.debug("更新品牌成功: id={}", id);
                return Result.success(true, "更新品牌成功");
            } else {
                log.warn("更新品牌失败: id={}", id);
                return Result.error(HttpStatus.BAD_REQUEST.value(), "更新品牌失败");
            }
        } catch (Exception e) {
            log.error("更新品牌失败: id={}, 错误: {}", id, e.getMessage(), e);
            return Result.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "更新品牌失败: " + e.getMessage());
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
    public Result<Boolean> deleteBrand(@Parameter(description = "品牌ID") @PathVariable Integer id) {
        try {
            log.debug("删除品牌: id={}", id);
            boolean result = brandService.deleteBrand(id);
            if (result) {
                log.debug("删除品牌成功: id={}", id);
                return Result.success(true, "删除品牌成功");
            } else {
                log.warn("删除品牌失败: id={}", id);
                return Result.error(HttpStatus.BAD_REQUEST.value(), "删除品牌失败");
            }
        } catch (Exception e) {
            log.error("删除品牌失败: id={}, 错误: {}", id, e.getMessage(), e);
            return Result.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "删除品牌失败: " + e.getMessage());
        }
    }
}