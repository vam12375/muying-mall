package com.muyingmall.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.muyingmall.dto.AdminLoginDTO;
import com.muyingmall.dto.LoginDTO;
import com.muyingmall.dto.UserDTO;
import com.muyingmall.entity.User;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 用户服务接口
 */
public interface UserService extends IService<User> {

    /**
     * 用户注册
     *
     * @param userDTO 用户信息
     * @return 注册成功的用户
     */
    User register(UserDTO userDTO);

    /**
     * 用户登录
     *
     * @param loginDTO 登录信息
     * @return 登录成功的用户
     */
    User login(LoginDTO loginDTO);

    /**
     * 通过用户名查询用户
     *
     * @param username 用户名
     * @return 用户对象
     */
    User findByUsername(String username);

    /**
     * 通过邮箱查询用户
     *
     * @param email 邮箱
     * @return 用户对象
     */
    User getByEmail(String email);

    /**
     * 更新用户信息
     *
     * @param user 用户信息
     * @return 是否成功
     */
    boolean updateUserInfo(User user);

    /**
     * 修改密码
     *
     * @param userId      用户ID
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     * @return 是否成功
     */
    boolean changePassword(Integer userId, String oldPassword, String newPassword);

    /**
     * 上传用户头像
     *
     * @param userId 用户ID
     * @param file   头像文件
     * @return 头像访问URL
     * @throws Exception 上传过程中的异常
     */
    String uploadAvatar(Integer userId, MultipartFile file) throws Exception;

    /**
     * 管理员登录
     *
     * @param adminLoginDTO 管理员登录信息
     * @return 登录成功的管理员用户
     */
    User adminLogin(AdminLoginDTO adminLoginDTO);

    /**
     * 根据用户名获取用户信息
     *
     * @param username 用户名
     * @return 用户信息
     */
    User getUserByUsername(String username);

    /**
     * 验证用户密码
     *
     * @param user     用户对象
     * @param password 密码
     * @return 是否验证通过
     */
    boolean verifyPassword(User user, String password);

    /**
     * 生成用户token
     *
     * @param user 用户对象
     * @return token字符串
     */
    String generateToken(User user);

    /**
     * 从token中获取用户信息
     *
     * @param token token字符串
     * @return 用户对象
     */
    User getUserFromToken(String token);

    /**
     * 登出处理
     *
     * @param token token字符串
     */
    void logout(String token);

    /**
     * 分页获取用户列表
     *
     * @param page    页码
     * @param size    每页大小
     * @param keyword 关键字（用户名、邮箱、昵称）
     * @param status  状态筛选
     * @param role    角色筛选
     * @return 用户分页数据
     */
    Page<User> getUserPage(int page, int size, String keyword, String status, String role);

    /**
     * 添加用户（管理员权限）
     *
     * @param user 用户信息
     * @return 添加成功的用户
     */
    User addUser(User user);

    /**
     * 更新用户信息（管理员权限）
     *
     * @param user 用户信息
     * @return 是否成功
     */
    boolean updateUserByAdmin(User user);

    /**
     * 删除用户
     *
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean deleteUser(Integer userId);

    /**
     * 冻结/解冻用户
     *
     * @param userId 用户ID
     * @param status 状态值：0-禁用，1-正常
     * @return 是否成功
     */
    boolean toggleUserStatus(Integer userId, Integer status);

    /**
     * 修改用户角色
     *
     * @param userId 用户ID
     * @param role   角色值：admin-管理员，user-普通用户
     * @return 是否成功
     */
    boolean updateUserRole(Integer userId, String role);

    User getUserById(Integer userId);

    void updateUser(User user);

    void deductBalance(Integer userId, BigDecimal amountToDeduct);

    /**
     * 获取用户统计数据
     *
     * @param userId 用户ID
     * @return 统计数据Map，包含orderCount、favoriteCount、commentCount
     */
    Map<String, Object> getUserStats(Integer userId);
}