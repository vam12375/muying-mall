package com.muyingmall.controller.admin;

import com.muyingmall.common.api.Result;
import com.muyingmall.entity.SysMenu;
import com.muyingmall.service.SysMenuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 菜单管理控制器
 * 提供系统菜单的增删改查功能
 */
@RestController
@RequestMapping("/admin/menu")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "后台-菜单管理", description = "系统菜单配置接口")
public class AdminMenuController {

    private final SysMenuService menuService;

    /**
     * 获取菜单树
     */
    @GetMapping("/tree")
    @Operation(summary = "获取菜单树", description = "获取树形结构的菜单列表")
    public Result<List<SysMenu>> getMenuTree() {
        try {
            List<SysMenu> menuTree = menuService.getMenuTree();
            return Result.success(menuTree);
        } catch (Exception e) {
            log.error("获取菜单树失败", e);
            return Result.error("获取菜单树失败: " + e.getMessage());
        }
    }

    /**
     * 获取所有菜单（平铺）
     */
    @GetMapping("/list")
    @Operation(summary = "获取菜单列表", description = "获取所有菜单的平铺列表")
    public Result<List<SysMenu>> getMenuList() {
        try {
            List<SysMenu> menus = menuService.list();
            return Result.success(menus);
        } catch (Exception e) {
            log.error("获取菜单列表失败", e);
            return Result.error("获取菜单列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取单个菜单
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取菜单详情", description = "根据ID获取菜单详情")
    public Result<SysMenu> getMenu(@PathVariable Integer id) {
        try {
            SysMenu menu = menuService.getById(id);
            if (menu == null) {
                return Result.error("菜单不存在");
            }
            return Result.success(menu);
        } catch (Exception e) {
            log.error("获取菜单详情失败: {}", id, e);
            return Result.error("获取菜单详情失败: " + e.getMessage());
        }
    }

    /**
     * 新增菜单
     */
    @PostMapping
    @Operation(summary = "新增菜单", description = "创建新的菜单项")
    public Result<Boolean> addMenu(@RequestBody SysMenu menu) {
        try {
            boolean success = menuService.addMenu(menu);
            return success ? Result.success(true) : Result.error("新增菜单失败");
        } catch (Exception e) {
            log.error("新增菜单失败", e);
            return Result.error("新增菜单失败: " + e.getMessage());
        }
    }

    /**
     * 更新菜单
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新菜单", description = "更新指定菜单信息")
    public Result<Boolean> updateMenu(@PathVariable Integer id, @RequestBody SysMenu menu) {
        try {
            menu.setId(id);
            boolean success = menuService.updateMenu(menu);
            return success ? Result.success(true) : Result.error("更新菜单失败");
        } catch (Exception e) {
            log.error("更新菜单失败: {}", id, e);
            return Result.error("更新菜单失败: " + e.getMessage());
        }
    }

    /**
     * 删除菜单
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除菜单", description = "删除指定菜单及其子菜单")
    public Result<Boolean> deleteMenu(@PathVariable Integer id) {
        try {
            boolean success = menuService.deleteMenu(id);
            return success ? Result.success(true) : Result.error("删除菜单失败");
        } catch (Exception e) {
            log.error("删除菜单失败: {}", id, e);
            return Result.error("删除菜单失败: " + e.getMessage());
        }
    }

    /**
     * 切换菜单可见性
     */
    @PutMapping("/{id}/visible")
    @Operation(summary = "切换可见性", description = "切换菜单的显示/隐藏状态")
    public Result<Boolean> toggleVisible(@PathVariable Integer id) {
        try {
            boolean success = menuService.toggleVisible(id);
            return success ? Result.success(true) : Result.error("切换可见性失败");
        } catch (Exception e) {
            log.error("切换菜单可见性失败: {}", id, e);
            return Result.error("切换可见性失败: " + e.getMessage());
        }
    }

    /**
     * 更新菜单排序
     */
    @PutMapping("/{id}/sort")
    @Operation(summary = "更新排序", description = "更新菜单的排序值")
    public Result<Boolean> updateSort(@PathVariable Integer id, @RequestBody Map<String, Integer> body) {
        try {
            Integer sort = body.get("sort");
            if (sort == null) {
                return Result.error("排序值不能为空");
            }
            boolean success = menuService.updateSort(id, sort);
            return success ? Result.success(true) : Result.error("更新排序失败");
        } catch (Exception e) {
            log.error("更新菜单排序失败: {}", id, e);
            return Result.error("更新排序失败: " + e.getMessage());
        }
    }

    /**
     * 批量更新菜单排序
     */
    @PutMapping("/batch-sort")
    @Operation(summary = "批量更新排序", description = "批量更新菜单的排序值和父级")
    public Result<Boolean> batchUpdateSort(@RequestBody List<Map<String, Integer>> sortList) {
        try {
            boolean success = menuService.batchUpdateSort(sortList);
            return success ? Result.success(true) : Result.error("批量更新排序失败");
        } catch (Exception e) {
            log.error("批量更新菜单排序失败", e);
            return Result.error("批量更新排序失败: " + e.getMessage());
        }
    }

    /**
     * 获取侧边栏菜单（仅可见的）
     */
    @GetMapping("/sidebar")
    @Operation(summary = "获取侧边栏菜单", description = "获取用于侧边栏显示的菜单树")
    public Result<List<SysMenu>> getSidebarMenus() {
        try {
            List<SysMenu> menuTree = menuService.getAllMenuTree();
            return Result.success(menuTree);
        } catch (Exception e) {
            log.error("获取侧边栏菜单失败", e);
            return Result.error("获取侧边栏菜单失败: " + e.getMessage());
        }
    }
}
