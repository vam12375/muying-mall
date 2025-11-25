package com.muyingmall.controller.admin;

import com.muyingmall.annotation.AdminOperationLog;
import com.muyingmall.common.api.CommonResult;
import com.muyingmall.entity.User;
import com.muyingmall.service.AdminLoginRecordService;
import com.muyingmall.service.AdminOperationLogService;
import com.muyingmall.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 管理员个人中心控制器
 */
@Slf4j
@RestController
@RequestMapping("/admin/profile")
@io.swagger.v3.oas.annotations.tags.Tag(name = "管理员个人中心", description = "管理员统计信息、登录记录、操作记录")
public class AdminProfileController {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private AdminLoginRecordService loginRecordService;
    
    @Autowired
    private AdminOperationLogService operationLogService;
    
    /**
     * 获取管理员统计信息
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasAuthority('admin')")
    @AdminOperationLog(operation = "查看个人统计", module = "个人中心", operationType = "READ")
    public CommonResult<Map<String, Object>> getStatistics(@RequestHeader("Authorization") String authHeader) {
        try {
            User user = getUserFromToken(authHeader);
            if (user == null) {
                return CommonResult.unauthorized("身份令牌无效");
            }
            
            Integer adminId = user.getUserId();
            
            // 登录统计
            Map<String, Object> loginStats = loginRecordService.getLoginStatistics(adminId, 30);
            Map<String, Object> stats = new HashMap<>(loginStats);
            
            // 操作统计
            Map<String, Object> operationStats = operationLogService.getOperationStatistics(adminId, 30);
            stats.putAll(operationStats);
            
            // 24小时活跃度
            int[] activeHours = loginRecordService.getHourlyActiveStats(adminId, 7);
            stats.put("activeHours", activeHours);
            
            // 操作类型分布
            Map<String, Integer> operationTypes = operationLogService.getOperationTypeDistribution(adminId, 30);
            stats.put("operationTypes", operationTypes);
            
            // 计算账户年龄（天数）
            if (user.getCreateTime() != null) {
                long accountAgeDays = java.time.temporal.ChronoUnit.DAYS.between(
                    user.getCreateTime().toLocalDate(),
                    java.time.LocalDate.now()
                );
                stats.put("accountAge", accountAgeDays);
            } else {
                stats.put("accountAge", 0);
            }
            
            // 账号状态
            stats.put("accountStatus", "正常");
            stats.put("securityScore", 95);
            
            return CommonResult.success(stats);
        } catch (Exception e) {
            log.error("获取统计信息失败", e);
            return CommonResult.failed("获取统计信息失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取登录记录（分页）
     */
    @GetMapping("/login-records")
    @PreAuthorize("hasAuthority('admin')")
    @AdminOperationLog(operation = "查看登录记录", module = "个人中心", operationType = "READ")
    public CommonResult<Map<String, Object>> getLoginRecords(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestHeader("Authorization") String authHeader) {
        try {
            User user = getUserFromToken(authHeader);
            if (user == null) {
                return CommonResult.unauthorized("身份令牌无效");
            }
            
            Integer adminId = user.getUserId();
            int offset = (page - 1) * pageSize;
            
            var records = loginRecordService.getLoginRecordsPage(page, pageSize, adminId, null, null, null, null);
            
            Map<String, Object> result = new HashMap<>();
            result.put("records", records.getRecords());
            result.put("total", records.getTotal());
            result.put("page", page);
            result.put("pageSize", pageSize);
            
            return CommonResult.success(result);
        } catch (Exception e) {
            log.error("获取登录记录失败", e);
            return CommonResult.failed("获取登录记录失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取操作记录（分页）
     */
    @GetMapping("/operation-logs")
    @PreAuthorize("hasAuthority('admin')")
    @AdminOperationLog(operation = "查看操作记录", module = "个人中心", operationType = "READ")
    public CommonResult<Map<String, Object>> getOperationLogs(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestHeader("Authorization") String authHeader) {
        try {
            User user = getUserFromToken(authHeader);
            if (user == null) {
                return CommonResult.unauthorized("身份令牌无效");
            }
            
            Integer adminId = user.getUserId();
            
            var logs = operationLogService.getOperationLogsPage(page, pageSize, adminId, null, null, null, null, null);
            
            Map<String, Object> result = new HashMap<>();
            result.put("logs", logs.getRecords());
            result.put("total", logs.getTotal());
            result.put("page", page);
            result.put("pageSize", pageSize);
            
            return CommonResult.success(result);
        } catch (Exception e) {
            log.error("获取操作记录失败", e);
            return CommonResult.failed("获取操作记录失败: " + e.getMessage());
        }
    }
    
    /**
     * 从Token获取用户信息
     */
    private User getUserFromToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        return userService.getUserFromToken(authHeader);
    }
}
