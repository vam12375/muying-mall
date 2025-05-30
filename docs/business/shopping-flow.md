# 母婴商城用户购物流程

## 概述

本文档详细描述了母婴商城系统中用户购物的完整流程，从浏览商品到完成支付、收货和评价的整个过程。这些流程是系统核心业务逻辑的重要组成部分，也是前后端交互的主要场景。

## 购物流程图

购物流程的完整流程图请参考：[购物流程图](./diagrams/shopping-flow.png)

## 详细流程说明

### 1. 浏览商品

**流程描述**：
- 用户进入商城首页或分类页
- 可以通过分类导航、搜索、促销活动等方式浏览商品
- 系统展示商品列表，包含基本信息（名称、图片、价格、促销信息等）

**技术实现**：
- 首页热门商品通过Redis缓存提高访问速度
- 分类列表通过多级缓存提高响应速度
- 搜索功能基于Elasticsearch实现，支持分词和过滤
- 商品列表支持分页和多种排序方式

**数据流向**：
1. 前端发起商品列表请求
2. API网关转发请求到商品服务
3. 商品服务优先从缓存获取数据
4. 如缓存未命中，则查询数据库并更新缓存
5. 返回商品列表数据给前端

### 2. 查看商品详情

**流程描述**：
- 用户点击商品卡片，进入商品详情页
- 详情页展示完整的商品信息、规格选择、评价等
- 用户可以查看商品图片、详情描述、规格参数、评价信息等

**技术实现**：
- 商品详情页数据通过组合API获取，减少请求次数
- 商品评价采用分页加载，按时间或好评度排序
- 相关推荐基于用户历史行为和商品属性计算
- 商品库存实时更新或缓存预占方案

**数据流向**：
1. 前端发起商品详情请求
2. API网关转发请求到商品服务
3. 商品服务获取基础信息、规格、图片等
4. 评价服务获取商品评价信息
5. 数据聚合后返回给前端

### 3. 加入购物车

**流程描述**：
- 用户选择商品规格（如颜色、尺寸等）和数量
- 点击"加入购物车"按钮
- 系统将商品加入用户购物车
- 显示加入成功提示

**技术实现**：
- 购物车数据双存储：登录用户存储在服务端，未登录用户存储在本地
- 服务端购物车基于Redis实现，定期同步到MySQL
- 合并购物车逻辑：用户登录时将本地购物车与服务端购物车合并
- 使用乐观锁处理并发添加场景

**数据流向**：
1. 前端发起添加购物车请求，包含商品ID、规格、数量等信息
2. API网关转发请求到购物车服务
3. 购物车服务验证商品信息和库存
4. 购物车服务更新Redis中的购物车数据
5. 定时任务将Redis数据同步到MySQL
6. 返回成功响应给前端

### 4. 购物车管理

**流程描述**：
- 用户查看购物车中的商品列表
- 可以调整商品数量、删除商品、选择/取消选择商品
- 系统实时计算选中商品的总价
- 用户点击"去结算"进入结算页面

**技术实现**：
- 购物车页面支持批量操作（删除、选择等）
- 购物车数据定期清理机制（如30天未更新的项目）
- 库存不足或失效商品的提示机制
- 价格和优惠实时计算

**数据流向**：
1. 前端发起购物车查询请求
2. 购物车服务获取用户购物车数据
3. 检查商品库存状态和价格变化
4. 返回购物车数据给前端
5. 前端发起购物车更新请求（如修改数量）
6. 购物车服务更新购物车数据
7. 返回更新结果给前端

### 5. 订单结算

**流程描述**：
- 用户进入结算页面，确认收货地址
- 选择配送方式和支付方式
- 使用优惠券、积分等
- 系统计算订单金额（商品金额、运费、折扣等）
- 用户确认订单信息

**技术实现**：
- 结算前再次验证商品库存和价格
- 优惠券和积分规则实时计算
- 收货地址管理（默认地址、新增地址等）
- 订单金额计算逻辑的抽象封装

