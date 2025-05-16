# Redis缓存优化说明文档

## 1. 优化概述

本次优化针对母婴商城(muying-mall)项目中的Redis缓存使用进行了全面的优化，主要涵盖四个核心模块：

1. **用户签到和积分服务缓存**
2. **用户消息服务缓存**
3. **购物车服务缓存**
4. **用户服务缓存**

通过添加适当的缓存机制，减少数据库访问频率，提高系统响应速度，优化用户体验。

## 2. 优化详情

### 2.1 缓存常量定义

在`CacheConstants.java`中新增了以下缓存键和过期时间常量：

- **积分系统相关**：
  - `POINTS_KEY_PREFIX`: 积分系统缓存前缀
  - `USER_SIGNIN_STATUS_KEY`: 用户签到状态缓存键
  - `USER_POINTS_HISTORY_KEY`: 用户积分历史缓存键
  - `USER_SIGNIN_CALENDAR_KEY`: 用户签到日历缓存键
  - `POINTS_STATUS_EXPIRE_TIME`: 积分状态缓存过期时间(5分钟)
  - `POINTS_HISTORY_EXPIRE_TIME`: 积分历史缓存过期时间(10分钟)
  - `SIGNIN_CALENDAR_EXPIRE_TIME`: 签到日历缓存过期时间(1小时)

- **消息系统相关**：
  - `MESSAGE_KEY_PREFIX`: 消息系统缓存前缀
  - `USER_UNREAD_COUNT_KEY`: 用户未读消息计数缓存键
  - `USER_MESSAGES_KEY`: 用户消息列表缓存键
  - `USER_UNREAD_TYPE_KEY`: 用户未读消息按类型统计缓存键
  - `MESSAGE_COUNT_EXPIRE_TIME`: 消息计数缓存过期时间(1分钟)
  - `MESSAGE_LIST_EXPIRE_TIME`: 消息列表缓存过期时间(5分钟)

### 2.2 积分系统缓存优化

在`PointsServiceImpl`类中优化了以下方法：

1. `getSignInStatus(Integer userId)`: 添加Redis缓存，避免重复计算连续签到天数
2. `getUserPointsHistory(Integer userId, int page, int size)`: 添加分页查询缓存
3. `getSignInCalendar(Integer userId, String month)`: 添加月度签到日历缓存

### 2.3 用户消息系统缓存优化

在`UserMessageServiceImpl`类中优化了以下方法：

1. `getUnreadCount(Integer userId)`: 缓存用户未读消息总数
2. `getUserMessages(Integer userId, String type, Integer isRead, int page, int size)`: 缓存分页消息列表
3. `getUnreadCountByType(Integer userId)`: 缓存按类型统计的未读消息数量
4. `clearMessageCountCache(Integer userId)`: 优化缓存清除策略，支持模式匹配删除

### 2.4 购物车系统缓存优化

在`CartServiceImpl`类中优化并添加了以下方法：

1. 新增`getCartCount(Integer userId)`: 添加购物车商品总数缓存
2. 新增`getSelectedCarts(Integer userId)`: 添加购物车选中项缓存
3. 优化`clearCartCache(Integer userId)`: 完善缓存清除策略

### 2.5 用户系统缓存优化

在`UserServiceImpl`类中优化了以下方法：

1. `getUserById(Integer userId)`: 添加用户详情缓存
2. `clearUserCache(User user)`: 实现更精细的缓存失效策略
   - 按需清除用户详情、用户名查询、邮箱查询缓存
   - 针对密码修改或状态变更时清除用户令牌缓存
   - 清除用户列表相关缓存

## 3. 测试方法

### 3.1 签到积分系统测试

1. **签到状态测试**：
   ```
   # 第一次查询（缓存未命中）
   访问: /api/points/signin/status
   检查Redis: GET points:signin:status:{userId}
   
   # 第二次查询（应命中缓存）
   再次访问: /api/points/signin/status
   检查日志中是否有"从缓存中获取用户签到状态"
   ```

2. **签到日历测试**：
   ```
   # 查询当前月份签到日历
   访问: /api/points/signin/calendar?month=2023-11
   检查Redis: GET points:signin:calendar:{userId}:2023-11
   ```

### 3.2 用户消息系统测试

1. **未读消息计数测试**：
   ```
   # 获取未读消息数
   访问: /api/messages/unread/count
   检查Redis: GET message:unread:count:{userId}
   
   # 标记消息已读后再获取未读数
   访问: /api/messages/mark-read/{messageId}
   再次访问: /api/messages/unread/count
   验证缓存是否已更新
   ```

2. **消息列表测试**：
   ```
   # 获取消息列表
   访问: /api/messages?page=1&size=10
   检查Redis: GET message:list:{userId}:page_1:size_10
   ```

### 3.3 购物车系统测试

1. **购物车数量测试**：
   ```
   # 获取购物车商品数量
   访问: /api/cart/count
   检查Redis: GET cart:count:{userId}
   
   # 添加商品后再获取
   访问: /api/cart/add
   再次访问: /api/cart/count
   验证缓存是否已更新
   ```

2. **选中项测试**：
   ```
   # 获取选中商品
   访问: /api/cart/selected
   检查Redis: GET cart:selected:{userId}
   ```

### 3.4 用户系统测试

1. **用户详情测试**：
   ```
   # 获取用户详情
   访问: /api/user/{userId}
   检查Redis: GET user:detail:{userId}
   ```

2. **缓存清除测试**：
   ```
   # 更新用户信息
   访问: /api/user/update
   验证相关缓存已被清除
   ```

## 4. 性能预期

预计本次缓存优化将带来以下性能提升：

1. **首页加载速度**：提升30%+（热门商品、推荐商品使用缓存）
2. **购物车操作**：提升50%+（减少数据库查询）
3. **消息中心**：提升40%+（未读消息统计使用缓存）
4. **签到页面**：提升60%+（签到日历和连续签到天数使用缓存）

## 5. 缓存维护建议

1. **监控缓存命中率**：通过Redis的monitor命令监控缓存命中情况
2. **定时清理缓存**：考虑使用定时任务清理过期但未自动删除的缓存
3. **缓存预热**：系统启动时预加载热门数据
4. **防止缓存雪崩**：为不同类型的缓存设置随机过期时间

## 6. 注意事项

1. 所有缓存操作都应捕获异常，确保缓存问题不影响主业务流程
2. 缓存更新与数据库操作应在同一事务中进行
3. 敏感数据（如用户密码）不应存入缓存
4. 缓存键设计应遵循命名规范，便于管理和监控

## 7. 后续优化记录

### 7.1 积分商城显示问题修复

积分商城页面显示问题已修复，详细说明请参见 [points-display-fix.md](./points-display-fix.md) 文档。

主要修复内容：
- API端点匹配修复
- 数据提取逻辑优化
- UI更新机制完善
- 错误处理增强

这些修复确保了积分信息、签到状态和会员等级能够正确显示和更新。

### 7.2 积分累计获得与已使用统计修复

积分商城中"累计获得"和"已使用"积分数值显示为0的问题已修复，详细说明请参见 [points-earned-used-fix.md](./points-earned-used-fix.md) 文档。

主要修复内容：
- 修复积分历史查询统计逻辑
- 更新签到方法中的返回值处理
- 增强签到日历中的积分统计数据
- 确保所有积分操作能正确更新相关统计值

这些修复确保了用户可以正确查看其积分收支情况，提高了积分系统的透明度和用户体验。 