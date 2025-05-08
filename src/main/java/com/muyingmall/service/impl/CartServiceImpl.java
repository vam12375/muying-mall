package com.muyingmall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.muyingmall.common.exception.BusinessException;
import com.muyingmall.dto.CartAddDTO;
import com.muyingmall.dto.CartUpdateDTO;
import com.muyingmall.entity.Cart;
import com.muyingmall.entity.Product;
import com.muyingmall.mapper.CartMapper;
import com.muyingmall.service.CartService;
import com.muyingmall.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
            return cart;
        }
    }

    @Override
    public List<Cart> getUserCarts(Integer userId) {
        LambdaQueryWrapper<Cart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Cart::getUserId, userId)
                .eq(Cart::getStatus, 1) // 只查询有效的购物车项
                .orderByDesc(Cart::getUpdateTime);
        return list(queryWrapper);
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
        return cart;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteCart(Integer userId, Integer cartId) {
        LambdaQueryWrapper<Cart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Cart::getCartId, cartId)
                .eq(Cart::getUserId, userId);
        return remove(queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void clearCart(Integer userId) {
        LambdaQueryWrapper<Cart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Cart::getUserId, userId);
        remove(queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void selectAllCarts(Integer userId, Boolean selected) {
        LambdaUpdateWrapper<Cart> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Cart::getUserId, userId)
                .set(Cart::getSelected, selected ? 1 : 0); // 确保布尔值转为 0 或 1
        update(updateWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void selectCartItem(Integer userId, Integer cartId, Boolean selected) {
        LambdaUpdateWrapper<Cart> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Cart::getUserId, userId)
                .eq(Cart::getCartId, cartId)
                .set(Cart::getSelected, selected ? 1 : 0);
        boolean success = update(updateWrapper);
        if (!success) {
            // 可以选择抛出异常或记录日志，取决于业务需求
            // 这里仅记录日志，因为更新0行也可能不算严格意义的错误
            log.warn("尝试更新购物车项选中状态失败或未找到记录: userId={}, cartId={}", userId, cartId);
        }
    }
}