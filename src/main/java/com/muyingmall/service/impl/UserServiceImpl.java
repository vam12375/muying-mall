package com.muyingmall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.muyingmall.common.exception.BusinessException;
import com.muyingmall.dto.AdminLoginDTO;
import com.muyingmall.dto.LoginDTO;
import com.muyingmall.dto.UserDTO;
import com.muyingmall.entity.User;
import com.muyingmall.mapper.UserMapper;
import com.muyingmall.service.UserService;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * 用户服务实现类
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final PasswordEncoder passwordEncoder;
    
    @Value("${upload.path:/uploads}")
    private String uploadPath;
    
    @Value("${upload.avatar.path:/avatar}")
    private String avatarPath;
    
    @Value("${upload.domain:http://localhost:8080}")
    private String domain;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public User register(UserDTO userDTO) {
        // 验证用户名是否已存在
        User existUser = findByUsername(userDTO.getUsername());
        if (existUser != null) {
            throw new BusinessException("用户名已存在");
        }

        // 验证邮箱是否已存在
        existUser = getByEmail(userDTO.getEmail());
        if (existUser != null) {
            throw new BusinessException("邮箱已存在");
        }

        // 验证两次密码是否一致
        if (userDTO.getConfirmPassword() != null && !userDTO.getPassword().equals(userDTO.getConfirmPassword())) {
            throw new BusinessException("两次密码不一致");
        }

        // 创建用户
        User user = new User();
        BeanUtils.copyProperties(userDTO, user);

        // 设置默认昵称
        if (user.getNickname() == null || user.getNickname().isEmpty()) {
            user.setNickname(userDTO.getUsername());
        }

        // 设置默认角色
        user.setRole("user");

        // 设置默认状态
        user.setStatus(1);

        // 加密密码
        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));

        // 保存用户
        save(user);

        return user;
    }

    @Override
    public User login(LoginDTO loginDTO) {
        // 根据用户名或邮箱查询用户
        User user = lambdaQuery()
                .eq(User::getUsername, loginDTO.getUsername())
                .or()
                .eq(User::getEmail, loginDTO.getUsername())
                .one();

        // 验证用户是否存在
        if (user == null) {
            throw new BusinessException("用户名或密码错误");
        }

        // 验证用户状态
        if (user.getStatus() != 1) {
            throw new BusinessException("账号已被禁用");
        }

        // 验证密码
        if (!passwordEncoder.matches(loginDTO.getPassword(), user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }

        return user;
    }

    @Override
    public User adminLogin(AdminLoginDTO adminLoginDTO) {
        // 查询用户
        User admin = lambdaQuery()
                .eq(User::getUsername, adminLoginDTO.getAdmin_name())
                .eq(User::getRole, "admin")
                .one();

        // 验证用户是否存在
        if (admin == null) {
            throw new BusinessException("用户名或密码错误");
        }

        // 验证用户状态
        if (admin.getStatus() != 1) {
            throw new BusinessException("账号已被禁用");
        }

        // 验证密码
        if (!passwordEncoder.matches(adminLoginDTO.getAdmin_pass(), admin.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }

        return admin;
    }

    @Override
    public User findByUsername(String username) {
        return lambdaQuery().eq(User::getUsername, username).one();
    }

    @Override
    public User getByEmail(String email) {
        return lambdaQuery().eq(User::getEmail, email).one();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateUserInfo(User user) {
        // 不允许修改敏感信息
        User updateUser = getById(user.getUserId());
        if (updateUser == null) {
            throw new BusinessException("用户不存在");
        }

        // 只更新允许修改的字段
        updateUser.setNickname(user.getNickname());
        updateUser.setAvatar(user.getAvatar());
        updateUser.setGender(user.getGender());
        updateUser.setBirthday(user.getBirthday());
        updateUser.setPhone(user.getPhone());
        updateUser.setEmail(user.getEmail());

        return updateById(updateUser);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean changePassword(Integer userId, String oldPassword, String newPassword) {
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 验证旧密码
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BusinessException("原密码错误");
        }

        // 更新密码
        user.setPassword(passwordEncoder.encode(newPassword));

        return updateById(user);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String uploadAvatar(Integer userId, MultipartFile file) throws Exception {
        // 验证用户是否存在
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        
        // 验证文件
        if (file == null || file.isEmpty()) {
            throw new BusinessException("文件不能为空");
        }
        
        // 获取文件后缀
        String originalFilename = file.getOriginalFilename();
        String suffix = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        
        // 限制文件类型
        if (!".jpg".equalsIgnoreCase(suffix) && !".jpeg".equalsIgnoreCase(suffix) && 
            !".png".equalsIgnoreCase(suffix) && !".gif".equalsIgnoreCase(suffix)) {
            throw new BusinessException("只支持jpg、jpeg、png、gif格式的图片");
        }
        
        // 确保上传目录存在
        String userAvatarPath = uploadPath + avatarPath + "/" + userId;
        Path uploadDir = Paths.get(userAvatarPath);
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }
        
        // 生成文件名
        String filename = UUID.randomUUID().toString().replace("-", "") + suffix;
        Path filePath = uploadDir.resolve(filename);
        
        // 保存文件
        Files.copy(file.getInputStream(), filePath);
        
        // 生成访问URL
        String avatarUrl = domain + avatarPath + "/" + userId + "/" + filename;
        
        // 更新用户头像
        user.setAvatar(avatarUrl);
        updateById(user);
        
        return avatarUrl;
    }
}