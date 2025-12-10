package com.muyingmall.controller.admin;

import com.muyingmall.common.api.Result;
import com.muyingmall.entity.SysDictItem;
import com.muyingmall.entity.SysDictType;
import com.muyingmall.service.SysDictItemService;
import com.muyingmall.service.SysDictTypeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 字典管理控制器
 * 提供字典类型和字典项的增删改查功能
 */
@RestController
@RequestMapping("/admin/dict")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "后台-字典管理", description = "系统字典配置接口")
public class AdminDictController {

    private final SysDictTypeService dictTypeService;
    private final SysDictItemService dictItemService;

    // ==================== 字典类型接口 ====================

    /**
     * 获取所有字典类型
     */
    @GetMapping("/types")
    @Operation(summary = "获取字典类型列表", description = "获取所有字典类型及其字典项数量")
    public Result<List<SysDictType>> getDictTypes() {
        try {
            List<SysDictType> dictTypes = dictTypeService.getAllDictTypes();
            return Result.success(dictTypes);
        } catch (Exception e) {
            log.error("获取字典类型列表失败", e);
            return Result.error("获取字典类型列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取单个字典类型
     */
    @GetMapping("/types/{id}")
    @Operation(summary = "获取字典类型详情", description = "根据ID获取字典类型详情")
    public Result<SysDictType> getDictType(@PathVariable Integer id) {
        try {
            SysDictType dictType = dictTypeService.getById(id);
            if (dictType == null) {
                return Result.error("字典类型不存在");
            }
            return Result.success(dictType);
        } catch (Exception e) {
            log.error("获取字典类型详情失败: {}", id, e);
            return Result.error("获取字典类型详情失败: " + e.getMessage());
        }
    }

    /**
     * 新增字典类型
     */
    @PostMapping("/types")
    @Operation(summary = "新增字典类型", description = "创建新的字典类型")
    public Result<Boolean> addDictType(@RequestBody SysDictType dictType) {
        try {
            boolean success = dictTypeService.addDictType(dictType);
            return success ? Result.success(true) : Result.error("新增字典类型失败");
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("新增字典类型失败", e);
            return Result.error("新增字典类型失败: " + e.getMessage());
        }
    }

    /**
     * 更新字典类型
     */
    @PutMapping("/types/{id}")
    @Operation(summary = "更新字典类型", description = "更新指定字典类型信息")
    public Result<Boolean> updateDictType(@PathVariable Integer id, @RequestBody SysDictType dictType) {
        try {
            dictType.setId(id);
            boolean success = dictTypeService.updateDictType(dictType);
            return success ? Result.success(true) : Result.error("更新字典类型失败");
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("更新字典类型失败: {}", id, e);
            return Result.error("更新字典类型失败: " + e.getMessage());
        }
    }

    /**
     * 删除字典类型
     */
    @DeleteMapping("/types/{id}")
    @Operation(summary = "删除字典类型", description = "删除指定字典类型及其所有字典项")
    public Result<Boolean> deleteDictType(@PathVariable Integer id) {
        try {
            boolean success = dictTypeService.deleteDictType(id);
            return success ? Result.success(true) : Result.error("删除字典类型失败");
        } catch (Exception e) {
            log.error("删除字典类型失败: {}", id, e);
            return Result.error("删除字典类型失败: " + e.getMessage());
        }
    }

    /**
     * 切换字典类型状态
     */
    @PutMapping("/types/{id}/status")
    @Operation(summary = "切换字典类型状态", description = "切换字典类型的启用/禁用状态")
    public Result<Boolean> toggleDictTypeStatus(@PathVariable Integer id) {
        try {
            boolean success = dictTypeService.toggleStatus(id);
            return success ? Result.success(true) : Result.error("切换状态失败");
        } catch (Exception e) {
            log.error("切换字典类型状态失败: {}", id, e);
            return Result.error("切换状态失败: " + e.getMessage());
        }
    }

    // ==================== 字典项接口 ====================

    /**
     * 获取指定字典类型的字典项
     */
    @GetMapping("/items/{dictCode}")
    @Operation(summary = "获取字典项列表", description = "根据字典类型编码获取字典项列表")
    public Result<List<SysDictItem>> getDictItems(@PathVariable String dictCode) {
        try {
            List<SysDictItem> items = dictItemService.getItemsByDictCode(dictCode);
            return Result.success(items);
        } catch (Exception e) {
            log.error("获取字典项列表失败: {}", dictCode, e);
            return Result.error("获取字典项列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取单个字典项
     */
    @GetMapping("/item/{id}")
    @Operation(summary = "获取字典项详情", description = "根据ID获取字典项详情")
    public Result<SysDictItem> getDictItem(@PathVariable Integer id) {
        try {
            SysDictItem item = dictItemService.getById(id);
            if (item == null) {
                return Result.error("字典项不存在");
            }
            return Result.success(item);
        } catch (Exception e) {
            log.error("获取字典项详情失败: {}", id, e);
            return Result.error("获取字典项详情失败: " + e.getMessage());
        }
    }

    /**
     * 新增字典项
     */
    @PostMapping("/items")
    @Operation(summary = "新增字典项", description = "创建新的字典项")
    public Result<Boolean> addDictItem(@RequestBody SysDictItem dictItem) {
        try {
            boolean success = dictItemService.addDictItem(dictItem);
            return success ? Result.success(true) : Result.error("新增字典项失败");
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("新增字典项失败", e);
            return Result.error("新增字典项失败: " + e.getMessage());
        }
    }

    /**
     * 更新字典项
     */
    @PutMapping("/items/{id}")
    @Operation(summary = "更新字典项", description = "更新指定字典项信息")
    public Result<Boolean> updateDictItem(@PathVariable Integer id, @RequestBody SysDictItem dictItem) {
        try {
            dictItem.setId(id);
            boolean success = dictItemService.updateDictItem(dictItem);
            return success ? Result.success(true) : Result.error("更新字典项失败");
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("更新字典项失败: {}", id, e);
            return Result.error("更新字典项失败: " + e.getMessage());
        }
    }

    /**
     * 删除字典项
     */
    @DeleteMapping("/items/{id}")
    @Operation(summary = "删除字典项", description = "删除指定字典项")
    public Result<Boolean> deleteDictItem(@PathVariable Integer id) {
        try {
            boolean success = dictItemService.deleteDictItem(id);
            return success ? Result.success(true) : Result.error("删除字典项失败");
        } catch (Exception e) {
            log.error("删除字典项失败: {}", id, e);
            return Result.error("删除字典项失败: " + e.getMessage());
        }
    }

    /**
     * 切换字典项状态
     */
    @PutMapping("/items/{id}/status")
    @Operation(summary = "切换字典项状态", description = "切换字典项的启用/禁用状态")
    public Result<Boolean> toggleDictItemStatus(@PathVariable Integer id) {
        try {
            boolean success = dictItemService.toggleStatus(id);
            return success ? Result.success(true) : Result.error("切换状态失败");
        } catch (Exception e) {
            log.error("切换字典项状态失败: {}", id, e);
            return Result.error("切换状态失败: " + e.getMessage());
        }
    }
}
