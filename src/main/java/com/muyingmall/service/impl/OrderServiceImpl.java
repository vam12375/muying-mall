package com.muyingmall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.muyingmall.common.exception.BusinessException;
import com.muyingmall.entity.Cart;
import com.muyingmall.entity.Order;
import com.muyingmall.entity.OrderProduct;
import com.muyingmall.entity.Payment;
import com.muyingmall.entity.Product;
import com.muyingmall.entity.User;
import com.muyingmall.entity.UserAddress;
import com.muyingmall.mapper.CartMapper;
import com.muyingmall.mapper.OrderMapper;
import com.muyingmall.mapper.OrderProductMapper;
import com.muyingmall.mapper.UserAddressMapper;
import com.muyingmall.mapper.UserMapper;
import com.muyingmall.service.OrderService;
import com.muyingmall.service.PaymentService;
import com.muyingmall.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

/**
 * 订单服务实现类
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

    private final UserMapper userMapper;
    private final UserAddressMapper addressMapper;
    private final CartMapper cartMapper;
    private final OrderProductMapper orderProductMapper;
    private final ProductService productService;
    private final PaymentService paymentService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> createOrder(Integer userId, Integer addressId, String remark) {
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
        LambdaQueryWrapper<Cart> cartQueryWrapper = new LambdaQueryWrapper<>();
        cartQueryWrapper.eq(Cart::getUserId, userId)
                .eq(Cart::getSelected, 1);
        List<Cart> cartList = cartMapper.selectList(cartQueryWrapper);

        if (cartList.isEmpty()) {
            throw new BusinessException("购物车中没有选中的商品");
        }

        // 创建订单
        Order order = new Order();
        String orderNo = generateOrderNo();
        order.setOrderNo(orderNo);
        order.setUserId(userId);
        order.setStatus("pending_payment");
        order.setAddressId(addressId);
        order.setReceiverName(address.getReceiver());
        order.setReceiverPhone(address.getPhone());
        order.setReceiverProvince(address.getProvince());
        order.setReceiverCity(address.getCity());
        order.setReceiverDistrict(address.getDistrict());
        order.setReceiverAddress(address.getAddress());
        order.setReceiverZip(address.getZip());
        order.setRemark(remark);
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
        order.setActualAmount(totalAmount); // 实际支付金额，可考虑优惠券、积分等
        order.setShippingFee(BigDecimal.ZERO); // 运费，可根据业务需求设置

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
        return result;
    }

    @Override
    public Order getOrderDetail(Integer orderId, Integer userId) {
        Order order = getById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }

        // 验证订单属于当前用户
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException("无权限查看此订单");
        }

        // 查询订单商品
        LambdaQueryWrapper<OrderProduct> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrderProduct::getOrderId, orderId);
        List<OrderProduct> orderProducts = orderProductMapper.selectList(queryWrapper);
        order.setProducts(orderProducts);

        return order;
    }

    @Override
    public Page<Order> getUserOrders(Integer userId, int page, int size, String status) {
        Page<Order> pageParam = new Page<>(page, size);

        LambdaQueryWrapper<Order> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Order::getUserId, userId);

        if (StringUtils.hasText(status)) {
            queryWrapper.eq(Order::getStatus, status);
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
        if (!"pending_payment".equals(order.getStatus())) {
            throw new BusinessException("当前订单状态不可取消");
        }

        // 更新订单状态
        order.setStatus("cancelled");
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
        if (!"pending_payment".equals(order.getStatus())) {
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
        payment.setStatus(0); // 待支付
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
        if (!"shipped".equals(order.getStatus())) {
            throw new BusinessException("当前订单状态不可确认收货");
        }

        // 更新订单状态
        order.setStatus("completed");
        order.setCompletionTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        return updateById(order);
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
            queryWrapper.eq(Order::getStatus, status);
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
        order.setStatus(status);
        order.setRemark(remark);
        order.setUpdateTime(LocalDateTime.now());

        // 根据状态设置相应的时间
        if ("cancelled".equals(status)) {
            order.setCancelTime(LocalDateTime.now());
        } else if ("shipped".equals(status)) {
            order.setShippingTime(LocalDateTime.now());
        } else if ("completed".equals(status)) {
            order.setCompletionTime(LocalDateTime.now());
        }

        return updateById(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean shipOrder(Integer orderId, String shippingCompany, String trackingNo) {
        Order order = getById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }

        // 只有待发货状态的订单可以发货
        if (!"pending_shipment".equals(order.getStatus())) {
            throw new BusinessException("当前订单状态不可发货");
        }

        // 更新订单状态
        order.setStatus("shipped");
        order.setShippingTime(LocalDateTime.now());
        order.setShippingCompany(shippingCompany);
        order.setTrackingNo(trackingNo);
        order.setUpdateTime(LocalDateTime.now());
        return updateById(order);
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
        pendingPaymentWrapper.eq(Order::getStatus, "pending_payment");
        long pendingPaymentCount = count(pendingPaymentWrapper);
        statistics.put("pendingPaymentCount", pendingPaymentCount);

        // 待发货订单数
        LambdaQueryWrapper<Order> pendingShipmentWrapper = new LambdaQueryWrapper<>();
        if (userId != null) {
            pendingShipmentWrapper.eq(Order::getUserId, userId);
        }
        pendingShipmentWrapper.eq(Order::getStatus, "pending_shipment");
        long pendingShipmentCount = count(pendingShipmentWrapper);
        statistics.put("pendingShipmentCount", pendingShipmentCount);

        // 已发货订单数
        LambdaQueryWrapper<Order> shippedWrapper = new LambdaQueryWrapper<>();
        if (userId != null) {
            shippedWrapper.eq(Order::getUserId, userId);
        }
        shippedWrapper.eq(Order::getStatus, "shipped");
        long shippedCount = count(shippedWrapper);
        statistics.put("shippedCount", shippedCount);

        // 已完成订单数
        LambdaQueryWrapper<Order> completedWrapper = new LambdaQueryWrapper<>();
        if (userId != null) {
            completedWrapper.eq(Order::getUserId, userId);
        }
        completedWrapper.eq(Order::getStatus, "completed");
        long completedCount = count(completedWrapper);
        statistics.put("completedCount", completedCount);

        // 已取消订单数
        LambdaQueryWrapper<Order> cancelledWrapper = new LambdaQueryWrapper<>();
        if (userId != null) {
            cancelledWrapper.eq(Order::getUserId, userId);
        }
        cancelledWrapper.eq(Order::getStatus, "cancelled");
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
}