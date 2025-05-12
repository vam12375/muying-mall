package com.muyingmall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.muyingmall.common.CacheConstants;
import com.muyingmall.common.exception.BusinessException;
import com.muyingmall.dto.OrderCreateDTO;
import com.muyingmall.entity.Cart;
import com.muyingmall.entity.Order;
import com.muyingmall.entity.OrderProduct;
import com.muyingmall.entity.Payment;
import com.muyingmall.entity.Product;
import com.muyingmall.entity.User;
import com.muyingmall.entity.UserAddress;
import com.muyingmall.enums.OrderStatus;
import com.muyingmall.enums.PaymentStatus;
import com.muyingmall.event.OrderCompletedEvent;
import com.muyingmall.mapper.CartMapper;
import com.muyingmall.mapper.OrderMapper;
import com.muyingmall.mapper.OrderProductMapper;
import com.muyingmall.mapper.UserAddressMapper;
import com.muyingmall.mapper.UserMapper;
import com.muyingmall.service.OrderService;
import com.muyingmall.service.PaymentService;
import com.muyingmall.service.ProductService;
import com.muyingmall.service.PointsService;
import com.muyingmall.util.EnumUtil;
import com.muyingmall.util.RedisUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.Set;

/**
 * 订单服务实现类
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

    // 订单状态常量定义
    private static final String ORDER_STATUS_PENDING_PAYMENT = "pending_payment"; // 待付款
    private static final String ORDER_STATUS_PENDING_SHIPMENT = "pending_shipment"; // 待发货
    private static final String ORDER_STATUS_SHIPPED = "shipped"; // 已发货
    private static final String ORDER_STATUS_COMPLETED = "completed"; // 已完成
    private static final String ORDER_STATUS_CANCELLED = "cancelled"; // 已取消

    private final UserMapper userMapper;
    private final UserAddressMapper addressMapper;
    private final CartMapper cartMapper;
    private final OrderProductMapper orderProductMapper;
    private final ProductService productService;
    private final PaymentService paymentService;
    private final ApplicationEventPublisher eventPublisher;
    private final PointsService pointsService;
    private final RedisUtil redisUtil;
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> createOrder(Integer userId, Integer addressId, String remark,
            String paymentMethod, Long couponId, List<Integer> cartIds, BigDecimal shippingFee, Integer pointsUsed) {
        // 校验用户
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 校验地址
        UserAddress address = addressMapper.selectById(addressId);
        if (address == null || !address.getUserId().equals(userId)) {
            throw new BusinessException("收货地址不存在或不属于当前用户");
        }

        // 获取购物车中已选中的商品
        List<Cart> cartList;

        // 如果指定了cartIds，则使用这些ID查询购物车
        if (cartIds != null && !cartIds.isEmpty()) {
            LambdaQueryWrapper<Cart> cartQueryWrapper = new LambdaQueryWrapper<>();
            cartQueryWrapper.eq(Cart::getUserId, userId)
                    .in(Cart::getCartId, cartIds);
            cartList = cartMapper.selectList(cartQueryWrapper);

            if (cartList.size() != cartIds.size()) {
                log.warn("部分购物车项不存在，请求数量:{}, 查询到数量:{}", cartIds.size(), cartList.size());
            }
        } else {
            // 否则，获取购物车中已选中的商品
            LambdaQueryWrapper<Cart> cartQueryWrapper = new LambdaQueryWrapper<>();
            cartQueryWrapper.eq(Cart::getUserId, userId)
                    .eq(Cart::getSelected, 1);
            cartList = cartMapper.selectList(cartQueryWrapper);
        }

        if (cartList.isEmpty()) {
            throw new BusinessException("购物车中没有选中的商品");
        }

        // 创建订单
        Order order = new Order();
        String orderNo = generateOrderNo();
        order.setOrderNo(orderNo);
        order.setUserId(userId);
        order.setStatus(EnumUtil.getOrderStatusByCode(ORDER_STATUS_PENDING_PAYMENT));
        order.setAddressId(addressId);
        order.setReceiverName(address.getReceiver());
        order.setReceiverPhone(address.getPhone());
        order.setReceiverProvince(address.getProvince());
        order.setReceiverCity(address.getCity());
        order.setReceiverDistrict(address.getDistrict());
        order.setReceiverAddress(address.getAddress());
        order.setReceiverZip(address.getZip());
        order.setRemark(remark);

        // 设置支付方式
        if (paymentMethod != null && !paymentMethod.isEmpty()) {
            order.setPaymentMethod(paymentMethod);
        }

        order.setCreateTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());

        // 计算订单总金额
        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderProduct> orderProducts = new ArrayList<>();

        for (Cart cart : cartList) {
            Product product = productService.getById(cart.getProductId());
            if (product == null) {
                throw new BusinessException("商品不存在：" + cart.getProductId());
            }

            if (!"上架".equals(product.getProductStatus())) {
                throw new BusinessException("商品已下架：" + product.getProductName());
            }

            if (product.getStock() < cart.getQuantity()) {
                throw new BusinessException("商品库存不足：" + product.getProductName());
            }

            // 创建订单商品项
            OrderProduct orderProduct = new OrderProduct();
            orderProduct.setProductId(product.getProductId());
            orderProduct.setProductName(product.getProductName());
            orderProduct.setProductImg(product.getProductImg());
            orderProduct.setPrice(product.getPriceNew());
            orderProduct.setQuantity(cart.getQuantity());
            orderProduct.setCreateTime(LocalDateTime.now());
            orderProduct.setUpdateTime(LocalDateTime.now());
            orderProducts.add(orderProduct);

            // 累加金额
            BigDecimal itemTotal = product.getPriceNew().multiply(new BigDecimal(cart.getQuantity()));
            totalAmount = totalAmount.add(itemTotal);
        }

        // 设置订单金额
        order.setTotalAmount(totalAmount);
        order.setActualAmount(totalAmount);

        // 设置运费，使用传入的运费参数
        order.setShippingFee(shippingFee != null ? shippingFee : BigDecimal.ZERO);

        // 如果有运费，将其加入实际支付金额
        if (shippingFee != null && shippingFee.compareTo(BigDecimal.ZERO) > 0) {
            order.setActualAmount(order.getActualAmount().add(shippingFee));
        }

        // 处理优惠券（如果有）
        if (couponId != null && couponId > 0) {
            // 此处添加优惠券逻辑，减少实际支付金额
            log.info("应用优惠券: {}", couponId);
            // TODO: 实现优惠券计算逻辑，以下是示例
            // UserCoupon userCoupon = userCouponService.getUserCoupon(userId, couponId);
            // if (userCoupon != null && userCoupon.getStatus().equals("UNUSED")) {
            // BigDecimal discountAmount = calculateCouponDiscount(userCoupon, totalAmount);
            // order.setActualAmount(totalAmount.subtract(discountAmount));
            // order.setCouponId(couponId);
            // order.setCouponAmount(discountAmount);
            // }

            // 为了演示，直接减去优惠金额50元
            BigDecimal discountAmount = new BigDecimal("50");
            if (totalAmount.compareTo(discountAmount) > 0) {
                order.setActualAmount(order.getActualAmount().subtract(discountAmount));
                order.setCouponId(couponId);
                order.setCouponAmount(discountAmount);
            }
        }

        // 处理积分抵扣（如果有）
        if (pointsUsed != null && pointsUsed > 0) {
            // 获取用户当前积分
            Integer userPoints = pointsService.getUserPoints(userId);
            if (userPoints < pointsUsed) {
                throw new BusinessException("积分不足，当前积分：" + userPoints);
            }

            // 计算积分抵扣金额（每100积分抵扣1元）
            BigDecimal pointsDiscountAmount = new BigDecimal(pointsUsed).divide(new BigDecimal("100"), 2,
                    BigDecimal.ROUND_DOWN);

            // 限制最大抵扣金额为50元
            BigDecimal maxDiscount = new BigDecimal("50");
            if (pointsDiscountAmount.compareTo(maxDiscount) > 0) {
                pointsDiscountAmount = maxDiscount;
                // 重新计算实际使用的积分
                pointsUsed = 5000; // 最多使用5000积分
            }

            // 确保抵扣金额不超过应付金额
            if (pointsDiscountAmount.compareTo(order.getActualAmount()) > 0) {
                pointsDiscountAmount = order.getActualAmount();
                // 重新计算实际使用的积分
                pointsUsed = pointsDiscountAmount.multiply(new BigDecimal("100")).intValue();
            }

            // 更新订单信息
            order.setPointsUsed(pointsUsed);
            order.setPointsDiscount(pointsDiscountAmount);
            order.setActualAmount(order.getActualAmount().subtract(pointsDiscountAmount));

            // 扣减用户积分
            boolean deductSuccess = pointsService.deductPoints(userId, pointsUsed, "order", orderNo, "订单抵扣");
            if (!deductSuccess) {
                throw new BusinessException("积分扣减失败");
            }

            log.info("订单 {} 使用积分 {} 抵扣金额 {}", orderNo, pointsUsed, pointsDiscountAmount);
        }

        // 保存订单
        save(order);

        // 保存订单商品
        for (OrderProduct orderProduct : orderProducts) {
            orderProduct.setOrderId(order.getOrderId());
            orderProductMapper.insert(orderProduct);
        }

        // 减少商品库存
        for (Cart cart : cartList) {
            productService.update(
                    new LambdaUpdateWrapper<Product>()
                            .eq(Product::getProductId, cart.getProductId())
                            .setSql("stock = stock - " + cart.getQuantity()));
        }

        // 清空购物车中已购买的商品
        for (Cart cart : cartList) {
            cartMapper.deleteById(cart.getCartId());
        }

        // 返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("orderId", order.getOrderId());
        result.put("orderNo", order.getOrderNo());
        result.put("totalAmount", order.getTotalAmount());
        result.put("actualAmount", order.getActualAmount());
        result.put("orderNumber", order.getOrderNo());

        // 清除用户订单列表缓存
        clearUserOrderListCache(userId);

        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public Order getOrderDetail(Integer orderId, Integer userId) {
        if (orderId == null) {
            return null;
        }

        // 构建缓存键
        String cacheKey = CacheConstants.ORDER_DETAIL_KEY + orderId;

        // 查询缓存
        Object cacheResult = redisUtil.get(cacheKey);
        if (cacheResult != null) {
            Order cachedOrder = (Order) cacheResult;
            // 验证权限
            if (userId != null && !userId.equals(cachedOrder.getUserId())) {
                throw new BusinessException("无权查看该订单");
            }
            log.debug("从缓存中获取订单详情: orderId={}", orderId);
            return cachedOrder;
        }

        // 缓存未命中，从数据库查询
        log.debug("缓存未命中，从数据库查询订单详情: orderId={}", orderId);
        Order order = getById(orderId);

        if (order == null) {
            return null;
        }

        // 验证权限
        if (userId != null && !userId.equals(order.getUserId())) {
            throw new BusinessException("无权查看该订单");
        }

        // 获取订单商品
        LambdaQueryWrapper<OrderProduct> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrderProduct::getOrderId, orderId);
        List<OrderProduct> orderProducts = orderProductMapper.selectList(queryWrapper);
        order.setProducts(orderProducts);

        // 缓存结果
        redisUtil.set(cacheKey, order, CacheConstants.ORDER_EXPIRE_TIME);
        log.debug("将订单详情缓存到Redis: orderId={}", orderId);

        return order;
    }

    @Override
    public Page<Order> getUserOrders(Integer userId, int page, int size, String status) {
        if (userId == null) {
            return new Page<>();
        }

        // 构建缓存键
        StringBuilder cacheKey = new StringBuilder(CacheConstants.USER_ORDER_LIST_KEY);
        cacheKey.append(userId)
                .append("_page_").append(page)
                .append("_size_").append(size);

        if (StringUtils.hasText(status)) {
            // 处理status大小写问题，确保与前端匹配
            String normalizedStatus = normalizeOrderStatus(status);
            cacheKey.append("_status_").append(normalizedStatus);
        }

        // 查询缓存
        Object cacheResult = redisUtil.get(cacheKey.toString());
        if (cacheResult != null) {
            log.debug("从缓存中获取用户订单列表: userId={}, page={}, size={}, status={}", userId, page, size, status);
            return (Page<Order>) cacheResult;
        }

        // 缓存未命中，从数据库查询
        log.debug("缓存未命中，从数据库查询用户订单列表: userId={}, page={}, size={}, status={}", userId, page, size, status);

        Page<Order> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Order> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Order::getUserId, userId);

        // 如果指定了状态，则按状态筛选
        if (StringUtils.hasText(status)) {
            // 处理status大小写问题，确保与前端匹配
            String normalizedStatus = normalizeOrderStatus(status);
            OrderStatus orderStatus = EnumUtil.getOrderStatusByCode(normalizedStatus);
            if (orderStatus != null) {
                queryWrapper.eq(Order::getStatus, orderStatus);
            }
            log.info("查询订单，用户ID: {}, 状态: {} (原始状态: {})", userId, normalizedStatus, status);
        } else {
            log.info("查询所有状态订单，用户ID: {}", userId);
        }

        // 按创建时间倒序排序
        queryWrapper.orderByDesc(Order::getCreateTime);

        Page<Order> orderPage = page(pageParam, queryWrapper);
        log.info("查询到订单总数: {}", orderPage.getTotal());

        // 查询订单商品
        List<Order> orders = orderPage.getRecords();
        if (!orders.isEmpty()) {
            List<Integer> orderIds = orders.stream()
                    .map(Order::getOrderId)
                    .collect(Collectors.toList());

            log.info("开始查询订单商品，订单ID: {}", orderIds);

            LambdaQueryWrapper<OrderProduct> productQueryWrapper = new LambdaQueryWrapper<>();
            productQueryWrapper.in(OrderProduct::getOrderId, orderIds);
            List<OrderProduct> allOrderProducts = orderProductMapper.selectList(productQueryWrapper);

            log.info("查询到 {} 条订单商品记录", allOrderProducts.size());

            // 为每个订单设置商品
            for (Order order : orders) {
                List<OrderProduct> orderProducts = allOrderProducts.stream()
                        .filter(op -> op.getOrderId().equals(order.getOrderId()))
                        .peek(this::processOrderProductSpecs)
                        .collect(Collectors.toList());

                if (orderProducts.isEmpty()) {
                    log.warn("订单ID {} 没有关联商品数据", order.getOrderId());
                }

                order.setProducts(orderProducts);
            }
        }

        // 缓存结果
        redisUtil.set(cacheKey.toString(), orderPage, CacheConstants.ORDER_LIST_EXPIRE_TIME);
        log.debug("将用户订单列表缓存到Redis: userId={}, page={}, size={}, status={}", userId, page, size, status);

        return orderPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean cancelOrder(Integer orderId, Integer userId) {
        Order order = getById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }

        // 验证订单属于当前用户
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException("无权限取消此订单");
        }

        // 只有待支付状态的订单可以取消
        if (!OrderStatus.PENDING_PAYMENT.equals(order.getStatus())) {
            throw new BusinessException("当前订单状态不可取消");
        }

        // 更新订单状态
        order.setStatus(OrderStatus.CANCELLED);
        order.setCancelTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        updateById(order);

        // 恢复库存
        LambdaQueryWrapper<OrderProduct> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrderProduct::getOrderId, orderId);
        List<OrderProduct> orderProducts = orderProductMapper.selectList(queryWrapper);

        for (OrderProduct orderProduct : orderProducts) {
            productService.update(
                    new LambdaUpdateWrapper<Product>()
                            .eq(Product::getProductId, orderProduct.getProductId())
                            .setSql("stock = stock + " + orderProduct.getQuantity()));
        }

        // 清除订单缓存
        clearOrderCache(orderId, userId);

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> payOrder(Integer orderId, Integer userId, String paymentMethod) {
        Order order = getById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }

        // 验证订单属于当前用户
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException("无权限支付此订单");
        }

        // 只有待支付状态的订单可以支付
        if (!OrderStatus.PENDING_PAYMENT.equals(order.getStatus())) {
            throw new BusinessException("当前订单状态不可支付");
        }

        // 创建支付记录
        Payment payment = new Payment();
        payment.setPaymentNo(generatePaymentNo());
        payment.setOrderId(order.getOrderId());
        payment.setOrderNo(order.getOrderNo());
        payment.setUserId(userId);
        payment.setAmount(order.getActualAmount());
        payment.setPaymentMethod(paymentMethod);
        payment.setStatus(PaymentStatus.PENDING); // 使用枚举值
        payment.setCreateTime(LocalDateTime.now());
        payment.setUpdateTime(LocalDateTime.now());
        payment.setExpireTime(LocalDateTime.now().plusHours(2)); // 2小时过期

        // 保存支付记录
        paymentService.createPayment(payment);

        // 更新订单支付ID
        order.setPaymentId(payment.getId());
        order.setPaymentMethod(paymentMethod);
        order.setUpdateTime(LocalDateTime.now());
        updateById(order);

        // 清除订单缓存
        clearOrderCache(orderId, userId);

        // 返回支付信息
        Map<String, Object> result = new HashMap<>();
        result.put("paymentId", payment.getId());
        result.put("paymentNo", payment.getPaymentNo());
        result.put("amount", payment.getAmount());
        result.put("paymentMethod", payment.getPaymentMethod());
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean confirmReceive(Integer userId, Integer orderId) {
        Order order = getById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }

        // 验证订单属于当前用户
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException("无权限操作此订单");
        }

        // 只有已发货状态的订单可以确认收货
        if (!OrderStatus.SHIPPED.equals(order.getStatus())) {
            throw new BusinessException("当前订单状态不可确认收货");
        }

        // 记录旧状态，用于事件
        OrderStatus oldStatus = order.getStatus();

        // 更新订单状态
        order.setStatus(OrderStatus.COMPLETED);
        order.setCompletionTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        boolean result = updateById(order);

        // 如果更新成功，发布订单完成事件
        if (result) {
            log.info("订单 {} 状态更新为 COMPLETED 成功", orderId);
            try {
                eventPublisher.publishEvent(new OrderCompletedEvent(
                        order.getOrderId(),
                        order.getUserId(),
                        order.getActualAmount(),
                        order.getOrderNo()));
                log.info("成功发布 OrderCompletedEvent for Order ID {}", order.getOrderId());
            } catch (Exception pubEx) {
                // 事件发布失败不应影响主流程，但需要记录错误
                log.error("发布 OrderCompletedEvent 失败 for Order ID {}: {}", order.getOrderId(), pubEx.getMessage(),
                        pubEx);
            }
        } else {
            log.error("订单 {} 状态更新为 COMPLETED 失败", orderId);
            // 考虑是否抛出异常或返回false有不同含义
        }

        // 清除订单缓存
        clearOrderCache(orderId, userId);

        return result;
    }

    @Override
    public Page<Order> getAdminOrders(int page, int size, String status) {
        return getOrdersByAdmin(page, size, status, null, null);
    }

    @Override
    public Page<Order> getOrdersByAdmin(int page, int size, String status, String orderNo, Integer userId) {
        Page<Order> pageParam = new Page<>(page, size);

        LambdaQueryWrapper<Order> queryWrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(status)) {
            queryWrapper.eq(Order::getStatus, EnumUtil.getOrderStatusByCode(status));
        }

        if (StringUtils.hasText(orderNo)) {
            queryWrapper.eq(Order::getOrderNo, orderNo);
        }

        if (userId != null) {
            queryWrapper.eq(Order::getUserId, userId);
        }

        queryWrapper.orderByDesc(Order::getCreateTime);

        Page<Order> orderPage = page(pageParam, queryWrapper);

        // 查询订单商品
        List<Order> orders = orderPage.getRecords();
        if (!orders.isEmpty()) {
            List<Integer> orderIds = orders.stream()
                    .map(Order::getOrderId)
                    .collect(Collectors.toList());

            LambdaQueryWrapper<OrderProduct> productQueryWrapper = new LambdaQueryWrapper<>();
            productQueryWrapper.in(OrderProduct::getOrderId, orderIds);
            List<OrderProduct> allOrderProducts = orderProductMapper.selectList(productQueryWrapper);

            // 为每个订单设置商品
            for (Order order : orders) {
                List<OrderProduct> orderProducts = allOrderProducts.stream()
                        .filter(op -> op.getOrderId().equals(order.getOrderId()))
                        .collect(Collectors.toList());
                order.setProducts(orderProducts);
            }
        }

        return orderPage;
    }

    @Override
    public Order getOrderDetailByAdmin(Integer orderId) {
        Order order = getById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }

        // 查询订单商品
        LambdaQueryWrapper<OrderProduct> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrderProduct::getOrderId, orderId);
        List<OrderProduct> orderProducts = orderProductMapper.selectList(queryWrapper);
        order.setProducts(orderProducts);

        // 查询支付信息，并将支付信息的关键字段添加到订单实体中
        if (order.getPaymentId() != null) {
            Payment payment = paymentService.getById(order.getPaymentId());
            if (payment != null) {
                // 设置支付流水号
                order.setTransactionId(payment.getTransactionId());

                // 设置支付时间（优先使用expire_time）
                order.setExpireTime(payment.getExpireTime());

                // 如果payTime为空，但payment中有记录，也同步过来
                if (order.getPayTime() == null && payment.getPayTime() != null) {
                    order.setPayTime(payment.getPayTime());
                }
            }
        }

        return order;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateOrderStatusByAdmin(Integer orderId, String status, String remark) {
        Order order = getById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }

        // 更新订单状态
        order.setStatus(EnumUtil.getOrderStatusByCode(status));
        order.setRemark(remark);
        order.setUpdateTime(LocalDateTime.now());

        // 根据状态设置相应的时间
        if (ORDER_STATUS_CANCELLED.equals(status)) {
            order.setCancelTime(LocalDateTime.now());
        } else if (ORDER_STATUS_SHIPPED.equals(status)) {
            order.setShippingTime(LocalDateTime.now());
        } else if (ORDER_STATUS_COMPLETED.equals(status)) {
            order.setCompletionTime(LocalDateTime.now());
        }

        boolean result = updateById(order);

        // 清除订单缓存
        if (result) {
            clearOrderCache(orderId, order.getUserId());
        }

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean shipOrder(Integer orderId, String shippingCompany, String trackingNo) {
        Order order = getById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }

        // 只有待发货状态的订单可以发货
        if (!OrderStatus.PENDING_SHIPMENT.equals(order.getStatus())) {
            throw new BusinessException("当前订单状态不可发货");
        }

        // 更新订单状态
        order.setStatus(OrderStatus.SHIPPED);
        order.setShippingTime(LocalDateTime.now());
        order.setShippingCompany(shippingCompany);
        order.setTrackingNo(trackingNo);
        order.setUpdateTime(LocalDateTime.now());
        boolean result = updateById(order);

        // 清除订单缓存
        if (result) {
            clearOrderCache(orderId, order.getUserId());
        }

        return result;
    }

    @Override
    public Order getOrderByOrderNo(String orderNo, Integer userId) {
        LambdaQueryWrapper<Order> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Order::getOrderNo, orderNo);

        if (userId != null) {
            queryWrapper.eq(Order::getUserId, userId);
        }

        Order order = getOne(queryWrapper);

        if (order != null) {
            // 查询订单商品
            LambdaQueryWrapper<OrderProduct> productQueryWrapper = new LambdaQueryWrapper<>();
            productQueryWrapper.eq(OrderProduct::getOrderId, order.getOrderId());
            List<OrderProduct> orderProducts = orderProductMapper.selectList(productQueryWrapper);
            order.setProducts(orderProducts);
        }

        return order;
    }

    @Override
    public Map<String, Object> getOrderStatistics(Integer userId) {
        Map<String, Object> statistics = new HashMap<>();

        LambdaQueryWrapper<Order> queryWrapper = new LambdaQueryWrapper<>();
        if (userId != null) {
            queryWrapper.eq(Order::getUserId, userId);
        }

        // 总订单数
        long totalCount = count(queryWrapper);
        statistics.put("totalCount", totalCount);

        // 待付款订单数
        LambdaQueryWrapper<Order> pendingPaymentWrapper = new LambdaQueryWrapper<>();
        if (userId != null) {
            pendingPaymentWrapper.eq(Order::getUserId, userId);
        }
        pendingPaymentWrapper.eq(Order::getStatus, OrderStatus.PENDING_PAYMENT);
        long pendingPaymentCount = count(pendingPaymentWrapper);
        statistics.put("pendingPaymentCount", pendingPaymentCount);

        // 待发货订单数
        LambdaQueryWrapper<Order> pendingShipmentWrapper = new LambdaQueryWrapper<>();
        if (userId != null) {
            pendingShipmentWrapper.eq(Order::getUserId, userId);
        }
        pendingShipmentWrapper.eq(Order::getStatus, OrderStatus.PENDING_SHIPMENT);
        long pendingShipmentCount = count(pendingShipmentWrapper);
        statistics.put("pendingShipmentCount", pendingShipmentCount);

        // 已发货订单数
        LambdaQueryWrapper<Order> shippedWrapper = new LambdaQueryWrapper<>();
        if (userId != null) {
            shippedWrapper.eq(Order::getUserId, userId);
        }
        shippedWrapper.eq(Order::getStatus, OrderStatus.SHIPPED);
        long shippedCount = count(shippedWrapper);
        statistics.put("shippedCount", shippedCount);

        // 已完成订单数
        LambdaQueryWrapper<Order> completedWrapper = new LambdaQueryWrapper<>();
        if (userId != null) {
            completedWrapper.eq(Order::getUserId, userId);
        }
        completedWrapper.eq(Order::getStatus, OrderStatus.COMPLETED);
        long completedCount = count(completedWrapper);
        statistics.put("completedCount", completedCount);

        // 已取消订单数
        LambdaQueryWrapper<Order> cancelledWrapper = new LambdaQueryWrapper<>();
        if (userId != null) {
            cancelledWrapper.eq(Order::getUserId, userId);
        }
        cancelledWrapper.eq(Order::getStatus, OrderStatus.CANCELLED);
        long cancelledCount = count(cancelledWrapper);
        statistics.put("cancelledCount", cancelledCount);

        return statistics;
    }

    /**
     * 生成订单号
     */
    private String generateOrderNo() {
        return "OD" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 6);
    }

    /**
     * 生成支付号
     */
    private String generatePaymentNo() {
        return "PY" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 6);
    }

    /**
     * 处理订单商品的规格信息
     * 确保specs字段正确解析并可供前端使用
     */
    private void processOrderProductSpecs(OrderProduct orderProduct) {
        try {
            String specs = orderProduct.getSpecs();
            if (specs != null) {
                // 如果specs是BLOB数据，这里可能需要特殊处理
                // 目前简单确保它是有效的JSON字符串
                if (specs.startsWith("[") && specs.endsWith("]")) {
                    // 看起来已经是有效的JSON数组格式，不需要处理
                } else {
                    // 可能是序列化后的二进制数据，尝试转换为JSON字符串数组
                    orderProduct.setSpecs("[]");
                    log.warn("订单商品ID {} 的specs字段格式无法识别，已设置为空数组", orderProduct.getId());
                }
            } else {
                // 如果specs为null，设置默认空数组
                orderProduct.setSpecs("[]");
            }
        } catch (Exception e) {
            // 出现异常时设置为空数组并记录日志
            orderProduct.setSpecs("[]");
            log.error("处理订单商品ID {} 的specs字段时出错: {}", orderProduct.getId(), e.getMessage(), e);
        }
    }

    /**
     * 标准化订单状态，确保与前端期望的格式一致
     */
    private String normalizeOrderStatus(String status) {
        if (status == null) {
            return null;
        }

        // 统一转为小写并去除空格
        String normalized = status.toLowerCase().trim();

        // 处理特殊情况
        switch (normalized) {
            case "pending_payment":
            case "pendingpayment":
            case "pending payment":
                return "pending_payment";
            case "pending_shipment":
            case "pendingshipment":
            case "pending shipment":
                return "pending_shipment";
            case "pending_receive":
            case "pendingreceive":
            case "pending receive":
            case "shipped":
                return "shipped";
            case "completed":
            case "complete":
                return "completed";
            case "cancelled":
            case "canceled":
                return "cancelled";
            case "closed":
                return "closed";
            default:
                return normalized;
        }
    }

    /**
     * 清除订单相关缓存
     *
     * @param orderId 订单ID
     * @param userId  用户ID
     */
    private void clearOrderCache(Integer orderId, Integer userId) {
        if (orderId == null) {
            return;
        }

        // 清除订单详情缓存
        String orderDetailCacheKey = CacheConstants.ORDER_DETAIL_KEY + orderId;
        redisUtil.del(orderDetailCacheKey);
        log.debug("清除订单详情缓存: orderId={}", orderId);

        // 清除用户订单列表缓存
        if (userId != null) {
            clearUserOrderListCache(userId);
        }

        // 清除订单统计缓存
        String orderStatsCacheKey = CacheConstants.ORDER_STATS_KEY + "*";
        Set<String> statsKeys = redisTemplate.keys(orderStatsCacheKey);
        if (statsKeys != null && !statsKeys.isEmpty()) {
            redisTemplate.delete(statsKeys);
            log.debug("清除订单统计缓存");
        }
    }

    /**
     * 清除用户订单列表缓存
     *
     * @param userId 用户ID
     */
    private void clearUserOrderListCache(Integer userId) {
        if (userId == null) {
            return;
        }

        // 清除用户订单列表缓存
        String userOrderListCacheKey = CacheConstants.USER_ORDER_LIST_KEY + userId + "*";
        Set<String> keys = redisTemplate.keys(userOrderListCacheKey);
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
            log.debug("清除用户订单列表缓存: userId={}", userId);
        }
    }
}