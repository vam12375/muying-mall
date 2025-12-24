package com.muyingmall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.muyingmall.dto.OrderCreateDTO;
import com.muyingmall.dto.SkuStockDTO;
import com.muyingmall.entity.*;
import com.muyingmall.enums.OrderStatus;
import com.muyingmall.exception.BusinessException;
import com.muyingmall.lock.DistributedLock;
import com.muyingmall.mapper.CartMapper;
import com.muyingmall.mapper.OrderMapper;
import com.muyingmall.mapper.OrderProductMapper;
import com.muyingmall.service.*;
import com.muyingmall.tcc.TccTransactionManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 订单TCC服务实现
 * 实现订单创建的TCC（Try-Confirm-Cancel）分布式事务模式
 *
 * 核心流程：
 * Try阶段：
 *   1. 校验用户、地址、购物车商品
 *   2. 预扣库存（使用Redis冻结库存）
 *   3. 创建"待确认"状态的订单
 *
 * Confirm阶段：
 *   1. 确认扣减数据库库存
 *   2. 更新订单状态为"待支付"
 *   3. 清理购物车
 *   4. 扣减优惠券/积分
 *
 * Cancel阶段：
 *   1. 释放预扣的库存
 *   2. 删除待确认的订单
 *   3. 恢复优惠券/积分状态
 *
 * @author MuyingMall
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderTccServiceImpl implements OrderTccService {

    private final TccTransactionManager tccTransactionManager;
    private final DistributedLock distributedLock;
    private final OrderMapper orderMapper;
    private final OrderProductMapper orderProductMapper;
    private final CartMapper cartMapper;
    private final UserService userService;
    private final AddressService addressService;
    private final ProductService productService;
    private final ProductSkuService productSkuService;
    private final SeckillService seckillService;
    private final UserCouponService userCouponService;
    private final MessageProducerService messageProducerService;

    private static final String ORDER_LOCK_KEY_PREFIX = "order:tcc:lock:";
    private static final long LOCK_EXPIRE_TIME = 30000; // 30秒

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> createOrderWithTcc(Integer userId, Integer addressId, String remark,
            String paymentMethod, Long couponId, List<Integer> cartIds,
            BigDecimal shippingFee, Integer pointsUsed) {

        // 构建订单创建参数
        OrderCreateDTO params = new OrderCreateDTO();
        params.setUserId(userId);
        params.setAddressId(addressId);
        params.setRemark(remark);
        params.setPaymentMethod(paymentMethod);
        params.setCouponId(couponId);
        params.setCartIds(cartIds);
        params.setShippingFee(shippingFee != null ? shippingFee : BigDecimal.ZERO);
        params.setPointsUsed(pointsUsed != null ? pointsUsed : 0);

        // 开始TCC事务
        String transactionId = tccTransactionManager.begin("createOrder",
                "user:" + userId + ":" + System.currentTimeMillis(), params);
        params.setTccTransactionId(transactionId);

        try {
            log.info("TCC订单事务开始: transactionId={}, userId={}", transactionId, userId);

            // Try阶段：预留资源
            Order order = tccTransactionManager.tryAction(transactionId, this, params);
            params.setPreOrderId(order.getOrderId());

            // Confirm阶段：确认订单
            tccTransactionManager.confirmAction(transactionId, this, params);

            // 发送订单超时延迟消息
            messageProducerService.sendOrderTimeoutDelayMessage(order.getOrderId(), order.getOrderNo());

            log.info("TCC订单事务成功: transactionId={}, orderId={}", transactionId, order.getOrderId());

            // 构建返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("orderId", order.getOrderId());
            result.put("orderNo", order.getOrderNo());
            result.put("totalAmount", order.getTotalAmount());
            result.put("paymentAmount", order.getActualAmount()); // 修复：使用actualAmount字段
            result.put("status", order.getStatus());

            return result;

        } catch (Exception e) {
            log.error("TCC订单事务失败，执行Cancel: transactionId={}, error={}", transactionId, e.getMessage());

            // Cancel阶段：释放资源
            try {
                tccTransactionManager.cancelAction(transactionId, this, params);
            } catch (Exception cancelError) {
                log.error("TCC Cancel阶段执行失败: transactionId={}, error={}",
                        transactionId, cancelError.getMessage());
            }

            throw new BusinessException("订单创建失败：" + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Order tryAction(OrderCreateDTO params) {
        String lockKey = ORDER_LOCK_KEY_PREFIX + params.getUserId();
        String requestId = UUID.randomUUID().toString();

        try {
            // 获取分布式锁，防止并发下单
            boolean locked = distributedLock.tryLock(lockKey, requestId, LOCK_EXPIRE_TIME);
            if (!locked) {
                throw new BusinessException("系统繁忙，请稍后重试");
            }

            log.info("TCC Try阶段开始: userId={}", params.getUserId());

            // 1. 校验用户
            User user = userService.getById(params.getUserId());
            if (user == null) {
                throw new BusinessException("用户不存在");
            }

            // 2. 校验收货地址
            Address address = addressService.getById(params.getAddressId());
            if (address == null || !address.getUserId().equals(params.getUserId())) {
                throw new BusinessException("收货地址不存在或无权使用");
            }

            // 3. 获取购物车商品
            List<Cart> cartItems = getCartItems(params.getUserId(), params.getCartIds());
            if (cartItems.isEmpty()) {
                throw new BusinessException("购物车中没有可购买的商品");
            }

            // 4. 校验商品并预扣库存
            List<OrderProduct> orderProducts = new ArrayList<>();
            BigDecimal productAmount = BigDecimal.ZERO;

            for (Cart cart : cartItems) {
                Product product = productService.getById(cart.getProductId());
                // 修复：使用productStatus字段，并检查是否为"上架"状态
                if (product == null || !"上架".equals(product.getProductStatus())) {
                    throw new BusinessException("商品 " + cart.getProductId() + " 已下架或不存在");
                }

                // 预扣库存（Redis冻结）
                boolean stockReserved = reserveStock(cart.getProductId(), cart.getSkuId(), cart.getQuantity());
                if (!stockReserved) {
                    throw new BusinessException("商品 " + product.getProductName() + " 库存不足"); // 修复：使用productName字段
                }

                // 构建订单商品
                OrderProduct orderProduct = new OrderProduct();
                orderProduct.setProductId(cart.getProductId());
                orderProduct.setSkuId(cart.getSkuId());
                orderProduct.setProductName(product.getProductName()); // 修复：使用productName字段
                orderProduct.setProductImg(product.getProductImg()); // 修复：使用productImg字段
                orderProduct.setQuantity(cart.getQuantity());

                // 获取价格（优先使用SKU价格）
                BigDecimal price = getProductPrice(product, cart.getSkuId());
                orderProduct.setPrice(price);
                // 修复：OrderProduct没有totalPrice字段，直接计算
                BigDecimal itemTotal = price.multiply(BigDecimal.valueOf(cart.getQuantity()));

                orderProducts.add(orderProduct);
                productAmount = productAmount.add(itemTotal); // 修复：使用计算的itemTotal
            }

            // 5. 计算优惠金额
            BigDecimal discountAmount = BigDecimal.ZERO;
            if (params.getCouponId() != null) {
                discountAmount = calculateCouponDiscount(params.getCouponId(), params.getUserId(), productAmount);
            }

            // 6. 计算积分抵扣
            BigDecimal pointsDiscount = BigDecimal.ZERO;
            if (params.getPointsUsed() != null && params.getPointsUsed() > 0) {
                pointsDiscount = calculatePointsDiscount(params.getPointsUsed(), params.getUserId());
            }

            // 7. 计算最终金额
            BigDecimal shippingFee = params.getShippingFee() != null ? params.getShippingFee() : BigDecimal.ZERO;
            BigDecimal totalAmount = productAmount.add(shippingFee);
            BigDecimal actualAmount = totalAmount.subtract(discountAmount).subtract(pointsDiscount); // 修复：使用actualAmount变量名
            if (actualAmount.compareTo(BigDecimal.ZERO) < 0) {
                actualAmount = BigDecimal.ZERO;
            }

            // 8. 创建待确认状态的订单
            Order order = new Order();
            order.setOrderNo(generateOrderNo());
            order.setUserId(params.getUserId());
            order.setStatus(OrderStatus.PENDING_CONFIRMATION); // 待确认状态（TCC专用）
            order.setTotalAmount(totalAmount);
            order.setActualAmount(actualAmount); // 修复：使用actualAmount字段
            order.setDiscountAmount(discountAmount.add(pointsDiscount));
            order.setShippingFee(shippingFee);
            order.setPaymentMethod(params.getPaymentMethod());
            order.setRemark(params.getRemark());
            order.setCouponId(params.getCouponId());
            order.setPointsUsed(params.getPointsUsed());

            // 设置收货信息
            order.setReceiverName(address.getReceiver()); // 修复：使用receiver字段
            order.setReceiverPhone(address.getPhone()); // 修复：使用phone字段
            order.setReceiverAddress(buildFullAddress(address));

            order.setCreateTime(LocalDateTime.now());
            order.setUpdateTime(LocalDateTime.now());

            // 保存订单
            orderMapper.insert(order);

            // 保存订单商品
            for (OrderProduct orderProduct : orderProducts) {
                orderProduct.setOrderId(order.getOrderId());
                orderProductMapper.insert(orderProduct);
            }

            log.info("TCC Try阶段完成: orderId={}, orderNo={}, status=PENDING_CONFIRMATION",
                    order.getOrderId(), order.getOrderNo());

            return order;

        } finally {
            // 释放锁
            distributedLock.releaseLock(lockKey, requestId);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmAction(OrderCreateDTO params) {
        String lockKey = ORDER_LOCK_KEY_PREFIX + params.getUserId();
        String requestId = UUID.randomUUID().toString();

        try {
            boolean locked = distributedLock.tryLock(lockKey, requestId, LOCK_EXPIRE_TIME);
            if (!locked) {
                throw new BusinessException("获取订单锁失败");
            }

            log.info("TCC Confirm阶段开始: orderId={}", params.getPreOrderId());

            // 1. 获取订单
            Order order = orderMapper.selectById(params.getPreOrderId());
            if (order == null) {
                throw new BusinessException("订单不存在");
            }

            // 检查订单状态
            if (order.getStatus() != OrderStatus.PENDING_CONFIRMATION) {
                log.warn("订单状态不是待确认，跳过Confirm: orderId={}, status={}",
                        order.getOrderId(), order.getStatus());
                return;
            }

            // 2. 获取订单商品
            LambdaQueryWrapper<OrderProduct> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(OrderProduct::getOrderId, order.getOrderId());
            List<OrderProduct> orderProducts = orderProductMapper.selectList(queryWrapper);

            // 3. 确认扣减数据库库存
            for (OrderProduct orderProduct : orderProducts) {
                confirmDeductStock(orderProduct.getProductId(), orderProduct.getSkuId(),
                        orderProduct.getQuantity());
            }

            // 4. 更新订单状态为待支付
            order.setStatus(OrderStatus.PENDING_PAYMENT);
            order.setUpdateTime(LocalDateTime.now());
            orderMapper.updateById(order);

            // 5. 清理购物车
            if (params.getCartIds() != null && !params.getCartIds().isEmpty()) {
                cartMapper.deleteBatchIds(params.getCartIds());
            } else {
                // 清理用户所有已选中的购物车商品
                LambdaQueryWrapper<Cart> cartQuery = new LambdaQueryWrapper<>();
                cartQuery.eq(Cart::getUserId, params.getUserId())
                         .eq(Cart::getSelected, true);
                cartMapper.delete(cartQuery);
            }

            // 6. 使用优惠券
            if (params.getCouponId() != null) {
                useCoupon(params.getCouponId(), params.getUserId(), order.getOrderId());
            }

            // 7. 扣减积分
            if (params.getPointsUsed() != null && params.getPointsUsed() > 0) {
                deductPoints(params.getUserId(), params.getPointsUsed(), order.getOrderId());
            }

            log.info("TCC Confirm阶段完成: orderId={}, status=PENDING_PAYMENT", order.getOrderId());

        } finally {
            distributedLock.releaseLock(lockKey, requestId);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelAction(OrderCreateDTO params) {
        String lockKey = ORDER_LOCK_KEY_PREFIX + params.getUserId();
        String requestId = UUID.randomUUID().toString();

        try {
            boolean locked = distributedLock.tryLock(lockKey, requestId, LOCK_EXPIRE_TIME);
            if (!locked) {
                log.warn("TCC Cancel获取锁失败，将由定时任务补偿: userId={}", params.getUserId());
                return;
            }

            log.info("TCC Cancel阶段开始: orderId={}", params.getPreOrderId());

            // 1. 如果订单已创建，则需要处理
            if (params.getPreOrderId() != null) {
                Order order = orderMapper.selectById(params.getPreOrderId());

                if (order != null && order.getStatus() == OrderStatus.PENDING_CONFIRMATION) {
                    // 获取订单商品
                    LambdaQueryWrapper<OrderProduct> queryWrapper = new LambdaQueryWrapper<>();
                    queryWrapper.eq(OrderProduct::getOrderId, order.getOrderId());
                    List<OrderProduct> orderProducts = orderProductMapper.selectList(queryWrapper);

                    // 释放预扣的库存
                    for (OrderProduct orderProduct : orderProducts) {
                        releaseReservedStock(orderProduct.getProductId(),
                                orderProduct.getSkuId(), orderProduct.getQuantity());
                    }

                    // 删除订单商品
                    orderProductMapper.delete(queryWrapper);

                    // 删除订单
                    orderMapper.deleteById(order.getOrderId());

                    log.info("TCC Cancel阶段：已删除待确认订单 orderId={}", order.getOrderId());
                }
            }

            log.info("TCC Cancel阶段完成");

        } finally {
            distributedLock.releaseLock(lockKey, requestId);
        }
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 获取购物车商品
     */
    private List<Cart> getCartItems(Integer userId, List<Integer> cartIds) {
        LambdaQueryWrapper<Cart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Cart::getUserId, userId);

        if (cartIds != null && !cartIds.isEmpty()) {
            queryWrapper.in(Cart::getCartId, cartIds);
        } else {
            queryWrapper.eq(Cart::getSelected, true);
        }

        return cartMapper.selectList(queryWrapper);
    }

    /**
     * 预扣库存（Redis冻结）
     */
    private boolean reserveStock(Integer productId, Long skuId, Integer quantity) {
        if (skuId != null && skuId > 0) {
            // SKU商品：使用秒杀服务的Redis预扣
            return seckillService.preDeductStock(skuId, quantity);
        } else {
            // 普通商品：检查数据库库存
            Product product = productService.getById(productId);
            return product != null && product.getStock() >= quantity;
        }
    }

    /**
     * 确认扣减数据库库存
     */
    private void confirmDeductStock(Integer productId, Long skuId, Integer quantity) {
        if (skuId != null && skuId > 0) {
            // SKU商品：扣减SKU库存（修复：使用正确的方法签名）
            productSkuService.deductStock(skuId, quantity);
        } else {
            // 普通商品：扣减商品库存
            productService.update(
                    new LambdaUpdateWrapper<Product>()
                            .eq(Product::getProductId, productId)
                            .ge(Product::getStock, quantity)
                            .setSql("stock = stock - " + quantity));
        }
    }

    /**
     * 释放预扣的库存
     */
    private void releaseReservedStock(Integer productId, Long skuId, Integer quantity) {
        if (skuId != null && skuId > 0) {
            // SKU商品：恢复Redis库存
            seckillService.restoreRedisStock(skuId, quantity);
        }
        // 普通商品在Try阶段未实际扣减数据库库存，无需恢复
    }

    /**
     * 获取商品价格
     */
    private BigDecimal getProductPrice(Product product, Long skuId) {
        if (skuId != null && skuId > 0) {
            ProductSku sku = productSkuService.getById(skuId);
            if (sku != null) {
                return sku.getPrice();
            }
        }
        return product.getPriceNew(); // 修复：使用priceNew字段
    }

    /**
     * 计算优惠券折扣
     */
    private BigDecimal calculateCouponDiscount(Long couponId, Integer userId, BigDecimal orderAmount) {
        UserCoupon userCoupon = userCouponService.getById(couponId);
        if (userCoupon == null || !userCoupon.getUserId().equals(userId)
                || !"UNUSED".equals(userCoupon.getStatus())) {
            return BigDecimal.ZERO;
        }

        // 修复：从关联的Coupon对象获取优惠券面值
        Coupon coupon = userCoupon.getCoupon();
        if (coupon != null && coupon.getValue() != null) {
            return coupon.getValue();
        }
        return BigDecimal.ZERO;
    }

    /**
     * 计算积分抵扣
     */
    private BigDecimal calculatePointsDiscount(Integer pointsUsed, Integer userId) {
        // 每100积分抵扣1元（修复：使用RoundingMode枚举）
        return BigDecimal.valueOf(pointsUsed).divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.DOWN);
    }

    /**
     * 使用优惠券
     */
    private void useCoupon(Long couponId, Integer userId, Integer orderId) {
        UserCoupon userCoupon = userCouponService.getById(couponId);
        if (userCoupon != null && userCoupon.getUserId().equals(userId)) {
            userCoupon.setStatus("USED");
            userCoupon.setUseTime(LocalDateTime.now());
            userCoupon.setOrderId(orderId.longValue());
            userCouponService.updateById(userCoupon);
        }
    }

    /**
     * 扣减积分
     */
    private void deductPoints(Integer userId, Integer points, Integer orderId) {
        // 调用用户服务扣减积分
        userService.update(
                new LambdaUpdateWrapper<User>()
                        .eq(User::getUserId, userId)
                        .ge(User::getPoints, points)
                        .setSql("points = points - " + points));
    }

    /**
     * 生成订单号
     */
    private String generateOrderNo() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
        int random = ThreadLocalRandom.current().nextInt(1000, 9999);
        return "TCC" + timestamp + random;
    }

    /**
     * 构建完整地址
     */
    private String buildFullAddress(Address address) {
        StringBuilder sb = new StringBuilder();
        if (address.getProvince() != null) sb.append(address.getProvince());
        if (address.getCity() != null) sb.append(address.getCity());
        if (address.getDistrict() != null) sb.append(address.getDistrict());
        if (address.getDetail() != null) sb.append(address.getDetail()); // 修复：使用detail字段
        return sb.toString();
    }
}
