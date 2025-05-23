package com.muyingmall.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.muyingmall.dto.CartAddDTO;
import com.muyingmall.dto.CartUpdateDTO;
import com.muyingmall.entity.Cart;

import java.util.List;

/**
 * 购物车服务接口
 */
public interface CartService extends IService<Cart> {

    /**
     * 添加商品到购物车
     *
     * @param userId     用户ID
     * @param cartAddDTO 购物车添加DTO
     * @return 购物车对象
     */
    Cart addCart(Integer userId, CartAddDTO cartAddDTO);

    /**
     * 获取用户购物车列表
     *
     * @param userId 用户ID
     * @return 购物车列表
     */
    List<Cart> getUserCarts(Integer userId);

    /**
     * 更新购物车
     *
     * @param userId        用户ID
     * @param cartUpdateDTO 购物车更新DTO
     * @return 购物车对象
     */
    Cart updateCart(Integer userId, CartUpdateDTO cartUpdateDTO);

    /**
     * 删除购物车
     *
     * @param userId 用户ID
     * @param cartId 购物车ID
     * @return 是否成功
     */
    boolean deleteCart(Integer userId, Integer cartId);

    /**
     * 清空购物车
     *
     * @param userId 用户ID
     */
    void clearCart(Integer userId);

    /**
     * 全选/取消全选购物车
     *
     * @param userId   用户ID
     * @param selected 是否选中
     */
    void selectAllCarts(Integer userId, Boolean selected);

    /**
     * 选中/取消选中单个购物车项
     *
     * @param userId   用户ID
     * @param cartId   购物车ID
     * @param selected 是否选中
     */
    void selectCartItem(Integer userId, Integer cartId, Boolean selected);

    /**
     * 获取用户购物车商品总数量
     *
     * @param userId 用户ID
     * @return 购物车商品总数
     */
    int getCartCount(Integer userId);

    /**
     * 获取用户购物车中已选中的商品
     *
     * @param userId 用户ID
     * @return 已选中的购物车项列表
     */
    List<Cart> getSelectedCarts(Integer userId);
}