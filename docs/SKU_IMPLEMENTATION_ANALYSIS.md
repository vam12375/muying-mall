# SKU 功能实现分析与完善建议

## 📊 当前实现状态

### ✅ 已实现部分

#### 1. 数据库层面
**product_specs 表** (商品规格表)
```sql
CREATE TABLE `product_specs` (
  `spec_id` int UNSIGNED NOT NULL AUTO_INCREMENT,
  `product_id` int UNSIGNED NOT NULL,
  `spec_name` varchar(50) NOT NULL COMMENT '规格名称，如颜色、尺寸',
  `spec_values` json NOT NULL COMMENT '规格值列表',
  `sort_order` int DEFAULT 0,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`spec_id`),
  INDEX `idx_product_id`(`product_id`),
  CONSTRAINT `fk_product_specs_product` FOREIGN KEY (`product_id`) 
    REFERENCES `product` (`product_id`) ON DELETE CASCADE
)
```

**数据示例**:
```json
{
  "spec_name": "规格",
  "spec_values": [
    {"id": 1, "name": "1段(0-6个月)"},
    {"id": 2, "name": "2段(6-12个月)"},
    {"id": 3, "name": "3段(1-3岁)"}
  ]
}
```

#### 2. 实体类层面
**Product.java** - 包含规格关联
```java
@TableField(exist = false)
private List<ProductSpecs> specsList;  // 商品规格列表
```

**ProductSpecs.java** - 规格实体
```java
private Integer specId;
private Integer productId;
private String specName;      // 规格名称
private String specValues;    // JSON 格式的规格值列表
private Integer sortOrder;
```

#### 3. 前端类型定义
**product.ts** - TypeScript 类型
```typescript
export interface Product {
  specsList?: any[] | null;  // 规格列表
}

export interface ProductFormData {
  specsList?: any[];  // 规格列表
}
```

---

## ❌ 缺失的核心功能

### 1. **SKU 表缺失** ⚠️ 严重问题

当前只有 `product_specs` 表存储规格选项,但**没有 SKU 表**来存储具体的规格组合及其对应的:
- 价格
- 库存
- SKU 编码
- 图片

**问题示例**:
- 商品有"1段/900g"和"2段/1.8kg"两种组合
- 当前无法为每个组合设置不同的价格和库存
- 只能在 `product` 表中设置统一的 `priceNew` 和 `stock`

### 2. **规格值表缺失**

虽然数据库中有 `spec_value` 表的创建语句,但:
- 没有对应的 Java 实体类
- 没有对应的 Service 和 Controller
- 规格值以 JSON 格式存储在 `product_specs.spec_values` 中

### 3. **购物车 SKU 关联缺失**

**cart 表**需要关联具体的 SKU:
```sql
-- 当前 cart 表只有 product_id
-- 缺少 sku_id 字段
```

### 4. **订单 SKU 关联缺失**

**order_product 表**需要记录购买的具体 SKU:
```sql
-- 当前 order_product 表只有 product_id
-- 缺少 sku_id 和 sku_name 字段
```

---

## 🎯 完善方案

### 方案一：标准 SKU 架构 (推荐)

#### 1. 创建 SKU 表

