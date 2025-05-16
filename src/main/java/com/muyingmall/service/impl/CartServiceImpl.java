package com.muyingmall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.muyingmall.common.CacheConstants;
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

        // 转换规格信息为JSON字符串
        String specsJson = null;
        String specsHash = "";
        Map<String, String> specs = cartAddDTO.getSpecs();
        if (specs != null && !specs.isEmpty()) {
            try {
                specsJson = objectMapper.writeValueAsString(specs);
                // 生成规格哈希值，用于唯一索引
                specsHash = org.springframework.util.DigestUtils.md5DigestAsHex(specsJson.getBytes());
            } catch (JsonProcessingException e) {
                log.error("规格信息转换失败", e);
                throw new BusinessException("规格信息格式错误");
            }
        }

        // 查询购物车是否已存在相同商品规格
        LambdaQueryWrapper<Cart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Cart::getUserId, userId)
                .eq(Cart::getProductId, cartAddDTO.getProductId())
                .eq(Cart::getSpecsHash, specsHash);
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
            cart.setPriceSnapshot(product.getPriceNew()); // 记录当前价格
            cart.setStatus(1); // 有效
            save(cart);
            
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
        Object cacheResult = redisUtil.get(cacheKey);
        if (cacheResult != null) {
            log.debug("从缓存中获取用户购物车: userId={}", userId);
            return (List<Cart>) cacheResult;
        }
        
        // 缓存未命中，从数据库查询
        log.debug("缓存未命中，从数据库查询用户购物车: userId={}", userId);
        LambdaQueryWrapper<Cart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Cart::getUserId, userId)
                .eq(Cart::getStatus, 1) // 只查询有效的购物车项
                .orderByDesc(Cart::getUpdateTime);
        List<Cart> cartList = list(queryWrapper);
        
        // 缓存结果
        if (cartList != null && !cartList.isEmpty()) {
            redisUtil.set(cacheKey, cartList, CacheConstants.CART_EXPIRE_TIME);
            log.debug("将用户购物车缓存到Redis: userId={}", userId);
        }
        
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
        Object cacheResult = redisUtil.get(cacheKey);
        if (cacheResult != null) {
            log.debug("从缓存中获取用户购物车商品数量: userId={}", userId);
            return (int) cacheResult;
        }
        
        // 缓存未命中，从数据库查询
        log.debug("缓存未命中，从数据库查询用户购物车商品数量: userId={}", userId);
        
        // 查询购物车商品总数
        LambdaQueryWrapper<Cart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Cart::getUserId, userId)
                .eq(Cart::getStatus, 1); // 只计算有效的购物车项
        
        // 获取购物车列表
        List<Cart> cartList = list(queryWrapper);
        
        // 计算商品总数
        int totalCount = 0;
        if (cartList != null && !cartList.isEmpty()) {
            for (Cart cart : cartList) {
                totalCount += cart.getQuantity();
            }
        }
        
        // 缓存结果
        redisUtil.set(cacheKey, totalCount, CacheConstants.CART_COUNT_EXPIRE_TIME);
        log.debug("将用户购物车商品数量缓存到Redis: userId={}, count={}, 过期时间={}秒", 
                userId, totalCount, CacheConstants.CART_COUNT_EXPIRE_TIME);
        
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
}