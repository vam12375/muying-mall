package com.muyingmall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.muyingmall.common.CacheConstants;
import com.muyingmall.common.exception.BusinessException;
import com.muyingmall.dto.AdminLoginDTO;
import com.muyingmall.dto.LoginDTO;
import com.muyingmall.dto.UserDTO;
import com.muyingmall.entity.User;
import com.muyingmall.mapper.UserMapper;
import com.muyingmall.service.UserService;
import com.muyingmall.util.JwtUtils;
import com.muyingmall.util.RedisUtil;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import java.math.BigDecimal;
import java.util.Set;

/**
 * 用户服务实现类
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final RedisUtil redisUtil;
    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${upload.path:/uploads}")
    private String uploadPath;

    @Value("${upload.avatar.path:/avatar}")
    private String avatarPath;

    @Value("${upload.domain:http://localhost:8080}")
    private String domain;

    // @Value("${jwt.expiration:86400}")
    // private long jwtExpiration;

    // // 生成一个静态的、安全的HS512密钥
    // private static final Key JWT_SIGNING_KEY =
    // Keys.secretKeyFor(SignatureAlgorithm.HS512);

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
        if (username == null || username.isEmpty()) {
            return null;
        }

        // 构建缓存键
        String cacheKey = CacheConstants.USER_NAME_KEY + username;

        // 查询缓存
        Object cacheResult = redisUtil.get(cacheKey);
        if (cacheResult != null) {
            log.debug("从缓存中获取用户信息: username={}", username);
            return (User) cacheResult;
        }

        // 缓存未命中，从数据库查询
        log.debug("缓存未命中，从数据库查询用户信息: username={}", username);
        User user = lambdaQuery().eq(User::getUsername, username).one();

        // 如果用户存在，则缓存结果
        if (user != null) {
            redisUtil.set(cacheKey, user, CacheConstants.USER_EXPIRE_TIME);
            log.debug("将用户信息缓存到Redis: username={}", username);
        }

        return user;
    }

    @Override
    public User getByEmail(String email) {
        if (email == null || email.isEmpty()) {
            return null;
        }

        // 构建缓存键
        String cacheKey = CacheConstants.USER_EMAIL_KEY + email;

        // 查询缓存
        Object cacheResult = redisUtil.get(cacheKey);
        if (cacheResult != null) {
            log.debug("从缓存中获取用户信息: email={}", email);
            return (User) cacheResult;
        }

        // 缓存未命中，从数据库查询
        log.debug("缓存未命中，从数据库查询用户信息: email={}", email);
        User user = lambdaQuery().eq(User::getEmail, email).one();

        // 如果用户存在，则缓存结果
        if (user != null) {
            redisUtil.set(cacheKey, user, CacheConstants.USER_EXPIRE_TIME);
            log.debug("将用户信息缓存到Redis: email={}", email);
        }

        return user;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateUserInfo(User user) {
        if (user == null || user.getUserId() == null) {
            return false;
        }

        boolean result = updateById(user);

        if (result) {
            // 更新成功后，清除相关缓存
            clearUserCache(user);
            log.debug("用户信息更新成功，已清除相关缓存: userId={}", user.getUserId());
        }

        return result;
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

    @Override
    public User getUserByUsername(String username) {
        return findByUsername(username);
    }

    @Override
    public boolean verifyPassword(User user, String password) {
        return passwordEncoder.matches(password, user.getPassword());
    }

    @Override
    public String generateToken(User user) {
        // log.info("[UserServiceImpl] Generating token for user: {}",
        // user.getUsername());
        // log.info("[UserServiceImpl] Using JwtUtils instance hash: {}",
        // System.identityHashCode(jwtUtils));
        // log.info("[UserServiceImpl] JwtUtils signingKey for token generation
        // (Base64): {}", jwtUtils.getSigningKeyBase64ForDebug());

        String token = jwtUtils.generateToken(user.getUserId(), user.getUsername(), user.getRole());
        // log.info("[UserServiceImpl] Token generated via JwtUtils: {}...", (token !=
        // null && token.length() > 10) ? token.substring(0, 10) : token);
        return token;
    }

    @Override
    public User getUserFromToken(String token) {
        if (token == null || !token.startsWith("Bearer ")) {
            return null;
        }

        String actualToken = token.substring(7);
        // log.debug("[UserServiceImpl] getUserFromToken: Attempting to parse token:
        // {}...", (actualToken.length() > 10) ? actualToken.substring(0,10) :
        // actualToken);

        if (jwtUtils.validateToken(actualToken)) {
            try {
                Claims claims = jwtUtils.getClaimsFromToken(actualToken);
                Integer userId = claims.get("userId", Integer.class);
                if (userId != null) {
                    // log.info("[UserServiceImpl] User ID {} retrieved from token. Fetching user.",
                    // userId);
                    return getById(userId);
                } else {
                    // log.warn("[UserServiceImpl] User ID not found in token claims.");
                    return null;
                }
            } catch (Exception e) {
                log.error("[UserServiceImpl] Error parsing claims or fetching user after token validation: {}",
                        e.getMessage(), e); // Keep this error log
                return null;
            }
        } else {
            // log.warn("[UserServiceImpl] Token validation failed by JwtUtils for token:
            // {}...", (actualToken.length() > 10) ? actualToken.substring(0,10) :
            // actualToken);
            return null;
        }
    }

    @Override
    public void logout(String token) {
        // 实际中应该将token加入黑名单或失效列表
        // 例如，放入Redis中设置过期时间为剩余的token有效期
        // 简化实现，这里不做具体处理
    }

    @Override
    public Page<User> getUserPage(int page, int size, String keyword, String status, String role) {
        // 创建分页对象
        Page<User> pageParam = new Page<>(page, size);

        // 创建查询条件构造器
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();

        // 添加关键字搜索条件（用户名、邮箱或昵称）
        if (StringUtils.hasText(keyword)) {
            queryWrapper.like(User::getUsername, keyword)
                    .or()
                    .like(User::getEmail, keyword)
                    .or()
                    .like(User::getNickname, keyword);
        }

        // 添加状态筛选条件
        if (StringUtils.hasText(status)) {
            queryWrapper.eq(User::getStatus, Integer.parseInt(status));
        }

        // 添加角色筛选条件
        if (StringUtils.hasText(role)) {
            queryWrapper.eq(User::getRole, role);
        }

        // 按创建时间降序排序
        queryWrapper.orderByDesc(User::getCreateTime);

        // 执行分页查询
        return page(pageParam, queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public User addUser(User user) {
        // 验证用户名是否已存在
        User existUser = findByUsername(user.getUsername());
        if (existUser != null) {
            throw new BusinessException("用户名已存在");
        }

        // 验证邮箱是否已存在
        existUser = getByEmail(user.getEmail());
        if (existUser != null) {
            throw new BusinessException("邮箱已存在");
        }

        // 设置默认昵称（如果未提供）
        if (user.getNickname() == null || user.getNickname().isEmpty()) {
            user.setNickname(user.getUsername());
        }

        // 设置默认角色（如果未提供）
        if (user.getRole() == null || user.getRole().isEmpty()) {
            user.setRole("user");
        }

        // 设置默认状态（如果未提供）
        if (user.getStatus() == null) {
            user.setStatus(1);
        }

        // 加密密码
        // 如果密码为空，设置默认密码为123456
        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode("123456"));
        } else {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }

        // 保存用户
        save(user);

        return user;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateUserByAdmin(User user) {
        // 获取原用户信息
        User existUser = getById(user.getUserId());
        if (existUser == null) {
            throw new BusinessException("用户不存在");
        }

        // 验证用户名是否重复（如果已修改）
        if (StringUtils.hasText(user.getUsername()) && !user.getUsername().equals(existUser.getUsername())) {
            User userByUsername = findByUsername(user.getUsername());
            if (userByUsername != null && !userByUsername.getUserId().equals(user.getUserId())) {
                throw new BusinessException("用户名已存在");
            }
        }

        // 验证邮箱是否重复（如果已修改）
        if (StringUtils.hasText(user.getEmail()) && !user.getEmail().equals(existUser.getEmail())) {
            User userByEmail = getByEmail(user.getEmail());
            if (userByEmail != null && !userByEmail.getUserId().equals(user.getUserId())) {
                throw new BusinessException("邮箱已存在");
            }
        }

        // 更新密码（如果提供了新密码）
        if (StringUtils.hasText(user.getPassword())) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        } else {
            // 不更新密码
            user.setPassword(null);
        }

        // 更新用户信息
        return updateById(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteUser(Integer userId) {
        // 检查用户是否存在
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 删除用户
        return removeById(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean toggleUserStatus(Integer userId, Integer status) {
        // 检查用户是否存在
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 更新用户状态
        user.setStatus(status);
        return updateById(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateUserRole(Integer userId, String role) {
        // 检查用户是否存在
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 检查角色是否有效
        if (!"admin".equals(role) && !"user".equals(role)) {
            throw new BusinessException("无效的角色值");
        }

        // 更新用户角色
        user.setRole(role);
        return updateById(user);
    }

    @Override
    public User getUserById(Integer userId) {
        if (userId == null) {
            return null;
        }

        // 构建缓存键
        String cacheKey = CacheConstants.USER_DETAIL_KEY + userId;

        // 查询缓存
        Object cacheResult = redisUtil.get(cacheKey);
        if (cacheResult != null) {
            log.debug("从缓存中获取用户信息: userId={}", userId);
            return (User) cacheResult;
        }

        // 缓存未命中，从数据库查询
        log.debug("缓存未命中，从数据库查询用户信息: userId={}", userId);
        User user = getBaseMapper().selectById(userId);

        // 如果用户存在，则缓存结果
        if (user != null) {
            redisUtil.set(cacheKey, user, CacheConstants.USER_EXPIRE_TIME);
            log.debug("将用户信息缓存到Redis: userId={}", userId);
        }

        return user;
    }

    @Override
    public void updateUser(User user) {
        if (user == null || user.getUserId() == null) {
            throw new BusinessException("用户信息不完整，无法更新");
        }

        boolean result = updateById(user);

        if (result) {
            // 更新成功后，清除相关缓存
            clearUserCache(user);
            log.debug("用户信息更新成功，已清除相关缓存: userId={}", user.getUserId());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deductBalance(Integer userId, BigDecimal amountToDeduct) {
        if (userId == null || amountToDeduct == null || amountToDeduct.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("参数错误：用户ID和扣款金额不能为空，且金额必须大于0");
        }

        User user = getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        BigDecimal currentBalance = user.getBalance();
        if (currentBalance == null) {
            currentBalance = BigDecimal.ZERO; // 如果余额为null，则视为0
            user.setBalance(currentBalance); // 初始化余额，防止后续计算NPE
        }

        if (currentBalance.compareTo(amountToDeduct) < 0) {
            throw new BusinessException("钱包余额不足");
        }

        user.setBalance(currentBalance.subtract(amountToDeduct));
        // updateById 会自动处理 @Version (如果User实体类中配置了版本号字段用于乐观锁)
        // 如果没有配置乐观锁，高并发下这里可能存在问题，需要更复杂的锁机制
        boolean success = updateById(user);
        if (!success) {
            throw new BusinessException("扣款失败，请重试"); // 或者更具体的错误，例如并发更新导致失败
        }
        // TODO: 考虑记录钱包流水
    }

    /**
     * 清除用户相关缓存
     *
     * @param user 用户对象
     */
    private void clearUserCache(User user) {
        if (user == null) {
            return;
        }

        // 清除用户ID缓存
        String userIdCacheKey = CacheConstants.USER_DETAIL_KEY + user.getUserId();
        redisUtil.del(userIdCacheKey);

        // 如果有用户名，清除用户名缓存
        if (StringUtils.hasText(user.getUsername())) {
            String usernameCacheKey = CacheConstants.USER_NAME_KEY + user.getUsername();
            redisUtil.del(usernameCacheKey);
        }

        // 如果有邮箱，清除邮箱缓存
        if (StringUtils.hasText(user.getEmail())) {
            String emailCacheKey = CacheConstants.USER_EMAIL_KEY + user.getEmail();
            redisUtil.del(emailCacheKey);
        }

        // 清除用户列表缓存
        // 使用通配符删除所有用户列表相关缓存
        String userListPattern = CacheConstants.USER_LIST_KEY + "*";
        Set<String> keys = redisTemplate.keys(userListPattern);
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }
}