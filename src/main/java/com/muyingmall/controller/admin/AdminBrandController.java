package com.muyingmall.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.muyingmall.common.api.CommonPage;
import com.muyingmall.common.api.CommonResult;
import com.muyingmall.dto.BrandDTO;
import com.muyingmall.entity.Brand;
import com.muyingmall.service.BrandService;
import com.muyingmall.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 管理后台品牌管理控制器
 */
@RestController
@RequestMapping("/admin/brands")
@RequiredArgsConstructor
@Tag(name = "管理后台品牌管理", description = "管理后台品牌查询、管理相关接口")
@Slf4j
public class AdminBrandController {

    private final BrandService brandService;
    private final ProductService productService;

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
    public CommonResult<Map<String, Object>> getBrandPage(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "keyword", required = false) String keyword) {
        try {
            log.debug("获取品牌分页列表: page={}, size={}, keyword={}", page, size, keyword);

            // 获取品牌分页数据
            Page<Brand> brandPage = brandService.getBrandPage(page, size, keyword);

            // 转换为前端期望的数据结构，并添加商品数量
            Map<String, Object> result = CommonPage.restPage(brandPage, brand -> {
                // 获取品牌关联的商品数量
                int productCount = productService.getProductCountByBrandId(brand.getBrandId());
                return BrandDTO.fromEntity(brand, productCount);
            });

            log.debug("获取品牌分页列表成功: 总记录数={}, 当前页记录数={}",
                    brandPage.getTotal(), brandPage.getRecords().size());

            return CommonResult.success(result);
        } catch (Exception e) {
            log.error("获取品牌列表失败", e);
            return CommonResult.failed("获取品牌列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取所有品牌列表（不分页，用于下拉选择）
     *
     * @return 品牌列表
     */
    @GetMapping("/all")
    @Operation(summary = "获取所有品牌列表")
    public CommonResult<List<BrandDTO>> getAllBrands() {
        try {
            log.debug("获取所有品牌列表");

            List<Brand> brands = brandService.list();
            List<BrandDTO> brandDTOs = brands.stream()
                    .map(brand -> {
                        // 获取品牌关联的商品数量
                        int productCount = productService.getProductCountByBrandId(brand.getBrandId());
                        return BrandDTO.fromEntity(brand, productCount);
                    })
                    .collect(Collectors.toList());

            log.debug("获取所有品牌列表成功: 总记录数={}", brands.size());

            return CommonResult.success(brandDTOs);
        } catch (Exception e) {
            log.error("获取品牌列表失败", e);
            return CommonResult.failed("获取品牌列表失败: " + e.getMessage());
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
    public CommonResult<BrandDTO> getBrandDetail(@PathVariable Integer id) {
        try {
            log.debug("获取品牌详情: id={}", id);

            Brand brand = brandService.getBrandDetail(id);
            if (brand == null) {
                log.warn("品牌不存在: id={}", id);
                return CommonResult.failed("品牌不存在");
            }

            // 获取品牌关联的商品数量
            int productCount = productService.getProductCountByBrandId(id);
            BrandDTO brandDTO = BrandDTO.fromEntity(brand, productCount);

            log.debug("获取品牌详情成功: id={}, name={}", id, brand.getName());

            return CommonResult.success(brandDTO);
        } catch (Exception e) {
            log.error("获取品牌详情失败: id=" + id, e);
            return CommonResult.failed("获取品牌详情失败: " + e.getMessage());
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
    @com.muyingmall.annotation.AdminOperationLog(operation = "创建品牌", module = "品牌管理", operationType = "CREATE", targetType = "brand")
    public CommonResult<Boolean> createBrand(@RequestBody Brand brand) {
        try {
            log.debug("创建品牌: {}", brand);

            boolean result = brandService.createBrand(brand);
            if (result) {
                log.debug("创建品牌成功: id={}, name={}", brand.getBrandId(), brand.getName());
                return CommonResult.success(true, "创建品牌成功");
            } else {
                log.warn("创建品牌失败");
                return CommonResult.failed("创建品牌失败");
            }
        } catch (Exception e) {
            log.error("创建品牌失败", e);
            return CommonResult.failed("创建品牌失败: " + e.getMessage());
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
    @com.muyingmall.annotation.AdminOperationLog(operation = "更新品牌", module = "品牌管理", operationType = "UPDATE", targetType = "brand")
    public CommonResult<Boolean> updateBrand(@PathVariable Integer id, @RequestBody Brand brand) {
        try {
            log.debug("更新品牌: id={}, brand={}", id, brand);

            brand.setBrandId(id);
            boolean result = brandService.updateBrand(brand);
            if (result) {
                log.debug("更新品牌成功: id={}", id);
                return CommonResult.success(true, "更新品牌成功");
            } else {
                log.warn("更新品牌失败: id={}", id);
                return CommonResult.failed("更新品牌失败");
            }
        } catch (Exception e) {
            log.error("更新品牌失败: id=" + id, e);
            return CommonResult.failed("更新品牌失败: " + e.getMessage());
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
    @com.muyingmall.annotation.AdminOperationLog(operation = "删除品牌", module = "品牌管理", operationType = "DELETE", targetType = "brand")
    public CommonResult<Boolean> deleteBrand(@PathVariable Integer id) {
        try {
            log.debug("删除品牌: id={}", id);

            boolean result = brandService.deleteBrand(id);
            if (result) {
                log.debug("删除品牌成功: id={}", id);
                return CommonResult.success(true, "删除品牌成功");
            } else {
                log.warn("删除品牌失败: id={}", id);
                return CommonResult.failed("删除品牌失败");
            }
        } catch (Exception e) {
            log.error("删除品牌失败: id=" + id, e);
            return CommonResult.failed("删除品牌失败: " + e.getMessage());
        }
    }
}