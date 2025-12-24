package com.muyingmall.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.muyingmall.annotation.AdminOperationLog;
import com.muyingmall.common.api.Result;
import com.muyingmall.dto.SeckillProductDTO;
import com.muyingmall.entity.SeckillProduct;
import com.muyingmall.service.SeckillProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 秒杀商品管理控制器（管理员权限）
 * 
 * @author MuyingMall
 */
@Slf4j
@RestController
@RequestMapping("/admin/seckill/products")
@RequiredArgsConstructor
@Tag(name = "秒杀商品管理", description = "秒杀商品管理相关接口，仅管理员可访问")
public class SeckillProductAdminController {

    private final SeckillProductService seckillProductService;

    /**
     * 分页获取秒杀商品列表
     */
    @GetMapping("/page")
    @Operation(summary = "分页获取秒杀商品列表")
    @PreAuthorize("hasAuthority('admin')")
    @AdminOperationLog(operation = "查询秒杀商品列表", module = "秒杀管理", operationType = "READ")
    public Result<IPage<SeckillProductDTO>> getProductPage(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "活动ID") @RequestParam(required = false) Long activityId,
            @Parameter(description = "商品名称关键字") @RequestParam(required = false) String keyword) {
        
        log.debug("查询秒杀商品列表 - page: {}, size: {}, activityId: {}, keyword: {}", 
                page, size, activityId, keyword);
        
        try {
            Page<SeckillProductDTO> pageParam = new Page<>(page, size);
            IPage<SeckillProductDTO> productPage = seckillProductService.getProductPage(pageParam, activityId, keyword);
            
            log.debug("查询成功，共 {} 条记录", productPage.getTotal());
            return Result.success(productPage);
        } catch (Exception e) {
            log.error("查询秒杀商品列表失败", e);
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 根据活动ID获取秒杀商品列表
     */
    @GetMapping("/activity/{activityId}")
    @Operation(summary = "根据活动ID获取秒杀商品列表")
    @PreAuthorize("hasAuthority('admin')")
    @AdminOperationLog(operation = "查询活动秒杀商品", module = "秒杀管理", operationType = "READ")
    public Result<List<SeckillProductDTO>> getProductsByActivityId(@PathVariable Long activityId) {
        log.debug("查询活动秒杀商品 - activityId: {}", activityId);
        
        try {
            List<SeckillProductDTO> products = seckillProductService.getProductsByActivityId(activityId);
            log.debug("查询成功，共 {} 个商品", products.size());
            return Result.success(products);
        } catch (Exception e) {
            log.error("查询活动秒杀商品失败 - activityId: {}", activityId, e);
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 获取秒杀商品详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取秒杀商品详情")
    @PreAuthorize("hasAuthority('admin')")
    @AdminOperationLog(operation = "查看秒杀商品详情", module = "秒杀管理", operationType = "READ")
    public Result<SeckillProductDTO> getProductById(@PathVariable Long id) {
        log.debug("查询秒杀商品详情 - id: {}", id);
        
        try {
            SeckillProductDTO product = seckillProductService.getProductDetailById(id);
            if (product == null) {
                return Result.error(404, "秒杀商品不存在");
            }
            return Result.success(product);
        } catch (Exception e) {
            log.error("查询秒杀商品详情失败 - id: {}", id, e);
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 添加秒杀商品
     */
    @PostMapping
    @Operation(summary = "添加秒杀商品")
    @PreAuthorize("hasAuthority('admin')")
    @AdminOperationLog(operation = "添加秒杀商品", module = "秒杀管理", operationType = "CREATE")
    public Result<SeckillProduct> addProduct(@RequestBody SeckillProduct seckillProduct) {
        log.debug("添加秒杀商品 - activityId: {}, productId: {}, skuId: {}", 
                seckillProduct.getActivityId(), seckillProduct.getProductId(), seckillProduct.getSkuId());
        
        try {
            // 参数校验
            if (seckillProduct.getActivityId() == null) {
                return Result.error("活动ID不能为空");
            }
            if (seckillProduct.getProductId() == null) {
                return Result.error("商品ID不能为空");
            }
            if (seckillProduct.getSkuId() == null) {
                return Result.error("SKU ID不能为空");
            }
            if (seckillProduct.getSeckillPrice() == null || seckillProduct.getSeckillPrice().compareTo(java.math.BigDecimal.ZERO) <= 0) {
                return Result.error("秒杀价格必须大于0");
            }
            if (seckillProduct.getSeckillStock() == null || seckillProduct.getSeckillStock() <= 0) {
                return Result.error("秒杀库存必须大于0");
            }
            if (seckillProduct.getLimitPerUser() == null || seckillProduct.getLimitPerUser() <= 0) {
                return Result.error("限购数量必须大于0");
            }
            
            boolean success = seckillProductService.save(seckillProduct);
            if (success) {
                log.info("添加秒杀商品成功 - id: {}", seckillProduct.getId());
                return Result.success(seckillProduct, "添加成功");
            } else {
                return Result.error("添加失败");
            }
        } catch (Exception e) {
            log.error("添加秒杀商品失败", e);
            return Result.error("添加失败：" + e.getMessage());
        }
    }

    /**
     * 批量添加秒杀商品
     */
    @PostMapping("/batch")
    @Operation(summary = "批量添加秒杀商品")
    @PreAuthorize("hasAuthority('admin')")
    @AdminOperationLog(operation = "批量添加秒杀商品", module = "秒杀管理", operationType = "CREATE")
    public Result<Boolean> addProductsBatch(@RequestBody List<SeckillProduct> seckillProducts) {
        log.debug("批量添加秒杀商品 - 数量: {}", seckillProducts.size());
        
        try {
            // 批量参数校验
            for (SeckillProduct product : seckillProducts) {
                if (product.getActivityId() == null || product.getProductId() == null || 
                    product.getSkuId() == null || product.getSeckillPrice() == null || 
                    product.getSeckillStock() == null || product.getLimitPerUser() == null) {
                    return Result.error("存在参数不完整的商品");
                }
            }
            
            boolean success = seckillProductService.saveBatch(seckillProducts);
            if (success) {
                log.info("批量添加秒杀商品成功 - 数量: {}", seckillProducts.size());
                return Result.success(true, "批量添加成功");
            } else {
                return Result.error("批量添加失败");
            }
        } catch (Exception e) {
            log.error("批量添加秒杀商品失败", e);
            return Result.error("批量添加失败：" + e.getMessage());
        }
    }

    /**
     * 更新秒杀商品
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新秒杀商品")
    @PreAuthorize("hasAuthority('admin')")
    @AdminOperationLog(operation = "更新秒杀商品", module = "秒杀管理", operationType = "UPDATE")
    public Result<Boolean> updateProduct(@PathVariable Long id, @RequestBody SeckillProduct seckillProduct) {
        log.debug("更新秒杀商品 - id: {}", id);
        
        try {
            // 检查商品是否存在
            SeckillProduct existingProduct = seckillProductService.getById(id);
            if (existingProduct == null) {
                return Result.error(404, "秒杀商品不存在");
            }
            
            // 参数校验
            if (seckillProduct.getSeckillPrice() != null && 
                seckillProduct.getSeckillPrice().compareTo(java.math.BigDecimal.ZERO) <= 0) {
                return Result.error("秒杀价格必须大于0");
            }
            if (seckillProduct.getSeckillStock() != null && seckillProduct.getSeckillStock() < 0) {
                return Result.error("秒杀库存不能为负数");
            }
            if (seckillProduct.getLimitPerUser() != null && seckillProduct.getLimitPerUser() <= 0) {
                return Result.error("限购数量必须大于0");
            }
            
            seckillProduct.setId(id);
            boolean success = seckillProductService.updateById(seckillProduct);
            
            if (success) {
                log.info("更新秒杀商品成功 - id: {}", id);
                return Result.success(true, "更新成功");
            } else {
                return Result.error("更新失败");
            }
        } catch (Exception e) {
            log.error("更新秒杀商品失败 - id: {}", id, e);
            return Result.error("更新失败：" + e.getMessage());
        }
    }

    /**
     * 删除秒杀商品
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除秒杀商品")
    @PreAuthorize("hasAuthority('admin')")
    @AdminOperationLog(operation = "删除秒杀商品", module = "秒杀管理", operationType = "DELETE")
    public Result<Boolean> deleteProduct(@PathVariable Long id) {
        log.debug("删除秒杀商品 - id: {}", id);
        
        try {
            // 检查商品是否存在
            SeckillProduct product = seckillProductService.getById(id);
            if (product == null) {
                return Result.error(404, "秒杀商品不存在");
            }
            
            boolean success = seckillProductService.removeById(id);
            if (success) {
                log.info("删除秒杀商品成功 - id: {}", id);
                return Result.success(true, "删除成功");
            } else {
                return Result.error("删除失败");
            }
        } catch (Exception e) {
            log.error("删除秒杀商品失败 - id: {}", id, e);
            return Result.error("删除失败：" + e.getMessage());
        }
    }

    /**
     * 批量删除秒杀商品
     */
    @DeleteMapping("/batch")
    @Operation(summary = "批量删除秒杀商品")
    @PreAuthorize("hasAuthority('admin')")
    @AdminOperationLog(operation = "批量删除秒杀商品", module = "秒杀管理", operationType = "DELETE")
    public Result<Boolean> deleteProductsBatch(@RequestBody List<Long> ids) {
        log.debug("批量删除秒杀商品 - 数量: {}", ids.size());
        
        try {
            boolean success = seckillProductService.removeByIds(ids);
            if (success) {
                log.info("批量删除秒杀商品成功 - 数量: {}", ids.size());
                return Result.success(true, "批量删除成功");
            } else {
                return Result.error("批量删除失败");
            }
        } catch (Exception e) {
            log.error("批量删除秒杀商品失败", e);
            return Result.error("批量删除失败：" + e.getMessage());
        }
    }

    /**
     * 调整秒杀商品库存
     */
    @PutMapping("/{id}/stock")
    @Operation(summary = "调整秒杀商品库存")
    @PreAuthorize("hasAuthority('admin')")
    @AdminOperationLog(operation = "调整秒杀商品库存", module = "秒杀管理", operationType = "UPDATE")
    public Result<Boolean> adjustStock(
            @PathVariable Long id,
            @Parameter(description = "新库存数量") @RequestParam Integer stock) {
        
        log.debug("调整秒杀商品库存 - id: {}, stock: {}", id, stock);
        
        try {
            // 检查商品是否存在
            SeckillProduct product = seckillProductService.getById(id);
            if (product == null) {
                return Result.error(404, "秒杀商品不存在");
            }
            
            // 库存校验
            if (stock < 0) {
                return Result.error("库存不能为负数");
            }
            
            product.setSeckillStock(stock);
            boolean success = seckillProductService.updateById(product);
            
            if (success) {
                log.info("调整秒杀商品库存成功 - id: {}, stock: {}", id, stock);
                return Result.success(true, "库存调整成功");
            } else {
                return Result.error("库存调整失败");
            }
        } catch (Exception e) {
            log.error("调整秒杀商品库存失败 - id: {}", id, e);
            return Result.error("库存调整失败：" + e.getMessage());
        }
    }

    /**
     * 调整秒杀商品价格
     */
    @PutMapping("/{id}/price")
    @Operation(summary = "调整秒杀商品价格")
    @PreAuthorize("hasAuthority('admin')")
    @AdminOperationLog(operation = "调整秒杀商品价格", module = "秒杀管理", operationType = "UPDATE")
    public Result<Boolean> adjustPrice(
            @PathVariable Long id,
            @Parameter(description = "新秒杀价格") @RequestParam java.math.BigDecimal price) {
        
        log.debug("调整秒杀商品价格 - id: {}, price: {}", id, price);
        
        try {
            // 检查商品是否存在
            SeckillProduct product = seckillProductService.getById(id);
            if (product == null) {
                return Result.error(404, "秒杀商品不存在");
            }
            
            // 价格校验
            if (price.compareTo(java.math.BigDecimal.ZERO) <= 0) {
                return Result.error("价格必须大于0");
            }
            
            product.setSeckillPrice(price);
            boolean success = seckillProductService.updateById(product);
            
            if (success) {
                log.info("调整秒杀商品价格成功 - id: {}, price: {}", id, price);
                return Result.success(true, "价格调整成功");
            } else {
                return Result.error("价格调整失败");
            }
        } catch (Exception e) {
            log.error("调整秒杀商品价格失败 - id: {}", id, e);
            return Result.error("价格调整失败：" + e.getMessage());
        }
    }
}
