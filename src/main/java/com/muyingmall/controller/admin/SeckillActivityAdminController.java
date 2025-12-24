package com.muyingmall.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.muyingmall.annotation.AdminOperationLog;
import com.muyingmall.common.api.Result;
import com.muyingmall.entity.SeckillActivity;
import com.muyingmall.service.SeckillActivityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 秒杀活动管理控制器（管理员权限）
 * 
 * @author MuyingMall
 */
@Slf4j
@RestController
@RequestMapping("/admin/seckill/activities")
@RequiredArgsConstructor
@Tag(name = "秒杀活动管理", description = "秒杀活动管理相关接口，仅管理员可访问")
public class SeckillActivityAdminController {

    private final SeckillActivityService seckillActivityService;

    /**
     * 分页获取秒杀活动列表
     */
    @GetMapping("/page")
    @Operation(summary = "分页获取秒杀活动列表")
    @PreAuthorize("hasAuthority('admin')")
    @AdminOperationLog(operation = "查询秒杀活动列表", module = "秒杀管理", operationType = "READ")
    public Result<IPage<SeckillActivity>> getActivityPage(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "活动名称关键字") @RequestParam(required = false) String keyword,
            @Parameter(description = "活动状态：0-未开始，1-进行中，2-已结束") @RequestParam(required = false) Integer status) {
        
        log.debug("查询秒杀活动列表 - page: {}, size: {}, keyword: {}, status: {}", page, size, keyword, status);
        
        try {
            Page<SeckillActivity> pageParam = new Page<>(page, size);
            IPage<SeckillActivity> activityPage = seckillActivityService.getActivityPage(pageParam, keyword, status);
            
            log.debug("查询成功，共 {} 条记录", activityPage.getTotal());
            return Result.success(activityPage);
        } catch (Exception e) {
            log.error("查询秒杀活动列表失败", e);
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 获取秒杀活动详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取秒杀活动详情")
    @PreAuthorize("hasAuthority('admin')")
    @AdminOperationLog(operation = "查看秒杀活动详情", module = "秒杀管理", operationType = "READ")
    public Result<SeckillActivity> getActivityById(@PathVariable Long id) {
        log.debug("查询秒杀活动详情 - id: {}", id);
        
        try {
            SeckillActivity activity = seckillActivityService.getById(id);
            if (activity == null) {
                return Result.error(404, "活动不存在");
            }
            return Result.success(activity);
        } catch (Exception e) {
            log.error("查询秒杀活动详情失败 - id: {}", id, e);
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 创建秒杀活动
     */
    @PostMapping
    @Operation(summary = "创建秒杀活动")
    @PreAuthorize("hasAuthority('admin')")
    @AdminOperationLog(operation = "创建秒杀活动", module = "秒杀管理", operationType = "CREATE")
    public Result<SeckillActivity> createActivity(@RequestBody SeckillActivity activity) {
        log.debug("创建秒杀活动 - name: {}", activity.getName());
        
        try {
            // 参数校验
            if (activity.getName() == null || activity.getName().trim().isEmpty()) {
                return Result.error("活动名称不能为空");
            }
            if (activity.getStartTime() == null || activity.getEndTime() == null) {
                return Result.error("活动开始时间和结束时间不能为空");
            }
            if (activity.getStartTime().isAfter(activity.getEndTime())) {
                return Result.error("开始时间不能晚于结束时间");
            }
            
            // 根据时间自动设置状态（过期检测）
            LocalDateTime now = LocalDateTime.now();
            if (now.isBefore(activity.getStartTime())) {
                activity.setStatus(0); // 未开始
            } else if (now.isAfter(activity.getEndTime())) {
                activity.setStatus(2); // 已结束
                log.info("创建的活动时间已过期，自动设置为已结束状态 - endTime: {}", activity.getEndTime());
            } else {
                activity.setStatus(1); // 进行中
            }
            
            boolean success = seckillActivityService.save(activity);
            if (success) {
                log.info("创建秒杀活动成功 - id: {}, name: {}, status: {}", activity.getId(), activity.getName(), activity.getStatus());
                return Result.success(activity, "创建成功");
            } else {
                return Result.error("创建失败");
            }
        } catch (Exception e) {
            log.error("创建秒杀活动失败", e);
            return Result.error("创建失败：" + e.getMessage());
        }
    }

    /**
     * 更新秒杀活动
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新秒杀活动")
    @PreAuthorize("hasAuthority('admin')")
    @AdminOperationLog(operation = "更新秒杀活动", module = "秒杀管理", operationType = "UPDATE")
    public Result<Boolean> updateActivity(@PathVariable Long id, @RequestBody SeckillActivity activity) {
        log.debug("更新秒杀活动 - id: {}", id);
        
        try {
            // 检查活动是否存在
            SeckillActivity existingActivity = seckillActivityService.getById(id);
            if (existingActivity == null) {
                return Result.error(404, "活动不存在");
            }
            
            // 参数校验
            if (activity.getStartTime() != null && activity.getEndTime() != null) {
                if (activity.getStartTime().isAfter(activity.getEndTime())) {
                    return Result.error("开始时间不能晚于结束时间");
                }
            }
            
            // 过期检测：如果更新了时间，自动检测并更新状态
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime startTime = activity.getStartTime() != null ? activity.getStartTime() : existingActivity.getStartTime();
            LocalDateTime endTime = activity.getEndTime() != null ? activity.getEndTime() : existingActivity.getEndTime();
            
            if (endTime != null && now.isAfter(endTime)) {
                activity.setStatus(2); // 已结束
                log.info("更新的活动时间已过期，自动设置为已结束状态 - id: {}, endTime: {}", id, endTime);
            } else if (startTime != null && endTime != null) {
                if (now.isBefore(startTime)) {
                    activity.setStatus(0); // 未开始
                } else if (now.isAfter(startTime) && now.isBefore(endTime)) {
                    activity.setStatus(1); // 进行中
                }
            }
            
            activity.setId(id);
            boolean success = seckillActivityService.updateById(activity);
            
            if (success) {
                log.info("更新秒杀活动成功 - id: {}, status: {}", id, activity.getStatus());
                return Result.success(true, "更新成功");
            } else {
                return Result.error("更新失败");
            }
        } catch (Exception e) {
            log.error("更新秒杀活动失败 - id: {}", id, e);
            return Result.error("更新失败：" + e.getMessage());
        }
    }

    /**
     * 删除秒杀活动
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除秒杀活动")
    @PreAuthorize("hasAuthority('admin')")
    @AdminOperationLog(operation = "删除秒杀活动", module = "秒杀管理", operationType = "DELETE")
    public Result<Boolean> deleteActivity(@PathVariable Long id) {
        log.debug("删除秒杀活动 - id: {}", id);
        
        try {
            // 检查活动是否存在
            SeckillActivity activity = seckillActivityService.getById(id);
            if (activity == null) {
                return Result.error(404, "活动不存在");
            }
            
            // 检查活动状态，进行中的活动不允许删除
            if (activity.getStatus() == 1) {
                return Result.error("进行中的活动不允许删除");
            }
            
            boolean success = seckillActivityService.removeById(id);
            if (success) {
                log.info("删除秒杀活动成功 - id: {}", id);
                return Result.success(true, "删除成功");
            } else {
                return Result.error("删除失败");
            }
        } catch (Exception e) {
            log.error("删除秒杀活动失败 - id: {}", id, e);
            return Result.error("删除失败：" + e.getMessage());
        }
    }

    /**
     * 启用/禁用秒杀活动
     */
    @PutMapping("/{id}/status")
    @Operation(summary = "启用/禁用秒杀活动")
    @PreAuthorize("hasAuthority('admin')")
    @AdminOperationLog(operation = "修改秒杀活动状态", module = "秒杀管理", operationType = "UPDATE")
    public Result<Boolean> toggleActivityStatus(
            @PathVariable Long id,
            @Parameter(description = "状态：0-未开始，1-进行中，2-已结束") @RequestParam Integer status) {
        
        log.debug("修改秒杀活动状态 - id: {}, status: {}", id, status);
        
        try {
            // 检查活动是否存在
            SeckillActivity activity = seckillActivityService.getById(id);
            if (activity == null) {
                return Result.error(404, "活动不存在");
            }
            
            // 状态校验
            if (status < 0 || status > 2) {
                return Result.error("无效的状态值");
            }
            
            activity.setStatus(status);
            boolean success = seckillActivityService.updateById(activity);
            
            if (success) {
                log.info("修改秒杀活动状态成功 - id: {}, status: {}", id, status);
                return Result.success(true, "状态修改成功");
            } else {
                return Result.error("状态修改失败");
            }
        } catch (Exception e) {
            log.error("修改秒杀活动状态失败 - id: {}", id, e);
            return Result.error("状态修改失败：" + e.getMessage());
        }
    }

    /**
     * 获取秒杀活动统计信息
     */
    @GetMapping("/statistics")
    @Operation(summary = "获取秒杀活动统计信息")
    @PreAuthorize("hasAuthority('admin')")
    @AdminOperationLog(operation = "查看秒杀活动统计", module = "秒杀管理", operationType = "READ")
    public Result<Map<String, Object>> getActivityStatistics() {
        log.debug("查询秒杀活动统计信息");
        
        try {
            Map<String, Object> statistics = new HashMap<>();
            
            // 总活动数
            long totalCount = seckillActivityService.count();
            statistics.put("totalCount", totalCount);
            
            // 进行中的活动数
            long ongoingCount = seckillActivityService.countByStatus(1);
            statistics.put("ongoingCount", ongoingCount);
            
            // 未开始的活动数
            long upcomingCount = seckillActivityService.countByStatus(0);
            statistics.put("upcomingCount", upcomingCount);
            
            // 已结束的活动数
            long endedCount = seckillActivityService.countByStatus(2);
            statistics.put("endedCount", endedCount);
            
            log.debug("查询秒杀活动统计成功 - total: {}, ongoing: {}, upcoming: {}, ended: {}", 
                    totalCount, ongoingCount, upcomingCount, endedCount);
            
            return Result.success(statistics);
        } catch (Exception e) {
            log.error("查询秒杀活动统计失败", e);
            return Result.error("查询失败：" + e.getMessage());
        }
    }
}
