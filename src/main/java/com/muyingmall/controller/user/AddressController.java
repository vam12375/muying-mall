package com.muyingmall.controller.user;

import com.muyingmall.common.api.Result;
import com.muyingmall.entity.Address;
import com.muyingmall.entity.User;
import com.muyingmall.service.AddressService;
import com.muyingmall.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户地址控制器
 */
@RestController
@RequestMapping("/user/addresses")
@RequiredArgsConstructor
@Tag(name = "地址管理", description = "用户收货地址的增删改查")
public class AddressController {

    private final AddressService addressService;
    private final UserService userService;

    /**
     * 获取用户地址列表
     */
    @GetMapping
    @Operation(summary = "获取用户地址列表")
    public Result<List<Address>> getUserAddresses() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return Result.error(401, "用户未认证");
        }

        String username = authentication.getName();
        User user = userService.findByUsername(username);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }

        List<Address> addresses = addressService.getUserAddresses(user.getUserId());
        return Result.success(addresses);
    }

    /**
     * 添加地址
     */
    @PostMapping
    @Operation(summary = "添加地址")
    public Result<Address> addUserAddress(@RequestBody @Valid Address address) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return Result.error(401, "用户未认证");
        }

        String username = authentication.getName();
        User user = userService.findByUsername(username);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }

        address.setUserId(user.getUserId());
        addressService.addAddress(address);
        return Result.success(address, "添加成功");
    }

    /**
     * 修改地址
     */
    @PutMapping("/{addressId}")
    @Operation(summary = "修改地址")
    public Result<Address> updateUserAddress(@PathVariable("addressId") Integer addressId, @RequestBody @Valid Address address) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return Result.error(401, "用户未认证");
        }

        String username = authentication.getName();
        User user = userService.findByUsername(username);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }

        // 验证地址是否属于当前用户
        Address existAddress = addressService.getById(addressId);
        if (existAddress == null || !existAddress.getUserId().equals(user.getUserId())) {
            return Result.error("地址不存在或不属于当前用户");
        }

        address.setAddressId(addressId);
        address.setUserId(user.getUserId());
        addressService.updateAddress(address);
        return Result.success(address, "修改成功");
    }

    /**
     * 删除地址
     */
    @DeleteMapping("/{addressId}")
    @Operation(summary = "删除地址")
    public Result<Void> deleteUserAddress(@PathVariable("addressId") Integer addressId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return Result.error(401, "用户未认证");
        }

        String username = authentication.getName();
        User user = userService.findByUsername(username);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }

        // 验证地址是否属于当前用户
        Address existAddress = addressService.getById(addressId);
        if (existAddress == null || !existAddress.getUserId().equals(user.getUserId())) {
            return Result.error("地址不存在或不属于当前用户");
        }

        addressService.removeById(addressId);
        return Result.success(null, "删除成功");
    }

    /**
     * 设置默认地址
     */
    @PutMapping("/{addressId}/default")
    @Operation(summary = "设置默认地址")
    public Result<Void> setDefaultAddress(@PathVariable("addressId") Integer addressId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return Result.error(401, "用户未认证");
        }

        String username = authentication.getName();
        User user = userService.findByUsername(username);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }

        // 验证地址是否属于当前用户
        Address existAddress = addressService.getById(addressId);
        if (existAddress == null || !existAddress.getUserId().equals(user.getUserId())) {
            return Result.error("地址不存在或不属于当前用户");
        }

        addressService.setDefaultAddress(user.getUserId(), addressId);
        return Result.success(null, "设置成功");
    }

    /**
     * 获取地址详情
     */
    @GetMapping("/{addressId}")
    @Operation(summary = "获取地址详情")
    public Result<Address> getAddressDetail(@PathVariable("addressId") Integer addressId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return Result.error(401, "用户未认证");
        }

        String username = authentication.getName();
        User user = userService.findByUsername(username);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }

        Address address = addressService.getById(addressId);
        if (address == null || !address.getUserId().equals(user.getUserId())) {
            return Result.error("地址不存在或不属于当前用户");
        }

        return Result.success(address);
    }
}