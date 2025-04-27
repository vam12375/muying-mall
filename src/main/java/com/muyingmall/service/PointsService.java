package com.muyingmall.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.muyingmall.entity.PointsHistory;
import com.muyingmall.entity.PointsProduct;
import com.muyingmall.entity.PointsRule;

import java.util.List;
import java.util.Map;

/**
 * 积分管理服务接口
 */
public interface PointsService {

    /**
     * 获取用户积分
     *
     * @param userId 用户ID
     * @return 积分数量
     */
    Integer getUserPoints(Integer userId);

    /**
     * 增加用户积分
     *
     * @param userId 用户ID
     * @param points 积分数量
     * @param source 积分来源
     * @param referenceId 关联ID
     * @param description 描述
     * @return 是否成功
     */
    boolean addPoints(Integer userId, Integer points, String source, String referenceId, String description);

    /**
     * 减少用户积分
     *
     * @param userId 用户ID
     * @param points 积分数量
     * @param source 积分来源
     * @param referenceId 关联ID
     * @param description 描述
     * @return 是否成功
     */
    boolean deductPoints(Integer userId, Integer points, String source, String referenceId, String description);

    /**
     * 获取用户积分历史记录
     *
     * @param userId 用户ID
     * @param page 页码
     * @param size 每页大小
     * @return 积分历史记录分页
     */
    Page<PointsHistory> getUserPointsHistory(Integer userId, int page, int size);

    /**
     * 获取积分规则列表
     *
     * @return 积分规则列表
     */
    List<PointsRule> getPointsRules();

    /**
     * 签到获取积分
     *
     * @param userId 用户ID
     * @return 获得的积分数量
     */
    Integer signIn(Integer userId);

    /**
     * 获取用户签到状态
     *
     * @param userId 用户ID
     * @return 签到信息，包含连续签到天数、今日是否已签到等
     */
    Map<String, Object> getSignInStatus(Integer userId);

    /**
     * 获取积分商品列表
     *
     * @param page     页码
     * @param size     每页大小
     * @param category 分类
     * @return 积分商品分页对象
     */
    Page<PointsProduct> getPointsProducts(int page, int size, String category);

    /**
     * 获取积分商品详情
     *
     * @param productId 商品ID
     * @return 积分商品
     */
    PointsProduct getPointsProductDetail(Long productId);

    /**
     * 用户签到
     *
     * @param userId 用户ID
     * @return 签到结果
     */
    Map<String, Object> userSignin(Integer userId);

    /**
     * 兑换积分商品
     *
     * @param userId    用户ID
     * @param productId 商品ID
     * @param addressId 地址ID
     * @param phone     手机号
     * @return 是否成功
     */
    boolean exchangeProduct(Integer userId, Long productId, Integer addressId, String phone);

    /**
     * 使用用户积分
     *
     * @param userId      用户ID
     * @param points      积分数量
     * @param source      来源
     * @param referenceId 关联ID
     * @param description 描述
     * @return 是否成功
     */
    boolean usePoints(Integer userId, int points, String source, String referenceId, String description);

    /**
     * 获取用户签到日历
     *
     * @param userId 用户ID
     * @param month  月份，格式为"yyyy-MM"，如果为空则获取当前月
     * @return 签到日历信息
     */
    Map<String, Object> getSignInCalendar(Integer userId, String month);
}