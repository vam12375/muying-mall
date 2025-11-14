package com.muyingmall.controller.admin;

import com.muyingmall.common.PageResult;
import com.muyingmall.common.Result;
import com.muyingmall.entity.User;
import com.muyingmall.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

/**
 * 后台管理系统 - 用户管理控制器
 * 用于管理员对所有用户进行增删改查操作
 */
@Slf4j
@RestController
@RequestMapping("/admin/users")
@Tag(name = "用户管理", description = "用户注册、登录、信息管理等接口")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    @Autowired
    private UserService userService;

    /**
     * 分页获取用户列表
     */
    @GetMapping("/page")
    @Operation(summary = "分页获取用户列表")
    public Result<PageResult<User>> getUserPage(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "搜索关键字(用户名/昵称/邮箱/手机)") @RequestParam(required = false) String keyword,
            @Parameter(description = "用户状态: 0-禁用, 1-正常") @RequestParam(required = false) Integer status,
            @Parameter(description = "用户角色: admin-管理员, user-普通用户") @RequestParam(required = false) String role) {

        try {
            PageResult<User> pageResult = userService.getUserPage(page, size, keyword, status, role);
            
            // 清除敏感信息
            pageResult.getRecords().forEach(user -> user.setPassword(null));
            
            return Result.success(pageResult);
        } catch (Exception e) {
            log.error("获取用户列表失败", e);
            return Result.error("获取用户列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取用户详情")
    public Result<User> getUserDetail(
            @Parameter(description = "用户ID", required = true) @PathVariable Integer id) {

        try {
            User user = userService.getUserById(id);
            if (user == null) {
                return Result.error("用户不存在");
            }
            
            // 清除敏感信息
            user.setPassword(null);
            
            return Result.success(user);
        } catch (Exception e) {
            log.error("获取用户详情失败: userId={}", id, e);
            return Result.error("获取用户详情失败: " + e.getMessage());
        }
    }

    /**
     * 添加用户
     */
    @PostMapping
    @Operation(summary = "添加用户")
    public Result<User> createUser(@Valid @RequestBody User user) {
        try {
            // 检查用户名是否已存在
            User existingUser = userService.getUserByUsername(user.getUsername());
            if (existingUser != null) {
                return Result.error("用户名已存在");
            }

            // 检查邮箱是否已存在
            if (user.getEmail() != null) {
                User existingEmail = userService.getUserByEmail(user.getEmail());
                if (existingEmail != null) {
                    return Result.error("邮箱已被使用");
                }
            }

            // 检查手机号是否已存在
            if (user.getPhone() != null) {
                User existingPhone = userService.getUserByPhone(user.getPhone());
                if (existingPhone != null) {
                    return Result.error("手机号已被使用");
                }
            }

            // 创建用户
            User createdUser = userService.createUser(user);
            
            // 清除敏感信息
            createdUser.setPassword(null);
            
            return Result.success(createdUser, "用户创建成功");
        } catch (Exception e) {
            log.error("创建用户失败", e);
            return Result.error("创建用户失败: " + e.getMessage());
        }
    }

    /**
     * 更新用户信息
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新用户信息")
    public Result<User> updateUser(
            @Parameter(description = "用户ID", required = true) @PathVariable Integer id,
            @Valid @RequestBody User user) {

        try {
            // 检查用户是否存在
            User existingUser = userService.getUserById(id);
            if (existingUser == null) {
                return Result.error("用户不存在");
            }

            // 设置用户ID
            user.setUserId(id);

            // 如果修改了邮箱，检查是否已被其他用户使用
            if (user.getEmail() != null && !user.getEmail().equals(existingUser.getEmail())) {
                User emailUser = userService.getUserByEmail(user.getEmail());
                if (emailUser != null && !emailUser.getUserId().equals(id)) {
                    return Result.error("邮箱已被其他用户使用");
                }
            }

            // 如果修改了手机号，检查是否已被其他用户使用
            if (user.getPhone() != null && !user.getPhone().equals(existingUser.getPhone())) {
                User phoneUser = userService.getUserByPhone(user.getPhone());
                if (phoneUser != null && !phoneUser.getUserId().equals(id)) {
                    return Result.error("手机号已被其他用户使用");
                }
            }

            // 更新用户信息
            boolean success = userService.updateUserInfo(user);
            if (success) {
                User updatedUser = userService.getUserById(id);
                updatedUser.setPassword(null);
                return Result.success(updatedUser, "用户信息更新成功");
            } else {
                return Result.error("用户信息更新失败");
            }
        } catch (Exception e) {
            log.error("更新用户信息失败: userId={}", id, e);
            return Result.error("更新用户信息失败: " + e.getMessage());
        }
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除用户")
    public Result<Void> deleteUser(
            @Parameter(description = "用户ID", required = true) @PathVariable Integer id) {

        try {
            // 检查用户是否存在
            User user = userService.getUserById(id);
            if (user == null) {
                return Result.error("用户不存在");
            }

            // 不允许删除管理员账户
            if ("admin".equals(user.getRole())) {
                return Result.error("不允许删除管理员账户");
            }

            // 删除用户
            boolean success = userService.deleteUser(id);
            if (success) {
                return Result.success(null, "用户删除成功");
            } else {
                return Result.error("用户删除失败");
            }
        } catch (Exception e) {
            log.error("删除用户失败: userId={}", id, e);
            return Result.error("删除用户失败: " + e.getMessage());
        }
    }

    /**
     * 修改用户角色
     */
    @PutMapping("/{id}/role")
    @Operation(summary = "修改用户角色")
    public Result<Void> updateUserRole(
            @Parameter(description = "用户ID", required = true) @PathVariable Integer id,
            @Parameter(description = "用户角色: admin-管理员, user-普通用户", required = true) @RequestParam String role) {

        try {
            // 检查用户是否存在
            User user = userService.getUserById(id);
            if (user == null) {
                return Result.error("用户不存在");
            }

            // 验证角色值
            if (!"admin".equals(role) && !"user".equals(role)) {
                return Result.error("无效的角色值");
            }

            // 更新角色
            boolean success = userService.updateUserRole(id, role);
            if (success) {
                return Result.success(null, "用户角色更新成功");
            } else {
                return Result.error("用户角色更新失败");
            }
        } catch (Exception e) {
            log.error("更新用户角色失败: userId={}, role={}", id, role, e);
            return Result.error("更新用户角色失败: " + e.getMessage());
        }
    }

    /**
     * 修改用户状态
     */
    @PutMapping("/{id}/status")
    @Operation(summary = "修改用户状态")
    public Result<Void> updateUserStatus(
            @Parameter(description = "用户ID", required = true) @PathVariable Integer id,
            @Parameter(description = "用户状态: 0-禁用, 1-正常", required = true) @RequestParam Integer status) {

        try {
            // 检查用户是否存在
            User user = userService.getUserById(id);
            if (user == null) {
                return Result.error("用户不存在");
            }

            // 不允许禁用管理员账户
            if ("admin".equals(user.getRole()) && status == 0) {
                return Result.error("不允许禁用管理员账户");
            }

            // 验证状态值
            if (status != 0 && status != 1) {
                return Result.error("无效的状态值");
            }

            // 更新状态
            boolean success = userService.updateUserStatus(id, status);
            if (success) {
                return Result.success(null, "用户状态更新成功");
            } else {
                return Result.error("用户状态更新失败");
            }
        } catch (Exception e) {
            log.error("更新用户状态失败: userId={}, status={}", id, status, e);
            return Result.error("更新用户状态失败: " + e.getMessage());
        }
    }

    /**
     * 重置用户密码
     */
    @PutMapping("/{id}/password/reset")
    @Operation(summary = "重置用户密码")
    public Result<Void> resetUserPassword(
            @Parameter(description = "用户ID", required = true) @PathVariable Integer id,
            @Parameter(description = "新密码", required = true) @RequestParam String newPassword) {

        try {
            // 检查用户是否存在
            User user = userService.getUserById(id);
            if (user == null) {
                return Result.error("用户不存在");
            }

            // 验证密码长度
            if (newPassword == null || newPassword.length() < 6 || newPassword.length() > 20) {
                return Result.error("密码长度必须在6-20位之间");
            }

            // 重置密码
            boolean success = userService.resetPassword(id, newPassword);
            if (success) {
                return Result.success(null, "密码重置成功");
            } else {
                return Result.error("密码重置失败");
            }
        } catch (Exception e) {
            log.error("重置用户密码失败: userId={}", id, e);
            return Result.error("重置用户密码失败: " + e.getMessage());
        }
    }
}
