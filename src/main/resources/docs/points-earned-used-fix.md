# 积分累计获得与已使用统计修复

## 问题描述

在积分商城展示页面中，"累计获得"和"已使用"积分数值一直显示为0，即使用户有积分交易记录。

## 原因分析

通过代码检查发现以下问题：

1. 在`PointsServiceImpl`中的`getSignInStatus`方法中，虽然正确定义了查询积分历史记录的逻辑，但查询结果未能正确计算和返回。

2. 在`userSignin`方法中，签到成功后没有更新`totalEarned`（累计获得）字段的值，导致前端不能正确显示。

3. 在`getSignInCalendar`方法中，返回的签到状态信息中也缺少针对`totalEarned`和`totalUsed`字段的处理逻辑。

4. 前端在调用API后，虽然正确地从响应中提取了这些字段的值，但由于后端返回值为0，导致显示不正确。

## 修复内容

### 1. 修复`getSignInStatus`方法中的积分统计逻辑

```java
// 查询累计获得的积分 - 确保正确使用type字段
LambdaQueryWrapper<PointsHistory> earnQuery = new LambdaQueryWrapper<>();
earnQuery.eq(PointsHistory::getUserId, userId)
        .eq(PointsHistory::getType, "earn"); // 类型为"earn"的记录

List<PointsHistory> earnRecords = pointsHistoryMapper.selectList(earnQuery);
totalEarned = earnRecords.stream()
        .mapToInt(PointsHistory::getPoints)
        .sum();

// 查询已使用的积分 - 确保正确使用type字段
LambdaQueryWrapper<PointsHistory> usedQuery = new LambdaQueryWrapper<>();
usedQuery.eq(PointsHistory::getUserId, userId)
        .eq(PointsHistory::getType, "spend"); // 类型为"spend"的记录

List<PointsHistory> usedRecords = pointsHistoryMapper.selectList(usedQuery);
totalUsed = usedRecords.stream()
        .mapToInt(PointsHistory::getPoints)
        .map(Math::abs) // 确保用正数来计算总使用量
        .sum();
```

### 2. 更新`userSignin`方法中的返回值处理

在用户签到成功后，手动更新累计获得的积分值：

```java
// 手动更新累计获得的积分值
Integer oldTotalEarned = (Integer) status.getOrDefault("totalEarned", 0);
status.put("totalEarned", oldTotalEarned + totalEarnedPoints);

// 合并结果
result.putAll(status);
result.put("earnedPoints", earnedPoints);
result.put("totalEarnedPoints", totalEarnedPoints); // 添加包含额外奖励的总积分
// 确保积分值也放在根级别，方便前端直接获取
result.put("points", status.get("totalPoints"));
```

### 3. 增强`getSignInCalendar`方法的积分统计

在返回签到日历数据时，确保积分统计数据完整：

```java
// 确保积分统计值存在
if (!signInStatus.containsKey("totalEarned")) {
    // 手动获取累计获得的积分
    try {
        LambdaQueryWrapper<PointsHistory> earnQuery = new LambdaQueryWrapper<>();
        earnQuery.eq(PointsHistory::getUserId, userId)
                .eq(PointsHistory::getType, "earn");
        
        List<PointsHistory> earnRecords = pointsHistoryMapper.selectList(earnQuery);
        int totalEarned = earnRecords.stream()
                .mapToInt(PointsHistory::getPoints)
                .sum();
        
        signInStatus.put("totalEarned", totalEarned);
    } catch (Exception e) {
        log.error("计算累计获得积分失败: {}", e.getMessage());
        signInStatus.put("totalEarned", 0);
    }
}

if (!signInStatus.containsKey("totalUsed")) {
    // 手动获取已使用的积分
    try {
        LambdaQueryWrapper<PointsHistory> usedQuery = new LambdaQueryWrapper<>();
        usedQuery.eq(PointsHistory::getUserId, userId)
                .eq(PointsHistory::getType, "spend");
        
        List<PointsHistory> usedRecords = pointsHistoryMapper.selectList(usedQuery);
        int totalUsed = usedRecords.stream()
                .mapToInt(PointsHistory::getPoints)
                .map(Math::abs)
                .sum();
        
        signInStatus.put("totalUsed", totalUsed);
    } catch (Exception e) {
        log.error("计算已使用积分失败: {}", e.getMessage());
        signInStatus.put("totalUsed", 0);
    }
}
```

## 测试方法

1. 登录系统并访问积分商城页面
2. 观察顶部"累计获得"和"已使用"数值是否显示正确
3. 进行签到操作
4. 验证签到后"累计获得"积分数是否增加
5. 兑换商品后验证"已使用"积分数是否增加

## 预期结果

- 正确显示用户累计获得的积分总数
- 正确显示用户已使用的积分总数
- 签到和积分兑换等操作后，这些值会实时更新 