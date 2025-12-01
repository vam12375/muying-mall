package com.muyingmall.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.muyingmall.entity.UserSearchHistory;

import java.util.List;

/**
 * 用户搜索历史服务接口
 */
public interface UserSearchHistoryService extends IService<UserSearchHistory> {

    /**
     * 记录用户搜索历史
     * @param userId 用户ID
     * @param keyword 搜索关键词
     */
    void recordSearchHistory(Integer userId, String keyword);

    /**
     * 获取用户搜索历史
     * @param userId 用户ID
     * @param limit 返回数量
     * @return 搜索历史关键词列表
     */
    List<String> getUserSearchHistory(Integer userId, int limit);

    /**
     * 删除用户单条搜索历史
     * @param userId 用户ID
     * @param keyword 搜索关键词
     * @return 是否删除成功
     */
    boolean deleteSearchHistory(Integer userId, String keyword);

    /**
     * 清空用户搜索历史
     * @param userId 用户ID
     * @return 删除的记录数
     */
    int clearSearchHistory(Integer userId);
}
