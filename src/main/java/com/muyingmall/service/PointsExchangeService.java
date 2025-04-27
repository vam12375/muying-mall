package com.muyingmall.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.muyingmall.entity.PointsExchange;

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
     * 获取兑换详情
     *
     * @param id 兑换ID
     * @return 兑换详情
     */
    PointsExchange getExchangeDetail(Long id);

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
}