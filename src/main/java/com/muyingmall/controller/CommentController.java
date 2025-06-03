package com.muyingmall.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.muyingmall.common.api.CommonResult;
import com.muyingmall.dto.CommentDTO;
import com.muyingmall.dto.CommentReplyDTO;
import com.muyingmall.dto.CommentStatsDTO;
import com.muyingmall.dto.CommentTagDTO;
import com.muyingmall.dto.CommentTemplateDTO;
import com.muyingmall.dto.CommentWithTagsDTO;
import com.muyingmall.entity.Comment;
import com.muyingmall.entity.CommentReply;
import com.muyingmall.entity.CommentRewardConfig;
import com.muyingmall.entity.CommentTag;
import com.muyingmall.entity.CommentTemplate;
import com.muyingmall.entity.Order;
import com.muyingmall.entity.UserMessage;
import com.muyingmall.enums.MessageType;
import com.muyingmall.enums.OrderStatus;
import com.muyingmall.service.CommentReplyService;
import com.muyingmall.service.CommentRewardConfigService;
import com.muyingmall.service.CommentService;
import com.muyingmall.service.CommentTagService;
import com.muyingmall.service.CommentTemplateService;
import com.muyingmall.service.OrderService;
import com.muyingmall.service.UserMessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
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
@Slf4j
public class CommentController {

    private final CommentService commentService;
    private final OrderService orderService;
    private final CommentReplyService commentReplyService;
    private final CommentTagService commentTagService;
    private final CommentTemplateService commentTemplateService;
    private final CommentRewardConfigService commentRewardConfigService;
    private final UserMessageService userMessageService;

    @Value("${upload.path:E:/11/muying-web/public}")
    private String uploadPath;

    @Value("${upload.domain:http://localhost:5173}")
    private String domain;

    // 使用ObjectMapper将对象转换为JSON字符串
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Operation(summary = "创建商品评价")
    @PostMapping("/create")
    public CommonResult<Boolean> createComment(@RequestBody CommentDTO commentDTO) {
        Comment comment = new Comment();
        BeanUtils.copyProperties(commentDTO, comment);
        // 如果images是列表，转换为JSON字符串
        if (commentDTO.getImages() != null && !commentDTO.getImages().isEmpty()) {
            try {
                comment.setImages(objectMapper.writeValueAsString(commentDTO.getImages()));
            } catch (JsonProcessingException e) {
                e.printStackTrace();
                return CommonResult.failed("图片格式转换失败: " + e.getMessage());
            }
        }
        boolean result = commentService.createComment(comment);
        return CommonResult.success(result);
    }

    @Operation(summary = "创建订单商品评价")
    @PostMapping("/order/{orderId}")
    public CommonResult<Boolean> createOrderComment(
            @PathVariable @Parameter(description = "订单ID") Integer orderId,
            @RequestBody CommentDTO commentDTO) {
        // 检查订单是否已评价
        if (orderService.isOrderCommented(orderId)) {
            return CommonResult.failed("该订单已评价");
        }

        // 设置订单ID
        commentDTO.setOrderId(orderId);

        Comment comment = new Comment();
        BeanUtils.copyProperties(commentDTO, comment);

        // 如果images是列表，转换为JSON字符串
        if (commentDTO.getImages() != null && !commentDTO.getImages().isEmpty()) {
            try {
                comment.setImages(objectMapper.writeValueAsString(commentDTO.getImages()));
            } catch (JsonProcessingException e) {
                e.printStackTrace();
                return CommonResult.failed("图片格式转换失败: " + e.getMessage());
            }
        }

        boolean result = commentService.createComment(comment);
        return CommonResult.success(result);
    }

    @Operation(summary = "根据订单ID获取评价")
    @GetMapping("/order/{orderId}")
    public CommonResult<List<CommentDTO>> getOrderComments(
            @PathVariable @Parameter(description = "订单ID") Integer orderId) {
        // 根据订单ID查询评价
        LambdaQueryWrapper<Comment> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Comment::getOrderId, orderId);
        List<Comment> comments = commentService.list(queryWrapper);

