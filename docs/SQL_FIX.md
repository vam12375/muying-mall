# SQL 错误修复说明

## 🐛 问题描述

MyBatis 动态 SQL 标签 `<if>` 没有被正确解析，导致 SQL 语法错误。

## ❌ 错误信息

```
You have an error in your SQL syntax near 'test='adminId != null'> AND admin_id = 1 </if>'
```

## ✅ 修复方案

在使用动态 SQL 标签时，必须用 `<script>` 标签包裹整个 SQL 语句。

### 修复前
```java
@Select("SELECT AVG(duration_seconds) as avg_duration " +
        "FROM admin_login_records " +
        "WHERE duration_seconds IS NOT NULL " +
        "<if test='adminId != null'> AND admin_id = #{adminId} </if>")
```

### 修复后
```java
@Select("<script>" +
        "SELECT AVG(duration_seconds) as avg_duration " +
        "FROM admin_login_records " +
        "WHERE duration_seconds IS NOT NULL " +
        "<if test='adminId != null'> AND admin_id = #{adminId} </if>" +
        "</script>")
```

## 📝 修复的方法

1. `selectAvgOnlineTime()` - 获取平均在线时长
2. `selectMaxSessionTime()` - 获取最长会话时长
3. `selectLoginStatistics()` - 获取登录统计数据

## 🔄 应用修复

```bash
# 重启后端服务
cd muying-mall
mvn spring-boot:run
```

## ✅ 验证

访问 `http://localhost:3000/profile`，统计数据应正常显示。

---

**修复时间**: 2025-11-25  
**文件**: `AdminLoginRecordMapper.java`
