package com.muyingmall.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.muyingmall.entity.Order;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 订单服务接口
 */
public interface OrderService extends IService<Order> {

    /**
     * 创建订单
     * 
     * @param userId        用户ID
     * @param addressId     地址ID
     * @param remark        备注
     * @param paymentMethod 支付方式
     * @param couponId      优惠券ID
     * @param cartIds       购物车项ID列表
     * @param shippingFee   运费
     * @param pointsUsed    使用的积分数量
     * @return 订单信息
     */
    Map<String, Object> createOrder(Integer userId, Integer addressId, String remark,
            String paymentMethod, Long couponId, List<Integer> cartIds, BigDecimal shippingFee, Integer pointsUsed);

    /**
     * 直接购买商品（不添加到购物车）
     * 
     * @param userId        用户ID
     * @param addressId     地址ID
     * @param productId     商品ID
     * @param quantity      数量
     * @param specs         规格（兼容旧版本）
     * @param skuId         SKU ID（新版本使用）
     * @param remark        备注
     * @param paymentMethod 支付方式
     * @param couponId      优惠券ID
     * @param shippingFee   运费
     * @param pointsUsed    使用的积分
     * @return 创建的订单信息
     */
    Map<String, Object> directPurchase(Integer userId, Integer addressId, Integer productId,
            Integer quantity, String specs, Long skuId, String remark,
            String paymentMethod, Long couponId,
            BigDecimal shippingFee, Integer pointsUsed);

    /**
     * 获取订单详情
     * 
     * @param orderId 订单ID
     * @param userId  用户ID，用于验证权限
     * @return 订单详情
     */
    Order getOrderDetail(Integer orderId, Integer userId);

    /**
     * 获取用户订单列表
     * 
     * @param userId 用户ID
     * @param page   页码
     * @param size   每页大小
     * @param status 订单状态，可为null
     * @return 订单分页
     */
    Page<Order> getUserOrders(Integer userId, int page, int size, String status);

    /**
     * 取消订单
     * 
     * @param orderId 订单ID
     * @param userId  用户ID
     * @return 是否成功
     */
    boolean cancelOrder(Integer orderId, Integer userId);

    /**
     * 支付订单
     * 
     * @param orderId       订单ID
     * @param userId        用户ID
     * @param paymentMethod 支付方式
     * @return 支付信息
     */
    Map<String, Object> payOrder(Integer orderId, Integer userId, String paymentMethod);

    /**
     * 确认收货
     * 
     * @param orderId 订单ID
     * @param userId  用户ID
     * @return 是否成功
     */
    boolean confirmReceive(Integer orderId, Integer userId);

    /**
     * 管理员获取所有订单
     * 
     * @param page   页码
     * @param size   每页大小
     * @param status 订单状态
     * @return 订单分页数据
     */
    Page<Order> getAdminOrders(int page, int size, String status);

    /**
     * 管理员获取所有订单，支持分页和条件筛选
     * 
     * @param page    页码
     * @param size    每页大小
     * @param status  订单状态，可为null
     * @param orderNo 订单号，可为null
     * @param userId  用户ID，可为null
     * @return 订单分页
     */
    Page<Order> getOrdersByAdmin(int page, int size, String status, String orderNo, Integer userId);

    /**
     * 管理员获取订单详情
     * 
     * @param orderId 订单ID
     * @return 订单详情
     */
    Order getOrderDetailByAdmin(Integer orderId);

    /**
     * 管理员更新订单状态
     * 
     * @param orderId 订单ID
     * @param status  新状态
     * @param remark  操作备注
     * @return 是否成功
     */
    boolean updateOrderStatusByAdmin(Integer orderId, String status, String remark);

    /**
     * 管理员发货
     * 
     * @param orderId         订单ID
     * @param shippingCompany 物流公司
     * @param trackingNo      物流单号
     * @return 是否成功
     */
    boolean shipOrder(Integer orderId, String shippingCompany, String trackingNo);

    /**
     * 根据订单号查询订单
     * 
     * @param orderNo 订单号
     * @param userId  用户ID
     * @return 订单信息
     */
    Order getOrderByOrderNo(String orderNo, Integer userId);

    /**
     * 获取订单统计数据
     * 
     * @param userId 用户ID，若为null则获取全部订单统计
     * @return 统计信息
     */
    Map<String, Object> getOrderStatistics(Integer userId);

    /**
     * 获取指定时间段内的销售额
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 销售总额
     */
    BigDecimal getSalesBetween(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 检查订单是否已评价
     * 
     * @param orderId 订单ID
     * @return 是否已评价
     */
    boolean isOrderCommented(Integer orderId);

    /**
     * 更新订单评价状态
     * 
     * @param orderId     订单ID
     * @param isCommented 评价状态：0-未评价，1-已评价
     * @return 是否成功
     */
    boolean updateOrderCommentStatus(Integer orderId, Integer isCommented);
}