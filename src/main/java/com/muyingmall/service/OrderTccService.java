package com.muyingmall.service;

import com.muyingmall.dto.OrderCreateDTO;
import com.muyingmall.entity.Order;
import com.muyingmall.tcc.TccAction;

import java.util.Map;

/**
 * 订单TCC服务接口
 * 实现订单创建的TCC（Try-Confirm-Cancel）分布式事务模式
 *
 * TCC事务流程：
 * 1. Try阶段：预留库存资源，创建待确认状态的订单
 * 2. Confirm阶段：确认订单，正式扣减库存
 * 3. Cancel阶段：取消订单，释放预留的库存资源
 *
 * @author MuyingMall
 */
public interface OrderTccService extends TccAction<OrderCreateDTO, Order> {

    /**
     * 使用TCC模式创建订单
     *
     * @param userId       用户ID
     * @param addressId    收货地址ID
     * @param remark       订单备注
     * @param paymentMethod 支付方式
     * @param couponId     优惠券ID（可选）
     * @param cartIds      购物车商品ID列表
     * @param shippingFee  运费
     * @param pointsUsed   使用积分数量
     * @return 创建结果，包含订单信息
     */
    Map<String, Object> createOrderWithTcc(Integer userId, Integer addressId, String remark,
            String paymentMethod, Long couponId, java.util.List<Integer> cartIds,
            java.math.BigDecimal shippingFee, Integer pointsUsed);

    /**
     * Try阶段：预留资源
     * - 检查库存是否充足
     * - 预扣库存（标记为冻结状态）
     * - 创建待确认状态的订单
     *
     * @param params 订单创建参数
     * @return 预创建的订单
     */
    @Override
    Order tryAction(OrderCreateDTO params);

    /**
     * Confirm阶段：确认订单
     * - 将订单状态更新为正式状态
     * - 确认扣减库存（将冻结库存转为已售）
     * - 扣减优惠券、积分等
     *
     * @param params 订单创建参数
     */
    @Override
    void confirmAction(OrderCreateDTO params);

    /**
     * Cancel阶段：取消订单
     * - 释放预扣的库存
     * - 删除或取消待确认的订单
     * - 恢复优惠券、积分等
     *
     * @param params 订单创建参数
     */
    @Override
    void cancelAction(OrderCreateDTO params);
}
