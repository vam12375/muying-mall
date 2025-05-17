package com.muyingmall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.muyingmall.common.exception.BusinessException;
import com.muyingmall.entity.CommentTemplate;
import com.muyingmall.mapper.CommentTemplateMapper;
import com.muyingmall.service.CommentTemplateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 评价模板服务实现类
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class CommentTemplateServiceImpl extends ServiceImpl<CommentTemplateMapper, CommentTemplate>
        implements CommentTemplateService {

    @Override
    public List<CommentTemplate> getSystemTemplates(Integer rating, Integer categoryId) {
        LambdaQueryWrapper<CommentTemplate> queryWrapper = new LambdaQueryWrapper<>();

        // 查询系统预设模板
        queryWrapper.eq(CommentTemplate::getTemplateType, 1)
                .eq(CommentTemplate::getStatus, 1);

        // 如果指定了评分，则按评分范围筛选
        if (rating != null) {
            queryWrapper.le(CommentTemplate::getMinRating, rating)
                    .ge(CommentTemplate::getMaxRating, rating);
        }

        // 如果指定了商品类别，则按类别筛选
        if (categoryId != null) {
            queryWrapper.and(wrapper -> wrapper
                    .eq(CommentTemplate::getCategoryId, categoryId)
                    .or()
                    .isNull(CommentTemplate::getCategoryId));
        }

        // 按权重降序排序
        queryWrapper.orderByDesc(CommentTemplate::getWeight);

        return this.list(queryWrapper);
    }

    @Override
    public List<CommentTemplate> getUserTemplates(Integer userId) {
        if (userId == null) {
            throw new BusinessException("用户ID不能为空");
        }

        LambdaQueryWrapper<CommentTemplate> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CommentTemplate::getUserId, userId)
                .eq(CommentTemplate::getTemplateType, 2) // 用户自定义模板
                .eq(CommentTemplate::getStatus, 1)
                .orderByDesc(CommentTemplate::getUseCount);

        return this.list(queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createUserTemplate(CommentTemplate template) {
        if (template == null) {
            throw new BusinessException("模板对象不能为空");
        }

        if (template.getUserId() == null) {
            throw new BusinessException("用户ID不能为空");
        }

        // 设置为用户自定义模板
        template.setTemplateType(2);
        template.setStatus(1);
        template.setUseCount(0);
        template.setWeight(0);
        template.setCreateTime(LocalDateTime.now());
        template.setUpdateTime(LocalDateTime.now());

        return this.save(template);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateTemplate(CommentTemplate template) {
        if (template == null || template.getTemplateId() == null) {
            throw new BusinessException("模板ID不能为空");
        }

        // 获取原模板
        CommentTemplate originalTemplate = this.getById(template.getTemplateId());
        if (originalTemplate == null) {
            throw new BusinessException("模板不存在");
        }

        // 系统预设模板只有管理员可以修改
        if (originalTemplate.getTemplateType() == 1 && template.getUserId() != null) {
            throw new BusinessException("无权修改系统预设模板");
        }

        // 用户自定义模板只能由创建者修改
        if (originalTemplate.getTemplateType() == 2 &&
                !originalTemplate.getUserId().equals(template.getUserId())) {
            throw new BusinessException("无权修改他人的模板");
        }

        // 保持原有的模板类型和创建者
        template.setTemplateType(originalTemplate.getTemplateType());
        template.setUserId(originalTemplate.getUserId());
        template.setUpdateTime(LocalDateTime.now());

        return this.updateById(template);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteTemplate(Integer templateId, Integer userId) {
        if (templateId == null) {
            throw new BusinessException("模板ID不能为空");
        }

        // 获取原模板
        CommentTemplate template = this.getById(templateId);
        if (template == null) {
            return true; // 模板不存在，视为删除成功
        }

        // 系统预设模板不允许删除
        if (template.getTemplateType() == 1) {
            throw new BusinessException("系统预设模板不允许删除");
        }

        // 用户自定义模板只能由创建者删除
        if (template.getTemplateType() == 2 && !template.getUserId().equals(userId)) {
            throw new BusinessException("无权删除他人的模板");
        }

        return this.removeById(templateId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean incrementUseCount(Integer templateId) {
        if (templateId == null) {
            return false;
        }

        LambdaUpdateWrapper<CommentTemplate> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(CommentTemplate::getTemplateId, templateId)
                .setSql("use_count = use_count + 1");

        return this.update(updateWrapper);
    }

    @Override
    public IPage<CommentTemplate> getTemplatesPage(int page, int size, Integer templateType) {
        Page<CommentTemplate> pageParam = new Page<>(page, size);

        LambdaQueryWrapper<CommentTemplate> queryWrapper = new LambdaQueryWrapper<>();

        // 如果指定了模板类型，则按类型筛选
        if (templateType != null) {
            queryWrapper.eq(CommentTemplate::getTemplateType, templateType);
        }

        // 按模板类型和权重排序
        queryWrapper.orderByAsc(CommentTemplate::getTemplateType)
                .orderByDesc(CommentTemplate::getWeight)
                .orderByDesc(CommentTemplate::getUseCount);

        return this.page(pageParam, queryWrapper);
    }
}