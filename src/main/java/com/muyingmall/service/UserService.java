package com.muyingmall.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.muyingmall.dto.AdminLoginDTO;
import com.muyingmall.dto.LoginDTO;
import com.muyingmall.dto.UserDTO;
import com.muyingmall.entity.User;
import org.springframework.web.multipart.MultipartFile;

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
}