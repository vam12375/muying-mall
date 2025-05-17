package com.muyingmall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.muyingmall.common.exception.BusinessException;
import com.muyingmall.entity.Comment;
import com.muyingmall.entity.Order;
import com.muyingmall.entity.Product;
import com.muyingmall.entity.User;
import com.muyingmall.mapper.CommentMapper;
import com.muyingmall.mapper.OrderMapper;
import com.muyingmall.mapper.ProductMapper;
import com.muyingmall.mapper.UserMapper;
import com.muyingmall.service.CommentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
        return this.save(comment);
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
                List<User> users = userMapper.selectBatchIds(userIds);
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

            List<Product> products = productMapper.selectBatchIds(productIds);
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
            Integer userId, Integer minRating, Integer maxRating, Integer status) {
        Page<Comment> pageParam = new Page<>(page, size);

        LambdaQueryWrapper<Comment> queryWrapper = new LambdaQueryWrapper<>();

        // 添加筛选条件
        if (productId != null) {
            queryWrapper.eq(Comment::getProductId, productId);
        }

        if (userId != null) {
            queryWrapper.eq(Comment::getUserId, userId);
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
            List<User> users = userMapper.selectBatchIds(userIds);
            List<Product> products = productMapper.selectBatchIds(productIds);

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

        // 设置开始日期
        LocalDateTime startDate = LocalDate.now().minusDays(days).atStartOfDay();

        // 初始化日期数据
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd");
        List<String> dateLabels = new ArrayList<>();
        for (int i = days - 1; i >= 0; i--) {
            dateLabels.add(LocalDate.now().minusDays(i).format(formatter));
        }
        result.put("dateLabels", dateLabels);

        // 查询评价数据
        LambdaQueryWrapper<Comment> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.ge(Comment::getCreateTime, startDate);
        List<Comment> comments = this.list(queryWrapper);

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
        for (Comment comment : comments) {
            String dateKey = comment.getCreateTime().format(formatter);
            int rating = comment.getRating();

            // 更新日评分数据
            if (dailyRatingCounts.containsKey(dateKey)) {
                Map<Integer, Integer> dailyCount = dailyRatingCounts.get(dateKey);
                dailyCount.put(rating, dailyCount.getOrDefault(rating, 0) + 1);
            }

            // 更新总评分数据
            totalRatingCounts.put(rating, totalRatingCounts.getOrDefault(rating, 0) + 1);
        }

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
        result.put("totalRatingCounts", totalRatingCounts);

        // 统计总评价数
        result.put("totalComments", comments.size());

        // 计算平均评分
        double totalScore = 0;
        int totalCount = 0;
        for (int rating = 1; rating <= 5; rating++) {
            totalScore += rating * totalRatingCounts.get(rating);
            totalCount += totalRatingCounts.get(rating);
        }

        double averageRating = totalCount > 0 ? totalScore / totalCount : 0;
        result.put("averageRating", averageRating);

        return result;
    }
}