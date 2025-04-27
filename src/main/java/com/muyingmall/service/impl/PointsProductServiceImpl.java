package com.muyingmall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.muyingmall.entity.PointsProduct;
import com.muyingmall.mapper.PointsProductMapper;
import com.muyingmall.service.PointsProductService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 积分商品服务实现类
 */
@Service
public class PointsProductServiceImpl extends ServiceImpl<PointsProductMapper, PointsProduct>
        implements PointsProductService {

    @Override
    public Page<PointsProduct> getPointsProductPage(int page, int size, String category, Boolean isHot) {
        Page<PointsProduct> pageParam = new Page<>(page, size);

        LambdaQueryWrapper<PointsProduct> queryWrapper = new LambdaQueryWrapper<>();
        // 只查询上架商品
        queryWrapper.eq(PointsProduct::getStatus, 1);

        // 条件查询
        if (StringUtils.hasText(category)) {
            queryWrapper.eq(PointsProduct::getCategory, category);
        }

        if (isHot != null && isHot) {
            queryWrapper.eq(PointsProduct::getIsHot, 1);
        }

        // 按排序号降序，创建时间降序排序
        queryWrapper.orderByDesc(PointsProduct::getSortOrder)
                .orderByDesc(PointsProduct::getCreateTime);

        return page(pageParam, queryWrapper);
    }

    @Override
    public PointsProduct getPointsProductDetail(Long id) {
        if (id == null) {
            return null;
        }
        return getById(id);
    }

    @Override
    public List<PointsProduct> getRecommendProducts(int limit) {
        LambdaQueryWrapper<PointsProduct> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PointsProduct::getStatus, 1) // 上架状态
                .eq(PointsProduct::getIsHot, 1) // 热门商品
                .orderByDesc(PointsProduct::getSortOrder)
                .orderByDesc(PointsProduct::getCreateTime)
                .last("LIMIT " + limit); // 限制返回数量

        return list(queryWrapper);
    }
}