package com.muyingmall.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.muyingmall.entity.PointsExchange;

import java.util.Map;

/**
 * 积分兑换服务接口
 */
public interface PointsExchangeService extends IService<PointsExchange> {

    /**
     * 创建积分兑换订单
     *
     * @param exchange 兑换信息
     * @return 兑换订单
     */
    PointsExchange createExchange(PointsExchange exchange);

    /**
     * 获取用户的积分兑换记录
     *
     * @param userId 用户ID
     * @param page   页码
     * @param size   每页大小
     * @param status 状态，null表示全部
     * @return 兑换记录分页
     */
    Page<PointsExchange> getUserExchanges(Integer userId, int page, int size, Integer status);

    /**
     * 获取用户的积分兑换记录（包含商品信息）
     *
     * @param userId 用户ID
     * @param page   页码
     * @param size   每页大小
     * @param status 状态，null表示全部
     * @return 兑换记录分页（包含商品信息）
     */
    Page<Map<String, Object>> getUserExchangesWithProduct(Integer userId, int page, int size, Integer status);

    /**
     * 获取兑换详情
     *
     * @param id 兑换ID
     * @return 兑换详情
     */
    PointsExchange getExchangeDetail(Long id);

    /**
     * 获取兑换详情（包含商品和用户信息）
     *
     * @param id 兑换ID
     * @return 兑换详情（包含商品、用户、地址信息）
     */
    Map<String, Object> getExchangeDetailWithInfo(Long id);

    /**
     * 获取用户兑换统计信息
     *
     * @param userId 用户ID
     * @return 统计信息
     */
    Map<String, Object> getUserExchangeStats(Integer userId);

    /**
     * 发货
     *
     * @param id              兑换ID
     * @param trackingNo      物流单号
     * @param trackingCompany 物流公司
     * @return 是否成功
     */
    boolean ship(Long id, String trackingNo, String trackingCompany);

    /**
     * 完成兑换
     *
     * @param id 兑换ID
     * @return 是否成功
     */
    boolean complete(Long id);

    /**
     * 取消兑换
     *
     * @param id     兑换ID
     * @param reason 取消原因
     * @return 是否成功
     */
    boolean cancel(Long id, String reason);

    /**
     * 取消兑换（用户操作）
     *
     * @param id     兑换ID
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean cancelExchange(Long id, Integer userId);

    /**
     * 确认收货
     *
     * @param id     兑换ID
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean confirmReceive(Long id, Integer userId);
}