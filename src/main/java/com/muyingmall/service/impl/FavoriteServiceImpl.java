package com.muyingmall.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.muyingmall.common.exception.BusinessException;
import com.muyingmall.entity.Favorite;
import com.muyingmall.entity.Product;
import com.muyingmall.mapper.FavoriteMapper;
import com.muyingmall.service.FavoriteService;
import com.muyingmall.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 收藏服务实现类
 */
@Service
@RequiredArgsConstructor
public class FavoriteServiceImpl extends ServiceImpl<FavoriteMapper, Favorite> implements FavoriteService {

    private final ProductService productService;

    @Override
    public Page<Favorite> getUserFavorites(Integer userId, int page, int pageSize) {
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
        return removeById(favoriteId);
    }

    @Override
    public boolean isFavorite(Integer userId, Integer productId) {
        return lambdaQuery()
                .eq(Favorite::getUserId, userId)
                .eq(Favorite::getProductId, productId)
                .count() > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean clearFavorites(Integer userId) {
        return lambdaUpdate()
                .eq(Favorite::getUserId, userId)
                .remove();
    }
}