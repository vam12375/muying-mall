# SKU selectedSku为null问题排查与修复

## 问题现状
- ✅ `hasSku` 已正确显示为 `true`
- ❌ `selectedSku` 显示为 `null`
- ❌ `skuId`、`skuCode`、`skuName` 都为 `null`

## 可能原因分析

### 1. SKU数据未加载
**症状**：`skuList` 为空数组
**原因**：
- SKU API接口调用失败
- 商品ID 120在数据库中没有SKU数据
- SKU数据的 `status` 字段为0（禁用状态）

### 2. Watch监听器未触发
**症状**：控制台没有看到 "useSku watch触发" 的日志
**原因**：
- `product.value` 在watch初始化时为null
- `hasSku` 计算属性返回false

### 3. SKU未被自动选中
**症状**：有SKU数据但 `selectedSku` 仍为null
**原因**：
- SKU数量大于1，需要用户手动选择
- 自动选择逻辑有问题

## 排查步骤

### 步骤1：检查数据库中的SKU数据
```bash
mysql -u root -p muying_mall < check_product_120_sku.sql
```

**预期结果**：
- 商品120的 `has_sku` 字段为 1
- `product_sku` 表中有商品120的SKU记录
- SKU的 `status` 字段为 1（启用状态）

### 步骤2：测试后端SKU API
```bash
bash test_sku_api.sh
```

**预期结果**：
```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "skuId": 377,
      "productId": 120,
      "skuCode": "BY120-NB-O",
      "skuName": "花王纸尿裤NB号 尺码:NB 包装:箱装",
      "specValues": "{\"尺码\":\"NB\",\"包装\":\"箱装\"}",
      "price": 169.00,
      "stock": 100,
      "status": 1
    }
  ]
}
```

### 步骤3：检查前端控制台日志
打开浏览器控制台，访问商品详情页，查看以下日志：

1. **useSku watch触发日志**
```
useSku watch触发 - 商品变化: {hasProduct: true, hasSku: true, productId: 120, goodsId: 120}
准备加载SKU, productId: 120
```

2. **SKU API调用日志**
```
开始获取SKU列表, productId: 120
SKU API响应: {code: 200, data: [...]}
SKU列表加载成功, 数量: 4, 数据: [...]
```

3. **SKU选择日志**
```
有多个SKU，需要用户手动选择
```
或
```
只有一个SKU，自动选中: {...}
```

## 修复方案

### 方案1：如果SKU数据不存在
```sql
-- 为商品120添加SKU数据
INSERT INTO product_sku (product_id, sku_code, sku_name, spec_values, price, stock, status)
VALUES 
(120, 'BY120-NB-O', '花王纸尿裤NB号 尺码:NB 包装:箱装', '{"尺码":"NB","包装":"箱装"}', 169.00, 100, 1),
(120, 'BY120-NB-X', '花王纸尿裤NB号 尺码:NB 包装:盒装', '{"尺码":"NB","包装":"盒装"}', 119.00, 50, 1);
```

### 方案2：如果SKU状态为禁用
```sql
-- 启用商品120的所有SKU
UPDATE product_sku 
SET status = 1 
WHERE product_id = 120;
```

### 方案3：如果有多个SKU需要用户选择
在商品详情页添加SKU选择器UI组件，让用户手动选择规格。

**前端实现示例**：
```vue
<template>
  <div v-if="hasSku && specDimensions.length > 0" class="sku-selector">
    <div v-for="dim in specDimensions" :key="dim.name" class="spec-group">
      <label>{{ dim.name }}:</label>
      <div class="spec-options">
        <button
          v-for="value in dim.values"
          :key="value"
          :class="{ active: selectedSpecs[dim.name] === value }"
          @click="selectSpec(dim.name, value)"
        >
          {{ value }}
        </button>
      </div>
    </div>
    <div v-if="selectedSku" class="selected-info">
      <p>已选: {{ formatSpecsDisplay() }}</p>
      <p>价格: ¥{{ selectedSku.price }}</p>
      <p>库存: {{ selectedSku.stock }}</p>
    </div>
  </div>
</template>
```

### 方案4：如果只有一个SKU，强制自动选中
修改 `useSku.js`，在商品加载完成后立即尝试选中唯一的SKU：

```javascript
// 在watch中添加
if (newProduct && hasSku.value) {
  const productId = newProduct.productId || newProduct.product_id || newProduct.goodsId || newProduct.id;
  if (productId) {
    resetSelection();
    await fetchSkuList(productId);
    
    // 如果只有一个SKU，强制选中
    if (skuList.value.length === 1) {
      selectSkuById(skuList.value[0].skuId);
    }
  }
}
```

## 验证修复

### 1. 清理缓存
```bash
# 清理Redis缓存
redis-cli KEYS "product:detail:*" | xargs redis-cli DEL

# 清理浏览器缓存
# 在浏览器中按 Ctrl+Shift+Delete
```

### 2. 重启服务
```bash
cd muying-mall
mvn clean package
# 重启Spring Boot应用
```

### 3. 测试验证
1. 访问商品详情页：`http://localhost:3000/product/120`
2. 打开浏览器控制台
3. 点击"立即购买"按钮
4. 检查日志输出：
   - `hasSku: true` ✅
   - `selectedSku: {...}` ✅（不再是null）
   - `skuId: 377` ✅
   - `skuCode: "BY120-NB-O"` ✅

## 注意事项
1. 如果商品有多个SKU，必须在UI上提供规格选择器
2. 用户必须选择完整的规格组合才能购买
3. SKU的 `status` 字段必须为1才会在前端显示
4. 确保 `spec_values` 字段是有效的JSON格式

## 相关文件
- 前端SKU逻辑：`muying-web/src/composables/useSku.js`
- 前端商品详情：`muying-web/src/scripts/ProductDetail.js`
- 后端SKU Controller：`muying-mall/src/main/java/com/muyingmall/controller/user/ProductSkuController.java`
- 后端SKU Service：`muying-mall/src/main/java/com/muyingmall/service/impl/ProductSkuServiceImpl.java`
