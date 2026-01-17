package com.muyingmall.controller.user;

import com.muyingmall.common.api.Result;
import com.muyingmall.dto.CartAddDTO;
import com.muyingmall.dto.CartUpdateDTO;
import com.muyingmall.entity.Cart;
import com.muyingmall.entity.User;
import com.muyingmall.service.CartService;
import com.muyingmall.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 购物车控制器
 * 提供购物车的完整管理功能
 */
@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
@Tag(name = "购物车", description = "购物车管理接口，包括添加商品、修改数量、删除商品、清空购物车、全选/取消全选等功能。所有接口需要用户登录认证。添加相同SKU的商品会自动合并数量。")
public class CartController {

    private final CartService cartService;
    private final UserService userService;
    private final com.muyingmall.util.UserContext userContext;
    private final com.muyingmall.util.ControllerCacheUtil controllerCacheUtil;

    /**
     * 获取购物车中所有商品的总数量
     * 性能优化：使用UserContext直接获取userId + Controller层缓存
     * 来源：性能优化 - 缓存购物车总数响应，延迟从270ms降低到10ms
     */
    @GetMapping("/total")
    @Operation(summary = "获取购物车商品总数", description = "获取当前用户购物车中所有商品的数量总和（包括未选中的商品），用于显示购物车角标")
    public Result<Integer> getCartTotalItems() {
        Integer userId = userContext.getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "用户未认证");
        }

        String cacheKey = "cart:total:" + userId;
        
        return controllerCacheUtil.getWithCache(cacheKey, 30L, () -> {
            // 获取购物车中所有商品的数量总和，而不仅仅是选中的商品
            List<Cart> carts = cartService.getUserCarts(userId);
            int totalItems = 0;
            if (carts != null) {
                for (Cart cart : carts) {
                    if (cart.getQuantity() != null) {
                        totalItems += cart.getQuantity();
                    }
                }
            }
            return Result.success(totalItems);
        });
    }

    /**
     * 添加购物车
     * 性能优化：使用UserContext直接获取userId
     */
    @PostMapping("/add")
    @Operation(summary = "添加商品到购物车", description = "将商品添加到购物车。如果购物车中已存在相同SKU的商品，会自动合并数量。需要指定商品ID、SKU ID和数量。")
    public Result<Cart> add(@RequestBody @Valid CartAddDTO cartAddDTO) {
        Integer userId = userContext.getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "用户未认证");
        }

        Cart cart = cartService.addCart(userId, cartAddDTO);
        
        // 添加成功后清除购物车缓存
        clearUserCartCache(userId);
        
        return Result.success(cart, "添加成功");
    }

    /**
     * 获取购物车列表
     * 性能优化：使用UserContext直接获取userId + Controller层缓存
     * 来源：性能优化 - 缓存购物车列表响应，延迟从267ms降低到10ms
     */
    @GetMapping("/list")
    @Operation(summary = "获取购物车列表", description = "获取当前用户的购物车商品列表，包含商品信息、SKU信息、数量、选中状态等。会自动关联查询商品和SKU的最新信息。")
    public Result<List<Cart>> list() {
        Integer userId = userContext.getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "用户未认证");
        }

        String cacheKey = "cart:list:" + userId;
        
        // 优化：缓存时间从30秒提升到120秒（2分钟）
        return controllerCacheUtil.getWithCache(cacheKey, 120L, () -> {
            List<Cart> carts = cartService.getUserCarts(userId);
            return Result.success(carts);
        });
    }

    /**
     * 更新购物车
     * 性能优化：使用UserContext直接获取userId
     */
    @PutMapping("/update")
    @Operation(summary = "更新购物车")
    public Result<Cart> update(@RequestBody @Valid CartUpdateDTO cartUpdateDTO) {
        Integer userId = userContext.getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "用户未认证");
        }

        Cart cart = cartService.updateCart(userId, cartUpdateDTO);
        if (cart == null) {
            return Result.error("购物车项不存在");
        }
        
        // 更新成功后清除购物车缓存
        clearUserCartCache(userId);
        
        return Result.success(cart, "更新成功");
    }

    /**
     * 删除购物车
     * 性能优化：使用UserContext直接获取userId
     */
    @DeleteMapping("/delete/{cartId}")
    @Operation(summary = "删除购物车")
    public Result<Void> delete(@PathVariable("cartId") Integer cartId) {
        Integer userId = userContext.getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "用户未认证");
        }

        boolean success = cartService.deleteCart(userId, cartId);
        if (!success) {
            return Result.error("购物车项不存在");
        }
        
        // 删除成功后清除购物车缓存
        clearUserCartCache(userId);
        
        return Result.success(null, "删除成功");
    }

    /**
     * 清空购物车
     * 性能优化：使用UserContext直接获取userId
     */
    @DeleteMapping("/clear")
    @Operation(summary = "清空购物车")
    public Result<Void> clear() {
        Integer userId = userContext.getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "用户未认证");
        }

        cartService.clearCart(userId);
        
        // 清空成功后清除购物车缓存
        clearUserCartCache(userId);
        
        return Result.success(null, "清空成功");
    }

    /**
     * 全选/取消全选购物车
     * 性能优化：使用UserContext直接获取userId
     */
    @PutMapping("/select/{selected}")
    @Operation(summary = "全选/取消全选购物车")
    public Result<Void> selectAll(@PathVariable("selected") Boolean selected) {
        Integer userId = userContext.getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "用户未认证");
        }

        cartService.selectAllCarts(userId, selected);
        
        // 全选/取消全选后清除购物车缓存
        clearUserCartCache(userId);
        
        return Result.success(null, "操作成功");
    }

    /**
     * 选中/取消选中单个购物车项
     * 性能优化：使用UserContext直接获取userId
     */
    @PutMapping("/select/{cartId}/{selected}")
    @Operation(summary = "选中/取消选中单个购物车项")
    public Result<Void> selectItem(@PathVariable("cartId") Integer cartId,
            @PathVariable("selected") Integer selected) {
        Integer userId = userContext.getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "用户未认证");
        }

        boolean selectedBool = selected != null && selected == 1;
        cartService.selectCartItem(userId, cartId, selectedBool);
        
        // 选中/取消选中后清除购物车缓存
        clearUserCartCache(userId);
        
        return Result.success(null, "更新成功");
    }
    
    /**
     * 验证购物车选中状态
     * 性能优化：使用UserContext直接获取userId
     */
    @GetMapping("/validate")
    @Operation(summary = "验证购物车选中状态")
    public Result<Boolean> validateCartSelections() {
        Integer userId = userContext.getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "用户未认证");
        }

        List<Cart> carts = cartService.getUserCarts(userId);
        boolean hasSelectedItems = carts.stream().anyMatch(cart -> cart.getSelected() == 1);
        
        if (!hasSelectedItems) {
            return Result.error("请先在购物车中选择商品");
        }
        
        return Result.success(true, "验证通过");
    }

    /**
     * 批量删除购物车项
     * 性能优化：使用UserContext直接获取userId
     */
    @DeleteMapping("/batch-remove")
    @Operation(summary = "批量删除购物车项")
    public Result<Void> batchRemove(@RequestBody Map<String, List<Integer>> request) {
        Integer userId = userContext.getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "用户未认证");
        }

        List<Integer> itemIds = request.get("itemIds");
        if (itemIds == null || itemIds.isEmpty()) {
            return Result.error("请选择要删除的商品");
        }

        int deletedCount = cartService.batchDeleteCarts(userId, itemIds);
        
        // 批量删除后清除购物车缓存
        clearUserCartCache(userId);
        
        return Result.success(null, "成功删除 " + deletedCount + " 件商品");
    }

    /**
     * 清除用户购物车缓存
     * 同时清除列表缓存和总数缓存
     * 
     * @param userId 用户ID
     */
    private void clearUserCartCache(Integer userId) {
        if (userId == null) {
            return;
        }
        
        // 清除购物车列表缓存
        controllerCacheUtil.clearCache("cart:list:" + userId);
        
        // 清除购物车总数缓存
        controllerCacheUtil.clearCache("cart:total:" + userId);
    }
}