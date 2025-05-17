package com.muyingmall.controller.admin;

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
 * 管理后台评价管理控制器
 */
@RestController
@RequestMapping("/admin/comments")
@RequiredArgsConstructor
@Tag(name = "管理后台评价管理", description = "管理后台评价查询、管理相关接口")
public class AdminCommentController {

    private final CommentService commentService;

    /**
     * 分页获取评价列表
     *
     * @param page      页码
     * @param size      每页大小
     * @param productId 商品ID，可为null
     * @param userId    用户ID，可为null
     * @param minRating 最低评分，可为null
     * @param maxRating 最高评分，可为null
     * @param status    状态，可为null
     * @param orderId   订单ID，可为null
     * @return 评价分页列表
     */
    @GetMapping("/page")
    @Operation(summary = "分页获取评价列表")
    public CommonResult<Map<String, Object>> getCommentPage(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "productId", required = false) Integer productId,
            @RequestParam(value = "userId", required = false) Integer userId,
            @RequestParam(value = "minRating", required = false) Integer minRating,
            @RequestParam(value = "maxRating", required = false) Integer maxRating,
            @RequestParam(value = "status", required = false) Integer status,
            @RequestParam(value = "orderId", required = false) Integer orderId) {
        try {
            IPage<Comment> commentPage = commentService.adminGetCommentPage(
                    page, size, productId, userId, minRating, maxRating, status, orderId);

            List<CommentDTO> records = convertToDTO(commentPage.getRecords());

            Map<String, Object> result = Map.of(
                    "total", commentPage.getTotal(),
                    "pages", commentPage.getPages(),
                    "current", commentPage.getCurrent(),
                    "records", records);

            return CommonResult.success(result);
        } catch (Exception e) {
            return CommonResult.failed("获取评价列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取评价详情
     *
     * @param id 评价ID
     * @return 评价详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取评价详情")
    public CommonResult<CommentDTO> getCommentDetail(@PathVariable Integer id) {
        try {
            Comment comment = commentService.getById(id);
            if (comment == null) {
                return CommonResult.failed("评价不存在");
            }

            CommentDTO commentDTO = new CommentDTO();
            BeanUtils.copyProperties(comment, commentDTO);

            // 处理图片，将逗号分隔的字符串转为列表
            if (comment.getImages() != null && !comment.getImages().isEmpty()) {
                commentDTO.setImages(List.of(comment.getImages().split(",")));
            }

            return CommonResult.success(commentDTO);
        } catch (Exception e) {
            return CommonResult.failed("获取评价详情失败: " + e.getMessage());
        }
    }

    /**
     * 更新评价状态
     *
     * @param id     评价ID
     * @param status 状态值（0-隐藏，1-显示）
     * @return 更新结果
     */
    @PutMapping("/{id}/status")
    @Operation(summary = "更新评价状态")
    public CommonResult<Boolean> updateCommentStatus(
            @PathVariable Integer id,
            @RequestParam("status") Integer status) {
        try {
            boolean result = commentService.updateCommentStatus(id, status);
            String statusDesc = status == 1 ? "显示" : "隐藏";
            if (result) {
                return CommonResult.success(true, "评价" + statusDesc + "成功");
            } else {
                return CommonResult.failed("评价" + statusDesc + "失败");
            }
        } catch (Exception e) {
            return CommonResult.failed("更新评价状态失败: " + e.getMessage());
        }
    }

    /**
     * 删除评价
     *
     * @param id 评价ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除评价")
    public CommonResult<Boolean> deleteComment(@PathVariable Integer id) {
        try {
            boolean result = commentService.deleteComment(id);
            if (result) {
                return CommonResult.success(true, "删除评价成功");
            } else {
                return CommonResult.failed("删除评价失败");
            }
        } catch (Exception e) {
            return CommonResult.failed("删除评价失败: " + e.getMessage());
        }
    }

    /**
     * 获取评价统计数据
     *
     * @param days 统计天数，如7表示最近7天
     * @return 统计数据
     */
    @GetMapping("/stats")
    @Operation(summary = "获取评价统计数据")
    public CommonResult<CommentStatsDTO> getCommentStats(
            @RequestParam(value = "days", defaultValue = "7") Integer days) {
        try {
            Map<String, Object> stats = commentService.getCommentStats(days);

            // 构建DTO
            CommentStatsDTO statsDTO = new CommentStatsDTO();
            statsDTO.setTotalComments((Integer) stats.get("totalComments"));
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
        } catch (Exception e) {
            return CommonResult.failed("获取评价统计数据失败: " + e.getMessage());
        }
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