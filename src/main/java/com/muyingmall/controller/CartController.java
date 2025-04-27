package com.muyingmall.controller;

import com.muyingmall.common.response.Result;
import com.muyingmall.dto.CartAddDTO;
import com.muyingmall.dto.CartUpdateDTO;
import com.muyingmall.entity.Cart;
import com.muyingmall.entity.User;
import com.muyingmall.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 购物车控制器
 */
@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
@Tag(name = "购物车管理", description = "购物车添加、更新、删除等接口")
public class CartController {

    private final CartService cartService;

    /**
     * 添加购物车
     */
    @PostMapping("/add")
    @Operation(summary = "添加购物车")
    public Result<Cart> add(@RequestBody @Valid CartAddDTO cartAddDTO, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return Result.error("未登录");
        }

        Cart cart = cartService.addCart(user.getUserId(), cartAddDTO);
        return Result.success(cart, "添加成功");
    }

    /**
     * 获取购物车列表
     */
    @GetMapping("/list")
    @Operation(summary = "获取购物车列表")
    public Result<List<Cart>> list(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return Result.error("未登录");
        }

        List<Cart> carts = cartService.getUserCarts(user.getUserId());
        return Result.success(carts);
    }

    /**
     * 更新购物车
     */
    @PutMapping("/update")
    @Operation(summary = "更新购物车")
    public Result<Cart> update(@RequestBody @Valid CartUpdateDTO cartUpdateDTO, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return Result.error("未登录");
        }

        Cart cart = cartService.updateCart(user.getUserId(), cartUpdateDTO);
        if (cart == null) {
            return Result.error("购物车项不存在");
        }
        return Result.success(cart, "更新成功");
    }

    /**
     * 删除购物车
     */
    @DeleteMapping("/delete/{cartId}")
    @Operation(summary = "删除购物车")
    public Result<Void> delete(@PathVariable("cartId") Integer cartId, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return Result.error("未登录");
        }

        boolean success = cartService.deleteCart(user.getUserId(), cartId);
        if (!success) {
            return Result.error("购物车项不存在");
        }
        return Result.success(null, "删除成功");
    }

    /**
     * 清空购物车
     */
    @DeleteMapping("/clear")
    @Operation(summary = "清空购物车")
    public Result<Void> clear(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return Result.error("未登录");
        }

        cartService.clearCart(user.getUserId());
        return Result.success(null, "清空成功");
    }

    /**
     * 选中/取消选中购物车
     */
    @PutMapping("/select/{selected}")
    @Operation(summary = "选中/取消选中购物车")
    public Result<Void> select(@PathVariable("selected") Boolean selected, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return Result.error("未登录");
        }

        cartService.selectAllCarts(user.getUserId(), selected);
        return Result.success(null, "操作成功");
    }
}