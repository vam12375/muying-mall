package com.muyingmall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.muyingmall.common.constants.CacheConstants;
import com.muyingmall.common.exception.BusinessException;
import com.muyingmall.dto.CartAddDTO;
import com.muyingmall.dto.CartUpdateDTO;
import com.muyingmall.entity.Cart;
import com.muyingmall.entity.Product;
import com.muyingmall.mapper.CartMapper;
import com.muyingmall.service.CartService;
import com.muyingmall.service.ProductService;
import com.muyingmall.util.RedisUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 购物车服务实现类
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CartServiceImpl extends ServiceImpl<CartMapper, Cart> implements CartService {

    private final ProductService productService;
    private final ObjectMapper objectMapper;
    private final RedisUtil redisUtil;
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Cart addCart(Integer userId, CartAddDTO cartAddDTO) {
        // 查询商品是否存在
        Product product = productService.getById(cartAddDTO.getProductId());
        if (product == null) {
            throw new BusinessException("商品不存在");
        }

        // 商品是否上架
        if (!"上架".equals(product.getProductStatus())) {
            throw new BusinessException("商品已下架");
        }

        // 库存是否充足
        if (product.getStock() < cartAddDTO.getQuantity()) {
            throw new BusinessException("库存不足");
        }

        // 处理SKU或规格信息
        String specsJson = null;
        String specsHash = null; // 默认为null，避免空字符串导致唯一索引冲突
        Long skuId = cartAddDTO.getSkuId();
        String skuName = cartAddDTO.getSkuName();
        
        // 优化：热点路径使用debug级别日志
        log.debug("【购物车添加】开始处理，productId={}, skuId={}, skuName={}", 
                cartAddDTO.getProductId(), skuId, skuName);
        
        // 优先使用SKU ID作为唯一标识
        if (skuId != null && skuId > 0) {
            // 使用SKU ID作为唯一标识，确保不同SKU有不同的specsHash
            specsHash = "sku_" + skuId;
        } else {
            // 兼容旧的规格系统（没有SKU的情况）
            Map<String, String> specs = cartAddDTO.getSpecs();
            if (specs != null && !specs.isEmpty()) {
                try {
                    specsJson = objectMapper.writeValueAsString(specs);
                    specsHash = org.springframework.util.DigestUtils.md5DigestAsHex(specsJson.getBytes());
                } catch (JsonProcessingException e) {
                    log.error("规格信息转换失败", e);
                    throw new BusinessException("规格信息格式错误");
                }
            }
            // 没有SKU也没有规格，specsHash保持为null
        }

        // 查询购物车是否已存在相同商品规格
        LambdaQueryWrapper<Cart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Cart::getUserId, userId)
                .eq(Cart::getProductId, cartAddDTO.getProductId());
        
        // 根据SKU或规格哈希查询（处理null值情况）
        if (skuId != null) {
            queryWrapper.eq(Cart::getSkuId, skuId);
        } else if (specsHash != null) {
            queryWrapper.eq(Cart::getSpecsHash, specsHash);
        } else {
            // 没有SKU也没有规格，查询specsHash为null的记录
            queryWrapper.isNull(Cart::getSpecsHash);
        }
        
        Cart existCart = getOne(queryWrapper);

        if (existCart != null) {
            // 购物车已存在相同商品规格，更新数量
            existCart.setQuantity(existCart.getQuantity() + cartAddDTO.getQuantity());
            existCart.setSelected(cartAddDTO.getSelected());
            updateById(existCart);
            
            // 清除购物车缓存
            clearCartCache(userId);
            
            return existCart;
        } else {
            // 购物车不存在相同商品规格，创建新的购物车项
            Cart cart = new Cart();
            cart.setUserId(userId);
            cart.setProductId(cartAddDTO.getProductId());
            cart.setQuantity(cartAddDTO.getQuantity());
            cart.setSelected(cartAddDTO.getSelected());
            cart.setSpecs(specsJson);
            cart.setSpecsHash(specsHash);
            cart.setSkuId(skuId);
            cart.setSkuName(skuName);
            
            // 记录价格快照：优先使用传入的SKU价格，否则使用商品主表价格
            if (cartAddDTO.getPrice() != null) {
                cart.setPriceSnapshot(cartAddDTO.getPrice());
            } else {
                cart.setPriceSnapshot(product.getPriceNew());
            }
            cart.setStatus(1); // 有效
            
            // 使用 baseMapper 直接插入
            baseMapper.insert(cart);
            
            // 清除购物车缓存
            clearCartCache(userId);
            
            return cart;
        }
    }

    @Override
    public List<Cart> getUserCarts(Integer userId) {
        if (userId == null) {
            return null;
        }
        
        // 构建缓存键
        String cacheKey = CacheConstants.USER_CART_KEY + userId;
        
        // 查询缓存
        long startTime = System.currentTimeMillis();
        Object cacheResult = redisUtil.get(cacheKey);
        if (cacheResult != null) {
            long cacheTime = System.currentTimeMillis() - startTime;
            log.debug("从缓存中获取用户购物车: userId={}, 耗时={}ms", userId, cacheTime);
            return (List<Cart>) cacheResult;
        }
        
        // 缓存未命中，从数据库查询
        log.debug("缓存未命中，从数据库查询用户购物车: userId={}", userId);
        long dbStartTime = System.currentTimeMillis();
        LambdaQueryWrapper<Cart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Cart::getUserId, userId)
                .eq(Cart::getStatus, 1) // 只查询有效的购物车项
                .orderByDesc(Cart::getUpdateTime);
        List<Cart> cartList = list(queryWrapper);
        long dbTime = System.currentTimeMillis() - dbStartTime;
        
        // 缓存结果 - 优化：延长缓存时间到10分钟，提高命中率
        if (cartList != null && !cartList.isEmpty()) {
            // 将缓存时间从原来的时间延长到600秒（10分钟）
            redisUtil.set(cacheKey, cartList, 600L);
            log.debug("将用户购物车缓存到Redis: userId={}, 数量={}, 数据库查询耗时={}ms, 缓存时间=600秒", 
                    userId, cartList.size(), dbTime);
        } else {
            // 空购物车也缓存，但时间较短（60秒），避免频繁查询
            redisUtil.set(cacheKey, Collections.emptyList(), 60L);
            log.debug("用户购物车为空，缓存空列表: userId={}, 缓存时间=60秒", userId);
        }
        
        // 优化：热点路径使用debug级别日志
        log.debug("购物车查询完成: userId={}, 数据库耗时={}ms", userId, dbTime);
        
        return cartList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Cart updateCart(Integer userId, CartUpdateDTO cartUpdateDTO) {
        // 查询购物车项是否存在
        LambdaQueryWrapper<Cart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Cart::getCartId, cartUpdateDTO.getCartId())
                .eq(Cart::getUserId, userId);
        Cart cart = getOne(queryWrapper);

        if (cart == null) {
            return null;
        }

        // 更新购物车项
        if (cartUpdateDTO.getQuantity() != null) {
            // 查询商品是否存在
            Product product = productService.getById(cart.getProductId());
            if (product == null) {
                throw new BusinessException("商品不存在");
            }

            // 检查库存
            if (product.getStock() < cartUpdateDTO.getQuantity()) {
                throw new BusinessException("库存不足");
            }

            cart.setQuantity(cartUpdateDTO.getQuantity());
        }

        if (cartUpdateDTO.getSelected() != null) {
            cart.setSelected(cartUpdateDTO.getSelected());
        }

        updateById(cart);
        
        // 清除购物车缓存
        clearCartCache(userId);
        
        return cart;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteCart(Integer userId, Integer cartId) {
        LambdaQueryWrapper<Cart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Cart::getCartId, cartId)
                .eq(Cart::getUserId, userId);
        boolean result = remove(queryWrapper);
        
        // 清除购物车缓存
        if (result) {
            clearCartCache(userId);
        }
        
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void clearCart(Integer userId) {
        LambdaQueryWrapper<Cart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Cart::getUserId, userId);
        boolean result = remove(queryWrapper);
        
        // 清除购物车缓存
        if (result) {
            clearCartCache(userId);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void selectAllCarts(Integer userId, Boolean selected) {
        LambdaUpdateWrapper<Cart> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Cart::getUserId, userId)
                .set(Cart::getSelected, selected);
        boolean result = update(updateWrapper);
        
        // 清除购物车缓存
        if (result) {
            clearCartCache(userId);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void selectCartItem(Integer userId, Integer cartId, Boolean selected) {
        LambdaUpdateWrapper<Cart> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Cart::getUserId, userId)
                .eq(Cart::getCartId, cartId)
                .set(Cart::getSelected, selected);
        boolean result = update(updateWrapper);
        
        // 清除购物车缓存
        if (result) {
            clearCartCache(userId);
        }
    }
    
    /**
     * 清除购物车相关缓存
     *
     * @param userId 用户ID
     */
    private void clearCartCache(Integer userId) {
        if (userId == null) {
            return;
        }
        
        // 清除用户购物车缓存
        String cartCacheKey = CacheConstants.USER_CART_KEY + userId;
        redisUtil.del(cartCacheKey);
        log.debug("清除用户购物车缓存: userId={}", userId);
        
        // 清除购物车数量缓存
        String cartCountCacheKey = CacheConstants.CART_COUNT_KEY + userId;
        redisUtil.del(cartCountCacheKey);
        log.debug("清除用户购物车数量缓存: userId={}", userId);
        
        // 清除购物车选中项缓存
        String cartSelectedCacheKey = CacheConstants.CART_SELECTED_KEY + userId;
        redisUtil.del(cartSelectedCacheKey);
        log.debug("清除用户购物车选中项缓存: userId={}", userId);
    }

    /**
     * 获取用户购物车商品总数
     * 优化：优先从购物车列表缓存计算，避免重复查询数据库
     *
     * @param userId 用户ID
     * @return 购物车商品总数
     */
    @Override
    public int getCartCount(Integer userId) {
        if (userId == null) {
            return 0;
        }

        // 构建缓存键
        String cacheKey = CacheConstants.CART_COUNT_KEY + userId;
        
        // 查询缓存
        long startTime = System.currentTimeMillis();
        Object cacheResult = redisUtil.get(cacheKey);
        if (cacheResult != null) {
            long cacheTime = System.currentTimeMillis() - startTime;
            log.debug("从缓存中获取用户购物车商品数量: userId={}, 耗时={}ms", userId, cacheTime);
            return (int) cacheResult;
        }
        
        // 优化：优先尝试从购物车列表缓存中计算，避免重复查询数据库
        String cartListCacheKey = CacheConstants.USER_CART_KEY + userId;
        Object cartListCache = redisUtil.get(cartListCacheKey);
        
        int totalCount = 0;
        if (cartListCache != null && cartListCache instanceof List) {
            // 从购物车列表缓存中计算总数
            List<Cart> cartList = (List<Cart>) cartListCache;
            for (Cart cart : cartList) {
                if (cart.getQuantity() != null) {
                    totalCount += cart.getQuantity();
                }
            }
            log.debug("从购物车列表缓存计算商品数量: userId={}, count={}", userId, totalCount);
        } else {
            // 购物车列表缓存也不存在，从数据库查询
            log.debug("缓存未命中，从数据库查询用户购物车商品数量: userId={}", userId);
            
            // 查询购物车商品总数
            LambdaQueryWrapper<Cart> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Cart::getUserId, userId)
                    .eq(Cart::getStatus, 1); // 只计算有效的购物车项
            
            // 获取购物车列表
            List<Cart> cartList = list(queryWrapper);
            
            // 计算商品总数
            if (cartList != null && !cartList.isEmpty()) {
                for (Cart cart : cartList) {
                    if (cart.getQuantity() != null) {
                        totalCount += cart.getQuantity();
                    }
                }
            }
        }
        
        // 缓存结果 - 优化：延长缓存时间到10分钟，与购物车列表缓存时间一致
        redisUtil.set(cacheKey, totalCount, 600L);
        long totalTime = System.currentTimeMillis() - startTime;
        log.debug("将用户购物车商品数量缓存到Redis: userId={}, count={}, 总耗时={}ms, 缓存时间=600秒", 
                userId, totalCount, totalTime);
        
        return totalCount;
    }
    
    /**
     * 获取用户购物车中选中的商品
     *
     * @param userId 用户ID
     * @return 选中的购物车项列表
     */
    @Override
    public List<Cart> getSelectedCarts(Integer userId) {
        if (userId == null) {
            return null;
        }
        
        // 构建缓存键
        String cacheKey = CacheConstants.CART_SELECTED_KEY + userId;
        
        // 查询缓存
        Object cacheResult = redisUtil.get(cacheKey);
        if (cacheResult != null) {
            log.debug("从缓存中获取用户购物车选中项: userId={}", userId);
            return (List<Cart>) cacheResult;
        }
        
        // 缓存未命中，从数据库查询
        log.debug("缓存未命中，从数据库查询用户购物车选中项: userId={}", userId);
        
        // 查询选中的购物车项
        LambdaQueryWrapper<Cart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Cart::getUserId, userId)
                .eq(Cart::getStatus, 1) // 只查询有效的购物车项
                .eq(Cart::getSelected, 1) // 只查询选中的购物车项
                .orderByDesc(Cart::getUpdateTime);
        
        List<Cart> selectedCarts = list(queryWrapper);
        
        // 缓存结果
        if (selectedCarts != null && !selectedCarts.isEmpty()) {
            redisUtil.set(cacheKey, selectedCarts, CacheConstants.CART_EXPIRE_TIME);
            log.debug("将用户购物车选中项缓存到Redis: userId={}, count={}", userId, selectedCarts.size());
        }
        
        return selectedCarts;
    }

    /**
     * 批量删除购物车项
     *
     * @param userId  用户ID
     * @param cartIds 购物车ID列表
     * @return 删除的数量
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchDeleteCarts(Integer userId, List<Integer> cartIds) {
        if (userId == null || cartIds == null || cartIds.isEmpty()) {
            return 0;
        }

        log.debug("批量删除购物车项: userId={}, cartIds={}", userId, cartIds);

        // 构建删除条件
        LambdaQueryWrapper<Cart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Cart::getUserId, userId)
                .in(Cart::getCartId, cartIds);

        // 执行删除
        int deletedCount = baseMapper.delete(queryWrapper);

        // 清除缓存
        if (deletedCount > 0) {
            String cacheKey = CacheConstants.USER_CART_KEY + userId;
            String selectedCacheKey = CacheConstants.CART_SELECTED_KEY + userId;
            redisUtil.del(cacheKey);
            redisUtil.del(selectedCacheKey);
            log.debug("批量删除购物车项成功: userId={}, deletedCount={}", userId, deletedCount);
        }

        return deletedCount;
    }
}