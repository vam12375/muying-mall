package com.muyingmall.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.muyingmall.common.api.CommonResult;
import com.muyingmall.dto.CommentDTO;
import com.muyingmall.dto.CommentReplyDTO;
import com.muyingmall.dto.CommentStatsDTO;
import com.muyingmall.dto.CommentTemplateDTO;
import com.muyingmall.entity.Comment;
import com.muyingmall.entity.CommentReply;
import com.muyingmall.entity.CommentTemplate;
import com.muyingmall.entity.User;
import com.muyingmall.entity.Product;
import com.muyingmall.service.CommentReplyService;
import com.muyingmall.service.CommentService;
import com.muyingmall.service.CommentTemplateService;
import com.muyingmall.service.UserService;
import com.muyingmall.service.ProductService;
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
import java.util.Comparator;

/**
 * 管理后台评价管理控制器
 */
@RestController
@RequestMapping("/admin/comments")
@RequiredArgsConstructor
@Tag(name = "管理后台评价管理", description = "管理后台评价查询、管理相关接口")
public class AdminCommentController {

    private final CommentService commentService;
    private final CommentReplyService commentReplyService;
    private final CommentTemplateService commentTemplateService;
    private final UserService userService;
    private final ProductService productService;

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

            // 查询每条评论的回复数量，设置hasReplied状态
            for (CommentDTO commentDTO : records) {
                List<CommentReply> replies = commentReplyService.getCommentReplies(commentDTO.getCommentId());
                commentDTO.setHasReplied(replies != null && !replies.isEmpty());
                // 如果需要返回回复内容，可以设置replies字段
                // commentDTO.setReplies(convertToReplyDTO(replies));
            }

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

            // 加载关联的用户信息
            User user = comment.getUser();
            if (user == null && comment.getUserId() != null) {
                // 如果用户信息未加载，尝试查询用户信息
                user = userService.getById(comment.getUserId());
                comment.setUser(user);
            }

            // 设置用户信息
            if (user != null) {
                commentDTO.setUserName(user.getUsername());
                commentDTO.setUserNickname(user.getNickname());
                commentDTO.setUserAvatar(user.getAvatar());
            }

            // 加载关联的商品信息
            Product product = comment.getProduct();
            if (product == null && comment.getProductId() != null) {
                // 如果商品信息未加载，尝试查询商品信息
                product = productService.getById(comment.getProductId());
                comment.setProduct(product);
            }

