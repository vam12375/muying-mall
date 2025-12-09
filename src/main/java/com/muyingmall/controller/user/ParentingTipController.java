package com.muyingmall.controller.user;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.muyingmall.common.api.Result;
import com.muyingmall.entity.ParentingTip;
import com.muyingmall.entity.ParentingTipCategory;
import com.muyingmall.entity.ParentingTipComment;
import com.muyingmall.service.ParentingTipService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 育儿知识控制器
 */
@RestController
@RequestMapping("/parenting-tips")
@RequiredArgsConstructor
public class ParentingTipController {

    private final ParentingTipService parentingTipService;

    /**
     * 分页查询育儿知识列表
     */
    @GetMapping
    public Result<Map<String, Object>> getList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) String keyword) {
        
        IPage<ParentingTip> pageResult = parentingTipService.getPage(page, pageSize, categoryId, keyword);
        
        Map<String, Object> data = new HashMap<>();
        data.put("list", pageResult.getRecords());
        data.put("total", pageResult.getTotal());
        data.put("page", pageResult.getCurrent());
        data.put("pageSize", pageResult.getSize());
        
        return Result.success(data);
    }

    /**
     * 获取热门知识
     */
    @GetMapping("/hot")
    public Result<List<ParentingTip>> getHotTips(@RequestParam(defaultValue = "6") Integer limit) {
        return Result.success(parentingTipService.getHotTips(limit));
    }

    /**
     * 获取所有分类
     */
    @GetMapping("/categories")
    public Result<List<ParentingTipCategory>> getCategories() {
        return Result.success(parentingTipService.getCategories());
    }

    /**
     * 获取详情
     */
    @GetMapping("/{id}")
    public Result<ParentingTip> getDetail(@PathVariable Long id) {
        ParentingTip tip = parentingTipService.getDetail(id);
        if (tip == null) {
            return Result.error("知识不存在");
        }
        return Result.success(tip);
    }

    /**
     * 增加浏览量
     */
    @PostMapping("/{id}/view")
    public Result<Void> increaseViewCount(@PathVariable Long id) {
        parentingTipService.increaseViewCount(id);
        return Result.success();
    }

    /**
     * 获取相关知识
     */
    @GetMapping("/{id}/related")
    public Result<List<ParentingTip>> getRelatedTips(
            @PathVariable Long id,
            @RequestParam Integer categoryId,
            @RequestParam(defaultValue = "4") Integer limit) {
        return Result.success(parentingTipService.getRelatedTips(categoryId, id, limit));
    }

    /**
     * 获取评论列表
     */
    @GetMapping("/{id}/comments")
    public Result<Map<String, Object>> getComments(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        var pageResult = parentingTipService.getComments(id, page, pageSize);
        Map<String, Object> data = new HashMap<>();
        data.put("list", pageResult.getRecords());
        data.put("total", pageResult.getTotal());
        return Result.success(data);
    }

    /**
     * 添加评论
     */
    @PostMapping("/{id}/comments")
    public Result<ParentingTipComment> addComment(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            jakarta.servlet.http.HttpSession session) {
        // 优先从Session中获取用户信息（JwtFilter会同步用户到Session）
        Integer userId = null;
        Object userObj = session.getAttribute("user");
        if (userObj instanceof com.muyingmall.entity.User) {
            userId = ((com.muyingmall.entity.User) userObj).getUserId();
        }
        
        // 如果Session中没有，尝试从SecurityContext获取
        if (userId == null) {
            var authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getDetails() instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> details = (Map<String, Object>) authentication.getDetails();
                userId = (Integer) details.get("userId");
            }
        }
        
        if (userId == null) {
            return Result.error("请先登录");
        }
        String content = body.get("content");
        if (content == null || content.trim().isEmpty()) {
            return Result.error("评论内容不能为空");
        }
        return Result.success(parentingTipService.addComment(id, userId, content.trim()));
    }
}
