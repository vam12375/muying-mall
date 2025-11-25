# SKU规格值格式修复说明

## 问题描述

商品详情页显示"规格: 默认"，而不是实际的规格值（如"NB码"）。

## 原因分析

`product_sku` 表的 `spec_values` 字段格式不正确：

### ❌ 错误格式（对象）
```json
{"规格":"NB码"}
```

### ✅ 正确格式（数组）
```json
[{"spec_name":"规格","spec_value":"NB码"}]
```

## 后端代码期望

`ProductSkuServiceImpl.convertToDTO()` 方法期望解析数组格式：

```java
List<Map<String, String>> specValues = JSON.parseObject(
    entity.getSpecValues(), 
    new TypeReference<List<Map<String, String>>>() {}
);
```

## 修复步骤

### 1. 执行修复SQL脚本

```bash
mysql -u root -p muying_mall < muying-mall/src/main/resources/sql/fix_sku_spec_values.sql
```

### 2. 验证修复结果

```sql
-- 查看商品120的SKU数据
SELECT product_id, sku_code, sku_name, spec_values 
FROM product_sku 
WHERE product_id = 120;
```

**修复前：**
```
spec_values: {"规格":"NB码"}
```

**修复后：**
```
spec_values: [{"spec_name":"规格","spec_value":"NB码"}]
```

### 3. 重启后端服务

```bash
cd muying-mall
mvn spring-boot:run
```

### 4. 清除前端缓存

```bash
# 清除浏览器缓存或使用无痕模式
# 或者在浏览器控制台执行
localStorage.clear();
sessionStorage.clear();
```

### 5. 测试验证

1. 访问商品详情页（如商品ID 120）
2. 检查规格显示是否正确
3. 选择规格后检查价格和库存
4. 添加到购物车测试

## 数据格式说明

### 单规格商品（如纸尿裤）
```json
[
  {
    "spec_name": "规格",
    "spec_value": "NB码"
  }
]
```

### 多规格商品（如奶粉）
```json
[
  {
    "spec_name": "段数",
    "spec_value": "1段"
  },
  {
    "spec_name": "重量",
    "spec_value": "900g"
  }
]
```

## 前端显示逻辑

### useSku.js 解析规格
```javascript
const specs = typeof sku.specValues === 'string' 
  ? JSON.parse(sku.specValues) 
  : sku.specValues;

// specs 应该是数组格式
// [{"spec_name":"规格","spec_value":"NB码"}]
```

### 提取规格维度
```javascript
specDimensions.value.forEach(sku => {
  sku.specValues.forEach(spec => {
    const name = spec.spec_name;  // "规格"
    const value = spec.spec_value; // "NB码"
    // ...
  });
});
```

## 常见问题

### Q1: 为什么要使用数组格式？
**A:** 数组格式支持多规格组合，更灵活。例如奶粉可以有"段数"和"重量"两个规格维度。

### Q2: 旧数据怎么办？
**A:** 执行 `fix_sku_spec_values.sql` 脚本会批量更新所有SKU数据。

### Q3: 如何添加新的SKU？
**A:** 使用正确的数组格式：
```sql
INSERT INTO product_sku (product_id, sku_code, sku_name, spec_values, ...)
VALUES (121, 'NEW-SKU', '新规格', '[{"spec_name":"规格","spec_value":"新值"}]', ...);
```

### Q4: 前端如何格式化显示？
**A:** 使用工具函数：
```javascript
import { formatSpecsText } from '@/utils/sku';

const text = formatSpecsText(sku.specValues);
// 输出: "规格: NB码"
```

## 相关文件

- 修复脚本：`muying-mall/src/main/resources/sql/fix_sku_spec_values.sql`
- 后端转换：`ProductSkuServiceImpl.convertToDTO()`
- 前端解析：`muying-web/src/composables/useSku.js`
- 工具函数：`muying-web/src/utils/sku.js`

---

**创建时间**: 2024-11-24  
**问题状态**: ✅ 已修复  
**影响范围**: 所有SKU商品的规格显示
