package com.muyingmall.fixtures;

import com.muyingmall.entity.User;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户测试夹具。
 */
public final class UserFixtures {

    private UserFixtures() {
    }

    /**
     * 构造一个启用状态的普通用户。
     */
    public static User normalUser(Integer userId, String username) {
        User user = new User();
        user.setUserId(userId);
        user.setUsername(username);
        user.setPassword("$2a$10$encoded");
        user.setNickname(username);
        user.setPhone("13800000" + String.format("%03d", userId % 1000));
        user.setEmail(username + "@test.com");
        user.setStatus(1);
        user.setRole("user");
        user.setBalance(BigDecimal.ZERO);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        return user;
    }

    /**
     * 构造一个禁用用户。
     */
    public static User disabledUser(Integer userId, String username) {
        User user = normalUser(userId, username);
        user.setStatus(0);
        return user;
    }

    /**
     * 构造一个管理员用户。
     */
    public static User adminUser(Integer userId, String username) {
        User user = normalUser(userId, username);
        user.setRole("admin");
        return user;
    }
}
