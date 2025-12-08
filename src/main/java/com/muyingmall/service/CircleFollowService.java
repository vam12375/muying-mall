package com.muyingmall.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.muyingmall.entity.CircleFollow;
import com.muyingmall.entity.User;

import java.util.List;
import java.util.Set;

/**
 * 育儿圈关注服务接口
 */
public interface CircleFollowService extends IService<CircleFollow> {

    /**
     * 关注用户
     */
    boolean followUser(Integer userId, Integer followUserId);

    /**
     * 取消关注
     */
    boolean unfollowUser(Integer userId, Integer followUserId);

    /**
     * 检查是否已关注
     */
    boolean isFollowing(Integer userId, Integer followUserId);

    /**
     * 获取关注列表
     */
    Page<User> getFollowingList(Integer userId, int page, int size);

    /**
     * 获取粉丝列表
     */
    Page<User> getFollowerList(Integer userId, int page, int size);

    /**
     * 获取关注数量
     */
    int getFollowingCount(Integer userId);

    /**
     * 获取粉丝数量
     */
    int getFollowerCount(Integer userId);

    /**
     * 获取用户关注的所有用户ID
     */
    Set<Integer> getFollowingUserIds(Integer userId);
}
