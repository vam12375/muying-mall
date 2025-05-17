package com.muyingmall.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.muyingmall.common.api.CommonResult;
import com.muyingmall.dto.CommentDTO;
import com.muyingmall.dto.CommentStatsDTO;
import com.muyingmall.entity.Comment;
import com.muyingmall.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 评价管理控制器
 */
@RestController
@RequestMapping("/comment")
@RequiredArgsConstructor
@Tag(name = "评价管理", description = "评价相关接口")
public class CommentController {

    private final CommentService commentService;

    @Operation(summary = "创建商品评价")
    @PostMapping("/create")
    public CommonResult<Boolean> createComment(@RequestBody CommentDTO commentDTO) {
        Comment comment = new Comment();
        BeanUtils.copyProperties(commentDTO, comment);
        // 如果images是列表，需要转换为JSON字符串
        if (commentDTO.getImages() != null && !commentDTO.getImages().isEmpty()) {
            comment.setImages(String.join(",", commentDTO.getImages()));
        }
        boolean result = commentService.createComment(comment);
        return CommonResult.success(result);
    }

    @Operation(summary = "获取商品评价列表")
    @GetMapping("/product/{productId}")
    public CommonResult<List<CommentDTO>> getProductComments(
            @PathVariable @Parameter(description = "商品ID") Integer productId) {
        List<Comment> comments = commentService.getProductComments(productId);
        List<CommentDTO> result = convertToDTO(comments);
        return CommonResult.success(result);
    }

    @Operation(summary = "分页获取商品评价")
    @GetMapping("/product/{productId}/page")
    public CommonResult<Map<String, Object>> getProductCommentPage(
            @PathVariable @Parameter(description = "商品ID") Integer productId,
            @RequestParam(defaultValue = "1") @Parameter(description = "页码") Integer page,
            @RequestParam(defaultValue = "10") @Parameter(description = "每页数量") Integer size) {
        IPage<Comment> commentPage = commentService.getProductCommentPage(productId, page, size);

        // 获取评分统计
        Map<String, Object> ratingStats = commentService.getProductRatingStats(productId);

        // 转换评价列表
        List<CommentDTO> records = convertToDTO(commentPage.getRecords());

        // 构建返回结果
        Map<String, Object> result = Map.of(
                "total", commentPage.getTotal(),
                "pages", commentPage.getPages(),
                "current", commentPage.getCurrent(),
                "records", records,
                "stats", ratingStats);

        return CommonResult.success(result);
    }

    @Operation(summary = "获取用户评价列表")
    @GetMapping("/user/{userId}")
    public CommonResult<List<CommentDTO>> getUserComments(
            @PathVariable @Parameter(description = "用户ID") Integer userId) {
        List<Comment> userComments = commentService.getUserCommentPage(userId, 1, 100).getRecords();
        List<CommentDTO> result = convertToDTO(userComments);
        return CommonResult.success(result);
    }

    @Operation(summary = "更新评价状态")
    @PutMapping("/{commentId}/status")
    public CommonResult<Boolean> updateCommentStatus(
            @PathVariable @Parameter(description = "评价ID") Integer commentId,
            @RequestParam @Parameter(description = "状态：0-隐藏，1-显示") Integer status) {
        boolean result = commentService.updateCommentStatus(commentId, status);
        return CommonResult.success(result);
    }

    @Operation(summary = "删除评价")
    @DeleteMapping("/{commentId}")
    public CommonResult<Boolean> deleteComment(
            @PathVariable @Parameter(description = "评价ID") Integer commentId) {
        boolean result = commentService.deleteComment(commentId);
        return CommonResult.success(result);
    }

    @Operation(summary = "获取商品评分统计")
    @GetMapping("/product/{productId}/stats")
    public CommonResult<Map<String, Object>> getProductRatingStats(
            @PathVariable @Parameter(description = "商品ID") Integer productId) {
        Map<String, Object> stats = commentService.getProductRatingStats(productId);
        return CommonResult.success(stats);
    }

