package com.muyingmall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.muyingmall.common.exception.BusinessException;
import com.muyingmall.entity.PointsRule;
import com.muyingmall.mapper.PointsRuleMapper;
import com.muyingmall.service.PointsRuleService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 积分规则服务实现类
 */
@Service
public class PointsRuleServiceImpl extends ServiceImpl<PointsRuleMapper, PointsRule> implements PointsRuleService {

    @Override
    public Page<PointsRule> getPointsRulePage(int page, int size, String type) {
        Page<PointsRule> pageParam = new Page<>(page, size);

        LambdaQueryWrapper<PointsRule> queryWrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(type)) {
            queryWrapper.eq(PointsRule::getType, type);
        }

        queryWrapper.orderByAsc(PointsRule::getSort)
                .orderByDesc(PointsRule::getCreateTime);

        return page(pageParam, queryWrapper);
    }

    @Override
    public PointsRule getRuleByType(String type) {
        if (!StringUtils.hasText(type)) {
            return null;
        }

        LambdaQueryWrapper<PointsRule> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PointsRule::getType, type)
                .eq(PointsRule::getEnabled, 1)
                .orderByAsc(PointsRule::getSort)
                .last("LIMIT 1");

        return getOne(queryWrapper);
    }

    @Override
    public boolean enableRule(Long id) {
        if (id == null) {
            throw new BusinessException("规则ID不能为空");
        }

        PointsRule rule = getById(id);
        if (rule == null) {
            throw new BusinessException("规则不存在");
        }

        // 如果已经是启用状态，直接返回成功
        if (rule.getEnabled() == 1) {
            return true;
        }

        LambdaUpdateWrapper<PointsRule> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(PointsRule::getId, id)
                .set(PointsRule::getEnabled, 1);

        return update(updateWrapper);
    }

    @Override
    public boolean disableRule(Long id) {
        if (id == null) {
            throw new BusinessException("规则ID不能为空");
        }

        PointsRule rule = getById(id);
        if (rule == null) {
            throw new BusinessException("规则不存在");
        }

        // 如果已经是禁用状态，直接返回成功
        if (rule.getEnabled() == 0) {
            return true;
        }

        LambdaUpdateWrapper<PointsRule> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(PointsRule::getId, id)
                .set(PointsRule::getEnabled, 0);

        return update(updateWrapper);
    }

    @Override
    public Page<PointsRule> adminListPointsRules(Integer page, Integer size, String name) {
        Page<PointsRule> pageParam = new Page<>(page, size);

        LambdaQueryWrapper<PointsRule> queryWrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(name)) {
            queryWrapper.like(PointsRule::getTitle, name);
        }

        queryWrapper.orderByAsc(PointsRule::getSort)
                .orderByDesc(PointsRule::getCreateTime);

        return page(pageParam, queryWrapper);
    }

    @Override
    public List<PointsRule> getAllRules() {
        LambdaQueryWrapper<PointsRule> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByAsc(PointsRule::getSort)
                .orderByDesc(PointsRule::getCreateTime);

        return list(queryWrapper);
    }
}