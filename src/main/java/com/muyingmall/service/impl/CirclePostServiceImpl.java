package com.muyingmall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.muyingmall.entity.CirclePost;
import com.muyingmall.entity.CircleTopic;
import com.muyingmall.entity.Product;
import com.muyingmall.entity.User;
import com.muyingmall.mapper.CirclePostMapper;
import com.muyingmall.mapper.CircleTopicMapper;
import com.muyingmall.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 育儿圈帖子服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CirclePostServiceImpl extends ServiceImpl<CirclePostMapper, CirclePost> implements CirclePostService {

    private final CircleTopicMapper topicMapper;
    private final UserService userService;
    private final ProductService productService;
    private final CircleLikeService likeService;
    private final CircleFollowService followService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CirclePost createPost(Integer userId, Integer topicId, String content, String images, Integer productId, Integer skuId, String skuSpecs) {
        CirclePost post = new CirclePost();
        post.setUserId(userId);
        post.setTopicId(topicId);
        post.setContent(content);
        post.setImages(images);
        post.setProductId(productId);
        post.setSkuId(skuId);
        post.setSkuSpecs(skuSpecs);
        post.setViewCount(0);
        post.setLikeCount(0);
        post.setCommentCount(0);
        post.setShareCount(0);
        post.setIsTop(0);
        post.setIsHot(0);
        post.setStatus(1);
        post.setCreateTime(java.time.LocalDateTime.now());
        save(post);
        
        // 更新话题帖子数
        if (topicId != null) {
            topicMapper.incrementPostCount(topicId);
        }
        
        // 填充关联信息
        post.setUser(userService.getById(userId));
        if (topicId != null) {
            post.setTopic(topicMapper.selectById(topicId));
        }
        return post;
    }

    @Override
    public CirclePost getPostDetail(Long postId, Integer currentUserId) {
        CirclePost post = getById(postId);
        if (post == null || post.getStatus() == 0) {
            return null;
        }
        fillPostInfo(Collections.singletonList(post), currentUserId);
        return post;
    }

    @Override
    public Page<CirclePost> getPostPage(int page, int size, Integer topicId, Integer userId, Integer currentUserId) {
        LambdaQueryWrapper<CirclePost> wrapper = new LambdaQueryWrapper<CirclePost>()
                .eq(CirclePost::getStatus, 1)
                .eq(topicId != null, CirclePost::getTopicId, topicId)
                .eq(userId != null, CirclePost::getUserId, userId)
                .orderByDesc(CirclePost::getIsTop)
                .orderByDesc(CirclePost::getCreateTime);
        
        Page<CirclePost> postPage = page(new Page<>(page, size), wrapper);
        fillPostInfo(postPage.getRecords(), currentUserId);
        return postPage;
    }

    @Override
    public Page<CirclePost> getFollowingPosts(Integer userId, int page, int size) {
        // 获取关注的用户ID
        Set<Integer> followingIds = followService.getFollowingUserIds(userId);
        if (followingIds.isEmpty()) {
            return new Page<>(page, size, 0);
        }
        
        Page<CirclePost> postPage = page(new Page<>(page, size),
                new LambdaQueryWrapper<CirclePost>()
                        .in(CirclePost::getUserId, followingIds)
                        .eq(CirclePost::getStatus, 1)
                        .orderByDesc(CirclePost::getCreateTime));
        
        fillPostInfo(postPage.getRecords(), userId);
        return postPage;
    }

    @Override
    public Page<CirclePost> getHotPosts(int page, int size, Integer currentUserId) {
        Page<CirclePost> postPage = page(new Page<>(page, size),
                new LambdaQueryWrapper<CirclePost>()
                        .eq(CirclePost::getStatus, 1)
                        .orderByDesc(CirclePost::getIsHot)
                        .orderByDesc(CirclePost::getLikeCount)
                        .orderByDesc(CirclePost::getCommentCount));
        
        fillPostInfo(postPage.getRecords(), currentUserId);
        return postPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deletePost(Long postId, Integer userId) {
        CirclePost post = getById(postId);
        if (post == null || !post.getUserId().equals(userId)) {
            return false;
        }
        // 软删除
        post.setStatus(0);
        updateById(post);
        // 更新话题帖子数
        if (post.getTopicId() != null) {
            topicMapper.decrementPostCount(post.getTopicId());
        }
        return true;
    }

    @Override
    public void incrementViewCount(Long postId) {
        baseMapper.incrementViewCount(postId);
    }

    /**
     * 填充帖子关联信息
     */
    private void fillPostInfo(List<CirclePost> posts, Integer currentUserId) {
        if (posts.isEmpty()) return;
        
        // 收集ID
        Set<Integer> userIds = posts.stream().map(CirclePost::getUserId).collect(Collectors.toSet());
        Set<Integer> topicIds = posts.stream().map(CirclePost::getTopicId).filter(Objects::nonNull).collect(Collectors.toSet());
        Set<Integer> productIds = posts.stream().map(CirclePost::getProductId).filter(Objects::nonNull).collect(Collectors.toSet());
        List<Long> postIds = posts.stream().map(CirclePost::getPostId).collect(Collectors.toList());
        
        // 批量查询
        Map<Integer, User> userMap = userService.listByIds(userIds).stream()
                .collect(Collectors.toMap(User::getUserId, u -> u));
        Map<Integer, CircleTopic> topicMap = topicIds.isEmpty() ? new HashMap<>() :
                topicMapper.selectBatchIds(topicIds).stream()
                        .collect(Collectors.toMap(CircleTopic::getTopicId, t -> t));
        Map<Integer, Product> productMap = productIds.isEmpty() ? new HashMap<>() :
                productService.listByIds(productIds).stream()
                        .collect(Collectors.toMap(Product::getProductId, p -> p));
        Set<Long> likedIds = likeService.getLikedPostIds(currentUserId, postIds);
        
        // 填充
        for (CirclePost post : posts) {
            post.setUser(userMap.get(post.getUserId()));
            post.setTopic(topicMap.get(post.getTopicId()));
            post.setProduct(productMap.get(post.getProductId()));
            post.setIsLiked(likedIds.contains(post.getPostId()));
        }
    }

    // ==================== 后台管理接口实现 ====================

    @Override
    public Page<CirclePost> getAdminPostPage(int page, int size, Integer topicId, Integer userId,
                                              Integer status, Integer isTop, Integer isHot, String keyword) {
        LambdaQueryWrapper<CirclePost> wrapper = new LambdaQueryWrapper<CirclePost>()
                .eq(topicId != null, CirclePost::getTopicId, topicId)
                .eq(userId != null, CirclePost::getUserId, userId)
                .eq(status != null, CirclePost::getStatus, status)
                .eq(isTop != null, CirclePost::getIsTop, isTop)
                .eq(isHot != null, CirclePost::getIsHot, isHot)
                .like(keyword != null && !keyword.isEmpty(), CirclePost::getContent, keyword)
                .orderByDesc(CirclePost::getCreateTime);
        
        Page<CirclePost> postPage = page(new Page<>(page, size), wrapper);
        fillPostInfo(postPage.getRecords(), null);
        return postPage;
    }

    @Override
    public long countByStatus(Integer status) {
        return count(new LambdaQueryWrapper<CirclePost>().eq(CirclePost::getStatus, status));
    }

    @Override
    public long countTopPosts() {
        return count(new LambdaQueryWrapper<CirclePost>()
                .eq(CirclePost::getIsTop, 1)
                .eq(CirclePost::getStatus, 1));
    }

    @Override
    public long countHotPosts() {
        return count(new LambdaQueryWrapper<CirclePost>()
                .eq(CirclePost::getIsHot, 1)
                .eq(CirclePost::getStatus, 1));
    }

    @Override
    public long countTodayPosts() {
        java.time.LocalDateTime today = java.time.LocalDate.now().atStartOfDay();
        return count(new LambdaQueryWrapper<CirclePost>()
                .ge(CirclePost::getCreateTime, today)
                .eq(CirclePost::getStatus, 1));
    }

    @Override
    public boolean updatePostStatus(Long postId, Integer status) {
        CirclePost post = new CirclePost();
        post.setPostId(postId);
        post.setStatus(status);
        return updateById(post);
    }

    @Override
    public boolean updatePostTop(Long postId, Integer isTop) {
        CirclePost post = new CirclePost();
        post.setPostId(postId);
        post.setIsTop(isTop);
        return updateById(post);
    }

    @Override
    public boolean updatePostHot(Long postId, Integer isHot) {
        CirclePost post = new CirclePost();
        post.setPostId(postId);
        post.setIsHot(isHot);
        return updateById(post);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean adminDeletePost(Long postId) {
        CirclePost post = getById(postId);
        if (post == null) {
            return false;
        }
        // 软删除
        post.setStatus(0);
        updateById(post);
        // 更新话题帖子数
        if (post.getTopicId() != null) {
            topicMapper.decrementPostCount(post.getTopicId());
        }
        return true;
    }
}