            // 设置商品信息
            if (product != null) {
                commentDTO.setProductName(product.getProductName());
                commentDTO.setProductImage(product.getProductImg());
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
            
            // 统计各状态的评价数量
            long pendingCount = commentService.count(new LambdaQueryWrapper<Comment>().eq(Comment::getStatus, 0));
            long approvedCount = commentService.count(new LambdaQueryWrapper<Comment>().eq(Comment::getStatus, 1));
            long rejectedCount = commentService.count(new LambdaQueryWrapper<Comment>().eq(Comment::getStatus, 2));
            long repliedCount = commentService.count(new LambdaQueryWrapper<Comment>().eq(Comment::getHasReplied, true));
            
            statsDTO.setPendingComments((int) pendingCount);
            statsDTO.setApprovedComments((int) approvedCount);
            statsDTO.setRejectedComments((int) rejectedCount);
            statsDTO.setRepliedComments((int) repliedCount);

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
     * 获取评价回复列表
     *
     * @param commentId 评价ID
     * @return 回复列表
     */
    @GetMapping("/{id}/replies")
    @Operation(summary = "获取评价回复列表")
    public CommonResult<List<CommentReplyDTO>> getCommentReplies(
            @PathVariable("id") @Parameter(description = "评价ID") Integer commentId) {
        try {
            List<CommentReply> replies = commentReplyService.getCommentReplies(commentId);
            List<CommentReplyDTO> result = convertToReplyDTO(replies);
            return CommonResult.success(result);
        } catch (Exception e) {
            return CommonResult.failed("获取评价回复列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取推荐回复模板
     *
     * @param productId 商品ID
     * @param rating    评分
     * @param limit     限制数量
     * @return 推荐模板列表
     */
    @GetMapping("/templates/recommended")
    @Operation(summary = "获取推荐回复模板")
    public CommonResult<List<CommentTemplateDTO>> getReplyTemplates(
            @RequestParam(required = false) @Parameter(description = "商品ID") Integer productId,
            @RequestParam(required = false) @Parameter(description = "评分") Integer rating,
            @RequestParam(defaultValue = "5") @Parameter(description = "限制数量") Integer limit) {
        try {
            // 获取推荐模板列表
            List<CommentTemplate> templates = commentTemplateService.getRecommendedTemplates(null, productId, rating,
                    limit);

            // 筛选出商家回复模板（模板名称包含"商家"关键词的模板）
            List<CommentTemplate> merchantReplyTemplates = templates.stream()
                    .filter(template -> template.getTemplateName() != null &&
                            (template.getTemplateName().contains("商家") || template.getTemplateName().contains("回复")))
                    .collect(Collectors.toList());

            // 如果没有找到符合条件的商家回复模板，再次尝试获取所有模板中ID大于等于8的模板
            // 因为在数据库中ID 8-10是预设的商家回复模板
            if (merchantReplyTemplates.isEmpty() && !templates.isEmpty()) {
                merchantReplyTemplates = templates.stream()
                        .filter(template -> template.getTemplateId() >= 8)
                        .collect(Collectors.toList());
            }

            // 如果符合条件的模板超过了限制数量，按权重和使用次数排序后截取前limit个
            if (merchantReplyTemplates.size() > limit) {
                merchantReplyTemplates.sort(Comparator
                        .<CommentTemplate>comparingInt(t -> t.getWeight() == null ? 0 : t.getWeight()).reversed()
                        .thenComparingInt(t -> t.getUseCount() == null ? 0 : t.getUseCount()).reversed());
                merchantReplyTemplates = merchantReplyTemplates.subList(0, limit);
            }

            // 转换为DTO
            List<CommentTemplateDTO> result = merchantReplyTemplates.stream()
                    .map(this::convertToTemplateDTO)
                    .collect(Collectors.toList());

            return CommonResult.success(result);
        } catch (Exception e) {
            return CommonResult.failed("获取推荐回复模板失败: " + e.getMessage());
        }
    }

    /**
     * 创建评价回复
     *
     * @param replyDTO 回复信息
     * @return 创建结果
     */
    @PostMapping("/reply")
    @Operation(summary = "创建评价回复")
    public CommonResult<Boolean> createCommentReply(@RequestBody CommentReplyDTO replyDTO) {
        try {
            CommentReply reply = new CommentReply();
            BeanUtils.copyProperties(replyDTO, reply);
            boolean result = commentReplyService.createCommentReply(reply);
            return CommonResult.success(result);
        } catch (Exception e) {
            return CommonResult.failed("创建评价回复失败: " + e.getMessage());
        }
    }

    /**
     * 删除评价回复
     *
     * @param replyId 回复ID
     * @return 删除结果
     */
    @DeleteMapping("/reply/{replyId}")
    @Operation(summary = "删除评价回复")
    public CommonResult<Boolean> deleteCommentReply(@PathVariable Integer replyId) {
        try {
            boolean result = commentReplyService.deleteCommentReply(replyId);
            return CommonResult.success(result);
        } catch (Exception e) {
            return CommonResult.failed("删除评价回复失败: " + e.getMessage());
        }
    }

    /**
     * 更新评价回复
     *
     * @param replyId 回复ID
     * @param data    更新内容
     * @return 更新结果
     */
    @PutMapping("/reply/{replyId}")
    @Operation(summary = "更新评价回复")
    public CommonResult<Boolean> updateCommentReply(
            @PathVariable Integer replyId,
            @RequestBody Map<String, String> data) {
        try {
            String content = data.get("content");
            if (content == null) {
                return CommonResult.failed("回复内容不能为空");
            }

            CommentReply reply = commentReplyService.getById(replyId);
            if (reply == null) {
                return CommonResult.failed("回复不存在");
            }

            reply.setContent(content);
            boolean result = commentReplyService.updateCommentReply(reply);
            return CommonResult.success(result);
        } catch (Exception e) {
            return CommonResult.failed("更新评价回复失败: " + e.getMessage());
        }
    }

    /**
     * 批量回复评价
     *
     * @param data 批量回复信息（包含评价ID列表、回复内容和回复用户ID）
     * @return 回复结果
     */
    @PostMapping("/batch/reply")
    @Operation(summary = "批量回复评价")
    public CommonResult<Boolean> batchReplyComments(@RequestBody Map<String, Object> data) {
        try {
            @SuppressWarnings("unchecked")
            List<Integer> commentIds = (List<Integer>) data.get("commentIds");
            String content = (String) data.get("content");
            Integer replyUserId = (Integer) data.get("replyUserId");

            if (commentIds == null || commentIds.isEmpty() || content == null || content.isEmpty()
                    || replyUserId == null) {
                return CommonResult.failed("参数错误");
            }

            boolean allSuccess = true;
            for (Integer commentId : commentIds) {
                CommentReply reply = new CommentReply();
                reply.setCommentId(commentId);
                reply.setContent(content);
                reply.setReplyUserId(replyUserId);
                reply.setReplyType(1); // 商家回复

                boolean success = commentReplyService.createCommentReply(reply);
                if (!success) {
                    allSuccess = false;
                }
            }

            return CommonResult.success(allSuccess);
        } catch (Exception e) {
            return CommonResult.failed("批量回复评价失败: " + e.getMessage());
        }
    }

    /**
     * 将CommentTemplate实体转换为CommentTemplateDTO
     */
    private CommentTemplateDTO convertToTemplateDTO(CommentTemplate template) {
        if (template == null) {
            return null;
        }
        CommentTemplateDTO dto = new CommentTemplateDTO();
        BeanUtils.copyProperties(template, dto);
        return dto;
    }

    /**
     * 将CommentReply实体转换为CommentReplyDTO
     */
    private List<CommentReplyDTO> convertToReplyDTO(List<CommentReply> replies) {
        if (replies == null || replies.isEmpty()) {
            return new ArrayList<>();
        }
        return replies.stream().map(reply -> {
            CommentReplyDTO dto = new CommentReplyDTO();
            BeanUtils.copyProperties(reply, dto);

            // 设置用户信息
            if (reply.getReplyUser() != null) {
                dto.setReplyUserName(reply.getReplyUser().getUsername());
                dto.setReplyUserNickname(reply.getReplyUser().getNickname());
            }

            return dto;
        }).collect(Collectors.toList());
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