**数据流向**：
1. 前端发起结算页面数据请求
2. 订单服务获取用户收货地址列表
3. 优惠券服务获取可用优惠券
4. 积分服务获取用户积分信息
5. 配送服务计算运费
6. 聚合数据返回给前端
7. 用户选择地址、优惠券等后，前端计算订单预览信息

### 6. 订单提交

**流程描述**：
- 用户点击"提交订单"按钮
- 系统创建订单，生成订单号
- 冻结库存，扣减优惠券和积分
- 用户进入支付页面

**技术实现**：
- 订单创建采用分布式事务保证数据一致性
- 使用分布式锁保证库存操作的原子性
- 异步通知相关服务（如库存服务、积分服务等）
- 订单状态机控制订单状态流转

**数据流向**：
1. 前端发起创建订单请求
2. API网关转发请求到订单服务
3. 订单服务开始分布式事务
4. 商品服务冻结库存
5. 优惠券服务使用优惠券
6. 积分服务扣减积分（如果使用）
7. 订单服务创建订单记录
8. 提交事务，发布订单创建事件
9. 返回订单信息给前端

### 7. 订单支付

**流程描述**：
- 用户选择支付方式（如支付宝、微信支付等）
- 系统生成支付单，调用第三方支付接口
- 用户完成支付
- 系统接收支付结果通知，更新订单状态

**技术实现**：
- 支持多种支付方式，采用适配器模式集成不同支付渠道
- 实现支付结果异步通知和主动查询双保险机制
- 支付超时自动取消订单
- 使用状态机处理支付状态变更

**数据流向**：
1. 前端发起支付请求，选择支付方式
2. 支付服务创建支付记录
3. 支付服务调用第三方支付接口生成支付信息
4. 返回支付链接或支付参数给前端
5. 用户完成支付后，第三方支付平台调用支付回调接口
6. 支付服务验证支付结果
7. 支付成功后，更新订单状态，扣减实际库存
8. 发布支付成功事件，触发后续流程（如通知商家）

### 8. 订单履行

**流程描述**：
- 商家接收到订单
- 系统分配订单并安排发货
- 物流公司揽收包裹并配送
- 用户收到商品，确认收货

**技术实现**：
- 订单流转状态实时更新和推送
- 物流信息定时抓取和更新
- 自动确认收货机制（如15天未确认）
- 订单完成后触发评价提醒

**数据流向**：
1. 商家通过后台接口更新发货信息
2. 订单服务更新订单状态为"已发货"
3. 物流服务创建物流记录
4. 通知服务向用户推送发货通知
5. 物流服务定期从第三方物流平台获取物流状态更新
6. 用户确认收货，订单服务更新订单状态为"已完成"
7. 积分服务增加用户购物积分
8. 触发评价提醒流程

### 9. 订单评价

**流程描述**：
- 用户收货后可对订单中的商品进行评价
- 可以上传评价图片，选择星级评分
- 商家可以回复评价
- 评价成功后获得额外积分奖励

**技术实现**：
- 评价与订单和商品关联
- 图片上传至对象存储服务
- 评价审核机制（敏感词过滤等）
- 评价数据用于商品评分计算

**数据流向**：
1. 用户提交商品评价
2. 评价服务保存评价内容和图片
3. 评价服务更新商品的评分和评价数量
4. 积分服务给用户增加评价奖励积分
5. 通知服务向商家推送新评价通知

## 业务规则

### 购物车规则

1. 单个用户购物车最多可添加99种商品
2. 每种商品最大购买数量由商品设置决定，默认限制为99件
3. 购物车商品保留期为30天，30天未更新自动清除
4. 商品失效或下架后，在购物车中标记为失效状态

### 订单规则

1. 订单提交成功后，库存锁定30分钟
2. 未付款订单30分钟后自动取消，特殊商品可延长至24小时
3. 订单支付成功不可取消，但可申请退款
4. 已发货订单15天未确认收货系统自动完成
5. 订单完成后7天内可以评价，超期未评价将默认好评

### 优惠券规则

