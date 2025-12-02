package com.muyingmall.service.impl;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.muyingmall.common.constants.CacheConstants;
import com.muyingmall.common.exception.BusinessException;
import com.muyingmall.dto.OrderCreateDTO;
import com.muyingmall.entity.Cart;
import com.muyingmall.entity.Order;
import com.muyingmall.entity.OrderProduct;
import com.muyingmall.entity.Payment;
import com.muyingmall.entity.Product;
import com.muyingmall.entity.User;
import com.muyingmall.entity.UserAddress;
import com.muyingmall.entity.UserCoupon;
import com.muyingmall.entity.Coupon;
import com.muyingmall.enums.OrderStatus;
import com.muyingmall.enums.PaymentStatus;
import com.muyingmall.event.OrderCompletedEvent;
import com.muyingmall.event.OrderStatusChangedEvent;
import com.muyingmall.mapper.CartMapper;
import com.muyingmall.mapper.OrderMapper;
import com.muyingmall.mapper.OrderProductMapper;
import com.muyingmall.mapper.UserAddressMapper;
import com.muyingmall.mapper.UserMapper;
import com.muyingmall.service.OrderService;
import com.muyingmall.service.PaymentService;
import com.muyingmall.service.ProductService;
import com.muyingmall.service.PointsService;
import com.muyingmall.service.CouponService;
import com.muyingmall.service.UserCouponService;
import com.muyingmall.service.MessageProducerService;
import com.muyingmall.service.ProductSkuService;
import com.muyingmall.entity.ProductSku;
import com.muyingmall.dto.OrderMessage;
import com.muyingmall.dto.SkuStockDTO;
import com.muyingmall.util.EnumUtil;
import com.muyingmall.util.RedisUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
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
    private final ObjectMapper objectMapper;
    private final CouponService couponService;
    private final UserCouponService userCouponService;
    private final MessageProducerService messageProducerService;
    private final ProductSkuService productSkuService;

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

        // 用于记录需要扣减库存的SKU列表
        List<SkuStockDTO> skuStockList = new ArrayList<>();

        for (Cart cart : cartList) {
            Product product = productService.getById(cart.getProductId());
            if (product == null) {
                throw new BusinessException("商品不存在：" + cart.getProductId());
            }

            if (!"上架".equals(product.getProductStatus())) {
                throw new BusinessException("商品已下架：" + product.getProductName());
            }

            // 创建订单商品项
            OrderProduct orderProduct = new OrderProduct();
            orderProduct.setProductId(product.getProductId());
            orderProduct.setProductName(product.getProductName());
            orderProduct.setProductImg(product.getProductImg());
            orderProduct.setQuantity(cart.getQuantity());
            orderProduct.setCreateTime(LocalDateTime.now());
            orderProduct.setUpdateTime(LocalDateTime.now());

            // 判断是否有SKU
            BigDecimal itemPrice;
            if (cart.getSkuId() != null) {
                // 有SKU，使用SKU的价格和库存
                ProductSku sku = productSkuService.getById(cart.getSkuId());
                if (sku == null) {
                    throw new BusinessException("商品规格不存在：" + product.getProductName());
                }
                if (sku.getStock() < cart.getQuantity()) {
                    throw new BusinessException("商品规格库存不足：" + product.getProductName() + " - " + sku.getSkuName());
                }
                
                itemPrice = sku.getPrice();
                orderProduct.setPrice(itemPrice);
                orderProduct.setSkuId(sku.getSkuId());
                orderProduct.setSkuCode(sku.getSkuCode());
                orderProduct.setSpecs(sku.getSpecValues());
                // 使用SKU图片（如果有）
                if (sku.getSkuImage() != null && !sku.getSkuImage().isEmpty()) {
                    orderProduct.setProductImg(sku.getSkuImage());
                }
                
                // 记录需要扣减的SKU库存
                SkuStockDTO stockDTO = new SkuStockDTO();
                stockDTO.setSkuId(sku.getSkuId());
                stockDTO.setQuantity(cart.getQuantity());
                skuStockList.add(stockDTO);
            } else {
                // 无SKU，使用商品主表的价格和库存
                if (product.getStock() < cart.getQuantity()) {
                    throw new BusinessException("商品库存不足：" + product.getProductName());
                }
                itemPrice = product.getPriceNew();
                orderProduct.setPrice(itemPrice);
                // 使用购物车中的规格信息（兼容旧数据）
                orderProduct.setSpecs(cart.getSpecs());
            }
            
            orderProducts.add(orderProduct);

            // 累加金额
            BigDecimal itemTotal = itemPrice.multiply(new BigDecimal(cart.getQuantity()));
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

        // 计算优惠金额
        BigDecimal couponAmount = BigDecimal.ZERO;

        // 处理优惠券（如果有）
        if (couponId != null && couponId > 0) {
            // 获取用户优惠券
            UserCoupon userCoupon = userCouponService.getById(couponId);
            if (userCoupon != null && userCoupon.getUserId().equals(userId)
                    && userCoupon.getStatus().equals("UNUSED")) {
                // 获取优惠券信息
                Coupon coupon = couponService.getById(userCoupon.getCouponId());
                if (coupon != null && "ACTIVE".equals(coupon.getStatus())) {
                    // 验证优惠券是否可用
                    if (order.getTotalAmount().compareTo(coupon.getMinSpend()) >= 0) {
                        couponAmount = coupon.getValue();
                        // 记录使用的优惠券
                        order.setCouponId(couponId);
                        order.setCouponAmount(couponAmount);

                        // 更新优惠券状态
                        userCoupon.setStatus("USED");
                        userCoupon.setUseTime(LocalDateTime.now());
                        // 只有在订单ID不为null时才设置OrderId，避免NullPointerException
                        if (order.getOrderId() != null) {
                            userCoupon.setOrderId(order.getOrderId().longValue());
                            userCouponService.updateById(userCoupon);
                        } else {
                            log.warn("订单ID为null，暂不更新优惠券状态，将在订单保存后更新");
                            // 标记此优惠券需要在后续更新
                            order.setCouponId(couponId); // 保存优惠券ID，后续可根据需要更新
                        }
                    } else {
                        log.warn("优惠券不可用于此订单，最低消费金额不足: 订单总额={}, 优惠券最低金额={}",
                                order.getTotalAmount(), coupon.getMinSpend());
                    }
                }
            }
        }

        // 计算积分抵扣金额
        BigDecimal pointsAmount = BigDecimal.ZERO;

        // 处理积分抵扣（如果有）
        if (pointsUsed != null && pointsUsed > 0) {
            // 获取用户当前积分
            Integer userPoints = pointsService.getUserPoints(userId);
            if (userPoints < pointsUsed) {
                throw new BusinessException("积分不足，当前积分：" + userPoints);
            }

            // 计算积分抵扣金额（每100积分抵扣1元）
            pointsAmount = new BigDecimal(pointsUsed).divide(new BigDecimal("100"), 2, RoundingMode.DOWN);

            // 限制最大抵扣金额为50元
            BigDecimal maxDiscount = new BigDecimal("50");
            if (pointsAmount.compareTo(maxDiscount) > 0) {
                pointsAmount = maxDiscount;
                // 重新计算实际使用的积分
                pointsUsed = 5000; // 最多使用5000积分
            }

            // 记录使用的积分
            order.setPointsUsed(pointsUsed);
            order.setPointsDiscount(pointsAmount);

            // 扣减用户积分
            boolean deductSuccess = pointsService.deductPoints(userId, pointsUsed, "order", orderNo, "订单抵扣");
            if (!deductSuccess) {
                throw new BusinessException("积分扣减失败");
            }

            log.info("订单 {} 使用积分 {} 抵扣金额 {}", orderNo, pointsUsed, pointsAmount);
        }

        // 计算实际支付金额
        BigDecimal actualAmount = order.getTotalAmount()
                .add(order.getShippingFee())
                .subtract(couponAmount)
                .subtract(pointsAmount);
        // 确保金额不为负数
        order.setActualAmount(actualAmount.max(BigDecimal.ZERO));

        // 保存订单
        save(order);

        // 添加调试日志，检查订单ID是否成功回填
        log.info("保存订单后的订单ID: {}", order.getOrderId());

        // 检查订单ID是否为null，如果为null则手动查询获取
        if (order.getOrderId() == null) {
            log.warn("订单ID为null，尝试通过订单号查询获取订单ID");

            // 根据订单号查询刚刚创建的订单
            LambdaQueryWrapper<Order> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Order::getOrderNo, order.getOrderNo());
            Order savedOrder = getOne(queryWrapper);

            if (savedOrder != null && savedOrder.getOrderId() != null) {
                log.info("通过订单号查询成功获取订单ID: {}", savedOrder.getOrderId());
                order.setOrderId(savedOrder.getOrderId());
            } else {
                log.error("无法获取订单ID，订单号: {}", order.getOrderNo());
                throw new BusinessException(500, "创建订单失败：无法获取订单ID");
            }
        }

        // 验证订单ID不为null
        if (order.getOrderId() == null) {
            log.error("订单ID仍然为null，无法继续处理");
            throw new BusinessException(500, "创建订单失败：订单ID为null");
        }

        // 保存订单商品
        for (OrderProduct orderProduct : orderProducts) {
            orderProduct.setOrderId(order.getOrderId());
            orderProductMapper.insert(orderProduct);
        }

        // 扣减SKU库存（如果有）
        if (!skuStockList.isEmpty()) {
            for (SkuStockDTO stockDTO : skuStockList) {
                stockDTO.setOrderId(order.getOrderId());
                stockDTO.setRemark("订单创建扣减库存");
            }
            productSkuService.batchDeductStock(skuStockList);
            log.info("订单 {} SKU库存扣减完成，共 {} 个SKU", order.getOrderNo(), skuStockList.size());
        }

        // 减少商品主表库存（仅对无SKU的商品）
        for (Cart cart : cartList) {
            if (cart.getSkuId() == null) {
                productService.update(
                        new LambdaUpdateWrapper<Product>()
                                .eq(Product::getProductId, cart.getProductId())
                                .setSql("stock = stock - " + cart.getQuantity()));
            }
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

        // 订单ID获取成功后，处理之前因为订单ID为null而未完成的优惠券状态更新
        if (couponId != null && couponId > 0) {
            UserCoupon userCoupon = userCouponService.getById(couponId);
            if (userCoupon != null && userCoupon.getUserId().equals(userId)
                    && "UNUSED".equals(userCoupon.getStatus())) {
                log.info("更新优惠券状态，订单ID: {}, 优惠券ID: {}", order.getOrderId(), couponId);
                userCoupon.setStatus("USED");
                userCoupon.setUseTime(LocalDateTime.now());
                userCoupon.setOrderId(order.getOrderId().longValue());
                userCouponService.updateById(userCoupon);
            }
        }

        // 发送订单创建消息
        try {
            OrderMessage orderMessage = OrderMessage.createOrderEvent(
                    order.getOrderId(),
                    order.getOrderNo(),
                    order.getUserId(),
                    order.getTotalAmount()
            );
            messageProducerService.sendOrderMessage(orderMessage);
            log.info("订单创建消息发送成功: orderId={}, orderNo={}", order.getOrderId(), order.getOrderNo());
        } catch (Exception e) {
            // 消息发送失败不影响主流程，但需要记录日志
            log.error("订单创建消息发送失败: orderId={}, orderNo={}, error={}", 
                    order.getOrderId(), order.getOrderNo(), e.getMessage(), e);
        }

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

        // 检查当前状态是否可以取消
        OrderStatus currentStatus = order.getStatus();
        if (!currentStatus.canTransitionTo(OrderStatus.CANCELLED)) {
            throw new BusinessException("当前订单状态不可取消");
        }

        // 保存旧状态用于发送消息通知
        String oldStatus = order.getStatus().getCode();

        // 更新订单状态
        order.setStatus(OrderStatus.CANCELLED);
        order.setCancelTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        boolean result = updateById(order);

        if (result) {
            // 恢复库存
            LambdaQueryWrapper<OrderProduct> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(OrderProduct::getOrderId, orderId);
            List<OrderProduct> orderProducts = orderProductMapper.selectList(queryWrapper);

            // 收集需要恢复的SKU库存
            List<SkuStockDTO> skuStockList = new ArrayList<>();
            
            for (OrderProduct orderProduct : orderProducts) {
                if (orderProduct.getSkuId() != null) {
                    // 有SKU，恢复SKU库存
                    SkuStockDTO stockDTO = new SkuStockDTO();
                    stockDTO.setSkuId(orderProduct.getSkuId());
                    stockDTO.setQuantity(orderProduct.getQuantity());
                    stockDTO.setOrderId(orderId);
                    stockDTO.setRemark("订单取消恢复库存");
                    skuStockList.add(stockDTO);
                } else {
                    // 无SKU，恢复商品主表库存
                    productService.update(
                            new LambdaUpdateWrapper<Product>()
                                    .eq(Product::getProductId, orderProduct.getProductId())
                                    .setSql("stock = stock + " + orderProduct.getQuantity()));
                }
            }
            
            // 批量恢复SKU库存
            if (!skuStockList.isEmpty()) {
                productSkuService.batchRestoreStock(skuStockList);
                log.info("订单 {} 取消，SKU库存恢复完成，共 {} 个SKU", orderId, skuStockList.size());
            }

            // 清除订单缓存
            clearOrderCache(orderId, userId);

            // 发送订单状态变更消息通知
            sendOrderStatusChangeNotification(order, oldStatus, ORDER_STATUS_CANCELLED);
        }

        return result;
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
        String oldStatus = order.getStatus().getCode();

        // 更新订单状态
        order.setStatus(OrderStatus.COMPLETED);
        order.setCompletionTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        boolean result = updateById(order);

        // 如果更新成功，发布订单完成事件
        if (result) {
            log.info("订单 {} 状态更新为 COMPLETED 成功", orderId);
            try {
                // 发布订单完成事件
                eventPublisher.publishEvent(new OrderCompletedEvent(
                        order.getOrderId(),
                        order.getUserId(),
                        order.getActualAmount(),
                        order.getOrderNo()));
                log.info("成功发布 OrderCompletedEvent for Order ID {}", order.getOrderId());

                // 发送订单状态变更消息通知
                sendOrderStatusChangeNotification(order, oldStatus, ORDER_STATUS_COMPLETED);
            } catch (Exception pubEx) {
                // 事件发布失败不应影响主流程，但需要记录错误
                log.error("发布事件失败 for Order ID {}: {}", order.getOrderId(), pubEx.getMessage(),
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

        // 保存旧状态用于发送消息通知
        String oldStatus = order.getStatus().getCode();

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

            // 发送订单状态变更消息通知
            sendOrderStatusChangeNotification(order, oldStatus, status);
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

        // 保存旧状态用于发送消息通知
        String oldStatus = order.getStatus().getCode();

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

            // 发送订单状态变更消息通知
            sendOrderStatusChangeNotification(order, oldStatus, ORDER_STATUS_SHIPPED);
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

    @Override
    public BigDecimal getSalesBetween(LocalDateTime startTime, LocalDateTime endTime) {
        try {
            // 查询指定时间段内的已完成订单
            QueryWrapper<Order> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("status", "completed") // 假设status=completed表示已完成
                    .between("create_time", startTime, endTime);

            List<Order> orders = this.list(queryWrapper);

            // 计算总销售额
            BigDecimal totalSales = BigDecimal.ZERO;
            if (orders != null && !orders.isEmpty()) {
                for (Order order : orders) {
                    if (order.getActualAmount() != null) {
                        totalSales = totalSales.add(order.getActualAmount());
                    }
                }
            }

            return totalSales;
        } catch (Exception e) {
            log.error("获取时间段销售额失败", e);
            return BigDecimal.ZERO;
        }
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

    /**
     * 更新订单状态后发送消息通知
     *
     * @param order     订单
     * @param oldStatus 原状态
     * @param newStatus 新状态
     */
    private void sendOrderStatusChangeNotification(Order order, String oldStatus, String newStatus) {
        try {
            log.info("发送订单状态变更通知: orderId={}, orderNo={}, oldStatus={}, newStatus={}", 
                    order.getOrderId(), order.getOrderNo(), oldStatus, newStatus);

            // 发送原有的Spring事件
            String extra = String.format("{\"orderId\":%d,\"oldStatus\":\"%s\",\"newStatus\":\"%s\"}",
                    order.getOrderId(), oldStatus, newStatus);

            OrderStatusChangedEvent event = new OrderStatusChangedEvent(
                    this,
                    order.getUserId(),
                    order.getOrderId(),
                    order.getOrderNo(),
                    oldStatus,
                    newStatus,
                    extra);

            eventPublisher.publishEvent(event);

            // 发送RabbitMQ消息
            try {
                // 创建订单消息
                OrderMessage orderMessage;
                
                // 根据新状态确定事件类型
                if (ORDER_STATUS_CANCELLED.equals(newStatus)) {
                    orderMessage = OrderMessage.cancelOrderEvent(
                            order.getOrderId(),
                            order.getOrderNo(),
                            order.getUserId(),
                            order.getTotalAmount()
                    );
                } else if (ORDER_STATUS_COMPLETED.equals(newStatus)) {
                    orderMessage = OrderMessage.completeOrderEvent(
                            order.getOrderId(),
                            order.getOrderNo(),
                            order.getUserId(),
                            order.getTotalAmount()
                    );
                } else {
                    // 通用状态变更事件
                    orderMessage = OrderMessage.statusChangeEvent(
                            order.getOrderId(),
                            order.getOrderNo(),
                            order.getUserId(),
                            oldStatus,
                            newStatus,
                            order.getTotalAmount()
                    );
                }

                // 发送消息（包含RabbitMQ和Redis通知）
                messageProducerService.sendOrderMessage(orderMessage);
                
                log.info("订单状态变更RabbitMQ消息发送成功: orderId={}, eventType={}", 
                        order.getOrderId(), orderMessage.getEventType());

            } catch (Exception mqEx) {
                // RabbitMQ消息发送失败不影响主流程，但需要记录日志
                log.error("订单状态变更RabbitMQ消息发送失败: orderId={}, orderNo={}, oldStatus={}, newStatus={}, error={}", 
                        order.getOrderId(), order.getOrderNo(), oldStatus, newStatus, mqEx.getMessage(), mqEx);
            }

            log.info("已发送订单状态变更消息通知: orderId={}, userId={}, oldStatus={}, newStatus={}",
                    order.getOrderId(), order.getUserId(), oldStatus, newStatus);
        } catch (Exception e) {
            log.error("发送订单状态变更消息通知失败: orderId={}, error={}",
                    order.getOrderId(), e.getMessage(), e);
        }
    }

    @Override
    public boolean isOrderCommented(Integer orderId) {
        Order order = this.getById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        return order.getIsCommented() != null && order.getIsCommented() == 1;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateOrderCommentStatus(Integer orderId, Integer isCommented) {
        Order order = this.getById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }

        int result = baseMapper.updateOrderCommentStatus(orderId, isCommented);

        // 如果更新成功，清除相关缓存
        if (result > 0) {
            clearOrderCache(orderId, order.getUserId());
        }

        return result > 0;
    }

    /**
     * 直接购买商品（不添加到购物车）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> directPurchase(Integer userId, Integer addressId, Integer productId,
            Integer quantity, String specs, Long skuId, String remark,
            String paymentMethod, Long couponId,
            BigDecimal shippingFee, Integer pointsUsed) {
        log.info("开始处理直接购买请求: 用户ID={}, 商品ID={}, skuId={}, 数量={}", userId, productId, skuId, quantity);

        // 获取用户地址信息
        LambdaQueryWrapper<UserAddress> addressQueryWrapper = new LambdaQueryWrapper<>();
        addressQueryWrapper.eq(UserAddress::getUserId, userId)
                .eq(UserAddress::getAddressId, addressId);
        UserAddress address = addressMapper.selectOne(addressQueryWrapper);

        if (address == null) {
            throw new BusinessException(400, "收货地址不存在");
        }

        // 获取商品信息
        Product product = productService.getById(productId);
        if (product == null) {
            throw new BusinessException(400, "商品不存在");
        }

        // SKU相关变量
        ProductSku sku = null;
        BigDecimal itemPrice;
        String processedSpecs;

        // 判断是否使用SKU
        if (skuId != null) {
            // 有SKU，使用SKU的价格和库存
            sku = productSkuService.getById(skuId);
            if (sku == null) {
                throw new BusinessException(400, "商品规格不存在");
            }
            if (!sku.getProductId().equals(productId)) {
                throw new BusinessException(400, "商品规格不匹配");
            }
            if (sku.getStock() < quantity) {
                throw new BusinessException(400, "商品规格库存不足");
            }
            itemPrice = sku.getPrice();
            processedSpecs = sku.getSpecValues();
            log.debug("使用SKU价格: skuId={}, price={}, specs={}", skuId, itemPrice, processedSpecs);
        } else {
            // 无SKU，使用商品主表的价格和库存
            if (product.getStock() < quantity) {
                throw new BusinessException(400, "商品库存不足");
            }
            itemPrice = product.getPriceNew();
            // 处理规格数据，确保是有效的JSON格式
            processedSpecs = processSpecsToJson(specs);
            log.debug("使用商品主表价格: price={}, specs={}", itemPrice, processedSpecs);
        }

        try {
            // 创建订单
            Order order = new Order();
            order.setUserId(userId);
            order.setOrderNo(generateOrderNo());
            order.setTotalAmount(itemPrice.multiply(new BigDecimal(quantity)));
            order.setStatus(EnumUtil.getOrderStatusByCode(ORDER_STATUS_PENDING_PAYMENT));
            // 设置订单其他属性
            order.setAddressId(addressId);
            order.setReceiverName(address.getReceiver());
            order.setReceiverPhone(address.getPhone());
            order.setReceiverProvince(address.getProvince());
            order.setReceiverCity(address.getCity());
            order.setReceiverDistrict(address.getDistrict());
            order.setReceiverAddress(address.getAddress());
            order.setReceiverZip(address.getZip());
            order.setRemark(remark);
            order.setPaymentMethod(paymentMethod);
            order.setShippingFee(shippingFee != null ? shippingFee : BigDecimal.ZERO);
            order.setCreateTime(LocalDateTime.now());
            order.setUpdateTime(LocalDateTime.now());

            // 计算优惠金额
            BigDecimal couponAmount = BigDecimal.ZERO;
            BigDecimal pointsAmount = BigDecimal.ZERO;

            // 处理优惠券
            if (couponId != null && couponId > 0) {
                // 获取用户优惠券
                UserCoupon userCoupon = userCouponService.getById(couponId);
                if (userCoupon != null && userCoupon.getUserId().equals(userId)
                        && userCoupon.getStatus().equals("UNUSED")) {
                    // 获取优惠券信息
                    Coupon coupon = couponService.getById(userCoupon.getCouponId());
                    if (coupon != null && "ACTIVE".equals(coupon.getStatus())) {
                        // 验证优惠券是否可用
                        if (order.getTotalAmount().compareTo(coupon.getMinSpend()) >= 0) {
                            couponAmount = coupon.getValue();
                            // 记录使用的优惠券
                            order.setCouponId(couponId);
                            order.setCouponAmount(couponAmount);

                            // 更新优惠券状态
                            userCoupon.setStatus("USED");
                            userCoupon.setUseTime(LocalDateTime.now());
                            // 只有在订单ID不为null时才设置OrderId，避免NullPointerException
                            if (order.getOrderId() != null) {
                                userCoupon.setOrderId(order.getOrderId().longValue());
                                userCouponService.updateById(userCoupon);
                            } else {
                                log.warn("订单ID为null，暂不更新优惠券状态，将在订单保存后更新");
                                // 标记此优惠券需要在后续更新
                                order.setCouponId(couponId); // 保存优惠券ID，后续可根据需要更新
                            }
                        } else {
                            log.warn("优惠券不可用于此订单，最低消费金额不足: 订单总额={}, 优惠券最低金额={}",
                                    order.getTotalAmount(), coupon.getMinSpend());
                        }
                    }
                }
            }

            // 处理积分抵扣
            if (pointsUsed != null && pointsUsed > 0) {
                // 检查用户积分是否足够
                Integer userPoints = pointsService.getUserPoints(userId);
                if (userPoints >= pointsUsed) {
                    // 计算积分抵扣金额，通常100积分=1元
                    pointsAmount = new BigDecimal(pointsUsed).divide(new BigDecimal("100"), 2, RoundingMode.DOWN);

                    // 记录使用的积分
                    order.setPointsUsed(pointsUsed);
                    order.setPointsDiscount(pointsAmount);

                    // 扣减用户积分
                    boolean deductSuccess = pointsService.deductPoints(userId, pointsUsed, "order", order.getOrderNo(),
                            "订单抵扣");
                    if (!deductSuccess) {
                        throw new BusinessException(400, "积分扣减失败");
                    }
                }
            }

            // 计算实际支付金额
            BigDecimal actualAmount = order.getTotalAmount()
                    .add(order.getShippingFee())
                    .subtract(couponAmount)
                    .subtract(pointsAmount);
            // 确保金额不为负数
            order.setActualAmount(actualAmount.max(BigDecimal.ZERO));

            // 保存订单
            save(order);

            // 添加调试日志，检查订单ID是否成功回填
            log.info("保存订单后的订单ID: {}", order.getOrderId());

            // 检查订单ID是否为null，如果为null则手动查询获取
            if (order.getOrderId() == null) {
                log.warn("订单ID为null，尝试通过订单号查询获取订单ID");

                // 根据订单号查询刚刚创建的订单
                LambdaQueryWrapper<Order> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(Order::getOrderNo, order.getOrderNo());
                Order savedOrder = getOne(queryWrapper);

                if (savedOrder != null && savedOrder.getOrderId() != null) {
                    log.info("通过订单号查询成功获取订单ID: {}", savedOrder.getOrderId());
                    order.setOrderId(savedOrder.getOrderId());
                } else {
                    log.error("无法获取订单ID，订单号: {}", order.getOrderNo());
                    throw new BusinessException(500, "创建订单失败：无法获取订单ID");
                }
            }

            // 验证订单ID不为null
            if (order.getOrderId() == null) {
                log.error("订单ID仍然为null，无法继续处理");
                throw new BusinessException(500, "创建订单失败：订单ID为null");
            }

            // 订单ID获取成功后，处理之前因为订单ID为null而未完成的优惠券状态更新
            if (couponId != null && couponId > 0) {
                UserCoupon userCoupon = userCouponService.getById(couponId);
                if (userCoupon != null && userCoupon.getUserId().equals(userId)
                        && "UNUSED".equals(userCoupon.getStatus())) {
                    log.info("更新优惠券状态，订单ID: {}, 优惠券ID: {}", order.getOrderId(), couponId);
                    userCoupon.setStatus("USED");
                    userCoupon.setUseTime(LocalDateTime.now());
                    userCoupon.setOrderId(order.getOrderId().longValue());
                    userCouponService.updateById(userCoupon);
                }
            }

            // 创建订单商品
            OrderProduct orderProduct = new OrderProduct();
            orderProduct.setOrderId(order.getOrderId());
            orderProduct.setProductId(product.getProductId());
            orderProduct.setProductName(product.getProductName());
            orderProduct.setPrice(itemPrice);
            orderProduct.setQuantity(quantity);
            orderProduct.setSpecs(processedSpecs);
            orderProduct.setCreateTime(LocalDateTime.now());
            orderProduct.setUpdateTime(LocalDateTime.now());

            // 设置SKU相关信息
            if (sku != null) {
                orderProduct.setSkuId(sku.getSkuId());
                orderProduct.setSkuCode(sku.getSkuCode());
                // 使用SKU图片（如果有）
                orderProduct.setProductImg(sku.getSkuImage() != null && !sku.getSkuImage().isEmpty() 
                        ? sku.getSkuImage() : product.getProductImg());
            } else {
                orderProduct.setProductImg(product.getProductImg());
            }

            // 保存订单商品
            orderProductMapper.insert(orderProduct);

            // 扣减库存
            if (sku != null) {
                // 扣减SKU库存（传递订单ID用于日志记录）
                productSkuService.deductStock(sku.getSkuId(), quantity, order.getOrderId(), "直接购买扣减库存");
                log.info("订单 {} SKU库存扣减完成: skuId={}, quantity={}", order.getOrderNo(), sku.getSkuId(), quantity);
            } else {
                // 扣减商品主表库存
                productService.update(
                        new LambdaUpdateWrapper<Product>()
                                .eq(Product::getProductId, productId)
                                .setSql("stock = stock - " + quantity)
                                .setSql("sales = sales + " + quantity));
            }

            // 创建支付记录 - 如果有支付服务
            if (paymentService != null) {
                try {
                    Payment payment = new Payment();
                    payment.setUserId(userId);
                    payment.setOrderId(order.getOrderId());
                    payment.setOrderNo(order.getOrderNo());
                    payment.setAmount(order.getActualAmount());
                    payment.setPaymentMethod(order.getPaymentMethod());
                    payment.setStatus(PaymentStatus.PENDING); // 使用枚举值
                    payment.setCreateTime(LocalDateTime.now());
                    paymentService.save(payment);
                } catch (Exception e) {
                    log.warn("创建支付记录失败，但不影响订单创建", e);
                }
            }

            // 如果有Redis服务，将订单放入Redis，设置过期时间
            if (redisUtil != null) {
                try {
                    String orderKey = CacheConstants.ORDER_KEY_PREFIX + order.getOrderNo();
                    redisUtil.set(orderKey, order, CacheConstants.ORDER_EXPIRE_TIME);
                } catch (Exception e) {
                    log.warn("将订单放入Redis失败，但不影响订单创建", e);
                }
            }

            Map<String, Object> result = new HashMap<>();
            result.put("orderId", order.getOrderId());
            result.put("orderNumber", order.getOrderNo());
            result.put("totalAmount", order.getTotalAmount());
            result.put("actualAmount", order.getActualAmount());
            result.put("pointsUsed", order.getPointsUsed());
            result.put("pointsDiscount", order.getPointsDiscount());

            // 清除用户订单列表缓存
            clearUserOrderListCache(userId);

            // 发送订单创建消息
            try {
                OrderMessage orderMessage = OrderMessage.createOrderEvent(
                        order.getOrderId(),
                        order.getOrderNo(),
                        order.getUserId(),
                        order.getTotalAmount()
                );
                messageProducerService.sendOrderMessage(orderMessage);
                log.info("直接购买订单创建消息发送成功: orderId={}, orderNo={}", order.getOrderId(), order.getOrderNo());
            } catch (Exception e) {
                // 消息发送失败不影响主流程，但需要记录日志
                log.error("直接购买订单创建消息发送失败: orderId={}, orderNo={}, error={}", 
                        order.getOrderId(), order.getOrderNo(), e.getMessage(), e);
            }

            return result;
        } catch (Exception e) {
            log.error("直接购买失败", e);
            // 记录更详细的错误信息
            log.error("直接购买失败 - 详细信息: 错误类型={}, 错误消息={}, 堆栈={}",
                    e.getClass().getName(), e.getMessage(), Arrays.toString(e.getStackTrace()));

            // 如果是空指针异常，提供更多上下文
            if (e instanceof NullPointerException) {
                log.error("空指针异常可能是由于订单ID为null，请检查订单保存和ID生成逻辑");
            }

            throw new BusinessException(500, "直接购买失败");
        }
    }

    /**
     * 处理规格数据为JSON格式
     * 
     * @param specs 规格字符串
     * @return JSON格式的规格数据
     */
    private String processSpecsToJson(String specs) {
        if (specs == null || specs.isEmpty()) {
            return "{}"; // 返回空JSON对象
        }

        try {
            // 检查是否已经是JSON格式
            objectMapper.readTree(specs);
            return specs; // 如果能够解析为JSON，则直接返回
        } catch (Exception e) {
            log.debug("规格数据不是JSON格式，尝试转换: {}", specs);

            // 将"类型:孕中"这样的格式转换为JSON
            try {
                Map<String, String> specsMap = new HashMap<>();

                // 处理如"类型:孕中"或"颜色:红色,尺寸:L"或"尺码:NB;和装:裙装"这样的格式
                // 支持分号和逗号两种分隔符
                if (specs.contains(":")) {
                    // 优先使用分号分隔，如果没有分号则使用逗号
                    String separator = specs.contains(";") ? ";" : ",";
                    String[] pairs = specs.split(separator);
                    for (String pair : pairs) {
                        if (pair.contains(":")) {
                            String[] kv = pair.split(":", 2);
                            if (kv.length == 2 && !kv[0].trim().isEmpty() && !kv[1].trim().isEmpty()) {
                                specsMap.put(kv[0].trim(), kv[1].trim());
                            }
                        }
                    }
                } else {
                    // 如果没有冒号，则将整个字符串作为规格值
                    specsMap.put("规格", specs.trim());
                }

                return objectMapper.writeValueAsString(specsMap);
            } catch (Exception ex) {
                log.error("转换规格数据为JSON失败", ex);
                // 如果转换失败，则创建一个包含原始字符串的JSON对象
                try {
                    Map<String, String> fallbackMap = new HashMap<>();
                    fallbackMap.put("规格文本", specs);
                    return objectMapper.writeValueAsString(fallbackMap);
                } catch (Exception fallbackEx) {
                    return "{}"; // 最终回退到空JSON对象
                }
            }
        }
    }


}