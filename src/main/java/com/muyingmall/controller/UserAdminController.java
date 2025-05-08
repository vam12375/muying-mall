package com.muyingmall.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.muyingmall.common.response.Result;
import com.muyingmall.entity.User;
import com.muyingmall.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * 用户管理控制器（管理员权限）
 */
@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
@Tag(name = "用户管理", description = "用户管理相关接口，仅管理员可访问")
@Slf4j
public class UserAdminController {

    private final UserService userService;

    /**
     * 分页获取用户列表
     */
    @GetMapping("/page")
    @Operation(summary = "分页获取用户列表")
    @PreAuthorize("hasAuthority('admin')")
    public Result<Page<User>> getUserPage(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "搜索关键字（用户名、邮箱或昵称）") @RequestParam(required = false) String keyword,
            @Parameter(description = "状态筛选（0-禁用，1-正常）") @RequestParam(required = false) String status,
            @Parameter(description = "角色筛选（admin-管理员，user-普通用户）") @RequestParam(required = false) String role) {

        log.info(
                "[UserAdminController] Entering getUserPage - Params: page={}, size={}, keyword={}, status={}, role={}",
                page, size, keyword, status, role);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null) {
            log.warn("[UserAdminController] getUserPage - Authentication object is null.");
            // 注意：通常 @PreAuthorize 应该已经处理了认证失败的情况，这里可能不会执行到
            return Result.error(401, "未认证 (Authentication is null in controller)");
        } else {
            log.info("[UserAdminController] getUserPage - Authentication Principal: {}", authentication.getPrincipal());
            log.info("[UserAdminController] getUserPage - Authentication Authorities: {}",
                    authentication.getAuthorities());
            log.info("[UserAdminController] getUserPage - Is Authenticated: {}", authentication.isAuthenticated());
        }

        // 内部手动权限检查已暂时注释掉，优先依赖 @PreAuthorize
        /*
         * String username = authentication.getName();
         * User admin = userService.findByUsername(username);
         * if (admin == null || !"admin".equals(admin.getRole())) {
         * log.
         * warn("[UserAdminController] getUserPage - Manual permission check failed for user: {}, role: {}"
         * , username, (admin != null ? admin.getRole() : "null"));
         * return Result.error(403, "无权限 (Controller Manual Check)");
         * }
         * log.
         * info("[UserAdminController] getUserPage - Manual permission check passed for user: {}"
         * , username);
         */

        Page<User> userPage = userService.getUserPage(page, size, keyword, status, role);
        userPage.getRecords().forEach(user -> user.setPassword(null));
        log.info("[UserAdminController] getUserPage - Successfully fetched. Record count: {}",
                userPage.getRecords().size());
        return Result.success(userPage);
    }

    /**
     * 获取用户详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取用户详情")
    @PreAuthorize("hasAuthority('admin')")
    public Result<User> getUserById(@PathVariable Integer id) {
        log.info("[UserAdminController] Entering getUserById - ID: {}", id);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            log.info("[UserAdminController] getUserById - Auth Principal: {}, Authorities: {}",
                    authentication.getPrincipal(), authentication.getAuthorities());
        } else {
            log.warn("[UserAdminController] getUserById - Authentication object is null.");
        }

        User user = userService.getById(id);
        if (user == null) {
            log.warn("[UserAdminController] getUserById - User not found for id: {}", id);
            return Result.error(404, "用户不存在");
        }
        user.setPassword(null);
        log.info("[UserAdminController] getUserById - Successfully fetched user details for id: {}", id);
        return Result.success(user);
    }

    /**
     * 添加用户
     */
    @PostMapping
    @Operation(summary = "添加用户")
    @PreAuthorize("hasAuthority('admin')")
    public Result<User> addUser(@RequestBody User user) {
        log.info("[UserAdminController] Entering addUser - Username: {}", user.getUsername());
        try {
            User newUser = userService.addUser(user);
            newUser.setPassword(null);
            log.info("[UserAdminController] addUser - Successfully added user: {}", newUser.getUsername());
            return Result.success(newUser, "添加成功");
        } catch (Exception e) {
            log.error("[UserAdminController] addUser - Error adding user: {}", e.getMessage(), e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 更新用户信息
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新用户信息")
    @PreAuthorize("hasAuthority('admin')")
    public Result<Boolean> updateUser(@PathVariable Integer id, @RequestBody User user) {
        log.info("[UserAdminController] Entering updateUser - ID: {}, Username: {}", id, user.getUsername());
        user.setUserId(id);
        try {
            boolean success = userService.updateUserByAdmin(user);
            if (success) {
                log.info("[UserAdminController] updateUser - Successfully updated user for id: {}", id);
                return Result.success(true, "更新成功");
            } else {
                log.warn("[UserAdminController] updateUser - Failed to update user for id: {}", id);
                return Result.error("更新失败");
            }
        } catch (Exception e) {
            log.error("[UserAdminController] updateUser - Error updating user for id: {}: {}", id, e.getMessage(), e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除用户")
    @PreAuthorize("hasAuthority('admin')")
    public Result<Boolean> deleteUser(@PathVariable Integer id) {
        log.info("[UserAdminController] Entering deleteUser - ID: {}", id);
        try {
            boolean success = userService.deleteUser(id);
            if (success) {
                log.info("[UserAdminController] deleteUser - Successfully deleted user for id: {}", id);
                return Result.success(true, "删除成功");
            } else {
                log.warn("[UserAdminController] deleteUser - Failed to delete user for id: {}", id);
                return Result.error("删除失败");
            }
        } catch (Exception e) {
            log.error("[UserAdminController] deleteUser - Error deleting user for id: {}: {}", id, e.getMessage(), e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 修改用户状态
     */
    @PutMapping("/{id}/status")
    @Operation(summary = "修改用户状态")
    @PreAuthorize("hasAuthority('admin')")
    public Result<Boolean> toggleUserStatus(
            @PathVariable Integer id,
            @Parameter(description = "状态值：0-禁用，1-正常") @RequestParam Integer status) {
        log.info("[UserAdminController] Entering toggleUserStatus - ID: {}, Status: {}", id, status);
        try {
            boolean success = userService.toggleUserStatus(id, status);
            if (success) {
                log.info("[UserAdminController] toggleUserStatus - Successfully toggled status for id: {}", id);
                return Result.success(true, status == 1 ? "启用成功" : "禁用成功");
            } else {
                log.warn("[UserAdminController] toggleUserStatus - Failed to toggle status for id: {}", id);
                return Result.error("操作失败");
            }
        } catch (Exception e) {
            log.error("[UserAdminController] toggleUserStatus - Error toggling status for id: {}: {}", id,
                    e.getMessage(), e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 修改用户角色
     */
    @PutMapping("/{id}/role")
    @Operation(summary = "修改用户角色")
    @PreAuthorize("hasAuthority('admin')")
    public Result<Boolean> updateUserRole(
            @PathVariable Integer id,
            @Parameter(description = "角色值：admin-管理员，user-普通用户") @RequestParam String role) {
        log.info("[UserAdminController] Entering updateUserRole - ID: {}, Role: {}", id, role);
        try {
            boolean success = userService.updateUserRole(id, role);
            if (success) {
                log.info("[UserAdminController] updateUserRole - Successfully updated role for id: {}", id);
                return Result.success(true, "角色修改成功");
            } else {
                log.warn("[UserAdminController] updateUserRole - Failed to update role for id: {}", id);
                return Result.error("角色修改失败");
            }
        } catch (Exception e) {
            log.error("[UserAdminController] updateUserRole - Error updating role for id: {}: {}", id, e.getMessage(),
                    e);
            return Result.error(e.getMessage());
        }
    }
}