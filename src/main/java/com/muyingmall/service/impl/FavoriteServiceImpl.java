package com.muyingmall.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.muyingmall.common.constants.CacheConstants;
import com.muyingmall.common.exception.BusinessException;
import com.muyingmall.entity.Favorite;
import com.muyingmall.entity.Product;
import com.muyingmall.mapper.FavoriteMapper;
import com.muyingmall.service.FavoriteService;
import com.muyingmall.service.ProductService;
import com.muyingmall.util.RedisUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * 收藏服务实现类
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FavoriteServiceImpl extends ServiceImpl<FavoriteMapper, Favorite> implements FavoriteService {

    private final ProductService productService;
    private final RedisUtil redisUtil;

    @Override
    @SuppressWarnings("unchecked")
    public Page<Favorite> getUserFavorites(Integer userId, int page, int pageSize) {
        // 构建缓存键
        String cacheKey = CacheConstants.USER_FAVORITE_LIST_KEY + userId + ":p" + page + "_s" + pageSize;
        
        // 尝试从缓存获取
        Object cached = redisUtil.get(cacheKey);
        if (cached != null) {
            log.debug("从缓存获取用户收藏列表: userId={}, page={}", userId, page);
            return (Page<Favorite>) cached;
        }
        
        // 缓存未命中，从数据库查询
        log.debug("缓存未命中，从数据库查询用户收藏列表: userId={}, page={}", userId, page);
        Page<Favorite> pageCond = new Page<>(page, pageSize);

        // 分页查询收藏记录
        Page<Favorite> favoritePage = lambdaQuery()
                .eq(Favorite::getUserId, userId)
                .orderByDesc(Favorite::getCreateTime)
                .page(pageCond);

        // 设置商品信息
        favoritePage.getRecords().forEach(favorite -> {
            Product product = productService.getById(favorite.getProductId());
            favorite.setProduct(product);
        });

        // 缓存结果
        if (favoritePage.getRecords() != null) {
            redisUtil.set(cacheKey, favoritePage, CacheConstants.FAVORITE_EXPIRE_TIME);
            log.debug("将用户收藏列表缓存到Redis: userId={}, page={}, 过期时间={}秒", userId, page, CacheConstants.FAVORITE_EXPIRE_TIME);
        }

        return favoritePage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Favorite addFavorite(Integer userId, Integer productId) {
        // 验证商品是否存在
        Product product = productService.getById(productId);
        if (product == null) {
            throw new BusinessException("商品不存在");
        }

        // 检查是否已收藏
        if (isFavorite(userId, productId)) {
            throw new BusinessException("已收藏该商品");
        }

        // 创建收藏记录
        Favorite favorite = new Favorite();
        favorite.setUserId(userId);
        favorite.setProductId(productId);
        favorite.setCreateTime(LocalDateTime.now());
        favorite.setUpdateTime(LocalDateTime.now());

        // 保存收藏
        save(favorite);

        // 设置商品信息
        favorite.setProduct(product);
        
        // 清除用户收藏缓存和收藏状态缓存
        clearUserFavoriteCache(userId);
        clearFavoriteStatusCache(userId, productId);

        return favorite;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeFavorite(Integer favoriteId) {
        // 验证收藏是否存在
        Favorite favorite = getById(favoriteId);
        if (favorite == null) {
            throw new BusinessException("收藏记录不存在");
        }

        // 删除收藏
        boolean result = removeById(favoriteId);
        
        // 清除用户收藏缓存和收藏状态缓存
        if (result) {
            clearUserFavoriteCache(favorite.getUserId());
            clearFavoriteStatusCache(favorite.getUserId(), favorite.getProductId());
        }
        
        return result;
    }

    @Override
    public boolean isFavorite(Integer userId, Integer productId) {
        // 构建缓存键
        String cacheKey = CacheConstants.FAVORITE_STATUS_KEY + userId + ":" + productId;
        
        // 尝试从缓存获取
        Object cached = redisUtil.get(cacheKey);
        if (cached != null) {
            log.debug("从缓存获取收藏状态: userId={}, productId={}", userId, productId);
            return (Boolean) cached;
        }
        
        // 缓存未命中，从数据库查询
        boolean isFavorite = lambdaQuery()
                .eq(Favorite::getUserId, userId)
                .eq(Favorite::getProductId, productId)
                .count() > 0;
        
        // 缓存结果
        redisUtil.set(cacheKey, isFavorite, CacheConstants.FAVORITE_EXPIRE_TIME);
        log.debug("将收藏状态缓存到Redis: userId={}, productId={}, isFavorite={}", userId, productId, isFavorite);
        
        return isFavorite;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean clearFavorites(Integer userId) {
        boolean result = lambdaUpdate()
                .eq(Favorite::getUserId, userId)
                .remove();
        
        // 清除用户所有收藏相关缓存
        if (result) {
            clearUserFavoriteCache(userId);
        }
        
        return result;
    }
    
    /**
     * 清除用户收藏列表缓存
     *
     * @param userId 用户ID
     */
    private void clearUserFavoriteCache(Integer userId) {
        if (userId == null) {
            return;
        }
        // 使用模式匹配删除所有分页缓存
        String pattern = CacheConstants.USER_FAVORITE_LIST_KEY + userId + ":*";
        Set<String> keys = redisUtil.keys(pattern);
        if (keys != null && !keys.isEmpty()) {
            redisUtil.del(keys);
        }
        log.debug("清除用户收藏列表缓存: userId={}", userId);
    }
    
    /**
     * 清除收藏状态缓存
     *
     * @param userId 用户ID
     * @param productId 商品ID
     */
    private void clearFavoriteStatusCache(Integer userId, Integer productId) {
        if (userId == null || productId == null) {
            return;
        }
        String cacheKey = CacheConstants.FAVORITE_STATUS_KEY + userId + ":" + productId;
        redisUtil.del(cacheKey);
        log.debug("清除收藏状态缓存: userId={}, productId={}", userId, productId);
    }
}