1. 一个订单只能使用一张优惠券
2. 优惠券有最低消费限制和使用范围限制
3. 优惠券不可与特定促销活动同时使用
4. 优惠券使用后不可退回，即使订单取消

### 积分规则

1. 订单金额每10元获得1积分，订单完成后发放
2. 评价商品额外获得5积分/件
3. 积分有效期为1年，过期自动清零
4. 积分可用于下单抵扣，100积分=1元

## 异常处理

### 商品缺货处理

1. 提交订单时检测到库存不足
   - 通知用户库存不足情况
   - 提供到货通知预约功能
   - 推荐类似替代商品

2. 付款后发现实际缺货
   - 通知用户缺货情况
   - 提供等待或退款选择
   - 给予补偿优惠券或额外积分

### 支付异常处理

1. 用户支付但系统未收到通知
   - 支付服务定时轮询检查未完成订单的支付状态
   - 支持用户主动提交支付凭证
   - 客服介入处理异常订单

2. 重复支付处理
   - 系统检测到重复支付自动发起退款
   - 通知用户退款进度
   - 记录完整的支付和退款流水

## 具体实现示例

### 添加购物车

```java
@Transactional
public CartItemDTO addToCart(Long userId, AddToCartRequest request) {
    // 1. 验证商品和规格是否存在
    Product product = productService.getProduct(request.getProductId());
    if (product == null || product.getStatus() != ProductStatus.ON_SALE) {
        throw new BusinessException("商品不存在或已下架");
    }
    
    // 2. 验证库存
    ProductSpecs specs = productSpecsService.getSpecsBySpecsId(request.getSpecsId());
    if (specs == null || specs.getStock() < request.getQuantity()) {
        throw new BusinessException("商品规格不存在或库存不足");
    }
    
    // 3. 查询用户购物车中是否已存在该商品规格
    CartItem existingItem = cartItemRepository.findByUserIdAndProductIdAndSpecsId(
        userId, request.getProductId(), request.getSpecsId()
    );
    
    // 4. 存在则更新数量，不存在则新增
    CartItem cartItem;
    if (existingItem != null) {
        int newQuantity = existingItem.getQuantity() + request.getQuantity();
        if (newQuantity > specs.getMaxBuyLimit()) {
            throw new BusinessException("超出最大购买数量限制");
        }
        existingItem.setQuantity(newQuantity);
        existingItem.setUpdateTime(new Date());
        cartItem = cartItemRepository.save(existingItem);
    } else {
        cartItem = new CartItem();
        cartItem.setUserId(userId);
        cartItem.setProductId(request.getProductId());
        cartItem.setSpecsId(request.getSpecsId());
        cartItem.setQuantity(request.getQuantity());
        cartItem.setSelected(true);
        cartItem.setPriceSnapshot(specs.getPrice());
        cartItem.setSpecsJson(JSON.toJSONString(specs));
        cartItem.setCreateTime(new Date());
        cartItem.setUpdateTime(new Date());
        cartItem = cartItemRepository.save(cartItem);
    }
    
    // 5. 更新Redis缓存
    cartCacheService.updateCartItem(userId, cartItem);
    
    // 6. 返回购物车项信息
    return cartItemConverter.toDTO(cartItem);
}
```

### 订单创建流程

