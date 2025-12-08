package com.muyingmall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.muyingmall.entity.CircleFollow;
import com.muyingmall.entity.User;
import com.muyingmall.mapper.CircleFollowMapper;
import com.muyingmall.service.CircleFollowService;
import com.muyingmall.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 育儿圈关注服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CircleFollowServiceImpl extends ServiceImpl<CircleFollowMapper, CircleFollow> implements CircleFollowService {

    private final UserService userService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean followUser(Integer userId, Integer followUserId) {
        // 不能关注自己
        if (userId.equals(followUserId)) {
            return false;
        }
        // 检查是否已关注
        if (isFollowing(userId, followUserId)) {
            return true;
        }
        CircleFollow follow = new CircleFollow();
        follow.setUserId(userId);
        follow.setFollowUserId(followUserId);
        return save(follow);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean unfollowUser(Integer userId, Integer followUserId) {
        return remove(new LambdaQueryWrapper<CircleFollow>()
                .eq(CircleFollow::getUserId, userId)
                .eq(CircleFollow::getFollowUserId, followUserId));
    }

    @Override
    public boolean isFollowing(Integer userId, Integer followUserId) {
        if (userId == null || followUserId == null) return false;
        return count(new LambdaQueryWrapper<CircleFollow>()
                .eq(CircleFollow::getUserId, userId)
                .eq(CircleFollow::getFollowUserId, followUserId)) > 0;
    }

    @Override
    public Page<User> getFollowingList(Integer userId, int page, int size) {
        // 获取关注的用户ID列表
        Page<CircleFollow> followPage = page(new Page<>(page, size),
                new LambdaQueryWrapper<CircleFollow>()
                        .eq(CircleFollow::getUserId, userId)
                        .orderByDesc(CircleFollow::getCreateTime));
        
        List<Integer> userIds = followPage.getRecords().stream()
                .map(CircleFollow::getFollowUserId)
                .collect(Collectors.toList());
        
        Page<User> userPage = new Page<>(page, size, followPage.getTotal());
        if (!userIds.isEmpty()) {
            userPage.setRecords(userService.listByIds(userIds));
        }
        return userPage;
    }

    @Override
    public Page<User> getFollowerList(Integer userId, int page, int size) {
        // 获取粉丝的用户ID列表
        Page<CircleFollow> followPage = page(new Page<>(page, size),
                new LambdaQueryWrapper<CircleFollow>()
                        .eq(CircleFollow::getFollowUserId, userId)
                        .orderByDesc(CircleFollow::getCreateTime));
        
        List<Integer> userIds = followPage.getRecords().stream()
                .map(CircleFollow::getUserId)
                .collect(Collectors.toList());
        
        Page<User> userPage = new Page<>(page, size, followPage.getTotal());
        if (!userIds.isEmpty()) {
            userPage.setRecords(userService.listByIds(userIds));
        }
        return userPage;
    }

    @Override
    public int getFollowingCount(Integer userId) {
        return (int) count(new LambdaQueryWrapper<CircleFollow>()
                .eq(CircleFollow::getUserId, userId));
    }

    @Override
    public int getFollowerCount(Integer userId) {
        return (int) count(new LambdaQueryWrapper<CircleFollow>()
                .eq(CircleFollow::getFollowUserId, userId));
    }

    @Override
    public Set<Integer> getFollowingUserIds(Integer userId) {
        if (userId == null) return new HashSet<>();
        return list(new LambdaQueryWrapper<CircleFollow>()
                .eq(CircleFollow::getUserId, userId)
                .select(CircleFollow::getFollowUserId))
                .stream()
                .map(CircleFollow::getFollowUserId)
                .collect(Collectors.toSet());
    }
}
