# 订单取消积分退还功能实现文档

## 问题描述

在系统中发现一个bug：当订单使用积分抵扣后取消或超时，积分没有正确退还给用户。具体场景包括：

1. 用户下单时使用积分抵扣，但未完成支付，订单超时自动取消
2. 用户下单时使用积分抵扣，手动取消订单
3. 系统管理员取消包含积分抵扣的订单

以上情况下，系统应当自动将抵扣的积分退还给用户，但当前未实现此功能。

## 实现方案

通过添加订单状态变更事件监听器，监听订单从"待支付"到"已取消"的状态变更，适配不同的取消触发方式（用户手动取消、系统超时取消等），并在订单取消时检查是否使用了积分，如果使用了积分则自动返还。

### 核心类设计

**OrderCancelListener**：订单取消事件监听器
- 监听OrderStateChangedEvent事件
- 筛选出取消事件（CANCEL）和超时事件（TIMEOUT）
- 检查订单是否使用了积分，如果有则调用积分服务进行退还

### 主要流程

1. 监听器捕获订单状态变更事件（OrderStateChangedEvent）
2. 判断事件类型是否为CANCEL或TIMEOUT，且新状态为CANCELLED
3. 读取订单中的积分使用记录（pointsUsed字段）
4. 如果使用了积分（pointsUsed > 0），使用pointsService.addPoints()将积分退还给用户
5. 记录积分退还日志

### 代码实现

```java
@Async
@EventListener
@Transactional(propagation = Propagation.REQUIRES_NEW)
public void handleOrderCancelled(OrderStateChangedEvent event) {
    Order order = event.getOrder();
    OrderEvent orderEvent = event.getEvent();
    OrderStatus newStatus = event.getNewStatus();

    // 只处理订单取消或超时事件，且订单状态为已取消
    if ((OrderEvent.CANCEL.equals(orderEvent) || OrderEvent.TIMEOUT.equals(orderEvent)) 
            && OrderStatus.CANCELLED.equals(newStatus)) {
        
        // 检查订单是否使用了积分
        Integer pointsUsed = order.getPointsUsed();
        if (pointsUsed != null && pointsUsed > 0) {
            Integer userId = order.getUserId();
            String orderNo = order.getOrderNo();
            
            // 调用积分服务，退还积分
            pointsService.addPoints(
                userId, 
                pointsUsed, 
                "order_cancel", 
                orderNo, 
                "订单取消返还积分");
        }
    }
}
```

### 测试验证

已编写单元测试，验证了以下场景：

1. 用户手动取消含积分抵扣的订单时，积分被正确退还
2. 订单超时自动取消时，积分被正确退还
3. 订单未使用积分时，不触发退还逻辑
4. 非取消事件（如支付事件）不触发退还逻辑

## 部署注意事项

1. 确保订单实体类（Order）的pointsUsed字段正确设置，值为订单抵扣的积分数量
2. 确保订单状态机正确发布OrderStateChangedEvent事件
3. 确保积分服务的addPoints方法能正确处理积分增加操作
4. 由于使用了@Async注解，确保应用程序启用了异步执行支持（@EnableAsync）

## 未来优化

1. 优化异常处理，增加积分返还失败后的重试机制
2. 增加系统警报，当积分返还失败时及时通知管理员
3. 在用户界面显示积分返还记录，提升用户体验 