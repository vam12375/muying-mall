# 测试数据生成

## 问题
统计数据显示为 0，因为数据库中没有足够的记录。

## 解决方案

### 方法1: 多次登录生成数据
```bash
# 重启后端服务
cd muying-mall
mvn spring-boot:run

# 然后多次登录系统（登出再登录）
# 每次登录都会自动记录到 admin_login_records 表
```

### 方法2: 手动插入测试数据
```sql
-- 插入登录记录测试数据
INSERT INTO admin_login_records 
(admin_id, admin_name, login_time, ip_address, location, device_type, browser, os, login_status, duration_seconds)
VALUES 
(1, 'admin', NOW() - INTERVAL 1 DAY, '127.0.0.1', '本地', 'Desktop', 'Chrome', 'Windows', 'success', 3600),
(1, 'admin', NOW() - INTERVAL 2 DAY, '127.0.0.1', '本地', 'Desktop', 'Chrome', 'Windows', 'success', 7200),
(1, 'admin', NOW() - INTERVAL 3 DAY, '127.0.0.1', '本地', 'Desktop', 'Chrome', 'Windows', 'success', 5400),
(1, 'admin', NOW() - INTERVAL 4 DAY, '127.0.0.1', '本地', 'Desktop', 'Chrome', 'Windows', 'success', 4800),
(1, 'admin', NOW() - INTERVAL 5 DAY, '127.0.0.1', '本地', 'Desktop', 'Chrome', 'Windows', 'success', 6000);

-- 插入操作记录测试数据
INSERT INTO admin_operation_logs 
(admin_id, admin_name, operation, module, operation_type, request_method, request_url, ip_address, operation_result, execution_time_ms)
VALUES 
(1, 'admin', '查看用户列表', '用户管理', 'READ', 'GET', '/admin/users', '127.0.0.1', 'success', 125),
(1, 'admin', '查看商品列表', '商品管理', 'READ', 'GET', '/admin/products', '127.0.0.1', 'success', 98),
(1, 'admin', '查看订单列表', '订单管理', 'READ', 'GET', '/admin/orders', '127.0.0.1', 'success', 156),
(1, 'admin', '修改商品信息', '商品管理', 'UPDATE', 'PUT', '/admin/products/1', '127.0.0.1', 'success', 234),
(1, 'admin', '创建优惠券', '优惠券管理', 'CREATE', 'POST', '/admin/coupons', '127.0.0.1', 'success', 189);
```

### 方法3: 使用现有数据
如果系统已经有操作记录，确保：
1. 后端服务已重启
2. 浏览器已刷新
3. 检查控制台是否有 API 错误

## 验证
刷新个人中心页面，统计数据应该显示正确的数字。

---

**创建时间**: 2025-11-25