    @Operation(summary = "管理员获取评价列表")
    @GetMapping("/admin/list")
    public CommonResult<Map<String, Object>> adminGetCommentList(
            @RequestParam(defaultValue = "1") @Parameter(description = "页码") Integer page,
            @RequestParam(defaultValue = "10") @Parameter(description = "每页数量") Integer size,
            @RequestParam(required = false) @Parameter(description = "商品ID") Integer productId,
            @RequestParam(required = false) @Parameter(description = "用户ID") Integer userId,
            @RequestParam(required = false) @Parameter(description = "最低评分") Integer minRating,
            @RequestParam(required = false) @Parameter(description = "最高评分") Integer maxRating,
            @RequestParam(required = false) @Parameter(description = "状态") Integer status) {

        IPage<Comment> commentPage = commentService.adminGetCommentPage(
                page, size, productId, userId, minRating, maxRating, status);

        List<CommentDTO> records = convertToDTO(commentPage.getRecords());

        Map<String, Object> result = Map.of(
                "total", commentPage.getTotal(),
                "pages", commentPage.getPages(),
                "current", commentPage.getCurrent(),
                "records", records);

        return CommonResult.success(result);
    }

    @Operation(summary = "获取评价统计数据")
    @GetMapping("/admin/stats")
    public CommonResult<CommentStatsDTO> getCommentStats(
            @RequestParam(defaultValue = "7") @Parameter(description = "统计天数") Integer days) {
        Map<String, Object> stats = commentService.getCommentStats(days);

        // 构建DTO
        CommentStatsDTO statsDTO = new CommentStatsDTO();
        statsDTO.setTotalComments((Long) stats.get("totalComments"));
        statsDTO.setAverageRating((Double) stats.get("averageRating"));
        statsDTO.setRatingDistribution((Map<Integer, Integer>) stats.get("totalRatingCounts"));
        statsDTO.setDateLabels((List<String>) stats.get("dateLabels"));
        statsDTO.setDailyRatingData((List<Map<String, Object>>) stats.get("dailyRatingData"));

        // 计算评价率
        Map<Integer, Integer> ratingCounts = (Map<Integer, Integer>) stats.get("totalRatingCounts");
        int totalCount = ratingCounts.values().stream().mapToInt(Integer::intValue).sum();

        if (totalCount > 0) {
            // 好评率 (4-5星)
            int positiveCount = ratingCounts.getOrDefault(4, 0) + ratingCounts.getOrDefault(5, 0);
            statsDTO.setPositiveRate((double) positiveCount / totalCount * 100);

            // 中评率 (3星)
            int neutralCount = ratingCounts.getOrDefault(3, 0);
            statsDTO.setNeutralRate((double) neutralCount / totalCount * 100);

            // 差评率 (1-2星)
            int negativeCount = ratingCounts.getOrDefault(1, 0) + ratingCounts.getOrDefault(2, 0);
            statsDTO.setNegativeRate((double) negativeCount / totalCount * 100);
        } else {
            statsDTO.setPositiveRate(0.0);
            statsDTO.setNeutralRate(0.0);
            statsDTO.setNegativeRate(0.0);
        }

        return CommonResult.success(statsDTO);
    }

    /**
     * 将Comment实体转换为CommentDTO
     */
    private List<CommentDTO> convertToDTO(List<Comment> comments) {
        if (comments == null || comments.isEmpty()) {
            return new ArrayList<>();
        }

        return comments.stream().map(comment -> {
            CommentDTO dto = new CommentDTO();
            BeanUtils.copyProperties(comment, dto);

            // 处理图片，将逗号分隔的字符串转为列表
            if (comment.getImages() != null && !comment.getImages().isEmpty()) {
                dto.setImages(List.of(comment.getImages().split(",")));
            }

            // 设置用户信息
            if (comment.getUser() != null) {
                dto.setUserName(comment.getUser().getUsername());
                dto.setUserNickname(comment.getUser().getNickname());
                dto.setUserAvatar(comment.getUser().getAvatar());
            }

            // 设置商品信息
            if (comment.getProduct() != null) {
                dto.setProductName(comment.getProduct().getProductName());
                dto.setProductImage(comment.getProduct().getProductImg());
            }

            return dto;
        }).collect(Collectors.toList());
    }
}