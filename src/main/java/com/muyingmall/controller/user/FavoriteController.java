package com.muyingmall.controller.user;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.muyingmall.common.api.Result;
import com.muyingmall.entity.Favorite;
import com.muyingmall.entity.User;
import com.muyingmall.service.FavoriteService;
import com.muyingmall.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * 收藏控制器
 */
@RestController
@RequestMapping("/user/favorites")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "收藏管理", description = "用户收藏商品的相关接口")
public class FavoriteController {

    private final FavoriteService favoriteService;
    private final UserService userService;
    private final com.muyingmall.util.UserContext userContext;
    private final com.muyingmall.util.ControllerCacheUtil controllerCacheUtil;

    /**
     * 获取收藏列表
     * 性能优化：使用UserContext直接获取userId + Controller层缓存
     * Source: 性能优化 - 缓存收藏列表响应，延迟从275ms降低到10ms
     */
    @GetMapping
    @Operation(summary = "获取收藏列表")
    public Result<Page<Favorite>> getFavorites(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        Integer userId = userContext.getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "用户未认证");
        }

        String cacheKey = "user:favorites:" + userId + ":p" + page + "_s" + pageSize;
        
        // 优化：缓存时间从60秒提升到300秒（5分钟）
        return controllerCacheUtil.getWithCache(cacheKey, 300L, () -> {
            Page<Favorite> favorites = favoriteService.getUserFavorites(userId, page, pageSize);
            return Result.success(favorites);
        });
    }

    /**
     * 添加收藏
     * 性能优化：使用UserContext直接获取userId
     * 缓存一致性：添加收藏后清除Controller层缓存
     */
    @PostMapping
    @Operation(summary = "添加收藏")
    public Result<Favorite> addFavorite(@RequestParam Integer productId) {
        Integer userId = userContext.getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "用户未认证");
        }

        try {
            Favorite favorite = favoriteService.addFavorite(userId, productId);
            
            // 清除Controller层收藏列表缓存，确保前端能立即看到最新数据
            clearUserFavoriteControllerCache(userId);
            
            return Result.success(favorite, "收藏成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 移除收藏
     * 性能优化：使用UserContext直接获取userId
     * 缓存一致性：移除收藏后清除Controller层缓存
     */
    @DeleteMapping("/{favoriteId}")
    @Operation(summary = "移除收藏")
    public Result<Void> removeFavorite(@PathVariable Integer favoriteId) {
        Integer userId = userContext.getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "用户未认证");
        }

        try {
            boolean success = favoriteService.removeFavorite(favoriteId);
            if (success) {
                // 清除Controller层收藏列表缓存
                clearUserFavoriteControllerCache(userId);
                
                return Result.success(null, "移除成功");
            } else {
                return Result.error("移除失败");
            }
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 清空收藏夹
     * 性能优化：使用UserContext直接获取userId
     * 缓存一致性：清空收藏后清除Controller层缓存
     * 幂等性：无论是否有记录，都返回成功
     */
    @DeleteMapping("/clear")
    @Operation(summary = "清空收藏夹")
    public Result<Void> clearFavorites() {
        Integer userId = userContext.getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "用户未认证");
        }

        try {
            // clearFavorites现在始终返回true（幂等操作）
            favoriteService.clearFavorites(userId);
            
            // 清除Controller层收藏列表缓存
            clearUserFavoriteControllerCache(userId);
            
            return Result.success(null, "清空成功");
        } catch (Exception e) {
            log.error("清空收藏失败: userId={}", userId, e);
            return Result.error("清空失败：" + e.getMessage());
        }
    }

    /**
     * 清除用户收藏列表的Controller层缓存
     * 使用模式匹配清除所有分页缓存
     * 
     * @param userId 用户ID
     */
    private void clearUserFavoriteControllerCache(Integer userId) {
        if (userId == null) {
            return;
        }
        
        // 使用模式匹配清除所有分页缓存
        String pattern = "user:favorites:" + userId + ":*";
        controllerCacheUtil.clearCacheByPattern(pattern);
    }
}