        List<CommentDTO> result = convertToDTO(comments);
        return CommonResult.success(result);
    }

    @Operation(summary = "检查订单是否已评价")
    @GetMapping("/order/{orderId}/status")
    public CommonResult<Boolean> checkOrderCommentStatus(
            @PathVariable @Parameter(description = "订单ID") Integer orderId) {
        boolean isCommented = orderService.isOrderCommented(orderId);
        return CommonResult.success(isCommented);
    }

    @Operation(summary = "获取商品评价列表")
    @GetMapping("/product/{productId}")
    public CommonResult<List<CommentDTO>> getProductComments(
            @PathVariable @Parameter(description = "商品ID") Integer productId) {
        List<Comment> comments = commentService.getProductComments(productId);
        List<CommentDTO> result = convertToDTO(comments);

        // 为每条评价获取回复
        for (CommentDTO dto : result) {
            // 获取评价回复
            List<CommentReply> replies = commentReplyService.getCommentReplies(dto.getCommentId());
            if (replies != null && !replies.isEmpty()) {
                // 转换回复并设置到评价DTO
                dto.setReplies(convertToReplyDTO(replies));
                dto.setHasReplied(true);
            } else {
                dto.setHasReplied(false);
            }
        }

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

        // 为每条评价获取回复
        for (CommentDTO dto : records) {
            // 获取评价回复
            List<CommentReply> replies = commentReplyService.getCommentReplies(dto.getCommentId());
            if (replies != null && !replies.isEmpty()) {
                // 转换回复并设置到评价DTO
                dto.setReplies(convertToReplyDTO(replies));
                dto.setHasReplied(true);
            } else {
                dto.setHasReplied(false);
            }
        }

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

    @Operation(summary = "分页获取用户评价列表")
    @GetMapping("/user/{userId}/page")
    public CommonResult<Map<String, Object>> getUserCommentPage(
            @PathVariable @Parameter(description = "用户ID") Integer userId,
            @RequestParam(defaultValue = "1") @Parameter(description = "页码") Integer page,
            @RequestParam(defaultValue = "10") @Parameter(description = "每页数量") Integer size,
            @RequestParam(defaultValue = "createTime") @Parameter(description = "排序字段") String sort,
            @RequestParam(defaultValue = "desc") @Parameter(description = "排序方式(asc/desc)") String order) {
        IPage<Comment> commentPage = commentService.getUserCommentPage(userId, page, size, sort, order);

        // 转换评价列表
        List<CommentDTO> records = convertToDTO(commentPage.getRecords());

        // 构建返回结果
        Map<String, Object> result = Map.of(
                "total", commentPage.getTotal(),
                "pages", commentPage.getPages(),
                "current", commentPage.getCurrent(),
                "records", records);

        return CommonResult.success(result);
    }

    @Operation(summary = "分页获取用户评价列表（带搜索和筛选）")
    @GetMapping("/user/{userId}/page/search")
    public CommonResult<Map<String, Object>> searchUserCommentPage(
            @PathVariable @Parameter(description = "用户ID") Integer userId,
            @RequestParam(defaultValue = "1") @Parameter(description = "页码") Integer page,
            @RequestParam(defaultValue = "10") @Parameter(description = "每页数量") Integer size,
            @RequestParam(defaultValue = "createTime") @Parameter(description = "排序字段") String sort,
            @RequestParam(defaultValue = "desc") @Parameter(description = "排序方式(asc/desc)") String order,
            @RequestParam(required = false) @Parameter(description = "搜索关键词") String keyword,
            @RequestParam(required = false) @Parameter(description = "评分筛选(good/neutral/bad)") String ratingFilter) {
        IPage<Comment> commentPage = commentService.searchUserCommentPage(userId, page, size, sort, order, keyword,
                ratingFilter);

        // 转换评价列表
        List<CommentDTO> records = convertToDTO(commentPage.getRecords());

        // 构建返回结果
        Map<String, Object> result = Map.of(
                "total", commentPage.getTotal(),
                "pages", commentPage.getPages(),
                "current", commentPage.getCurrent(),
                "records", records);

        return CommonResult.success(result);
    }

    @Operation(summary = "获取用户评价统计数据")
    @GetMapping("/user/{userId}/stats")
    public CommonResult<Map<String, Object>> getUserCommentStats(
            @PathVariable @Parameter(description = "用户ID") Integer userId) {
        try {
            Map<String, Object> stats = commentService.getUserCommentStats(userId);
            return CommonResult.success(stats);
        } catch (Exception e) {
            log.error("获取用户评价统计数据失败", e);
            return CommonResult.failed("系统繁忙，请稍后再试");
        }
    }

    @Operation(summary = "获取用户评价趋势数据")
    @GetMapping("/user/{userId}/trend")
    public CommonResult<Map<String, Object>> getUserCommentTrend(
            @PathVariable @Parameter(description = "用户ID") Integer userId,
            @RequestParam(defaultValue = "30") @Parameter(description = "天数") Integer days) {
        try {
            Map<String, Object> trend = commentService.getUserCommentTrend(userId, days);
            return CommonResult.success(trend);
        } catch (Exception e) {
            log.error("获取用户评价趋势数据失败", e);
            return CommonResult.failed("系统繁忙，请稍后再试");
        }
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

    @Operation(summary = "更新评价")
    @PutMapping("/{commentId}")
    public CommonResult<Boolean> updateComment(
            @PathVariable @Parameter(description = "评价ID") Integer commentId,
            @RequestBody CommentDTO commentDTO) {
        // 设置评价ID
        commentDTO.setCommentId(commentId);

        Comment comment = new Comment();
        BeanUtils.copyProperties(commentDTO, comment);

        // 如果images是列表，转换为JSON字符串
        if (commentDTO.getImages() != null && !commentDTO.getImages().isEmpty()) {
            try {
                comment.setImages(objectMapper.writeValueAsString(commentDTO.getImages()));
            } catch (JsonProcessingException e) {
                e.printStackTrace();
                return CommonResult.failed("图片格式转换失败: " + e.getMessage());
            }
        }

        boolean result = commentService.updateById(comment);
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
            @RequestParam(required = false) @Parameter(description = "状态") Integer status,
            @RequestParam(required = false) @Parameter(description = "订单ID") Integer orderId) {

        IPage<Comment> commentPage = commentService.adminGetCommentPage(
                page, size, productId, userId, minRating, maxRating, status, orderId);

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
    }

    @Operation(summary = "创建评价回复")
    @PostMapping("/reply")
    public CommonResult<Boolean> createCommentReply(@RequestBody CommentReplyDTO replyDTO) {
        CommentReply reply = new CommentReply();
        BeanUtils.copyProperties(replyDTO, reply);
        boolean result = commentReplyService.createCommentReply(reply);
        return CommonResult.success(result);
    }

    @Operation(summary = "获取评价回复列表")
    @GetMapping("/{commentId}/replies")
    public CommonResult<List<CommentReplyDTO>> getCommentReplies(
            @PathVariable @Parameter(description = "评价ID") Integer commentId) {
        List<CommentReply> replies = commentReplyService.getCommentReplies(commentId);
        List<CommentReplyDTO> result = convertToReplyDTO(replies);
        return CommonResult.success(result);
    }

    @Operation(summary = "删除评价回复")
    @DeleteMapping("/reply/{replyId}")
    public CommonResult<Boolean> deleteCommentReply(
            @PathVariable @Parameter(description = "回复ID") Integer replyId) {
        boolean result = commentReplyService.deleteCommentReply(replyId);
        return CommonResult.success(result);
    }

    @Operation(summary = "更新评价回复")
    @PutMapping("/reply/{replyId}")
    public CommonResult<Boolean> updateCommentReply(
            @PathVariable @Parameter(description = "回复ID") Integer replyId,
            @RequestBody CommentReplyDTO replyDTO) {
        CommentReply reply = new CommentReply();
        BeanUtils.copyProperties(replyDTO, reply);
        reply.setReplyId(replyId);
        boolean result = commentReplyService.updateCommentReply(reply);
        return CommonResult.success(result);
    }

    @Operation(summary = "获取用户评价的回复列表")
    @GetMapping("/user/{userId}/replies")
    public CommonResult<List<CommentReplyDTO>> getUserCommentReplies(
            @PathVariable @Parameter(description = "用户ID") Integer userId) {
        List<CommentReply> replies = commentReplyService.getUserCommentReplies(userId);
        List<CommentReplyDTO> result = convertToReplyDTO(replies);
        return CommonResult.success(result);
    }

    @Operation(summary = "创建商品评价（带标签）")
    @PostMapping("/create/with-tags")
    public CommonResult<Boolean> createCommentWithTags(@RequestBody CommentWithTagsDTO commentDTO) {
        Comment comment = new Comment();
        BeanUtils.copyProperties(commentDTO, comment);

        // 如果images是列表，转换为JSON字符串
        if (commentDTO.getImages() != null && !commentDTO.getImages().isEmpty()) {
            try {
                comment.setImages(objectMapper.writeValueAsString(commentDTO.getImages()));
            } catch (JsonProcessingException e) {
                e.printStackTrace();
                return CommonResult.failed("图片格式转换失败: " + e.getMessage());
            }
        }

        // 获取标签ID列表
        List<Integer> tagIds = null;
        if (commentDTO.getTags() != null && !commentDTO.getTags().isEmpty()) {
            tagIds = commentDTO.getTags().stream()
                    .map(CommentTagDTO::getTagId)
                    .collect(Collectors.toList());
        }

        boolean result = commentService.createCommentWithTags(comment, tagIds);
        return CommonResult.success(result);
    }

    @Operation(summary = "获取评价的标签")
    @GetMapping("/{commentId}/tags")
    public CommonResult<List<CommentTagDTO>> getCommentTags(
            @PathVariable @Parameter(description = "评价ID") Integer commentId) {
        List<CommentTag> tags = commentService.getCommentTags(commentId);
        List<CommentTagDTO> result = convertToTagDTO(tags);
        return CommonResult.success(result);
    }

    @Operation(summary = "为评价添加标签")
    @PostMapping("/{commentId}/tags")
    public CommonResult<Boolean> addCommentTags(
            @PathVariable @Parameter(description = "评价ID") Integer commentId,
            @RequestBody List<Integer> tagIds) {
        boolean result = commentService.addCommentTags(commentId, tagIds);
        return CommonResult.success(result);
    }

    @Operation(summary = "更新评价标签")
    @PutMapping("/{commentId}/tags")
    public CommonResult<Boolean> updateCommentTags(
            @PathVariable @Parameter(description = "评价ID") Integer commentId,
            @RequestBody List<Integer> tagIds) {
        boolean result = commentService.updateCommentTags(commentId, tagIds);
        return CommonResult.success(result);
    }

    @Operation(summary = "删除评价标签")
    @DeleteMapping("/{commentId}/tags/{tagId}")
    public CommonResult<Boolean> removeCommentTag(
            @PathVariable @Parameter(description = "评价ID") Integer commentId,
            @PathVariable @Parameter(description = "标签ID") Integer tagId) {
        boolean result = commentService.removeCommentTag(commentId, tagId);
        return CommonResult.success(result);
    }

    @Operation(summary = "删除评价的所有标签")
    @DeleteMapping("/{commentId}/tags")
    public CommonResult<Boolean> removeAllCommentTags(
            @PathVariable @Parameter(description = "评价ID") Integer commentId) {
        boolean result = commentService.removeAllCommentTags(commentId);
        return CommonResult.success(result);
    }

    @Operation(summary = "获取热门标签列表")
    @GetMapping("/tags/hot")
    public CommonResult<List<CommentTagDTO>> getHotTags(
            @RequestParam(defaultValue = "10") @Parameter(description = "限制数量") Integer limit) {
        List<CommentTag> tags = commentTagService.getHotTags(limit);
        List<CommentTagDTO> result = convertToTagDTO(tags);
        return CommonResult.success(result);
    }

    @Operation(summary = "根据商品分类获取标签列表")
    @GetMapping("/tags/category/{categoryId}")
    public CommonResult<List<CommentTagDTO>> getTagsByCategory(
            @PathVariable @Parameter(description = "商品分类ID") Integer categoryId) {
        List<CommentTag> tags = commentTagService.getTagsByCategory(categoryId);
        List<CommentTagDTO> result = convertToTagDTO(tags);
        return CommonResult.success(result);
    }

    @Operation(summary = "搜索标签")
    @GetMapping("/tags/search")
    public CommonResult<List<CommentTagDTO>> searchTags(
            @RequestParam @Parameter(description = "搜索关键词") String keyword,
            @RequestParam(defaultValue = "10") @Parameter(description = "限制数量") Integer limit) {
        List<CommentTag> tags = commentTagService.searchTags(keyword, limit);
        List<CommentTagDTO> result = convertToTagDTO(tags);
        return CommonResult.success(result);
    }

    @Operation(summary = "获取评价推荐标签")
    @GetMapping("/tags/recommended")
    public CommonResult<List<CommentTagDTO>> getRecommendedTags(
            @RequestParam(required = false) @Parameter(description = "商品ID") Integer productId,
            @RequestParam(required = false) @Parameter(description = "分类ID") Integer categoryId,
            @RequestParam(defaultValue = "10") @Parameter(description = "限制数量") Integer limit) {
        List<CommentTag> tags = commentTagService.getRecommendedTags(productId, categoryId, limit);
        List<CommentTagDTO> result = convertToTagDTO(tags);
        return CommonResult.success(result);
    }

    @Operation(summary = "根据标签筛选用户评价")
    @GetMapping("/user/{userId}/tag/{tagId}")
    public CommonResult<Map<String, Object>> getUserCommentsByTag(
            @PathVariable @Parameter(description = "用户ID") Integer userId,
            @PathVariable @Parameter(description = "标签ID") Integer tagId,
            @RequestParam(defaultValue = "1") @Parameter(description = "页码") Integer page,
            @RequestParam(defaultValue = "10") @Parameter(description = "每页数量") Integer size,
            @RequestParam(defaultValue = "createTime") @Parameter(description = "排序字段") String sort,
            @RequestParam(defaultValue = "desc") @Parameter(description = "排序方式(asc/desc)") String order) {
        IPage<Comment> commentPage = commentService.getUserCommentsByTag(userId, tagId, page, size, sort, order);

        // 转换评价列表
        List<CommentWithTagsDTO> records = convertToCommentWithTagsDTO(commentPage.getRecords());

        // 构建返回结果
        Map<String, Object> result = Map.of(
                "total", commentPage.getTotal(),
                "pages", commentPage.getPages(),
                "current", commentPage.getCurrent(),
                "records", records);

        return CommonResult.success(result);
    }

    @Operation(summary = "获取系统预设评价模板")
    @GetMapping("/templates/system")
    public CommonResult<List<CommentTemplateDTO>> getSystemTemplates(
            @RequestParam(required = false) @Parameter(description = "评分") Integer rating,
            @RequestParam(required = false) @Parameter(description = "商品分类ID") Integer categoryId) {
        List<CommentTemplate> templates = commentTemplateService.getSystemTemplates(rating, categoryId);
        List<CommentTemplateDTO> result = templates.stream()
                .map(this::convertToTemplateDTO)
                .collect(Collectors.toList());
        return CommonResult.success(result);
    }

    @Operation(summary = "获取用户自定义评价模板")
    @GetMapping("/templates/user/{userId}")
    public CommonResult<List<CommentTemplateDTO>> getUserTemplates(
            @PathVariable @Parameter(description = "用户ID") Integer userId) {
        List<CommentTemplate> templates = commentTemplateService.getUserTemplates(userId);
        List<CommentTemplateDTO> result = templates.stream()
                .map(this::convertToTemplateDTO)
                .collect(Collectors.toList());
        return CommonResult.success(result);
    }

    @Operation(summary = "创建用户自定义评价模板")
    @PostMapping("/templates")
    public CommonResult<Boolean> createUserTemplate(@RequestBody CommentTemplateDTO templateDTO) {
        CommentTemplate template = new CommentTemplate();
        BeanUtils.copyProperties(templateDTO, template);
        boolean result = commentTemplateService.createUserTemplate(template);
        return CommonResult.success(result);
    }

    @Operation(summary = "更新评价模板")
    @PutMapping("/templates/{templateId}")
    public CommonResult<Boolean> updateTemplate(
            @PathVariable @Parameter(description = "模板ID") Integer templateId,
            @RequestBody CommentTemplateDTO templateDTO) {
        CommentTemplate template = new CommentTemplate();
        BeanUtils.copyProperties(templateDTO, template);
        template.setTemplateId(templateId);
        boolean result = commentTemplateService.updateTemplate(template);
        return CommonResult.success(result);
    }

    @Operation(summary = "删除评价模板")
    @DeleteMapping("/templates/{templateId}")
    public CommonResult<Boolean> deleteTemplate(
            @PathVariable @Parameter(description = "模板ID") Integer templateId,
            @RequestParam @Parameter(description = "用户ID") Integer userId) {
        boolean result = commentTemplateService.deleteTemplate(templateId, userId);
        return CommonResult.success(result);
    }

    @Operation(summary = "增加模板使用次数")
    @PostMapping("/templates/{templateId}/use")
    public CommonResult<Boolean> incrementTemplateUseCount(
            @PathVariable @Parameter(description = "模板ID") Integer templateId) {
        boolean result = commentTemplateService.incrementUseCount(templateId);
        return CommonResult.success(result);
    }

    @Operation(summary = "分页获取所有模板（管理员使用）")
    @GetMapping("/admin/templates")
    public CommonResult<Map<String, Object>> getTemplatesPage(
            @RequestParam(defaultValue = "1") @Parameter(description = "页码") Integer page,
            @RequestParam(defaultValue = "10") @Parameter(description = "每页数量") Integer size,
            @RequestParam(required = false) @Parameter(description = "模板类型") Integer templateType) {
        IPage<CommentTemplate> templatesPage = commentTemplateService.getTemplatesPage(page, size, templateType);

        // 转换为DTO
        List<CommentTemplateDTO> records = templatesPage.getRecords().stream()
                .map(this::convertToTemplateDTO)
                .collect(Collectors.toList());

        // 构建返回结果
        Map<String, Object> result = Map.of(
                "total", templatesPage.getTotal(),
                "pages", templatesPage.getPages(),
                "current", templatesPage.getCurrent(),
                "records", records);

        return CommonResult.success(result);
    }

    @Operation(summary = "获取评价奖励配置")
    @GetMapping("/reward/config")
    public CommonResult<CommentRewardConfig> getRewardConfig() {
        CommentRewardConfig config = commentRewardConfigService.getActiveRewardConfig();
        return CommonResult.success(config);
    }

    @Operation(summary = "计算评价可获得的奖励")
    @PostMapping("/reward/calculate")
    public CommonResult<Map<String, Object>> calculateReward(@RequestBody CommentDTO commentDTO) {
        Comment comment = new Comment();
        BeanUtils.copyProperties(commentDTO, comment);
        Map<String, Object> reward = commentRewardConfigService.calculateReward(comment);
        return CommonResult.success(reward);
    }

    @Operation(summary = "获取未评价订单列表")
    @GetMapping("/user/{userId}/unrated")
    public CommonResult<List<Order>> getUnratedOrders(
            @PathVariable @Parameter(description = "用户ID") Integer userId) {
        // 查询用户已完成但未评价的订单
        LambdaQueryWrapper<Order> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Order::getUserId, userId)
                .eq(Order::getStatus, OrderStatus.COMPLETED)
                .eq(Order::getIsCommented, 0)
                .orderByDesc(Order::getCompletionTime);

        List<Order> orders = orderService.list(queryWrapper);
        return CommonResult.success(orders);
    }

    @Operation(summary = "获取评价情感分析数据")
    @GetMapping("/sentiment-analysis")
    public CommonResult<Map<String, Object>> getCommentSentimentAnalysis(
            @RequestParam(required = false) @Parameter(description = "商品ID") Integer productId,
            @RequestParam(defaultValue = "30") @Parameter(description = "天数") Integer days) {

        Map<String, Object> result = commentService.getCommentSentimentAnalysis(productId, days);
        return CommonResult.success(result);
    }

    @Operation(summary = "获取用户评价奖励历史")
    @GetMapping("/user/{userId}/rewards")
    public CommonResult<List<Map<String, Object>>> getUserCommentRewards(
            @PathVariable @Parameter(description = "用户ID") Integer userId,
            @RequestParam(defaultValue = "1") @Parameter(description = "页码") Integer page,
            @RequestParam(defaultValue = "10") @Parameter(description = "每页数量") Integer size) {

        // 通过消息中心查询用户的评价奖励消息
        IPage<UserMessage> messages = userMessageService.getUserMessages(
                userId,
                MessageType.COMMENT_REWARD.getCode(),
                null,
                page,
                size);

        List<Map<String, Object>> result = new ArrayList<>();

        if (messages != null && messages.getRecords() != null) {
            for (UserMessage message : messages.getRecords()) {
                Map<String, Object> rewardInfo = new HashMap<>();
                rewardInfo.put("messageId", message.getMessageId());
                rewardInfo.put("title", message.getTitle());
                rewardInfo.put("content", message.getContent());
                rewardInfo.put("createTime", message.getCreateTime());
                rewardInfo.put("isRead", message.getIsRead());

                // 尝试解析额外信息
                if (message.getExtra() != null && !message.getExtra().isEmpty()) {
                    try {
                        Map<String, Object> extraInfo = objectMapper.readValue(message.getExtra(), Map.class);
                        rewardInfo.put("commentId", extraInfo.get("commentId"));
                        rewardInfo.put("productId", extraInfo.get("productId"));
                        rewardInfo.put("totalReward", extraInfo.get("totalReward"));
                        rewardInfo.put("rewards", extraInfo.get("rewards"));
                    } catch (Exception e) {
                        log.error("解析评价奖励额外信息失败: {}", e.getMessage());
                    }
                }

                result.add(rewardInfo);
            }
        }

        return CommonResult.success(result);
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

            // 处理图片，将JSON字符串转为列表
            if (comment.getImages() != null && !comment.getImages().isEmpty()) {
                try {
                    // 尝试将JSON字符串解析为列表
                    dto.setImages(objectMapper.readValue(comment.getImages(), List.class));
                } catch (JsonProcessingException e) {
                    // 如果解析失败，尝试使用旧的逗号分隔格式
                    dto.setImages(List.of(comment.getImages().split(",")));
                }
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
                dto.setReplyUserAvatar(reply.getReplyUser().getAvatar());
            }

            return dto;
        }).collect(Collectors.toList());
    }

    /**
     * 将评价列表转换为带标签的DTO
     *
     * @param comments 评价列表
     * @return 带标签的评价DTO列表
     */
    private List<CommentWithTagsDTO> convertToCommentWithTagsDTO(List<Comment> comments) {
        if (comments == null || comments.isEmpty()) {
            return new ArrayList<>();
        }

        return comments.stream().map(comment -> {
            CommentWithTagsDTO dto = new CommentWithTagsDTO();
            BeanUtils.copyProperties(comment, dto);

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

            // 设置图片列表
            if (comment.getImages() != null && !comment.getImages().isEmpty()) {
                try {
                    // 尝试将JSON字符串解析为列表
                    dto.setImages(objectMapper.readValue(comment.getImages(), List.class));
                } catch (JsonProcessingException e) {
                    // 如果解析失败，尝试使用旧的逗号分隔格式
                    dto.setImages(List.of(comment.getImages().split(",")));
                }
            }

            // 获取评价标签
            List<CommentTag> tags = commentService.getCommentTags(comment.getCommentId());
            if (tags != null && !tags.isEmpty()) {
                dto.setTags(convertToTagDTO(tags));
            }

            return dto;
        }).collect(Collectors.toList());
    }

    /**
     * 将标签列表转换为DTO
     *
     * @param tags 标签列表
     * @return 标签DTO列表
     */
    private List<CommentTagDTO> convertToTagDTO(List<CommentTag> tags) {
        if (tags == null || tags.isEmpty()) {
            return new ArrayList<>();
        }

        return tags.stream().map(tag -> {
            CommentTagDTO dto = new CommentTagDTO();
            BeanUtils.copyProperties(tag, dto);
            return dto;
        }).collect(Collectors.toList());
    }

    /**
     * 转换为模板DTO
     */
    private CommentTemplateDTO convertToTemplateDTO(CommentTemplate template) {
        if (template == null) {
            return null;
        }
        CommentTemplateDTO dto = new CommentTemplateDTO();
        BeanUtils.copyProperties(template, dto);
        return dto;
    }

    @Operation(summary = "获取推荐评价模板")
    @GetMapping("/templates/recommended")
    public CommonResult<List<CommentTemplateDTO>> getRecommendedTemplates(
            @RequestParam(required = false) @Parameter(description = "用户ID") Integer userId,
            @RequestParam(required = false) @Parameter(description = "商品ID") Integer productId,
            @RequestParam(required = false) @Parameter(description = "评分") Integer rating,
            @RequestParam(defaultValue = "10") @Parameter(description = "限制数量") Integer limit) {

        List<CommentTemplate> templates = commentTemplateService.getRecommendedTemplates(userId, productId, rating,
                limit);
        List<CommentTemplateDTO> result = templates.stream()
                .map(this::convertToTemplateDTO)
                .collect(Collectors.toList());
        return CommonResult.success(result);
    }

    @Operation(summary = "获取评价关键词")
    @GetMapping("/keywords")
    public CommonResult<Map<String, Object>> getCommentKeywords(
            @RequestParam(required = false) @Parameter(description = "商品ID") Integer productId,
            @RequestParam(required = false) @Parameter(description = "最低评分") Integer minRating,
            @RequestParam(required = false) @Parameter(description = "最高评分") Integer maxRating,
            @RequestParam(defaultValue = "20") @Parameter(description = "关键词数量") Integer limit) {

        Map<String, Object> keywords = commentService.getCommentKeywords(productId, minRating, maxRating, limit);
        return CommonResult.success(keywords);
    }

    /**
     * 上传评论图片
     */
    @Operation(summary = "上传评论图片")
    @PostMapping("/upload/image")
    public CommonResult<Map<String, String>> uploadCommentImage(@RequestParam("file") MultipartFile file) {
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

            // 生成评论图片存储路径
            String relativePath = "/comment/" + java.time.LocalDate.now().toString().replace("-", "/");
            String storageDir = uploadPath + relativePath;

            // 确保目录存在
            Path uploadDir = Paths.get(storageDir);
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            // 生成文件名: 时间戳_随机数.后缀
            String filename = System.currentTimeMillis() + "_" +
                    java.util.UUID.randomUUID().toString().substring(0, 8) + suffix;
            Path filePath = uploadDir.resolve(filename);

            // 保存文件
            Files.copy(file.getInputStream(), filePath);

            // 生成访问URL
            String fileUrl = domain + relativePath + "/" + filename;

            // 返回结果
            Map<String, String> result = new HashMap<>();
            result.put("url", fileUrl);
            result.put("name", originalFilename);

            return CommonResult.success(result);
        } catch (IOException e) {
            e.printStackTrace();
            return CommonResult.failed("图片上传失败: " + e.getMessage());
        }
    }
}