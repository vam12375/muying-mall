package com.muyingmall.controller;

import com.muyingmall.annotation.AdminOperationLog;
import com.muyingmall.common.api.CommonResult;
import com.muyingmall.dto.AdminLoginDTO;
import com.muyingmall.entity.AdminLoginRecord;
import com.muyingmall.entity.User;
import com.muyingmall.service.AdminLoginRecordService;
import com.muyingmall.service.AdminOperationLogService;
import com.muyingmall.service.ExcelExportService;
import com.muyingmall.service.UserService;
import com.muyingmall.websocket.AdminStatsWebSocket;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 管理员控制器
 */
@Slf4j
@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UserService userService;

    @Autowired
    private AdminLoginRecordService loginRecordService;

    @Autowired
    private AdminOperationLogService operationLogService;

    @Autowired
    private ExcelExportService excelExportService;

    @Value("${file.upload.path}")
    private String uploadPath;

    @Value("${file.access.url}")
    private String accessUrl;

    /**
     * 管理员登录
     */
    @PostMapping("/login")
    public CommonResult login(@RequestBody AdminLoginDTO loginParam) {
        // 根据用户名查询用户
        User user = userService.getUserByUsername(loginParam.getAdmin_name());

        // 用户不存在或密码错误
        if (user == null || !userService.verifyPassword(user, loginParam.getAdmin_pass())) {
            return CommonResult.failed("用户名或密码错误");
        }

        // 检查用户角色是否为管理员
        if (!"admin".equals(user.getRole())) {
            return CommonResult.failed("该账号没有管理员权限");
        }

        // 用户存在且验证通过，生成token
        String token = userService.generateToken(user);

        // 封装返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);

        // 用户信息（去除敏感信息）
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getUserId());
        userInfo.put("username", user.getUsername());
        userInfo.put("nickname", user.getNickname());
        userInfo.put("avatar", user.getAvatar());
        userInfo.put("role", user.getRole());

        result.put("user", userInfo);

        return CommonResult.success(result);
    }

    /**
     * 获取当前管理员信息
     */
    @GetMapping("/info")
    @PreAuthorize("hasAuthority('admin')")
    public CommonResult<Map<String, Object>> getUserInfo(@RequestHeader("Authorization") String authHeader) {
        // 从token中解析用户信息
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return CommonResult.unauthorized("未提供合法的身份令牌");
        }

        User user = userService.getUserFromToken(authHeader);
        if (user == null) {
            return CommonResult.unauthorized("身份令牌无效或已过期");
        }

        // 检查用户角色是否为管理员
        if (!"admin".equals(user.getRole())) {
            return CommonResult.forbidden("该账号没有管理员权限");
        }

        // 封装完整的用户信息
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getUserId());
        userInfo.put("username", user.getUsername());
        userInfo.put("nickname", user.getNickname());
        userInfo.put("avatar", user.getAvatar());
        userInfo.put("email", user.getEmail()); // 添加邮箱
        userInfo.put("phone", user.getPhone()); // 添加手机号
        userInfo.put("role", user.getRole());
        userInfo.put("status", user.getStatus()); // 添加状态
        userInfo.put("createTime", user.getCreateTime()); // 添加创建时间
        userInfo.put("lastLogin", null); // 添加最后登录时间（暂无数据）
        userInfo.put("loginCount", 0); // 添加登录次数（暂无数据）

        return CommonResult.success(userInfo);
    }

    /**
     * 文件上传接口
     * 
     * @param file 上传的文件
     * @return 上传结果，包含文件URL
     */
    @PostMapping("/upload")
    public CommonResult<Map<String, String>> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            // 验证文件
            if (file == null || file.isEmpty()) {
                return CommonResult.failed("文件不能为空");
            }

            // 获取文件后缀
            String originalFilename = file.getOriginalFilename();
            String suffix = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
            }

            // 限制文件类型
            if (!".jpg".equalsIgnoreCase(suffix) && !".jpeg".equalsIgnoreCase(suffix) &&
                    !".png".equalsIgnoreCase(suffix) && !".gif".equalsIgnoreCase(suffix)) {
                return CommonResult.failed("只支持jpg、jpeg、png、gif格式的图片");
            }

            // 生成存储路径
            String relativeDir = "/images/" + UUID.randomUUID().toString().substring(0, 8);
            String storageDir = uploadPath + relativeDir;

            // 确保目录存在
            Path uploadDir = Paths.get(storageDir);
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            // 生成文件名
            String filename = UUID.randomUUID().toString().replace("-", "") + suffix;
            Path filePath = uploadDir.resolve(filename);

            // 保存文件
            Files.copy(file.getInputStream(), filePath);

            // 生成访问URL
            String fileUrl = accessUrl + relativeDir + "/" + filename;

            // 返回结果
            Map<String, String> result = new HashMap<>();
            result.put("url", fileUrl);
            result.put("name", originalFilename);

            return CommonResult.success(result);
        } catch (IOException e) {
            return CommonResult.failed("文件上传失败: " + e.getMessage());
        }
    }

    /**
     * 管理员头像上传接口
     */
    @PostMapping("/avatar/upload")
    @PreAuthorize("hasAuthority('admin')")
    public CommonResult<Map<String, String>> uploadAvatar(
            @RequestParam("file") MultipartFile file,
            @RequestHeader("Authorization") String authHeader) {

        // 从token中解析用户信息
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return CommonResult.unauthorized("未提供合法的身份令牌");
        }

        User user = userService.getUserFromToken(authHeader);
        if (user == null) {
            return CommonResult.unauthorized("身份令牌无效或已过期");
        }

        // 检查用户角色是否为管理员
        if (!"admin".equals(user.getRole())) {
            return CommonResult.forbidden("该账号没有管理员权限");
        }

        try {
            // 调用上传头像服务
            String avatarUrl = userService.uploadAvatar(user.getUserId(), file);

            // 返回结果
            Map<String, String> result = new HashMap<>();
            result.put("url", avatarUrl);
            result.put("name", file.getOriginalFilename());

            return CommonResult.success(result);
        } catch (Exception e) {
            return CommonResult.failed("头像上传失败: " + e.getMessage());
        }
    }

    /**
     * 更新管理员信息
     */
    @PutMapping("/update")
    @PreAuthorize("hasAuthority('admin')")
    public CommonResult<Map<String, Object>> updateAdminInfo(
            @RequestBody User adminInfo,
            @RequestHeader("Authorization") String authHeader) {

        // 从token中解析用户信息
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return CommonResult.unauthorized("未提供合法的身份令牌");
        }

        User user = userService.getUserFromToken(authHeader);
        if (user == null) {
            return CommonResult.unauthorized("身份令牌无效或已过期");
        }

        // 检查用户角色是否为管理员
        if (!"admin".equals(user.getRole())) {
            return CommonResult.forbidden("该账号没有管理员权限");
        }

        // 设置ID为当前登录用户ID（防止篡改其他用户信息）
        adminInfo.setUserId(user.getUserId());

        // 保留重要字段不被修改
        adminInfo.setRole(user.getRole()); // 保持角色不变
        adminInfo.setUsername(user.getUsername()); // 用户名不可修改
        adminInfo.setStatus(user.getStatus()); // 状态不可通过此接口修改

        try {
            // 更新用户信息
            boolean result = userService.updateUserInfo(adminInfo);
            if (result) {
                // 获取更新后的用户信息
                User updatedUser = userService.getUserById(user.getUserId());

                // 封装响应信息
                Map<String, Object> updatedInfo = new HashMap<>();
                updatedInfo.put("id", updatedUser.getUserId());
                updatedInfo.put("username", updatedUser.getUsername());
                updatedInfo.put("nickname", updatedUser.getNickname());
                updatedInfo.put("avatar", updatedUser.getAvatar());
                updatedInfo.put("email", updatedUser.getEmail());
                updatedInfo.put("phone", updatedUser.getPhone());
                updatedInfo.put("role", updatedUser.getRole());
                updatedInfo.put("status", updatedUser.getStatus());
                updatedInfo.put("createTime", updatedUser.getCreateTime());
                updatedInfo.put("lastLogin", null); // 当前无此数据
                updatedInfo.put("loginCount", 0); // 当前无此数据

                return CommonResult.success(updatedInfo, "更新成功");
            } else {
                return CommonResult.failed("更新失败");
            }
        } catch (Exception e) {
            return CommonResult.failed("更新失败: " + e.getMessage());
        }
    }

    /**
     * 管理员修改密码
     */
    @PutMapping("/password")
    @PreAuthorize("hasAuthority('admin')")
    public CommonResult updatePassword(
            @RequestBody Map<String, String> passwordMap,
            @RequestHeader("Authorization") String authHeader) {

        String oldPassword = passwordMap.get("oldPassword");
        String newPassword = passwordMap.get("newPassword");

        if (oldPassword == null || newPassword == null) {
            return CommonResult.validateFailed("旧密码和新密码不能为空");
        }

        // 从token中解析用户信息
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return CommonResult.unauthorized("未提供合法的身份令牌");
        }

        User user = userService.getUserFromToken(authHeader);
        if (user == null) {
            return CommonResult.unauthorized("身份令牌无效或已过期");
        }

        // 检查用户角色是否为管理员
        if (!"admin".equals(user.getRole())) {
            return CommonResult.forbidden("该账号没有管理员权限");
        }

        try {
            // 更新密码
            boolean result = userService.changePassword(user.getUserId(), oldPassword, newPassword);
            if (result) {
                return CommonResult.success(null, "密码修改成功");
            } else {
                return CommonResult.failed("密码修改失败");
            }
        } catch (Exception e) {
            return CommonResult.failed("密码修改失败: " + e.getMessage());
        }
    }

    /**
     * 获取管理员登录记录
     */
    @GetMapping("/login-records")
    @PreAuthorize("hasAuthority('admin')")
    @AdminOperationLog(operation = "查看登录记录", module = "管理员管理", operationType = "READ")
    public CommonResult<Map<String, Object>> getLoginRecords(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime,
            @RequestParam(required = false) String loginStatus,
            @RequestParam(required = false) String ipAddress) {

        try {
            // 解析时间参数
            java.time.LocalDateTime startDateTime = null;
            java.time.LocalDateTime endDateTime = null;

            if (startTime != null && !startTime.isEmpty()) {
                startDateTime = java.time.LocalDateTime.parse(startTime + "T00:00:00");
            }
            if (endTime != null && !endTime.isEmpty()) {
                endDateTime = java.time.LocalDateTime.parse(endTime + "T23:59:59");
            }

            // 查询登录记录
            com.baomidou.mybatisplus.core.metadata.IPage<AdminLoginRecord> loginRecordsPage = loginRecordService
                    .getLoginRecordsPage(page, size, null, startDateTime, endDateTime,
                            loginStatus, ipAddress);

            Map<String, Object> result = new HashMap<>();
            result.put("records", loginRecordsPage.getRecords());
            result.put("total", loginRecordsPage.getTotal());
            result.put("current", loginRecordsPage.getCurrent());
            result.put("size", loginRecordsPage.getSize());

            return CommonResult.success(result);
        } catch (Exception e) {
            return CommonResult.failed("获取登录记录失败: " + e.getMessage());
        }
    }

    /**
     * 获取系统日志 (兼容前端路由)
     */
    @GetMapping("/system/logs")
    @PreAuthorize("hasAuthority('admin')")
    @AdminOperationLog(operation = "查看系统日志", module = "系统管理", operationType = "READ")
    public CommonResult<Map<String, Object>> getSystemLogs(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime,
            @RequestParam(required = false) String operationType,
            @RequestParam(required = false) String module,
            @RequestParam(required = false) String operationResult) {

        try {
            log.info(
                    "获取系统日志请求 - page: {}, size: {}, startTime: {}, endTime: {}, operationType: {}, module: {}, operationResult: {}",
                    page, size, startTime, endTime, operationType, module, operationResult);

            // 解析时间参数
            java.time.LocalDateTime startDateTime = null;
            java.time.LocalDateTime endDateTime = null;

            if (startTime != null && !startTime.isEmpty()) {
                startDateTime = java.time.LocalDateTime.parse(startTime + "T00:00:00");
                log.info("解析开始时间: {}", startDateTime);
            }
            if (endTime != null && !endTime.isEmpty()) {
                endDateTime = java.time.LocalDateTime.parse(endTime + "T23:59:59");
                log.info("解析结束时间: {}", endDateTime);
            }

            // 查询操作记录
            com.baomidou.mybatisplus.core.metadata.IPage<com.muyingmall.entity.AdminOperationLog> operationLogsPage = operationLogService
                    .getOperationLogsPage(page, size, null, startDateTime, endDateTime,
                            operationType, module, operationResult);

            log.info("查询结果 - total: {}, records size: {}", operationLogsPage.getTotal(),
                    operationLogsPage.getRecords().size());

            Map<String, Object> result = new HashMap<>();
            result.put("records", operationLogsPage.getRecords());
            result.put("total", operationLogsPage.getTotal());
            result.put("current", operationLogsPage.getCurrent());
            result.put("size", operationLogsPage.getSize());

            return CommonResult.success(result);
        } catch (Exception e) {
            log.error("获取系统日志失败", e);
            return CommonResult.failed("获取系统日志失败: " + e.getMessage());
        }
    }

    /**
     * 获取系统日志详情
     */
    @GetMapping("/system/logs/{id}")
    @PreAuthorize("hasAuthority('admin')")
    @com.muyingmall.annotation.AdminOperationLog(operation = "查看系统日志详情", module = "系统管理", operationType = "READ")
    public CommonResult<com.muyingmall.entity.AdminOperationLog> getSystemLogDetail(@PathVariable Long id) {
        try {
            com.muyingmall.entity.AdminOperationLog logDetail = operationLogService.getById(id);
            if (logDetail == null) {
                return CommonResult.failed("日志不存在");
            }
            return CommonResult.success(logDetail);
        } catch (Exception e) {
            log.error("获取系统日志详情失败", e);
            return CommonResult.failed("获取系统日志详情失败: " + e.getMessage());
        }
    }

    /**
     * 获取管理员操作记录
     */
    @GetMapping("/operation-records")
    @PreAuthorize("hasAuthority('admin')")
    @AdminOperationLog(operation = "查看操作记录", module = "管理员管理", operationType = "READ")
    public CommonResult<Map<String, Object>> getOperationRecords(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime,
            @RequestParam(required = false) String operationType,
            @RequestParam(required = false) String module,
            @RequestParam(required = false) String operationResult) {

        try {
            // 解析时间参数
            java.time.LocalDateTime startDateTime = null;
            java.time.LocalDateTime endDateTime = null;

            if (startTime != null && !startTime.isEmpty()) {
                startDateTime = java.time.LocalDateTime.parse(startTime + "T00:00:00");
            }
            if (endTime != null && !endTime.isEmpty()) {
                endDateTime = java.time.LocalDateTime.parse(endTime + "T23:59:59");
            }

            // 查询操作记录
            com.baomidou.mybatisplus.core.metadata.IPage<com.muyingmall.entity.AdminOperationLog> operationLogsPage = operationLogService
                    .getOperationLogsPage(page, size, null, startDateTime, endDateTime,
                            operationType, module, operationResult);

            Map<String, Object> result = new HashMap<>();
            result.put("records", operationLogsPage.getRecords());
            result.put("total", operationLogsPage.getTotal());
            result.put("current", operationLogsPage.getCurrent());
            result.put("size", operationLogsPage.getSize());

            return CommonResult.success(result);
        } catch (Exception e) {
            return CommonResult.failed("获取操作记录失败: " + e.getMessage());
        }
    }

    /**
     * 获取管理员统计信息
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasAuthority('admin')")
    @AdminOperationLog(operation = "查看统计信息", module = "管理员管理", operationType = "READ")
    public CommonResult<Map<String, Object>> getAdminStatistics() {

        try {
            Map<String, Object> statistics = new HashMap<>();

            // 获取登录统计
            Map<String, Object> loginStats = loginRecordService.getLoginStatistics(null, 30);
            statistics.putAll(loginStats);

            // 获取操作统计
            Map<String, Object> operationStats = operationLogService.getOperationStatistics(null, 30);
            statistics.putAll(operationStats);

            // 获取24小时活跃度
            int[] activeHours = loginRecordService.getHourlyActiveStats(null, 7);
            statistics.put("activeHours", activeHours);

            // 获取操作类型分布
            Map<String, Integer> operationTypes = operationLogService.getOperationTypeDistribution(null, 30);
            statistics.put("operationTypes", operationTypes);

            // 添加一些固定统计信息
            statistics.put("accountAge", 365);
            statistics.put("securityScore", 95);

            return CommonResult.success(statistics);
        } catch (Exception e) {
            return CommonResult.failed("获取统计信息失败: " + e.getMessage());
        }
    }

    /**
     * 导出登录记录
     */
    @GetMapping("/login-records/export")
    @PreAuthorize("hasAuthority('admin')")
    @AdminOperationLog(operation = "导出登录记录", module = "管理员管理", operationType = "EXPORT")
    public void exportLoginRecords(
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime,
            @RequestParam(required = false) String loginStatus,
            @RequestParam(required = false) String ipAddress,
            HttpServletResponse response) {

        try {
            // 解析时间参数
            java.time.LocalDateTime startDateTime = null;
            java.time.LocalDateTime endDateTime = null;

            if (startTime != null && !startTime.isEmpty()) {
                startDateTime = java.time.LocalDateTime.parse(startTime + "T00:00:00");
            }
            if (endTime != null && !endTime.isEmpty()) {
                endDateTime = java.time.LocalDateTime.parse(endTime + "T23:59:59");
            }

            excelExportService.exportLoginRecords(response, null, startDateTime, endDateTime,
                    loginStatus, ipAddress);
        } catch (Exception e) {
            log.error("导出登录记录失败: {}", e.getMessage(), e);
            response.setStatus(500);
        }
    }

    /**
     * 导出操作记录
     */
    @GetMapping("/operation-records/export")
    @PreAuthorize("hasAuthority('admin')")
    @AdminOperationLog(operation = "导出操作记录", module = "管理员管理", operationType = "EXPORT")
    public void exportOperationRecords(
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime,
            @RequestParam(required = false) String operationType,
            @RequestParam(required = false) String module,
            @RequestParam(required = false) String operationResult,
            HttpServletResponse response) {

        try {
            // 解析时间参数
            java.time.LocalDateTime startDateTime = null;
            java.time.LocalDateTime endDateTime = null;

            if (startTime != null && !startTime.isEmpty()) {
                startDateTime = java.time.LocalDateTime.parse(startTime + "T00:00:00");
            }
            if (endTime != null && !endTime.isEmpty()) {
                endDateTime = java.time.LocalDateTime.parse(endTime + "T23:59:59");
            }

            excelExportService.exportOperationLogs(response, null, startDateTime, endDateTime,
                    operationType, module, operationResult);
        } catch (Exception e) {
            log.error("导出操作记录失败: {}", e.getMessage(), e);
            response.setStatus(500);
        }
    }

    /**
     * 获取WebSocket连接状态
     */
    @GetMapping("/websocket/status")
    @PreAuthorize("hasAuthority('admin')")
    @AdminOperationLog(operation = "查看WebSocket状态", module = "管理员管理", operationType = "READ")
    public CommonResult<Map<String, Object>> getWebSocketStatus() {
        try {
            Map<String, Object> status = new HashMap<>();
            status.put("onlineCount", AdminStatsWebSocket.getOnlineCount());
            status.put("onlineAdmins", AdminStatsWebSocket.getWebSocketMap().keySet());
            status.put("timestamp", System.currentTimeMillis());

            return CommonResult.success(status);
        } catch (Exception e) {
            return CommonResult.failed("获取WebSocket状态失败: " + e.getMessage());
        }
    }

    /**
     * 发送系统通知
     */
    @PostMapping("/notification/send")
    @PreAuthorize("hasAuthority('admin')")
    @AdminOperationLog(operation = "发送系统通知", module = "管理员管理", operationType = "CREATE")
    public CommonResult<String> sendSystemNotification(
            @RequestBody Map<String, Object> notificationData) {
        try {
            String message = (String) notificationData.get("message");
            Integer targetAdminId = (Integer) notificationData.get("targetAdminId");

            if (message == null || message.trim().isEmpty()) {
                return CommonResult.failed("通知消息不能为空");
            }

            // 直接通过WebSocket发送通知
            if (targetAdminId != null) {
                AdminStatsWebSocket.sendInfo(message, targetAdminId.toString());
            } else {
                AdminStatsWebSocket.broadcast(message);
            }

            return CommonResult.success("系统通知发送成功");
        } catch (Exception e) {
            return CommonResult.failed("发送系统通知失败: " + e.getMessage());
        }
    }

    /**
     * 手动触发统计数据推送
     */
    @PostMapping("/stats/push")
    @PreAuthorize("hasAuthority('admin')")
    @AdminOperationLog(operation = "推送统计数据", module = "管理员管理", operationType = "UPDATE")
    public CommonResult<String> pushStatsUpdate() {
        try {
            // 获取最新统计数据并推送
            Map<String, Object> stats = new HashMap<>();

            // 获取登录统计
            Map<String, Object> loginStats = loginRecordService.getLoginStatistics(null, 30);
            stats.putAll(loginStats);

            // 获取操作统计
            Map<String, Object> operationStats = operationLogService.getOperationStatistics(null, 30);
            stats.putAll(operationStats);

            // 获取24小时活跃度
            int[] activeHours = loginRecordService.getHourlyActiveStats(null, 7);
            stats.put("activeHours", activeHours);

            // 获取操作类型分布
            Map<String, Integer> operationTypes = operationLogService.getOperationTypeDistribution(null, 30);
            stats.put("operationTypes", operationTypes);

            // 添加当前在线管理员数量
            stats.put("onlineAdmins", AdminStatsWebSocket.getOnlineCount());
            stats.put("updateTime", System.currentTimeMillis());

            // 广播统计数据
            AdminStatsWebSocket.broadcastStats(stats);

            return CommonResult.success("统计数据推送成功");
        } catch (Exception e) {
            return CommonResult.failed("推送统计数据失败: " + e.getMessage());
        }
    }
}