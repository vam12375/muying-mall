package com.muyingmall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.muyingmall.common.exception.BusinessException;
import com.muyingmall.entity.Comment;
import com.muyingmall.entity.CommentTag;
import com.muyingmall.entity.CommentTagRelation;
import com.muyingmall.entity.Order;
import com.muyingmall.entity.OrderProduct;
import com.muyingmall.entity.Product;
import com.muyingmall.entity.User;
import com.muyingmall.mapper.CommentMapper;
import com.muyingmall.mapper.CommentTagMapper;
import com.muyingmall.mapper.CommentTagRelationMapper;
import com.muyingmall.mapper.OrderMapper;
import com.muyingmall.mapper.OrderProductMapper;
import com.muyingmall.mapper.ProductMapper;
import com.muyingmall.mapper.UserMapper;
import com.muyingmall.service.CommentService;
import com.muyingmall.service.CommentTagService;
import com.muyingmall.service.CommentRewardConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.muyingmall.entity.Order;
import com.muyingmall.enums.OrderStatus;

/**
 * 评价服务实现类
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements CommentService {

    private final UserMapper userMapper;
    private final ProductMapper productMapper;
    private final OrderMapper orderMapper;
    private final OrderProductMapper orderProductMapper;
    private final CommentTagMapper commentTagMapper;
    private final CommentTagRelationMapper commentTagRelationMapper;
    private final CommentTagService commentTagService;
    private final CommentRewardConfigService commentRewardConfigService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createComment(Comment comment) {
        // 校验用户是否存在
        User user = userMapper.selectById(comment.getUserId());
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 校验商品是否存在
        Product product = productMapper.selectById(comment.getProductId());
        if (product == null) {
            throw new BusinessException("商品不存在");
        }

        // 校验订单是否存在且属于该用户
        Order order = orderMapper.selectById(comment.getOrderId());
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (!order.getUserId().equals(comment.getUserId())) {
            throw new BusinessException("无权评价此订单");
        }

        // 校验是否已经评价过
        LambdaQueryWrapper<Comment> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Comment::getOrderId, comment.getOrderId())
                .eq(Comment::getProductId, comment.getProductId())
                .eq(Comment::getUserId, comment.getUserId());
        long count = this.count(queryWrapper);
        if (count > 0) {
            throw new BusinessException("您已经评价过该商品");
        }

        // 设置默认值
        if (comment.getRating() == null) {
            comment.setRating(5); // 默认5星好评
        }
        if (comment.getIsAnonymous() == null) {
            comment.setIsAnonymous(0); // 默认不匿名
        }
        if (comment.getStatus() == null) {
            comment.setStatus(1); // 默认显示
        }

        // 保存评价
        boolean result = this.save(comment);

        // 更新订单评价状态
        if (result) {
            orderMapper.updateOrderCommentStatus(comment.getOrderId(), 1);
        }

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createCommentWithTags(Comment comment, List<Integer> tagIds) {
        // 创建评价
        boolean result = createComment(comment);

        // 如果评价创建成功且有标签，添加标签关联
        if (result && !CollectionUtils.isEmpty(tagIds)) {
            addCommentTags(comment.getCommentId(), tagIds);
        }

        return result;
    }

    @Override
    public IPage<Comment> getProductCommentPage(Integer productId, Integer page, Integer size) {
        Page<Comment> pageParam = new Page<>(page, size);

        LambdaQueryWrapper<Comment> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Comment::getProductId, productId)
                .eq(Comment::getStatus, 1) // 只查询已审核通过的评价
                .orderByDesc(Comment::getCreateTime);

        IPage<Comment> commentPage = this.page(pageParam, queryWrapper);

        // 填充用户信息（非匿名评价）
        List<Comment> records = commentPage.getRecords();
        if (!records.isEmpty()) {
            List<Integer> userIds = records.stream()
                    .filter(c -> c.getIsAnonymous() == 0)
                    .map(Comment::getUserId)
                    .collect(Collectors.toList());

            if (!userIds.isEmpty()) {
                List<User> users = userMapper.selectList(new LambdaQueryWrapper<User>()
                        .in(User::getUserId, userIds));
                Map<Integer, User> userMap = users.stream()
                        .collect(Collectors.toMap(User::getUserId, user -> user));

                records.forEach(comment -> {
                    if (comment.getIsAnonymous() == 0 && userMap.containsKey(comment.getUserId())) {
                        comment.setUser(userMap.get(comment.getUserId()));
                    } else {
                        // 匿名评价，创建一个匿名用户对象
                        User anonymousUser = new User();
                        anonymousUser.setNickname("匿名用户");
                        comment.setUser(anonymousUser);
                    }
                });
            }
        }

        return commentPage;
    }

    @Override
    public List<Comment> getProductComments(Integer productId) {
        return baseMapper.getProductComments(productId);
    }

    @Override
    public IPage<Comment> getUserCommentPage(Integer userId, Integer page, Integer size) {
        Page<Comment> pageParam = new Page<>(page, size);

        LambdaQueryWrapper<Comment> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Comment::getUserId, userId)
                .orderByDesc(Comment::getCreateTime);

        IPage<Comment> commentPage = this.page(pageParam, queryWrapper);

        // 填充商品信息
        List<Comment> records = commentPage.getRecords();
        if (!records.isEmpty()) {
            List<Integer> productIds = records.stream()
                    .map(Comment::getProductId)
                    .collect(Collectors.toList());

            List<Product> products = productMapper.selectList(new LambdaQueryWrapper<Product>()
                    .in(Product::getProductId, productIds));
            Map<Integer, Product> productMap = products.stream()
                    .collect(Collectors.toMap(Product::getProductId, product -> product));

            records.forEach(comment -> {
                if (productMap.containsKey(comment.getProductId())) {
                    comment.setProduct(productMap.get(comment.getProductId()));
                }
            });
        }

        return commentPage;
    }

    @Override
    public IPage<Comment> getUserCommentPage(Integer userId, Integer page, Integer size, String sort, String order) {
        Page<Comment> pageParam = new Page<>(page, size);

        // 创建查询条件
        LambdaQueryWrapper<Comment> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Comment::getUserId, userId);

        // 处理排序
        if ("rating".equals(sort)) {
            if ("asc".equals(order)) {
                queryWrapper.orderByAsc(Comment::getRating);
            } else {
                queryWrapper.orderByDesc(Comment::getRating);
            }
        } else if ("createTime".equals(sort)) {
            if ("asc".equals(order)) {
                queryWrapper.orderByAsc(Comment::getCreateTime);
            } else {
                queryWrapper.orderByDesc(Comment::getCreateTime);
            }
        }

        // 执行查询
        IPage<Comment> commentPage = this.page(pageParam, queryWrapper);

        // 填充商品信息
        List<Comment> records = commentPage.getRecords();
        if (!records.isEmpty()) {
            List<Integer> productIds = records.stream()
                    .map(Comment::getProductId)
                    .collect(Collectors.toList());

            List<Product> products = productMapper.selectList(new LambdaQueryWrapper<Product>()
                    .in(Product::getProductId, productIds));
            Map<Integer, Product> productMap = products.stream()
                    .collect(Collectors.toMap(Product::getProductId, product -> product));

            records.forEach(comment -> {
                if (productMap.containsKey(comment.getProductId())) {
                    comment.setProduct(productMap.get(comment.getProductId()));
                }
            });
        }

        return commentPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateCommentStatus(Integer commentId, Integer status) {
        LambdaUpdateWrapper<Comment> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Comment::getCommentId, commentId)
                .set(Comment::getStatus, status);

        return this.update(updateWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteComment(Integer commentId) {
        // 删除评价前，先删除关联的标签
        removeAllCommentTags(commentId);

        return this.removeById(commentId);
    }

    @Override
    public Map<String, Object> getProductRatingStats(Integer productId) {
        Map<String, Object> result = new HashMap<>();

        // 获取平均评分
        Double avgRating = baseMapper.getProductAverageRating(productId);
        result.put("averageRating", avgRating);

        // 获取评分分布
        List<Map<String, Object>> distribution = baseMapper.getRatingDistribution(productId);

        // 初始化所有评分等级的计数
        Map<Integer, Integer> ratingDistribution = new HashMap<>();
        for (int i = 1; i <= 5; i++) {
            ratingDistribution.put(i, 0);
        }

        // 填充实际评分分布
        if (distribution != null) {
            for (Map<String, Object> rating : distribution) {
                int ratingValue = Integer.valueOf(rating.get("rating").toString());
                int count = Integer.valueOf(rating.get("count").toString());
                ratingDistribution.put(ratingValue, count);
            }
        }

        result.put("ratingDistribution", ratingDistribution);

        // 获取总评价数
        LambdaQueryWrapper<Comment> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Comment::getProductId, productId)
                .eq(Comment::getStatus, 1);
        long totalCount = this.count(queryWrapper);
        result.put("totalCount", totalCount);

        return result;
    }

    @Override
    public IPage<Comment> adminGetCommentPage(Integer page, Integer size, Integer productId,
            Integer userId, Integer minRating, Integer maxRating, Integer status, Integer orderId) {
        Page<Comment> pageParam = new Page<>(page, size);

        LambdaQueryWrapper<Comment> queryWrapper = new LambdaQueryWrapper<>();

        // 添加筛选条件
        if (productId != null) {
            queryWrapper.eq(Comment::getProductId, productId);
        }

        if (userId != null) {
            queryWrapper.eq(Comment::getUserId, userId);
        }

        if (orderId != null) {
            queryWrapper.eq(Comment::getOrderId, orderId);
        }

        if (minRating != null) {
            queryWrapper.ge(Comment::getRating, minRating);
        }

        if (maxRating != null) {
            queryWrapper.le(Comment::getRating, maxRating);
        }

        if (status != null) {
            queryWrapper.eq(Comment::getStatus, status);
        }

        queryWrapper.orderByDesc(Comment::getCreateTime);

        IPage<Comment> commentPage = this.page(pageParam, queryWrapper);

        // 填充用户信息和商品信息
        List<Comment> records = commentPage.getRecords();
        if (!records.isEmpty()) {
            // 获取用户ID列表
            List<Integer> userIds = records.stream()
                    .map(Comment::getUserId)
                    .distinct()
                    .collect(Collectors.toList());

            // 获取商品ID列表
            List<Integer> productIds = records.stream()
                    .map(Comment::getProductId)
                    .distinct()
                    .collect(Collectors.toList());

            // 批量查询用户和商品
            List<User> users = userMapper.selectList(new LambdaQueryWrapper<User>()
                    .in(User::getUserId, userIds));
            List<Product> products = productMapper.selectList(new LambdaQueryWrapper<Product>()
                    .in(Product::getProductId, productIds));

            // 创建映射
            Map<Integer, User> userMap = users.stream()
                    .collect(Collectors.toMap(User::getUserId, user -> user));

            Map<Integer, Product> productMap = products.stream()
                    .collect(Collectors.toMap(Product::getProductId, product -> product));

            // 填充关联数据
            records.forEach(comment -> {
                if (userMap.containsKey(comment.getUserId())) {
                    comment.setUser(userMap.get(comment.getUserId()));
                }

                if (productMap.containsKey(comment.getProductId())) {
                    comment.setProduct(productMap.get(comment.getProductId()));
                }
            });
        }

        return commentPage;
    }

    @Override
    public Map<String, Object> getCommentStats(Integer days) {
        Map<String, Object> result = new HashMap<>();

        // 设置开始日期，如果days=999，则获取所有数据
        LocalDateTime startDate;
        if (days == 999) {
            // 设置一个很早的日期，例如5年前
            startDate = LocalDate.now().minusYears(5).atStartOfDay();
        } else {
            startDate = LocalDate.now().minusDays(days).atStartOfDay();
        }

        // 初始化日期数据
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd");
        List<String> dateLabels = new ArrayList<>();
        for (int i = days - 1; i >= 0; i--) {
            dateLabels.add(LocalDate.now().minusDays(i).format(formatter));
        }
        result.put("dateLabels", dateLabels);

        // 查询评价数据
        LambdaQueryWrapper<Comment> recentQueryWrapper = new LambdaQueryWrapper<>();
        // 如果days=999，获取所有评价，否则按时间筛选
        if (days != 999) {
            recentQueryWrapper.ge(Comment::getCreateTime, startDate);
        }
        List<Comment> recentComments = this.list(recentQueryWrapper);

        // 查询全部评价数据（用于统计总数）
        Integer totalCommentsCount = Math.toIntExact(this.count());
        result.put("totalComments", totalCommentsCount);

        // 统计有图片的评价数量
        LambdaQueryWrapper<Comment> withImagesQuery = new LambdaQueryWrapper<>();
        withImagesQuery.isNotNull(Comment::getImages)
                .ne(Comment::getImages, "");
        Integer commentsWithImages = Math.toIntExact(this.count(withImagesQuery));
        result.put("commentWithImages", commentsWithImages);

        // 统计匿名评价数量
        LambdaQueryWrapper<Comment> anonymousQuery = new LambdaQueryWrapper<>();
        anonymousQuery.eq(Comment::getIsAnonymous, 1);
        Integer anonymousComments = Math.toIntExact(this.count(anonymousQuery));
        result.put("anonymousComments", anonymousComments);

        // 按日期和评分统计
        Map<String, Map<Integer, Integer>> dailyRatingCounts = new HashMap<>();
        Map<Integer, Integer> totalRatingCounts = new HashMap<>();

        // 初始化
        for (String dateLabel : dateLabels) {
            dailyRatingCounts.put(dateLabel, new HashMap<>());
            for (int rating = 1; rating <= 5; rating++) {
                dailyRatingCounts.get(dateLabel).put(rating, 0);
            }
        }

        for (int rating = 1; rating <= 5; rating++) {
            totalRatingCounts.put(rating, 0);
        }

        // 统计每日评分数据
        for (Comment comment : recentComments) {
            // 对于所有评论，更新评分分布
            int rating = comment.getRating();
            totalRatingCounts.put(rating, totalRatingCounts.getOrDefault(rating, 0) + 1);

            // 只处理在日期范围内的评价用于趋势图
            LocalDate commentDate = comment.getCreateTime().toLocalDate();
            LocalDate startDay = LocalDate.now().minusDays(days - 1);
            if (!commentDate.isBefore(startDay)) {
                String dateKey = comment.getCreateTime().format(formatter);

                // 更新日评分数据
                if (dailyRatingCounts.containsKey(dateKey)) {
                    Map<Integer, Integer> dailyCount = dailyRatingCounts.get(dateKey);
                    dailyCount.put(rating, dailyCount.getOrDefault(rating, 0) + 1);
                }
            }
        }

        // 获取所有评价的评分分布
        Map<Integer, Integer> fullRatingCounts;
        if (days == 999) {
            // 如果是全部数据模式，直接使用上面统计的结果
            fullRatingCounts = totalRatingCounts;
        } else {
            // 否则查询数据库获取所有评价的评分分布
            List<Map<String, Object>> allRatingDistribution = baseMapper.getRatingDistribution(null);
            fullRatingCounts = new HashMap<>();

            // 初始化所有评分等级
            for (int rating = 1; rating <= 5; rating++) {
                fullRatingCounts.put(rating, 0);
            }

            // 填充实际评分分布
            if (allRatingDistribution != null) {
                for (Map<String, Object> ratingMap : allRatingDistribution) {
                    int ratingValue = Integer.valueOf(ratingMap.get("rating").toString());
                    int count = Integer.valueOf(ratingMap.get("count").toString());
                    fullRatingCounts.put(ratingValue, count);
                }
            }
        }

        result.put("totalRatingCounts", fullRatingCounts);

        // 转换为图表数据格式
        List<Map<String, Object>> dailyData = new ArrayList<>();
        for (int rating = 1; rating <= 5; rating++) {
            Map<String, Object> seriesData = new HashMap<>();
            seriesData.put("name", rating + "星");

            List<Integer> data = new ArrayList<>();
            for (String dateLabel : dateLabels) {
                data.add(dailyRatingCounts.get(dateLabel).get(rating));
            }

            seriesData.put("data", data);
            dailyData.add(seriesData);
        }

        result.put("dailyRatingData", dailyData);

        // 计算平均评分（基于所有评价）
        double totalScore = 0;
        int totalCount = 0;
        for (int rating = 1; rating <= 5; rating++) {
            totalScore += rating * fullRatingCounts.get(rating);
            totalCount += fullRatingCounts.get(rating);
        }

        double averageRating = totalCount > 0 ? totalScore / totalCount : 0;
        result.put("averageRating", averageRating);

        return result;
    }

    @Override
    public IPage<Comment> searchUserCommentPage(Integer userId, Integer page, Integer size,
            String sort, String order, String keyword, String ratingFilter) {
        Page<Comment> pageParam = new Page<>(page, size);

        // 创建查询条件
        LambdaQueryWrapper<Comment> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Comment::getUserId, userId);

        // 处理评分筛选
        if (ratingFilter != null && !ratingFilter.isEmpty()) {
            switch (ratingFilter) {
                case "good":
                    queryWrapper.ge(Comment::getRating, 4);
                    break;
                case "neutral":
                    queryWrapper.eq(Comment::getRating, 3);
                    break;
                case "bad":
                    queryWrapper.lt(Comment::getRating, 3);
                    break;
            }
        }

        // 执行查询
        IPage<Comment> commentPage = this.page(pageParam, queryWrapper);

        // 填充商品信息
        List<Comment> records = commentPage.getRecords();
        if (!records.isEmpty()) {
            List<Integer> productIds = records.stream()
                    .map(Comment::getProductId)
                    .collect(Collectors.toList());

            List<Product> products = productMapper.selectList(new LambdaQueryWrapper<Product>()
                    .in(Product::getProductId, productIds));
            Map<Integer, Product> productMap = products.stream()
                    .collect(Collectors.toMap(Product::getProductId, product -> product));

            records.forEach(comment -> {
                if (productMap.containsKey(comment.getProductId())) {
                    comment.setProduct(productMap.get(comment.getProductId()));
                }
            });

            // 处理关键词搜索（在内存中过滤，因为需要搜索关联的商品信息）
            if (keyword != null && !keyword.isEmpty()) {
                String lowerKeyword = keyword.toLowerCase();
                records.removeIf(comment -> {
                    String productName = comment.getProduct() != null ? comment.getProduct().getProductName() : "";
                    String content = comment.getContent() != null ? comment.getContent() : "";
                    return !productName.toLowerCase().contains(lowerKeyword) &&
                            !content.toLowerCase().contains(lowerKeyword);
                });

                // 更新总记录数
                commentPage.setTotal(records.size());
            }
        }

        // 处理排序
        if ("rating".equals(sort)) {
            if ("asc".equals(order)) {
                records.sort(Comparator.comparing(Comment::getRating));
            } else {
                records.sort(Comparator.comparing(Comment::getRating).reversed());
            }
        } else if ("createTime".equals(sort)) {
            if ("asc".equals(order)) {
                records.sort(Comparator.comparing(Comment::getCreateTime));
            } else {
                records.sort(Comparator.comparing(Comment::getCreateTime).reversed());
            }
        }

        return commentPage;
    }

    @Override
    public IPage<Comment> getUserCommentsByTag(Integer userId, Integer tagId, Integer page, Integer size, String sort,
            String order) {
        if (userId == null || tagId == null) {
            throw new BusinessException("用户ID和标签ID不能为空");
        }

        // 获取带有指定标签的评价ID列表
        List<Integer> commentIds = commentTagRelationMapper.getCommentIdsByTagId(tagId);

        if (CollectionUtils.isEmpty(commentIds)) {
            // 如果没有找到评价，返回空页
            return new Page<>(page, size);
        }

        // 创建查询条件
        LambdaQueryWrapper<Comment> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Comment::getUserId, userId)
                .in(Comment::getCommentId, commentIds);

        // 处理排序
        if ("rating".equals(sort)) {
            if ("asc".equals(order)) {
                queryWrapper.orderByAsc(Comment::getRating);
            } else {
                queryWrapper.orderByDesc(Comment::getRating);
            }
        } else if ("createTime".equals(sort)) {
            if ("asc".equals(order)) {
                queryWrapper.orderByAsc(Comment::getCreateTime);
            } else {
                queryWrapper.orderByDesc(Comment::getCreateTime);
            }
        }

        // 执行查询
        Page<Comment> pageParam = new Page<>(page, size);
        IPage<Comment> commentPage = this.page(pageParam, queryWrapper);

        // 填充商品信息
        fillProductInfo(commentPage.getRecords());

        return commentPage;
    }

    @Override
    public List<CommentTag> getCommentTags(Integer commentId) {
        if (commentId == null) {
            return new ArrayList<>();
        }
        return commentTagRelationMapper.getCommentTags(commentId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addCommentTags(Integer commentId, List<Integer> tagIds) {
        if (commentId == null || CollectionUtils.isEmpty(tagIds)) {
            return false;
        }

        // 检查评价是否存在
        Comment comment = this.getById(commentId);
        if (comment == null) {
            throw new BusinessException("评价不存在");
        }

        try {
            // 批量插入标签关联
            commentTagRelationMapper.batchInsert(commentId, tagIds);

            // 更新标签使用次数
            for (Integer tagId : tagIds) {
                commentTagService.incrementUsageCount(tagId);
            }

            return true;
        } catch (Exception e) {
            log.error("添加评价标签失败", e);
            throw new BusinessException("添加评价标签失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateCommentTags(Integer commentId, List<Integer> tagIds) {
        if (commentId == null) {
            return false;
        }

        // 检查评价是否存在
        Comment comment = this.getById(commentId);
        if (comment == null) {
            throw new BusinessException("评价不存在");
        }

        try {
            // 获取当前标签
            List<CommentTag> currentTags = getCommentTags(commentId);
            List<Integer> currentTagIds = currentTags.stream()
                    .map(CommentTag::getTagId)
                    .collect(Collectors.toList());

            // 删除所有现有标签关联
            removeAllCommentTags(commentId);

            // 如果有新标签，添加新的标签关联
            if (!CollectionUtils.isEmpty(tagIds)) {
                addCommentTags(commentId, tagIds);
            }

            return true;
        } catch (Exception e) {
            log.error("更新评价标签失败", e);
            throw new BusinessException("更新评价标签失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeCommentTag(Integer commentId, Integer tagId) {
        if (commentId == null || tagId == null) {
            return false;
        }

        try {
            // 删除标签关联
            int result = commentTagRelationMapper.deleteByCommentIdAndTagId(commentId, tagId);

            // 减少标签使用次数
            if (result > 0) {
                commentTagService.decrementUsageCount(tagId);
            }

            return result > 0;
        } catch (Exception e) {
            log.error("删除评价标签失败", e);
            throw new BusinessException("删除评价标签失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeAllCommentTags(Integer commentId) {
        if (commentId == null) {
            return false;
        }

        try {
            // 获取当前标签
            List<CommentTag> currentTags = getCommentTags(commentId);

            // 删除所有标签关联
            int result = commentTagRelationMapper.deleteByCommentId(commentId);

            // 减少标签使用次数
            for (CommentTag tag : currentTags) {
                commentTagService.decrementUsageCount(tag.getTagId());
            }

            return result > 0;
        } catch (Exception e) {
            log.error("删除评价所有标签失败", e);
            throw new BusinessException("删除评价所有标签失败");
        }
    }

    /**
     * 填充商品信息
     *
     * @param comments 评价列表
     */
    private void fillProductInfo(List<Comment> comments) {
        if (CollectionUtils.isEmpty(comments)) {
            return;
        }

        List<Integer> productIds = comments.stream()
                .map(Comment::getProductId)
                .collect(Collectors.toList());

        List<Product> products = productMapper.selectList(new LambdaQueryWrapper<Product>()
                .in(Product::getProductId, productIds));
        Map<Integer, Product> productMap = products.stream()
                .collect(Collectors.toMap(Product::getProductId, product -> product));

        comments.forEach(comment -> {
            if (productMap.containsKey(comment.getProductId())) {
                comment.setProduct(productMap.get(comment.getProductId()));
            }
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> createCommentWithReward(Comment comment) {
        Map<String, Object> result = new HashMap<>();

        // 创建评价
        boolean success = this.createComment(comment);
        result.put("success", success);
        result.put("commentId", comment.getCommentId());

        if (success) {
            // 计算并发放奖励
            Map<String, Object> rewardResult = commentRewardConfigService.grantReward(comment);
            result.put("reward", rewardResult);
        }

        return result;
    }

    @Override
    public IPage<Order> getUnratedOrders(Integer userId, int page, int size) {
        Page<Order> pageParam = new Page<>(page, size);

        // 查询用户已完成但未评价的订单
        // 优化：只查询最近30天内的订单，按完成时间倒序排序
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);

        LambdaQueryWrapper<Order> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Order::getUserId, userId)
                .eq(Order::getStatus, OrderStatus.COMPLETED)
                .eq(Order::getIsCommented, 0)
                .ge(Order::getCompletionTime, thirtyDaysAgo)
                .orderByDesc(Order::getCompletionTime);

        IPage<Order> orderPage = orderMapper.selectPage(pageParam, queryWrapper);

        // 填充订单商品信息，以便在提醒中显示
        if (orderPage.getRecords() != null && !orderPage.getRecords().isEmpty()) {
            List<Integer> orderIds = orderPage.getRecords().stream()
                    .map(Order::getOrderId)
                    .collect(Collectors.toList());

            // 查询订单商品信息
            LambdaQueryWrapper<OrderProduct> productQueryWrapper = new LambdaQueryWrapper<>();
            productQueryWrapper.in(OrderProduct::getOrderId, orderIds);
            List<OrderProduct> allOrderProducts = orderProductMapper.selectList(productQueryWrapper);

            // 按订单ID分组
            Map<Integer, List<OrderProduct>> orderProductMap = allOrderProducts.stream()
                    .collect(Collectors.groupingBy(OrderProduct::getOrderId));

            // 为每个订单设置商品信息
            for (Order order : orderPage.getRecords()) {
                order.setProducts(orderProductMap.getOrDefault(order.getOrderId(), new ArrayList<>()));
            }
        }

        return orderPage;
    }

    @Override
    public Map<String, Object> getUserCommentStats(Integer userId) {
        Map<String, Object> result = new HashMap<>();

        // 查询用户评价总数
        LambdaQueryWrapper<Comment> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Comment::getUserId, userId);
        long totalCount = this.count(queryWrapper);
        result.put("totalCount", totalCount);

        if (totalCount == 0) {
            result.put("averageRating", 0);
            result.put("ratingDistribution", new HashMap<>());
            return result;
        }

        // 计算平均评分
        // 使用自定义查询计算平均评分
        LambdaQueryWrapper<Comment> ratingQuery = new LambdaQueryWrapper<>();
        ratingQuery.eq(Comment::getUserId, userId)
                .select(Comment::getRating);
        List<Object> ratingList = baseMapper.selectObjs(ratingQuery);

        double sum = 0;
        for (Object rating : ratingList) {
            if (rating instanceof Integer) {
                sum += (Integer) rating;
            }
        }
        double averageRating = ratingList.isEmpty() ? 0 : sum / ratingList.size();
        result.put("averageRating", averageRating);

        // 获取评分分布
        Map<Integer, Long> ratingDistribution = new HashMap<>();
        for (int i = 1; i <= 5; i++) {
            long count = count(new LambdaQueryWrapper<Comment>()
                    .eq(Comment::getUserId, userId)
                    .eq(Comment::getRating, i));
            ratingDistribution.put(i, count);
        }
        result.put("ratingDistribution", ratingDistribution);

        // 计算好评率（4-5星）
        long goodRatingCount = ratingDistribution.getOrDefault(4, 0L) + ratingDistribution.getOrDefault(5, 0L);
        double goodRatingPercentage = totalCount > 0 ? (double) goodRatingCount / totalCount * 100 : 0;
        result.put("goodRatingPercentage", goodRatingPercentage);

        return result;
    }

    @Override
    public Map<String, Object> getUserCommentTrend(Integer userId, int days) {
        Map<String, Object> result = new HashMap<>();

        // 获取指定天数内的评价趋势
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusDays(days);

        // 查询日期范围内的评价
        LambdaQueryWrapper<Comment> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Comment::getUserId, userId)
                .between(Comment::getCreateTime, startDate, endDate);
        List<Comment> comments = this.list(queryWrapper);

        // 构建日期和评价数量的映射
        Map<String, Integer> dateCountMap = new HashMap<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        // 初始化所有日期的评价数量为0
        for (int i = 0; i < days; i++) {
            String date = endDate.minusDays(i).format(formatter);
            dateCountMap.put(date, 0);
        }

        // 填充实际的评价数量
        for (Comment comment : comments) {
            String date = comment.getCreateTime().format(formatter);
            dateCountMap.put(date, dateCountMap.getOrDefault(date, 0) + 1);
        }

        // 转换为前端所需的格式
        List<String> dateList = new ArrayList<>(dateCountMap.keySet());
        Collections.sort(dateList);

        List<Integer> countList = new ArrayList<>();
        for (String date : dateList) {
            countList.add(dateCountMap.get(date));
        }

        result.put("dates", dateList);
        result.put("counts", countList);

        return result;
    }

    @Override
    public Map<String, Object> getCommentKeywords(Integer productId, Integer minRating, Integer maxRating,
            Integer limit) {
        Map<String, Object> result = new HashMap<>();

        // 构建查询条件
        LambdaQueryWrapper<Comment> queryWrapper = new LambdaQueryWrapper<>();

        // 添加筛选条件
        if (productId != null) {
            queryWrapper.eq(Comment::getProductId, productId);
        }

        if (minRating != null) {
            queryWrapper.ge(Comment::getRating, minRating);
        }

        if (maxRating != null) {
            queryWrapper.le(Comment::getRating, maxRating);
        }

        // 只查询显示状态的评价
        queryWrapper.eq(Comment::getStatus, 1);

        // 查询评价内容
        List<Comment> comments = this.list(queryWrapper);

        // 提取评价内容
        List<String> contents = comments.stream()
                .map(Comment::getContent)
                .filter(StringUtils::hasText)
                .collect(Collectors.toList());

        // 分词并统计词频
        Map<String, Integer> wordFrequency = new HashMap<>();

        // 简单分词处理（实际项目中可以使用专业的分词库，如HanLP、IK等）
        for (String content : contents) {
            // 简单分词：按空格、标点符号分割
            String[] words = content.replaceAll("[\\p{P}\\p{Z}]", " ").split("\\s+");

            for (String word : words) {
                if (word.length() > 1) { // 忽略单字符词
                    wordFrequency.put(word, wordFrequency.getOrDefault(word, 0) + 1);
                }
            }
        }

        // 排序并限制数量
        List<Map.Entry<String, Integer>> sortedWords = wordFrequency.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(limit != null ? limit : 20)
                .collect(Collectors.toList());

        // 构建返回结果
        List<Map<String, Object>> keywords = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : sortedWords) {
            Map<String, Object> keyword = new HashMap<>();
            keyword.put("name", entry.getKey());
            keyword.put("value", entry.getValue());
            keywords.add(keyword);
        }

        result.put("keywords", keywords);
        result.put("total", comments.size());

        return result;
    }

    @Override
    public Map<String, Object> getCommentSentimentAnalysis(Integer productId, Integer days) {
        Map<String, Object> result = new HashMap<>();

        // 设置开始日期
        LocalDateTime startDate = LocalDate.now().minusDays(days).atStartOfDay();

        // 构建查询条件
        LambdaQueryWrapper<Comment> queryWrapper = new LambdaQueryWrapper<>();

        // 添加筛选条件
        if (productId != null) {
            queryWrapper.eq(Comment::getProductId, productId);
        }

        queryWrapper.ge(Comment::getCreateTime, startDate)
                .eq(Comment::getStatus, 1);

        // 查询评价
        List<Comment> comments = this.list(queryWrapper);

        // 按评分分类统计
        int positiveCount = 0; // 4-5星
        int neutralCount = 0; // 3星
        int negativeCount = 0; // 1-2星

        // 按日期和情感分类统计
        Map<String, int[]> dailySentiment = new HashMap<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        // 初始化日期数据
        for (int i = 0; i < days; i++) {
            String date = LocalDate.now().minusDays(i).format(formatter);
            dailySentiment.put(date, new int[3]); // [positive, neutral, negative]
        }

        // 统计情感分布
        for (Comment comment : comments) {
            int rating = comment.getRating();

            // 全局统计
            if (rating >= 4) {
                positiveCount++;
            } else if (rating == 3) {
                neutralCount++;
            } else {
                negativeCount++;
            }

            // 按日期统计
            String date = comment.getCreateTime().format(formatter);
            if (dailySentiment.containsKey(date)) {
                int[] counts = dailySentiment.get(date);
                if (rating >= 4) {
                    counts[0]++;
                } else if (rating == 3) {
                    counts[1]++;
                } else {
                    counts[2]++;
                }
            }
        }

        // 计算情感比例
        int totalCount = positiveCount + neutralCount + negativeCount;
        double positiveRate = totalCount > 0 ? (double) positiveCount / totalCount * 100 : 0;
        double neutralRate = totalCount > 0 ? (double) neutralCount / totalCount * 100 : 0;
        double negativeRate = totalCount > 0 ? (double) negativeCount / totalCount * 100 : 0;

        // 构建情感趋势数据
        List<String> dateLabels = new ArrayList<>();
        List<Integer> positiveData = new ArrayList<>();
        List<Integer> neutralData = new ArrayList<>();
        List<Integer> negativeData = new ArrayList<>();

        // 按日期排序
        List<String> sortedDates = new ArrayList<>(dailySentiment.keySet());
        Collections.sort(sortedDates);

        for (String date : sortedDates) {
            int[] counts = dailySentiment.get(date);
            dateLabels.add(date);
            positiveData.add(counts[0]);
            neutralData.add(counts[1]);
            negativeData.add(counts[2]);
        }

        // 构建返回结果
        result.put("totalCount", totalCount);
        result.put("positiveCount", positiveCount);
        result.put("neutralCount", neutralCount);
        result.put("negativeCount", negativeCount);
        result.put("positiveRate", positiveRate);
        result.put("neutralRate", neutralRate);
        result.put("negativeRate", negativeRate);

        result.put("dateLabels", dateLabels);
        result.put("positiveData", positiveData);
        result.put("neutralData", neutralData);
        result.put("negativeData", negativeData);

        return result;
    }
}