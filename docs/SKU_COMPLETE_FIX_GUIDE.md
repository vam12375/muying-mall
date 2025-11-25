# SKU完整修复指南

## 📋 问题总结

商品ID 120显示"规格: 默认"而不是"NB码"的原因：

1. ✅ **数据库格式** - 已修复（spec_values格式正确）
2. ❌ **商品has_sku字段** - 需要更新为1
3. ❌ **前端未加载SKU** - 因为has_sku=0

## 🔧 完整修复步骤

### 步骤1：执行SQL修复脚本

```bash
# 1. 修复spec_values格式
mysql -u root -p muying_mall < muying-mall/src/main/resources/sql/fix_sku_spec_values.sql

# 2. 更新has_sku字段
mysql -u root -p muying_mall < muying-mall/src/main/resources/sql/update_product_has_sku.sql
```

### 步骤2：验证数据

```sql
-- 检查商品120的数据
SELECT 
    p.product_id,
    p.product_name,
    p.has_sku,
    ps.sku_name,
    ps.spec_values
FROM product p
LEFT JOIN product_sku ps ON p.product_id = ps.product_id
WHERE p.product_id = 120;
```

**期望结果：**
```
product_id: 120
product_name: 花王纸尿裤NB号
has_sku: 1
sku_name: NB码
spec_values: [{"spec_name":"规格","spec_value":"NB码"}]
```

### 步骤3：重启后端服务

```bash
cd muying-mall
mvn spring-boot:run
```

### 步骤4：清除前端缓存并测试

1. 清除浏览器缓存（Ctrl + Shift + Delete）
2. 或使用无痕模式
3. 访问：`http://localhost:5173/product/120`

## 🎯 两种SKU模式说明

### 模式1：多SKU商品（has_sku=1）
**示例**：奶粉（商品ID 1、2）

- 有多个SKU（如：1段/900g、1段/1800g）
- 前端显示SKU选择器
- 用户必须选择规格才能购买
- 价格和库存根据选择的SKU变化

**数据特征：**
```sql
product.has_sku = 1
product_sku表中有多条记录
```

### 模式2：单SKU商品（has_sku=1，但只有1个SKU）
**示例**：纸尿裤（商品ID 120）

- 只有1个默认SKU
- 前端显示SKU选择器（自动选中唯一SKU）
- 规格显示为实际值（如"NB码"）
- 价格和库存来自SKU

**数据特征：**
```sql
product.has_sku = 1
product_sku表中只有1条记录
```

### 模式3：无SKU商品（has_sku=0）
**示例**：旧系统商品

- 不使用SKU系统
- 使用旧的specs字段
- 价格和库存来自product表

## 📊 数据修复清单

### 已修复的SQL脚本

1. **fix_sku_spec_values.sql** - 修复spec_values格式
   ```sql
   -- 将 {"规格":"NB码"} 
   -- 改为 [{"spec_name":"规格","spec_value":"NB码"}]
   ```

2. **update_product_has_sku.sql** - 更新has_sku字段
   ```sql
   -- 为所有有SKU数据的商品设置has_sku=1
   UPDATE product p
   SET p.has_sku = 1
   WHERE EXISTS (SELECT 1 FROM product_sku ps WHERE ps.product_id = p.product_id);
   ```

### 一键执行所有修复

```bash
# Windows PowerShell
cd muying-mall/src/main/resources/sql
Get-Content fix_sku_spec_values.sql, update_product_has_sku.sql | mysql -u root -p muying_mall
```

## 🔍 故障排查

### 问题1：还是显示"默认"

**检查点：**
```sql
-- 1. 检查has_sku字段
SELECT product_id, product_name, has_sku FROM product WHERE product_id = 120;
-- 应该返回 has_sku = 1

-- 2. 检查SKU数据
SELECT sku_id, sku_name, spec_values FROM product_sku WHERE product_id = 120;
-- spec_values应该是数组格式

-- 3. 检查API响应
-- 浏览器控制台查看 /api/products/120 的响应
-- hasSku 应该是 1
```

### 问题2：前端不显示SKU选择器

**检查点：**
1. 浏览器控制台是否有错误
2. 检查 `useSku.js` 是否正确导入
3. 检查 `SkuSelector.vue` 组件是否存在

### 问题3：选择规格后无法加入购物车

**检查点：**
```javascript
// 浏览器控制台检查
console.log('hasSku:', hasSku.value)
console.log('selectedSku:', selectedSku.value)
console.log('canAddToCart:', canAddToCart.value)
```

## ✅ 验证清单

- [ ] 数据库spec_values格式正确
- [ ] 商品has_sku字段为1
- [ ] 后端API返回SKU数据
- [ ] 前端显示SKU选择器
- [ ] 规格显示正确（不是"默认"）
- [ ] 能够选择规格
- [ ] 价格和库存正确更新
- [ ] 能够加入购物车
- [ ] 购物车显示SKU信息

## 📝 快速命令参考

```bash
# 查看商品120的完整信息
mysql -u root -p -e "
SELECT 
    p.product_id,
    p.product_name,
    p.has_sku,
    ps.sku_id,
    ps.sku_name,
    ps.spec_values,
    ps.price,
    ps.stock
FROM muying_mall.product p
LEFT JOIN muying_mall.product_sku ps ON p.product_id = ps.product_id
WHERE p.product_id = 120;
"

# 批量更新所有商品的has_sku
mysql -u root -p muying_mall -e "
UPDATE product p
SET p.has_sku = 1
WHERE EXISTS (SELECT 1 FROM product_sku ps WHERE ps.product_id = p.product_id);
"

# 查看SKU统计
mysql -u root -p muying_mall -e "
SELECT 
    COUNT(DISTINCT product_id) as total_products,
    COUNT(*) as total_skus
FROM product_sku;
"
```

---

**创建时间**: 2024-11-24  
**最后更新**: 2024-11-24  
**状态**: ✅ 完整修复方案
