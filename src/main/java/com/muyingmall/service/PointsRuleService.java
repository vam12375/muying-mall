package com.muyingmall.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.muyingmall.entity.PointsRule;

import java.util.List;

/**
 * 积分规则服务接口
 */
public interface PointsRuleService extends IService<PointsRule> {

    /**
     * 获取积分规则列表
     * 
     * @return 积分规则列表
     */
    List<PointsRule> getAllRules();

    /**
     * 获取某一类型的积分规则
     *
     * @param type 规则类型
     * @return 积分规则
     */
    PointsRule getRuleByType(String type);

    /**
     * 管理员分页查询积分规则
     *
     * @param page 页码
     * @param size 每页大小
     * @param name 规则名称，可为空
     * @return 规则分页对象
     */
    Page<PointsRule> adminListPointsRules(Integer page, Integer size, String name);

    /**
     * 分页获取积分规则列表
     *
     * @param page 页码
     * @param size 每页大小
     * @param type 规则类型，可为null
     * @return 积分规则分页对象
     */
    Page<PointsRule> getPointsRulePage(int page, int size, String type);

    /**
     * 启用规则
     *
     * @param id 规则ID
     * @return 是否成功
     */
    boolean enableRule(Long id);

    /**
     * 禁用规则
     *
     * @param id 规则ID
     * @return 是否成功
     */
    boolean disableRule(Long id);
}