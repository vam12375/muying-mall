package com.muyingmall.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.muyingmall.common.api.CommonResult;
import com.muyingmall.entity.LogisticsCompany;
import com.muyingmall.service.LogisticsCompanyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 管理后台物流公司控制器
 */
@RestController
@RequestMapping("/admin/logistics/companies")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "管理后台物流公司管理", description = "管理后台物流公司管理相关接口")
public class AdminLogisticsCompanyController {

    private final LogisticsCompanyService logisticsCompanyService;

    /**
     * 分页获取物流公司列表
     *
     * @param page     页码
     * @param pageSize 每页大小
     * @param keyword  搜索关键词
     * @return 物流公司列表
     */
    @GetMapping
    @Operation(summary = "分页获取物流公司列表")
    public CommonResult<Map<String, Object>> getCompanyList(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "pageSize", defaultValue = "10") int pageSize,
            @RequestParam(value = "keyword", required = false) String keyword) {
        try {
            Page<LogisticsCompany> companyPage = logisticsCompanyService.getCompanyList(page, pageSize, keyword);

            Map<String, Object> result = new HashMap<>();
            result.put("list", companyPage.getRecords());
            result.put("total", companyPage.getTotal());

            return CommonResult.success(result);
        } catch (Exception e) {
            log.error("获取物流公司列表失败", e);
            return CommonResult.failed("获取物流公司列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取所有启用的物流公司
     *
     * @return 物流公司列表
     */
    @GetMapping("/enabled")
    @Operation(summary = "获取所有启用的物流公司")
    public CommonResult<List<LogisticsCompany>> getEnabledCompanies() {
        try {
            List<LogisticsCompany> companies = logisticsCompanyService.getAllEnabledCompanies();
            return CommonResult.success(companies);
        } catch (Exception e) {
            log.error("获取启用物流公司失败", e);
            return CommonResult.failed("获取启用物流公司失败: " + e.getMessage());
        }
    }

    /**
     * 获取物流公司详情
     *
     * @param id 物流公司ID
     * @return 物流公司详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取物流公司详情")
    public CommonResult<LogisticsCompany> getCompanyDetail(@PathVariable("id") Integer id) {
        try {
            LogisticsCompany company = logisticsCompanyService.getCompanyById(id);
            if (company == null) {
                return CommonResult.failed("物流公司不存在");
            }
            return CommonResult.success(company);
        } catch (Exception e) {
            log.error("获取物流公司详情失败", e);
            return CommonResult.failed("获取物流公司详情失败: " + e.getMessage());
        }
    }

    /**
     * 添加物流公司
     *
     * @param company 物流公司信息
     * @return 添加结果
     */
    @PostMapping
    @Operation(summary = "添加物流公司")
    public CommonResult<Boolean> addCompany(@RequestBody LogisticsCompany company) {
        try {
            boolean result = logisticsCompanyService.addCompany(company);
            if (result) {
                return CommonResult.success(true, "添加物流公司成功");
            } else {
                return CommonResult.failed("添加物流公司失败");
            }
        } catch (Exception e) {
            log.error("添加物流公司失败", e);
            return CommonResult.failed("添加物流公司失败: " + e.getMessage());
        }
    }

    /**
     * 更新物流公司
     *
     * @param id      物流公司ID
     * @param company 物流公司信息
     * @return 更新结果
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新物流公司")
    public CommonResult<Boolean> updateCompany(
            @PathVariable("id") Integer id,
            @RequestBody LogisticsCompany company) {
        try {
            // 设置ID确保更新正确的记录
            company.setId(id);
            boolean result = logisticsCompanyService.updateCompany(company);
            if (result) {
                return CommonResult.success(true, "更新物流公司成功");
            } else {
                return CommonResult.failed("更新物流公司失败");
            }
        } catch (Exception e) {
            log.error("更新物流公司失败", e);
            return CommonResult.failed("更新物流公司失败: " + e.getMessage());
        }
    }

    /**
     * 删除物流公司
     *
     * @param id 物流公司ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除物流公司")
    public CommonResult<Boolean> deleteCompany(@PathVariable("id") Integer id) {
        try {
            boolean result = logisticsCompanyService.deleteCompany(id);
            if (result) {
                return CommonResult.success(true, "删除物流公司成功");
            } else {
                return CommonResult.failed("删除物流公司失败");
            }
        } catch (Exception e) {
            log.error("删除物流公司失败", e);
            return CommonResult.failed("删除物流公司失败: " + e.getMessage());
        }
    }
}