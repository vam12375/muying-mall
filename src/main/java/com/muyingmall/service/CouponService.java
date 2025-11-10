package com.muyingmall.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.muyingmall.entity.Coupon;
import com.muyingmall.entity.CouponBatch;
import com.muyingmall.entity.CouponRule;
import com.muyingmall.entity.UserCoupon;

import java.util.List;
import java.util.Map;

/**
 * 优惠券服务接口
 */
public interface CouponService extends IService<Coupon> {

    /**
     * 获取可用优惠券列表
     *
     * @param userId 用户ID，可为空
     * @return 优惠券列表
     */
    List<Coupon> getAvailableCoupons(Integer userId);

    /**
     * 获取用户优惠券列表
     *
     * @param userId 用户ID
     * @param status 优惠券状态：all-全部, UNUSED-未使用, USED-已使用, EXPIRED-已过期
     * @return 用户优惠券列表
     */
    List<UserCoupon> getUserCoupons(Integer userId, String status);

    /**
     * 领取优惠券
     *
     * @param userId   用户ID
     * @param couponId 优惠券ID
     * @return 是否成功
     */
    boolean receiveCoupon(Integer userId, Long couponId);

    /**
     * 获取订单可用优惠券
     *
     * @param userId     用户ID
     * @param amount     订单金额
     * @param productIds 商品ID列表
     * @return 可用优惠券列表
     */
    List<UserCoupon> getOrderCoupons(Integer userId, Double amount, List<Integer> productIds);
    
    /**
     * 管理员分页查询优惠券列表
     *
     * @param page   页码
     * @param size   每页大小
     * @param name   优惠券名称，可为空
     * @param type   优惠券类型，可为空
     * @param status 优惠券状态，可为空
     * @return 优惠券分页对象
     */
    Page<Coupon> adminListCoupons(Integer page, Integer size, String name, String type, String status);

    /**
     * 保存优惠券
     *
     * @param coupon 优惠券对象
     * @return 是否成功
     */
    boolean saveCoupon(Coupon coupon);

    /**
     * 更新优惠券
     *
     * @param coupon 优惠券对象
     * @return 是否成功
     */
    boolean updateCoupon(Coupon coupon);

    /**
     * 删除优惠券
     *
     * @param id 优惠券ID
     * @return 是否成功
     */
    boolean deleteCoupon(Long id);

    /**
     * 更新优惠券状态
     *
     * @param id     优惠券ID
     * @param status 状态
     * @return 是否成功
     */
    boolean updateCouponStatus(Long id, String status);

    /**
     * 分页查询优惠券批次
     *
     * @param page       页码
     * @param size       每页大小
     * @param couponName 优惠券名称，可为空
     * @return 批次分页对象
     */
    Page<CouponBatch> listCouponBatches(Integer page, Integer size, String couponName);

    /**
     * 保存优惠券批次
     *
     * @param batch 批次对象
     * @return 是否成功
     */
    boolean saveCouponBatch(CouponBatch batch);

    /**
     * 获取优惠券批次详情
     *
     * @param batchId 批次ID
     * @return 批次对象
     */
    CouponBatch getCouponBatchDetail(Integer batchId);

    /**
     * 分页查询优惠券规则
     *
     * @param page 页码
     * @param size 每页大小
     * @param name 规则名称，可为空
     * @return 规则分页对象
     */
    Page<CouponRule> listCouponRules(Integer page, Integer size, String name);

    /**
     * 保存优惠券规则
     *
     * @param rule 规则对象
     * @return 是否成功
     */
    boolean saveCouponRule(CouponRule rule);

    /**
     * 更新优惠券规则
     *
     * @param rule 规则对象
     * @return 是否成功
     */
    boolean updateCouponRule(CouponRule rule);

    /**
     * 获取优惠券统计数据
     *
     * @return 统计数据
     */
    Map<String, Object> getCouponStats();

    /**
     * 通过优惠码领取优惠券
     *
     * @param userId 用户ID
     * @param code   优惠码
     * @return 是否成功
     */
    boolean receiveCouponByCode(Integer userId, String code);

    /**
     * 获取用户优惠券统计数据
     *
     * @param userId 用户ID
     * @return 统计数据
     */
    Map<String, Object> getUserCouponStats(Integer userId);
}