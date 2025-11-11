package com.muyingmall.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.muyingmall.entity.PointsHistory;
import com.muyingmall.entity.PointsProduct;
import com.muyingmall.entity.PointsRule;
import com.muyingmall.entity.PointsExchange;
import com.muyingmall.entity.UserPoints;
import com.baomidou.mybatisplus.extension.service.IService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 积分管理服务接口
 */
public interface PointsService extends IService<UserPoints> {

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
     * @param userId      用户ID
     * @param points      积分数量
     * @param source      积分来源
     * @param referenceId 关联ID
     * @param description 描述
     * @return 是否成功
     */
    boolean addPoints(Integer userId, Integer points, String source, String referenceId, String description);

    /**
     * 减少用户积分
     *
     * @param userId      用户ID
     * @param points      积分数量
     * @param source      积分来源
     * @param referenceId 关联ID
     * @param description 描述
     * @return 是否成功
     */
    boolean deductPoints(Integer userId, Integer points, String source, String referenceId, String description);

    /**
     * 获取用户积分历史记录
     *
     * @param userId 用户ID
     * @param page   页码
     * @param size   每页大小
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

    /**
     * 根据订单完成情况发放积分
     *
     * @param userId      用户ID
     * @param orderId     订单ID
     * @param orderAmount 订单金额
     */
    void awardPointsForOrder(Integer userId, Integer orderId, BigDecimal orderAmount);

    /**
     * 管理员分页查询积分历史记录
     *
     * @param page      页码
     * @param size      每页数量
     * @param userId    用户ID，可为空
     * @param type      积分类型，可为空
     * @param source    积分来源，可为空
     * @param startDate 开始日期，可为空
     * @param endDate   结束日期，可为空
     * @return 积分历史记录分页
     */
    Page<PointsHistory> adminListPointsHistory(Integer page, Integer size, Integer userId,
            String type, String source, LocalDate startDate, LocalDate endDate);

    /**
     * 管理员调整用户积分
     *
     * @param userId      用户ID
     * @param points      积分值，正数为增加，负数为减少
     * @param description 描述
     * @return 是否成功
     */
    boolean adminAdjustPoints(Integer userId, Integer points, String description);

    /**
     * 管理员查询积分兑换记录
     *
     * @param page      页码
     * @param size      每页数量
     * @param userId    用户ID，可为空
     * @param productId 商品ID，可为空
     * @param status    状态，可为空
     * @param startDate 开始日期，可为空
     * @param endDate   结束日期，可为空
     * @return 积分兑换记录分页
     */
    Page<PointsExchange> adminListPointsExchanges(Integer page, Integer size, Integer userId,
            Long productId, String status, LocalDate startDate, LocalDate endDate);

    /**
     * 更新积分兑换状态
     *
     * @param id     兑换记录ID
     * @param status 状态
     * @return 是否成功
     */
    boolean updateExchangeStatus(Long id, String status);

    /**
     * 积分兑换发货
     *
     * @param id                兑换记录ID
     * @param logisticsCompany  物流公司
     * @param trackingNumber    物流单号
     * @param shipRemark        发货备注
     * @return 是否成功
     */
    boolean shipExchange(Long id, String logisticsCompany, String trackingNumber, String shipRemark);

    /**
     * 获取积分兑换详情
     *
     * @param id 兑换记录ID
     * @return 兑换记录
     */
    PointsExchange getExchangeById(Long id);

    /**
     * 获取积分统计数据
     *
     * @param startDate 开始日期，可为空
     * @param endDate   结束日期，可为空
     * @return 统计数据
     */
    Map<String, Object> getPointsStats(LocalDate startDate, LocalDate endDate);

    /**
     * 分页查询用户积分列表并关联用户信息
     *
     * @param page         分页参数
     * @param queryWrapper 查询条件
     * @return 分页结果
     */
    Page<UserPoints> pageWithUser(Page<UserPoints> page, LambdaQueryWrapper<UserPoints> queryWrapper);
}