package com.muyingmall.controller.admin;

import com.muyingmall.common.Result;
import com.muyingmall.entity.AdminLoginRecord;
import com.muyingmall.entity.User;
import com.muyingmall.service.AdminLoginRecordService;
import com.muyingmall.service.AdminOperationLogService;
import com.muyingmall.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 管理员个人中心控制器
 * 
 * Source: 为管理员个人中心提供API接口
 * 遵循 KISS, YAGNI, SOLID 原则
 */
@Slf4j
@RestController
@RequestMapping("/admin/profile")
@Tag(name = "管理员个人中心", description = "管理员个人信息、统计数据、登录记录、操作记录等接口")
@PreAuthorize("hasAuthority('admin')")
public class AdminProfileController {

    @Autowired
    private UserService userService;

    @Autowired
    private AdminLoginRecordService loginRecordService;

    @Autowired
    private AdminOperationLogService operationLogService;

    /**
     * 获取当前管理员的个人信息
     */
    @GetMapping("/info")
    @Operation(summary = "获取管理员个人信息")
    @com.muyingmall.annotation.AdminOperationLog(operation = "查看个人信息", module = "个人中心", operationType = "READ")
    public Result<Map<String, Object>> getProfileInfo() {
        try {
            // 获取当前登录用户
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            User user = userService.findByUsername(username);
            
            if (user == null) {
                return Result.error(404, "用户不存在");
            }
            
            // 获取最近登录记录
            List<AdminLoginRecord> recentLogins = loginRecordService.getRecentLogins(user.getUserId(), 1);
            
            // 获取登录统计（获取总登录次数）
            Map<String, Object> loginStats = loginRecordService.getLoginStatistics(user.getUserId(), 365);
            
            // 封装用户信息
            Map<String, Object> profileInfo = new HashMap<>();
            profileInfo.put("userId", user.getUserId());
            profileInfo.put("username", user.getUsername());
            profileInfo.put("nickname", user.getNickname());
            profileInfo.put("avatar", user.getAvatar());
            profileInfo.put("email", user.getEmail());
            profileInfo.put("phone", user.getPhone());
            profileInfo.put("role", user.getRole());
            profileInfo.put("status", user.getStatus());
            profileInfo.put("createTime", user.getCreateTime());
            
            // 添加登录次数统计
            profileInfo.put("loginCount", loginStats.get("totalLogins"));
            
            // 添加最后登录信息
            if (!recentLogins.isEmpty()) {
                AdminLoginRecord lastLogin = recentLogins.get(0);
                profileInfo.put("lastLoginTime", lastLogin.getLoginTime());
                profileInfo.put("lastLoginIp", lastLogin.getIpAddress());
                profileInfo.put("lastLoginLocation", lastLogin.getLocation());
            }
            
            return Result.success(profileInfo);
        } catch (Exception e) {
            log.error("获取个人信息失败", e);
            return Result.error("获取个人信息失败: " + e.getMessage());
        }
    }

    /**
     * 获取管理员统计数据
     */
    @GetMapping("/statistics")
    @Operation(summary = "获取管理员统计数据")
    @com.muyingmall.annotation.AdminOperationLog(operation = "查看统计数据", module = "个人中心", operationType = "READ")
    public Result<Map<String, Object>> getStatistics() {
        try {
            // 获取当前登录用户
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            User user = userService.findByUsername(username);
            
            if (user == null) {
                return Result.error(404, "用户不存在");
            }
            
            Map<String, Object> statistics = new HashMap<>();
            
            // 获取登录统计（最近30天）
            Map<String, Object> loginStats = loginRecordService.getLoginStatistics(user.getUserId(), 30);
            
            // 获取操作统计（最近30天）
            Map<String, Object> operationStats = operationLogService.getOperationStatistics(user.getUserId(), 30);
            
            // 合并统计数据
            statistics.put("loginCount", loginStats.get("totalLogins"));
            statistics.put("todayLogins", loginStats.get("todayLogins"));
            statistics.put("weekLogins", loginStats.get("weekLogins"));
            statistics.put("monthLogins", loginStats.get("monthLogins"));
            statistics.put("avgSessionDuration", loginStats.get("avgSessionDuration"));
            
            statistics.put("operationCount", operationStats.get("totalOperations"));
            statistics.put("todayOperations", operationStats.get("todayOperations"));
            statistics.put("weekOperations", operationStats.get("weekOperations"));
            statistics.put("monthOperations", operationStats.get("monthOperations"));
            
            // 获取操作类型分布
            Map<String, Integer> operationTypeDistribution = operationLogService
                    .getOperationTypeDistribution(user.getUserId(), 30);
            statistics.put("operationTypeDistribution", operationTypeDistribution);
            
            // 获取模块操作统计
            List<Map<String, Object>> moduleStats = operationLogService
                    .getModuleOperationStats(user.getUserId(), 30);
            statistics.put("moduleStats", moduleStats);
            
            return Result.success(statistics);
        } catch (Exception e) {
            log.error("获取统计数据失败", e);
            return Result.error("获取统计数据失败: " + e.getMessage());
        }
    }

