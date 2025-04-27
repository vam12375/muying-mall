package com.muyingmall.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.muyingmall.entity.Favorite;

import java.util.List;

/**
 * 收藏服务接口
 */
public interface FavoriteService extends IService<Favorite> {

    /**
     * 获取用户收藏列表
     *
     * @param userId   用户ID
     * @param page     页码
     * @param pageSize 每页条数
     * @return 收藏分页列表
     */
    Page<Favorite> getUserFavorites(Integer userId, int page, int pageSize);

    /**
     * 添加收藏
     *
     * @param userId    用户ID
     * @param productId 商品ID
     * @return 收藏对象
     */
    Favorite addFavorite(Integer userId, Integer productId);

    /**
     * 移除收藏
     *
     * @param favoriteId 收藏ID
     * @return 是否成功
     */
    boolean removeFavorite(Integer favoriteId);

    /**
     * 检查商品是否已收藏
     *
     * @param userId    用户ID
     * @param productId 商品ID
     * @return 是否已收藏
     */
    boolean isFavorite(Integer userId, Integer productId);

    /**
     * 清空收藏夹
     *
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean clearFavorites(Integer userId);
}