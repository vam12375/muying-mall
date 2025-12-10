package com.muyingmall.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.muyingmall.common.api.Result;
import com.muyingmall.entity.SysNotice;
import com.muyingmall.service.SysNoticeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 通知公告管理控制器
 * 提供系统公告的发布和管理功能
 */
@RestController
@RequestMapping("/admin/notice")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "后台-通知公告", description = "系统公告管理接口")
public class AdminNoticeController {

    private final SysNoticeService noticeService;

    /**
     * 分页查询公告
     */
    @GetMapping
    @Operation(summary = "分页查询公告", description = "支持按类型、状态、关键词筛选")
    public Result<Map<String, Object>> getNoticePage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword) {
        try {
            IPage<SysNotice> noticePage = noticeService.getNoticePage(page, size, type, status, keyword);
            
            Map<String, Object> result = new HashMap<>();
            result.put("items", noticePage.getRecords());
            result.put("total", noticePage.getTotal());
            result.put("page", noticePage.getCurrent());
            result.put("size", noticePage.getSize());
            
            return Result.success(result);
        } catch (Exception e) {
            log.error("查询公告列表失败", e);
            return Result.error("查询公告列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取公告统计
     */
    @GetMapping("/stats")
    @Operation(summary = "获取公告统计", description = "获取公告数量统计信息")
    public Result<Map<String, Object>> getNoticeStats() {
        try {
            Map<String, Object> stats = noticeService.getNoticeStats();
            return Result.success(stats);
        } catch (Exception e) {
            log.error("获取公告统计失败", e);
            return Result.error("获取公告统计失败: " + e.getMessage());
        }
    }

    /**
     * 获取公告详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取公告详情", description = "根据ID获取公告详情并增加浏览量")
    public Result<SysNotice> getNotice(@PathVariable Integer id) {
        try {
            SysNotice notice = noticeService.getById(id);
            if (notice == null) {
                return Result.error("公告不存在");
            }
            // 增加浏览量
            noticeService.incrementViewCount(id);
            return Result.success(notice);
        } catch (Exception e) {
            log.error("获取公告详情失败: {}", id, e);
            return Result.error("获取公告详情失败: " + e.getMessage());
        }
    }

    /**
     * 发布公告
     */
    @PostMapping
    @Operation(summary = "发布公告", description = "创建并发布新公告")
    public Result<Boolean> publishNotice(@RequestBody SysNotice notice) {
        try {
            boolean success = noticeService.publishNotice(notice);
            return success ? Result.success(true) : Result.error("发布公告失败");
        } catch (Exception e) {
            log.error("发布公告失败", e);
            return Result.error("发布公告失败: " + e.getMessage());
        }
    }

    /**
     * 保存草稿
     */
    @PostMapping("/draft")
    @Operation(summary = "保存草稿", description = "保存公告为草稿状态")
    public Result<Boolean> saveDraft(@RequestBody SysNotice notice) {
        try {
            notice.setStatus("draft");
            boolean success = noticeService.save(notice);
            return success ? Result.success(true) : Result.error("保存草稿失败");
        } catch (Exception e) {
            log.error("保存草稿失败", e);
            return Result.error("保存草稿失败: " + e.getMessage());
        }
    }

    /**
     * 更新公告
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新公告", description = "更新指定公告信息")
    public Result<Boolean> updateNotice(@PathVariable Integer id, @RequestBody SysNotice notice) {
        try {
            notice.setId(id);
            boolean success = noticeService.updateById(notice);
            return success ? Result.success(true) : Result.error("更新公告失败");
        } catch (Exception e) {
            log.error("更新公告失败: {}", id, e);
            return Result.error("更新公告失败: " + e.getMessage());
        }
    }

    /**
     * 删除公告
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除公告", description = "删除指定公告")
    public Result<Boolean> deleteNotice(@PathVariable Integer id) {
        try {
            boolean success = noticeService.removeById(id);
            return success ? Result.success(true) : Result.error("删除公告失败");
        } catch (Exception e) {
            log.error("删除公告失败: {}", id, e);
            return Result.error("删除公告失败: " + e.getMessage());
        }
    }

    /**
     * 撤回公告
     */
    @PutMapping("/{id}/withdraw")
    @Operation(summary = "撤回公告", description = "将已发布的公告撤回为草稿状态")
    public Result<Boolean> withdrawNotice(@PathVariable Integer id) {
        try {
            boolean success = noticeService.withdrawNotice(id);
            return success ? Result.success(true) : Result.error("撤回公告失败");
        } catch (Exception e) {
            log.error("撤回公告失败: {}", id, e);
            return Result.error("撤回公告失败: " + e.getMessage());
        }
    }

    /**
     * 切换置顶状态
     */
    @PutMapping("/{id}/pinned")
    @Operation(summary = "切换置顶", description = "切换公告的置顶状态")
    public Result<Boolean> togglePinned(@PathVariable Integer id) {
        try {
            boolean success = noticeService.togglePinned(id);
            return success ? Result.success(true) : Result.error("切换置顶状态失败");
        } catch (Exception e) {
            log.error("切换置顶状态失败: {}", id, e);
            return Result.error("切换置顶状态失败: " + e.getMessage());
        }
    }
}
