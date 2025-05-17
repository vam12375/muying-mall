package com.muyingmall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.muyingmall.common.exception.BusinessException;
import com.muyingmall.entity.CommentTag;
import com.muyingmall.entity.Product;
import com.muyingmall.mapper.CommentTagMapper;
import com.muyingmall.mapper.ProductMapper;
import com.muyingmall.service.CommentTagService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 评价标签服务实现类
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class CommentTagServiceImpl extends ServiceImpl<CommentTagMapper, CommentTag> implements CommentTagService {

    private final ProductMapper productMapper;

    @Override
    public List<CommentTag> getHotTags(Integer limit) {
        if (limit == null || limit <= 0) {
            limit = 10; // 默认返回10个热门标签
        }
        return baseMapper.getHotTags(limit);
    }

    @Override
    public List<CommentTag> getTagsByCategory(Integer categoryId) {
        if (categoryId == null) {
            // 返回通用标签（不关联特定分类的标签）
            LambdaQueryWrapper<CommentTag> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.isNull(CommentTag::getProductCategoryId)
                    .eq(CommentTag::getStatus, 1)
                    .orderByDesc(CommentTag::getUsageCount);
            return this.list(queryWrapper);
        }
        return baseMapper.getTagsByCategory(categoryId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createTag(CommentTag tag) {
        // 检查标签名是否已存在
        LambdaQueryWrapper<CommentTag> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CommentTag::getTagName, tag.getTagName());
        long count = this.count(queryWrapper);
        if (count > 0) {
            throw new BusinessException("标签名称已存在");
        }

        // 设置默认值
        if (tag.getTagType() == null) {
            tag.setTagType(2); // 默认为用户自定义标签
        }
        if (tag.getStatus() == null) {
            tag.setStatus(1); // 默认启用
        }
        if (tag.getUsageCount() == null) {
            tag.setUsageCount(0); // 默认使用次数为0
        }

        return this.save(tag);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateTagStatus(Integer tagId, Integer status) {
        if (tagId == null || status == null) {
            return false;
        }
        LambdaUpdateWrapper<CommentTag> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(CommentTag::getTagId, tagId)
                .set(CommentTag::getStatus, status);
        return this.update(updateWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean incrementUsageCount(Integer tagId) {
        if (tagId == null) {
            return false;
        }
        return baseMapper.incrementUsageCount(tagId) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean decrementUsageCount(Integer tagId) {
        if (tagId == null) {
            return false;
        }
        return baseMapper.decrementUsageCount(tagId) > 0;
    }

    @Override
    public List<CommentTag> searchTags(String keyword, Integer limit) {
        if (!StringUtils.hasText(keyword)) {
            return new ArrayList<>();
        }
        if (limit == null || limit <= 0) {
            limit = 10; // 默认返回10个搜索结果
        }
        return baseMapper.searchTags(keyword, limit);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CommentTag getOrCreateTag(String tagName) {
        if (!StringUtils.hasText(tagName)) {
            throw new BusinessException("标签名称不能为空");
        }

        // 查询标签是否存在
        LambdaQueryWrapper<CommentTag> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CommentTag::getTagName, tagName);
        CommentTag tag = this.getOne(queryWrapper);

        // 如果不存在，则创建新标签
        if (tag == null) {
            tag = new CommentTag();
            tag.setTagName(tagName);
            tag.setTagType(2); // 用户自定义标签
            tag.setStatus(1); // 启用状态
            tag.setUsageCount(0); // 初始使用次数为0
            this.save(tag);
        }

        return tag;
    }

    @Override
    public List<CommentTag> getRecommendedTags(Integer productId, Integer categoryId, Integer limit) {
        if (limit == null || limit <= 0) {
            limit = 10; // 默认返回10个推荐标签
        }

        List<CommentTag> recommendedTags = new ArrayList<>();

        // 如果提供了商品ID，获取该商品的分类ID
        if (productId != null) {
            Product product = productMapper.selectById(productId);
            if (product != null && product.getCategoryId() != null) {
                categoryId = product.getCategoryId();
            }
        }

        // 获取分类相关的标签
        if (categoryId != null) {
            List<CommentTag> categoryTags = this.getTagsByCategory(categoryId);
            recommendedTags.addAll(categoryTags);
        }

        // 如果标签数量不足，添加热门通用标签
        if (recommendedTags.size() < limit) {
            // 获取通用标签（不关联特定分类的标签）
            LambdaQueryWrapper<CommentTag> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.isNull(CommentTag::getProductCategoryId)
                    .eq(CommentTag::getStatus, 1)
                    .orderByDesc(CommentTag::getUsageCount)
                    .last("LIMIT " + (limit - recommendedTags.size()));
            List<CommentTag> generalTags = this.list(queryWrapper);

            // 过滤掉已经包含的标签
            List<Integer> existingTagIds = recommendedTags.stream()
                    .map(CommentTag::getTagId)
                    .collect(Collectors.toList());
            List<CommentTag> filteredGeneralTags = generalTags.stream()
                    .filter(tag -> !existingTagIds.contains(tag.getTagId()))
                    .collect(Collectors.toList());

            recommendedTags.addAll(filteredGeneralTags);
        }

        // 限制返回数量
        return recommendedTags.stream().limit(limit).collect(Collectors.toList());
    }
}