```java
@Transactional
public OrderDTO createOrder(Long userId, CreateOrderRequest request) {
    // 1. 获取购物车选中的商品
    List<CartItem> cartItems = cartItemService.getSelectedItems(userId);
    if (cartItems.isEmpty()) {
        throw new BusinessException("购物车中没有选中的商品");
    }
    
    // 2. 验证商品库存和价格
    List<OrderItem> orderItems = new ArrayList<>();
    for (CartItem cartItem : cartItems) {
        ProductSpecs specs = productSpecsService.getSpecsBySpecsId(cartItem.getSpecsId());
        if (specs == null || specs.getStock() < cartItem.getQuantity()) {
            throw new BusinessException("商品" + cartItem.getProductId() + "库存不足");
        }
        
        // 检查价格变动
        if (!specs.getPrice().equals(cartItem.getPriceSnapshot())) {
            throw new BusinessException("商品" + cartItem.getProductId() + "价格已变动，请重新确认");
        }
        
        OrderItem orderItem = new OrderItem();
        orderItem.setProductId(cartItem.getProductId());
        orderItem.setProductName(productService.getProduct(cartItem.getProductId()).getName());
        orderItem.setSpecsId(cartItem.getSpecsId());
        orderItem.setSpecsJson(cartItem.getSpecsJson());
        orderItem.setQuantity(cartItem.getQuantity());
        orderItem.setPrice(specs.getPrice());
        orderItems.add(orderItem);
    }
    
    // 3. 计算订单金额
    OrderPriceCalculator calculator = new OrderPriceCalculator(orderItems);
    if (request.getCouponId() != null) {
        Coupon coupon = couponService.getCouponById(request.getCouponId());
        calculator.applyCoupon(coupon);
    }
    if (request.getUsePoints() > 0) {
        calculator.applyPoints(request.getUsePoints());
    }
    
    // 4. 创建订单
    Order order = new Order();
    order.setOrderNo(generateOrderNo());
    order.setUserId(userId);
    order.setAddressId(request.getAddressId());
    order.setTotalAmount(calculator.getTotalAmount());
    order.setPayAmount(calculator.getPayAmount());
    order.setDiscountAmount(calculator.getDiscountAmount());
    order.setFreightAmount(calculator.getFreightAmount());
    order.setPointsAmount(calculator.getPointsAmount());
    order.setCouponId(request.getCouponId());
    order.setUsePoints(request.getUsePoints());
    order.setPaymentType(request.getPaymentType());
    order.setStatus(OrderStatus.PENDING_PAYMENT);
    order.setCreateTime(new Date());
    order.setUpdateTime(new Date());
    order.setExpireTime(DateUtils.addMinutes(new Date(), 30)); // 30分钟支付超时
    order = orderRepository.save(order);
    
    // 5. 保存订单商品
    for (OrderItem item : orderItems) {
        item.setOrderId(order.getOrderId());
        item.setCreateTime(new Date());
        orderItemRepository.save(item);
    }
    
    // 6. 锁定库存
    try {
        for (OrderItem item : orderItems) {
            productService.lockStock(item.getProductId(), item.getSpecsId(), item.getQuantity());
        }
    } catch (Exception e) {
        throw new BusinessException("锁定库存失败: " + e.getMessage());
    }
    
    // 7. 使用优惠券
    if (order.getCouponId() != null) {
        couponService.useCoupon(userId, order.getCouponId(), order.getOrderId());
    }
    
    // 8. 扣减积分
    if (order.getUsePoints() > 0) {
        pointsService.deductPoints(userId, order.getUsePoints(), "订单" + order.getOrderNo() + "抵扣", order.getOrderId());
    }
    
    // 9. 清空购物车已购商品
    cartItemService.removeSelectedItems(userId);
    
    // 10. 创建支付超时取消任务
    orderTimeoutTaskManager.addTimeoutTask(order.getOrderId(), order.getExpireTime());
    
    // 11. 发布订单创建事件
    orderEventPublisher.publishOrderCreatedEvent(order);
    
    // 12. 返回订单信息
    return orderConverter.toDTO(order);
}
```

## 业务优化措施

1. **性能优化**
   - 热点商品的多级缓存策略
   - 购物车数据Redis存储，提升访问速度
   - 订单查询按时间范围分库分表
   - 大型促销活动的秒杀库存预热

2. **用户体验优化**
   - 购物车商品价格变动提醒
   - 订单状态实时推送通知
   - 下单后相似商品推荐
   - 一键复购功能

3. **安全措施**
   - 订单防刷机制
   - 敏感操作验证码校验
   - 支付接口防重复提交
   - 价格计算多重校验

## 相关业务流程

- [订单处理流程](./order-processing.md)
- [支付流程](./payment-flow.md)
- [退款流程](./refund-flow.md) 