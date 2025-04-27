package com.muyingmall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.muyingmall.entity.Product;
import com.muyingmall.entity.ProductImage;
import com.muyingmall.entity.ProductSpecs;
import com.muyingmall.mapper.ProductImageMapper;
import com.muyingmall.mapper.ProductMapper;
import com.muyingmall.mapper.ProductSpecsMapper;
import com.muyingmall.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 商品服务实现类
 */
@Service
@RequiredArgsConstructor
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements ProductService {

    private final ProductImageMapper productImageMapper;
    private final ProductSpecsMapper productSpecsMapper;

    @Override
    public Page<Product> getProductPage(int page, int size, Integer categoryId, Boolean isHot, Boolean isNew,
            Boolean isRecommend, String keyword) {
        Page<Product> pageParam = new Page<>(page, size);

        LambdaQueryWrapper<Product> queryWrapper = new LambdaQueryWrapper<>();
        // 只查询上架商品
        queryWrapper.eq(Product::getProductStatus, "上架");

        // 条件查询
        if (categoryId != null) {
            queryWrapper.eq(Product::getCategoryId, categoryId);
        }

        if (isHot != null && isHot) {
            queryWrapper.eq(Product::getIsHot, 1);
        }

        if (isNew != null && isNew) {
            queryWrapper.eq(Product::getIsNew, 1);
        }

        if (isRecommend != null && isRecommend) {
            queryWrapper.eq(Product::getIsRecommend, 1);
        }

        if (StringUtils.hasText(keyword)) {
            queryWrapper.and(wrapper -> wrapper.like(Product::getProductName, keyword)
                    .or()
                    .like(Product::getProductDetail, keyword));
        }

        // 默认按创建时间降序排序
        queryWrapper.orderByDesc(Product::getCreateTime);

        return page(pageParam, queryWrapper);
    }

    @Override
    @Transactional(readOnly = true)
    public Product getProductDetail(Integer id) {
        // 获取商品基本信息
        Product product = getById(id);

        if (product != null) {
            // 获取商品图片
            LambdaQueryWrapper<ProductImage> imageQueryWrapper = new LambdaQueryWrapper<>();
            imageQueryWrapper.eq(ProductImage::getProductId, id);
            imageQueryWrapper.orderByAsc(ProductImage::getSortOrder);
            List<ProductImage> images = productImageMapper.selectList(imageQueryWrapper);
            product.setImages(images);

            // 获取商品规格
            LambdaQueryWrapper<ProductSpecs> specsQueryWrapper = new LambdaQueryWrapper<>();
            specsQueryWrapper.eq(ProductSpecs::getProductId, id);
            List<ProductSpecs> specsList = productSpecsMapper.selectList(specsQueryWrapper);
            product.setSpecsList(specsList);
        }

        return product;
    }
}