    /**
     * 获取登录记录
     */
    @GetMapping("/login-records")
    @Operation(summary = "获取登录记录")
    @com.muyingmall.annotation.AdminOperationLog(operation = "查看登录记录", module = "个人中心", operationType = "READ")
    public Result<Map<String, Object>> getLoginRecords(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "开始时间") @RequestParam(required = false) String startTime,
            @Parameter(description = "结束时间") @RequestParam(required = false) String endTime,
            @Parameter(description = "登录状态") @RequestParam(required = false) String loginStatus) {
        
        try {
            // 获取当前登录用户
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            User user = userService.findByUsername(username);
            
            if (user == null) {
                return Result.error(404, "用户不存在");
            }
            
            // 解析时间参数
            LocalDateTime startDateTime = null;
            LocalDateTime endDateTime = null;
            
            if (startTime != null && !startTime.isEmpty()) {
                startDateTime = LocalDateTime.parse(startTime);
            }
            if (endTime != null && !endTime.isEmpty()) {
                endDateTime = LocalDateTime.parse(endTime);
            }
            
            // 查询登录记录
            com.baomidou.mybatisplus.core.metadata.IPage<AdminLoginRecord> loginRecordsPage = 
                    loginRecordService.getLoginRecordsPage(page, size, user.getUserId(), 
                            startDateTime, endDateTime, loginStatus, null);
            
            Map<String, Object> result = new HashMap<>();
            result.put("records", loginRecordsPage.getRecords());
            result.put("total", loginRecordsPage.getTotal());
            result.put("current", loginRecordsPage.getCurrent());
            result.put("size", loginRecordsPage.getSize());
            
            return Result.success(result);
        } catch (Exception e) {
            log.error("获取登录记录失败", e);
            return Result.error("获取登录记录失败: " + e.getMessage());
        }
    }

    /**
     * 获取操作记录
     */
    @GetMapping("/operation-records")
    @Operation(summary = "获取操作记录")
    @com.muyingmall.annotation.AdminOperationLog(operation = "查看操作记录", module = "个人中心", operationType = "READ")
    public Result<Map<String, Object>> getOperationRecords(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "开始时间") @RequestParam(required = false) String startTime,
            @Parameter(description = "结束时间") @RequestParam(required = false) String endTime,
            @Parameter(description = "操作类型") @RequestParam(required = false) String operationType,
            @Parameter(description = "操作模块") @RequestParam(required = false) String module) {
        
        try {
            // 获取当前登录用户
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            User user = userService.findByUsername(username);
            
            if (user == null) {
                return Result.error(404, "用户不存在");
            }
            
            // 解析时间参数
            LocalDateTime startDateTime = null;
            LocalDateTime endDateTime = null;
            
            if (startTime != null && !startTime.isEmpty()) {
                startDateTime = LocalDateTime.parse(startTime);
            }
            if (endTime != null && !endTime.isEmpty()) {
                endDateTime = LocalDateTime.parse(endTime);
            }
            
            // 查询操作记录
            com.baomidou.mybatisplus.core.metadata.IPage<com.muyingmall.entity.AdminOperationLog> operationLogsPage = 
                    operationLogService.getOperationLogsPage(page, size, user.getUserId(), 
                            startDateTime, endDateTime, operationType, module, null);
            
            Map<String, Object> result = new HashMap<>();
            result.put("records", operationLogsPage.getRecords());
            result.put("total", operationLogsPage.getTotal());
            result.put("current", operationLogsPage.getCurrent());
            result.put("size", operationLogsPage.getSize());
            
            return Result.success(result);
        } catch (Exception e) {
            log.error("获取操作记录失败", e);
            return Result.error("获取操作记录失败: " + e.getMessage());
        }
    }

    /**
     * 获取24小时活跃度统计
     */
    @GetMapping("/hourly-activity")
    @Operation(summary = "获取24小时活跃度统计")
    @com.muyingmall.annotation.AdminOperationLog(operation = "查看活跃度统计", module = "个人中心", operationType = "READ")
    public Result<int[]> getHourlyActivity(
            @Parameter(description = "统计天数") @RequestParam(defaultValue = "7") Integer days) {
        
        try {
            // 获取当前登录用户
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            User user = userService.findByUsername(username);
            
            if (user == null) {
                return Result.error(404, "用户不存在");
            }
            
            // 获取24小时活跃度统计
            int[] hourlyStats = loginRecordService.getHourlyActiveStats(user.getUserId(), days);
            
            return Result.success(hourlyStats);
        } catch (Exception e) {
            log.error("获取活跃度统计失败", e);
            return Result.error("获取活跃度统计失败: " + e.getMessage());
        }
    }
}