```sql
-- SKU 表（商品库存单位）
CREATE TABLE `product_sku` (
  `sku_id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'SKU ID',
  `product_id` int UNSIGNED NOT NULL COMMENT '商品ID',
  `sku_code` varchar(50) NOT NULL COMMENT 'SKU编码',
  `sku_name` varchar(200) NOT NULL COMMENT 'SKU名称（如：1段/900g）',
  `spec_values` json NOT NULL COMMENT '规格值组合 JSON',
  `price` decimal(10,2) NOT NULL COMMENT 'SKU价格',
  `stock` int NOT NULL DEFAULT 0 COMMENT 'SKU库存',
  `sku_image` varchar(255) NULL COMMENT 'SKU图片',
  `weight` decimal(10,2) NULL COMMENT '重量(kg)',
  `volume` decimal(10,2) NULL COMMENT '体积(m³)',
  `status` tinyint(1) NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
  `sort_order` int DEFAULT 0 COMMENT '排序',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`sku_id`),
  UNIQUE KEY `uk_sku_code` (`sku_code`),
  KEY `idx_product_id` (`product_id`),
  KEY `idx_status` (`status`),
  CONSTRAINT `fk_sku_product` FOREIGN KEY (`product_id`) 
    REFERENCES `product` (`product_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品SKU表';

-- SKU 数据示例
INSERT INTO `product_sku` VALUES 
(1, 1, 'WYS-1D-900G', '惠氏启赋 1段/900g', 
 '[{"spec_name":"规格","spec_value":"1段(0-6个月)"},{"spec_name":"重量","spec_value":"900g"}]',
 358.00, 100, 'sku_1_1.jpg', 0.9, NULL, 1, 1, NOW(), NOW()),
(2, 1, 'WYS-1D-1800G', '惠氏启赋 1段/1.8kg',
 '[{"spec_name":"规格","spec_value":"1段(0-6个月)"},{"spec_name":"重量","spec_value":"1.8kg"}]',
 688.00, 50, 'sku_1_2.jpg', 1.8, NULL, 1, 2, NOW(), NOW());
```

#### 2. 修改相关表结构

```sql
-- 修改 cart 表，添加 SKU 关联
ALTER TABLE `cart` 
ADD COLUMN `sku_id` bigint UNSIGNED NULL COMMENT 'SKU ID' AFTER `product_id`,
ADD INDEX `idx_sku_id` (`sku_id`);

-- 修改 order_product 表，添加 SKU 信息
ALTER TABLE `order_product`
ADD COLUMN `sku_id` bigint UNSIGNED NULL COMMENT 'SKU ID' AFTER `product_id`,
ADD COLUMN `sku_code` varchar(50) NULL COMMENT 'SKU编码' AFTER `sku_id`,
ADD COLUMN `sku_name` varchar(200) NULL COMMENT 'SKU名称' AFTER `sku_code`,
ADD INDEX `idx_sku_id` (`sku_id`);

-- 修改 product 表，添加 SKU 相关字段
ALTER TABLE `product`
ADD COLUMN `has_sku` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否有SKU：0-否，1-是' AFTER `product_status`,
ADD COLUMN `min_price` decimal(10,2) NULL COMMENT '最低价格' AFTER `price_old`,
ADD COLUMN `max_price` decimal(10,2) NULL COMMENT '最高价格' AFTER `min_price`;
```

#### 3. 创建 Java 实体类

```java
// ProductSku.java
package com.muyingmall.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品SKU实体类
 */
@Data
@TableName("product_sku")
public class ProductSku implements Serializable {
    
    private static final long serialVersionUID = 1L;

    /**
     * SKU ID
     */
    @TableId(value = "sku_id", type = IdType.AUTO)
    private Long skuId;

    /**
     * 商品ID
     */
    private Integer productId;

    /**
     * SKU编码
     */
    private String skuCode;

    /**
     * SKU名称
     */
    private String skuName;

    /**
     * 规格值组合（JSON格式）
     */
    private String specValues;

    /**
     * SKU价格
     */
    private BigDecimal price;

    /**
     * SKU库存
     */
    private Integer stock;

    /**
     * SKU图片
     */
    private String skuImage;

    /**
     * 重量(kg)
     */
    private BigDecimal weight;

    /**
     * 体积(m³)
     */
    private BigDecimal volume;

    /**
     * 状态：0-禁用，1-启用
     */
    private Integer status;

    /**
     * 排序
     */
    private Integer sortOrder;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
```

#### 4. 创建 DTO 类

```java
// ProductSkuDTO.java
package com.muyingmall.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 商品SKU DTO
 */
@Data
public class ProductSkuDTO {
    
    private Long skuId;
    private Integer productId;
    private String skuCode;
    private String skuName;
    private BigDecimal price;
    private Integer stock;
    private String skuImage;
    private BigDecimal weight;
    private BigDecimal volume;
    private Integer status;
    private Integer sortOrder;
    
    /**
     * 规格值列表
     * 示例: [{"spec_name":"规格","spec_value":"1段(0-6个月)"}]
     */
    private List<Map<String, String>> specValues;
}
```

#### 5. 创建 Mapper 接口

```java
// ProductSkuMapper.java
package com.muyingmall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.muyingmall.entity.ProductSku;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * 商品SKU Mapper
 */
@Mapper
public interface ProductSkuMapper extends BaseMapper<ProductSku> {
    
    /**
     * 根据商品ID查询SKU列表
     */
    List<ProductSku> selectByProductId(@Param("productId") Integer productId);
    
    /**
     * 根据SKU编码查询
     */
    ProductSku selectBySkuCode(@Param("skuCode") String skuCode);
    
    /**
     * 批量更新库存
     */
    int batchUpdateStock(@Param("list") List<ProductSku> list);
    
    /**
     * 扣减库存
     */
    int deductStock(@Param("skuId") Long skuId, @Param("quantity") Integer quantity);
    
    /**
     * 恢复库存
     */
    int restoreStock(@Param("skuId") Long skuId, @Param("quantity") Integer quantity);
}
```

#### 6. 创建 Service 接口

```java
// ProductSkuService.java
package com.muyingmall.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.muyingmall.entity.ProductSku;
import com.muyingmall.dto.ProductSkuDTO;
import java.util.List;

/**
 * 商品SKU Service
 */
public interface ProductSkuService extends IService<ProductSku> {
    
    /**
     * 根据商品ID获取SKU列表
     */
    List<ProductSkuDTO> getSkuListByProductId(Integer productId);
    
    /**
     * 根据SKU ID获取SKU详情
     */
    ProductSkuDTO getSkuById(Long skuId);
    
    /**
     * 根据SKU编码获取SKU详情
     */
    ProductSkuDTO getSkuByCode(String skuCode);
    
    /**
     * 批量保存或更新SKU
     */
    boolean saveOrUpdateBatch(Integer productId, List<ProductSkuDTO> skuList);
    
    /**
     * 扣减库存
     */
    boolean deductStock(Long skuId, Integer quantity);
    
    /**
     * 恢复库存
     */
    boolean restoreStock(Long skuId, Integer quantity);
    
    /**
     * 检查库存是否充足
     */
    boolean checkStock(Long skuId, Integer quantity);
    
    /**
     * 删除商品的所有SKU
     */
    boolean deleteByProductId(Integer productId);
}
```

#### 7. 前端类型定义

```typescript
// muying-admin/src/types/product.ts

/**
 * SKU 规格值
 */
export interface SkuSpecValue {
  spec_name: string;   // 规格名称
  spec_value: string;  // 规格值
}

/**
 * 商品 SKU
 */
export interface ProductSku {
  skuId?: number;           // SKU ID
  productId: number;        // 商品ID
  skuCode: string;          // SKU编码
  skuName: string;          // SKU名称
  specValues: SkuSpecValue[]; // 规格值组合
  price: number;            // SKU价格
  stock: number;            // SKU库存
  skuImage?: string;        // SKU图片
  weight?: number;          // 重量
  volume?: number;          // 体积
  status: number;           // 状态
  sortOrder?: number;       // 排序
}

/**
 * 商品表单数据（更新）
 */
export interface ProductFormData {
  // ... 其他字段
  hasSku: number;           // 是否有SKU
  skuList?: ProductSku[];   // SKU列表
}
```

---

### 方案二：简化方案（适用于规格简单的场景）

如果商品规格组合不复杂，可以继续使用当前的 `product_specs` 表，但需要:

1. **规范化 spec_values 的 JSON 结构**
```json
{
  "spec_name": "规格",
  "spec_values": [
    {
      "id": 1,
      "name": "1段(0-6个月)",
      "price": 358.00,
      "stock": 100,
      "image": "spec_1.jpg"
    },
    {
      "id": 2,
      "name": "2段(6-12个月)",
      "price": 368.00,
      "stock": 80,
      "image": "spec_2.jpg"
    }
  ]
}
```

2. **修改 ProductSpecs 实体类**
```java
@Data
public class SpecValue {
    private Integer id;
    private String name;
    private BigDecimal price;
    private Integer stock;
    private String image;
}

@Data
@TableName("product_specs")
public class ProductSpecs {
    // ... 其他字段
    
    @TableField(exist = false)
    private List<SpecValue> specValueList;  // 解析后的规格值列表
}
```

**缺点**:
- 不适合多规格组合（如：颜色+尺寸）
- 查询和更新库存较复杂
- 不符合数据库范式

---

## 🚀 实施步骤

### 阶段一：数据库改造（1-2天）
1. ✅ 创建 `product_sku` 表
2. ✅ 修改 `cart`、`order_product`、`product` 表
3. ✅ 迁移现有数据（如果有）
4. ✅ 测试数据库约束和索引

### 阶段二：后端开发（3-5天）
1. ✅ 创建 SKU 相关实体类、DTO、VO
2. ✅ 创建 Mapper、Service、Controller
3. ✅ 实现 SKU 的 CRUD 操作
4. ✅ 实现库存管理逻辑
5. ✅ 修改购物车和订单逻辑
6. ✅ 编写单元测试

### 阶段三：前端开发（3-5天）
1. ✅ 更新 TypeScript 类型定义
2. ✅ 创建 SKU 管理组件
3. ✅ 实现 SKU 选择器组件
4. ✅ 修改商品详情页
5. ✅ 修改购物车页面
6. ✅ 修改订单页面

### 阶段四：测试与优化（2-3天）
1. ✅ 功能测试
2. ✅ 性能测试
3. ✅ 库存并发测试
4. ✅ 用户体验优化

---

## 📋 API 设计建议

### 1. SKU 管理 API

```java
// ProductSkuController.java

/**
 * 获取商品的SKU列表
 * GET /api/admin/products/{productId}/skus
 */
@GetMapping("/{productId}/skus")
public Result<List<ProductSkuDTO>> getSkuList(@PathVariable Integer productId);

/**
 * 获取SKU详情
 * GET /api/admin/skus/{skuId}
 */
@GetMapping("/skus/{skuId}")
public Result<ProductSkuDTO> getSkuDetail(@PathVariable Long skuId);

/**
 * 批量保存或更新SKU
 * POST /api/admin/products/{productId}/skus
 */
@PostMapping("/{productId}/skus")
public Result<Boolean> saveOrUpdateSkus(
    @PathVariable Integer productId,
    @RequestBody List<ProductSkuDTO> skuList
);

/**
 * 删除SKU
 * DELETE /api/admin/skus/{skuId}
 */
@DeleteMapping("/skus/{skuId}")
public Result<Boolean> deleteSku(@PathVariable Long skuId);

/**
 * 更新SKU库存
 * PUT /api/admin/skus/{skuId}/stock
 */
@PutMapping("/skus/{skuId}/stock")
public Result<Boolean> updateStock(
    @PathVariable Long skuId,
    @RequestParam Integer stock
);
```

### 2. 前台 SKU API

```java
/**
 * 获取商品SKU列表（前台）
 * GET /api/products/{productId}/skus
 */
@GetMapping("/{productId}/skus")
public Result<List<ProductSkuDTO>> getProductSkus(@PathVariable Integer productId);

/**
 * 检查SKU库存
 * GET /api/skus/{skuId}/stock
 */
@GetMapping("/skus/{skuId}/stock")
public Result<Integer> checkSkuStock(@PathVariable Long skuId);
```

---

## ⚠️ 注意事项

### 1. 库存并发控制
```java
// 使用乐观锁或分布式锁
@Update("UPDATE product_sku SET stock = stock - #{quantity}, " +
        "version = version + 1 " +
        "WHERE sku_id = #{skuId} AND stock >= #{quantity} AND version = #{version}")
int deductStockWithVersion(@Param("skuId") Long skuId, 
                          @Param("quantity") Integer quantity,
                          @Param("version") Integer version);
```

### 2. 数据一致性
- 商品删除时级联删除 SKU
- SKU 删除时检查是否有未完成的订单
- 库存扣减失败时回滚订单

### 3. 性能优化
- SKU 列表查询添加缓存
- 库存查询使用 Redis
- 批量操作使用事务

### 4. 用户体验
- SKU 选择器支持图片预览
- 实时显示库存状态
- 价格随 SKU 变化实时更新

---

## 📚 参考资料

- [电商SKU设计最佳实践](https://example.com)
- [MyBatis-Plus 官方文档](https://baomidou.com/)
- [Vue3 + TypeScript 开发指南](https://vuejs.org/)

---

**文档创建时间**: 2024-11-24  
**遵循协议**: AURA-X-KYS (KISS/YAGNI/SOLID)  
**核心原则**: 简洁、实用、可扩展
