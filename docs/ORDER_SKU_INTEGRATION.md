# 订单SKU信息集成指南

## 问题描述

订单详情页面的商品信息区域没有显示SKU规格信息，导致用户无法看到购买的具体商品规格。

## 根本原因

1. **数据库层面**：`order_product`表缺少`sku_id`字段，无法关联到具体的SKU记录
2. **业务逻辑层面**：订单创建时只保存了规格文本，没有保存SKU ID
3. **前端显示层面**：`specs`字段格式不统一（有JSON对象、字符串、NULL等），导致解析失败

## 解决方案

### 阶段1：快速修复（已完成）

#### 1.1 前端修复

修改订单详情页面，添加规格信息格式化函数：

```javascript
// 格式化商品规格信息
const formatProductSpecs = (product) => {
  if (!product.specs) {
    return '默认规格';
  }
  
  try {
    // 如果specs是字符串，尝试解析为JSON
    let specsObj = product.specs;
    if (typeof specsObj === 'string') {
      specsObj = JSON.parse(specsObj);
    }
    
    // 如果是对象，转换为"键:值"格式
    if (typeof specsObj === 'object' && !Array.isArray(specsObj)) {
      const specsArray = Object.entries(specsObj).map(([key, value]) => `${key}: ${value}`);
      return specsArray.join(' · ') || '默认规格';
    }
    
    // 如果是数组，直接join
    if (Array.isArray(specsObj)) {
      return specsObj.join(' · ') || '默认规格';
    }
    
    return String(specsObj) || '默认规格';
  } catch (e) {
    return typeof product.specs === 'string' ? product.specs : '默认规格';
  }
}
```

### 阶段2：完整SKU集成（进行中）

#### 2.1 数据库变更

**执行SQL脚本**：

1. `order_product_add_sku.sql` - 为订单商品表添加SKU字段
2. `cart_add_sku.sql` - 为购物车表添加SKU字段
3. `fix_order_product_specs.sql` - 修复现有订单的规格数据

```sql
-- 1. 为order_product表添加SKU字段
ALTER TABLE `order_product` 
ADD COLUMN `sku_id` BIGINT NULL COMMENT 'SKU ID' AFTER `product_id`,
ADD COLUMN `sku_code` VARCHAR(50) NULL COMMENT 'SKU编码' AFTER `sku_id`,
ADD COLUMN `sku_name` VARCHAR(200) NULL COMMENT 'SKU名称（含规格）' AFTER `sku_code`,
ADD INDEX `idx_sku_id`(`sku_id` ASC);

-- 2. 为cart表添加SKU字段
ALTER TABLE `cart` 
ADD COLUMN `sku_id` BIGINT NULL COMMENT 'SKU ID' AFTER `product_id`,
ADD INDEX `idx_sku_id`(`sku_id` ASC);
```

#### 2.2 实体类更新

**OrderProduct.java**：
```java
/**
 * SKU ID
 */
private Long skuId;

/**
 * SKU编码
 */
private String skuCode;

/**
 * SKU名称（含规格信息）
 */
private String skuName;
```

**Cart.java**：
```java
/**
 * SKU ID
 */
private Long skuId;
```

#### 2.3 业务逻辑更新

**订单创建逻辑**（OrderServiceImpl.java）：

从购物车创建订单时，保存SKU信息：
```java
// 保存SKU信息
if (cart.getSkuId() != null) {
    orderProduct.setSkuId(cart.getSkuId());
}

// 保存规格信息
orderProduct.setSpecs(cart.getSpecs());
```

#### 2.4 前端集成（待完成）

**需要修改的文件**：

1. **商品详情页面** (`muying-web/src/views/product/Detail/index.vue`)
   - 用户选择规格后，获取对应的SKU ID
   - 添加到购物车时传递SKU ID

2. **购物车API** (`muying-web/src/api/modules/cart.js`)
   - 添加到购物车接口增加`skuId`参数

3. **购物车Store** (`muying-web/src/stores/modules/cart.js`)
   - 保存SKU ID到购物车项

**示例代码**：
```javascript
// 添加到购物车
const cartData = {
  productId: product.id,
  skuId: selectedSku.value?.id, // 添加SKU ID
  quantity: quantity.value,
  specs: selectedSpecs.value,
  selected: 1
}

await addToCartApi(cartData);
```

#### 2.5 后端API更新（待完成）

**CartController.java**：
```java
@PostMapping("/add")
public Result<String> addToCart(@RequestBody CartAddDTO cartAddDTO) {
    // 验证SKU ID是否有效
    if (cartAddDTO.getSkuId() != null) {
        ProductSku sku = productSkuService.getById(cartAddDTO.getSkuId());
        if (sku == null) {
            return Result.error("SKU不存在");
        }
        // 验证SKU库存
        if (sku.getStock() < cartAddDTO.getQuantity()) {
            return Result.error("SKU库存不足");
        }
    }
    
    cartService.addToCart(cartAddDTO);
    return Result.success("添加成功");
}
```

## 数据迁移

### 现有订单数据处理

对于已经创建的订单，由于没有SKU ID，需要：

1. **保持现有specs字段数据**：确保历史订单的规格信息可以正常显示
2. **标准化specs格式**：将所有specs字段转换为统一的JSON格式
3. **新订单使用SKU ID**：从现在开始，所有新订单都应该保存SKU ID

### 执行步骤

```bash
# 1. 备份数据库
mysqldump -u root -p muying_mall > backup_before_sku_integration.sql

# 2. 执行数据库变更
mysql -u root -p muying_mall < order_product_add_sku.sql
mysql -u root -p muying_mall < cart_add_sku.sql

# 3. 修复现有数据
mysql -u root -p muying_mall < fix_order_product_specs.sql

# 4. 验证数据
mysql -u root -p muying_mall -e "SELECT * FROM order_product LIMIT 10;"
```

## 测试计划

### 1. 单元测试

- [ ] 测试订单创建时SKU信息保存
- [ ] 测试订单详情查询时SKU信息返回
- [ ] 测试规格信息格式化函数

### 2. 集成测试

- [ ] 测试从商品详情页添加到购物车（带SKU）
- [ ] 测试从购物车创建订单（带SKU）
- [ ] 测试订单详情页显示SKU信息

### 3. 回归测试

- [ ] 测试历史订单的规格信息显示
- [ ] 测试没有SKU的商品订单创建
- [ ] 测试订单列表和详情的兼容性

## 注意事项

1. **向后兼容**：确保没有SKU ID的历史订单仍然可以正常显示
2. **数据一致性**：SKU ID和specs字段应该保持一致
3. **库存管理**：使用SKU ID后，库存扣减应该基于SKU而不是商品
4. **价格管理**：订单价格应该使用SKU价格而不是商品基础价格

## 相关文档

- [SKU系统实现指南](./SKU_IMPLEMENTATION_GUIDE.md)
- [SKU快速开始](./SKU_QUICK_START.md)
- [数据库设计文档](./DATABASE_DESIGN.md)

## 更新日志

- 2025-11-24：创建文档，完成阶段1快速修复
- 2025-11-24：添加数据库变更脚本和实体类更新
