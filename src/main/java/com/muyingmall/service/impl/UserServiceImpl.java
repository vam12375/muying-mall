package com.muyingmall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.muyingmall.common.constants.CacheConstants;
import com.muyingmall.common.exception.BusinessException;
import com.muyingmall.dto.AdminLoginDTO;
import com.muyingmall.dto.LoginDTO;
import com.muyingmall.dto.UserDTO;
import com.muyingmall.entity.User;
import com.muyingmall.entity.UserAccount;
import com.muyingmall.entity.Order;
import com.muyingmall.entity.Favorite;
import com.muyingmall.entity.Comment;
import com.muyingmall.entity.UserPoints;
import com.muyingmall.entity.UserCoupon;
import com.muyingmall.mapper.UserMapper;
import com.muyingmall.service.UserAccountService;
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
import java.util.Map;
import java.util.HashMap;

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
    private final UserAccountService userAccountService;
    private final com.muyingmall.mapper.OrderMapper orderMapper;
    private final com.muyingmall.mapper.FavoriteMapper favoriteMapper;
    private final com.muyingmall.mapper.CommentMapper commentMapper;
    private final com.muyingmall.mapper.UserPointsMapper userPointsMapper;
    private final com.muyingmall.mapper.UserCouponMapper userCouponMapper;

    @Value("${upload.path:G:/muying/muying-web/public}")
    private String uploadPath;

    @Value("${upload.avatar.path:/avatars}")
    private String avatarPath;

    @Value("${upload.domain:http://localhost:5173}")
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
        // 优化：优先使用缓存查询，减少数据库压力
        String loginName = loginDTO.getUsername();
        User user = null;
        
        // 先尝试按用户名查询（利用缓存）
        user = findByUsername(loginName);
        
        // 如果用户名未找到，尝试按邮箱查询（利用缓存）
        if (user == null) {
            user = getByEmail(loginName);
        }

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
        // 优化：优先使用缓存查询，减少数据库压力
        User admin = findByUsername(adminLoginDTO.getAdmin_name());

        // 验证用户是否存在
        if (admin == null) {
            throw new BusinessException("用户名或密码错误");
        }
        
        // 验证是否为管理员
        if (!"admin".equals(admin.getRole())) {
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
        // log.debug("[UserServiceImpl] Generating token for user: {}",
        // user.getUsername());
        // log.debug("[UserServiceImpl] Using JwtUtils instance hash: {}",
        // System.identityHashCode(jwtUtils));
        // log.debug("[UserServiceImpl] JwtUtils signingKey for token generation
        // (Base64): {}", jwtUtils.getSigningKeyBase64ForDebug());

        String token = jwtUtils.generateToken(user.getUserId(), user.getUsername(), user.getRole());
        // log.debug("[UserServiceImpl] Token generated via JwtUtils: {}...", (token !=
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
                    // log.debug("[UserServiceImpl] User ID {} retrieved from token. Fetching user.",
                    // userId);
                    return getById(userId);
                } else {
                    // log.warn("[UserServiceImpl] User ID not found in token claims.");
                    return null;
                }
            } catch (Exception e) {
                log.error("[UserServiceImpl] Error parsing claims or fetching user after token validation: {}",
                        e.getMessage(), e); // 保留此错误日志
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
    public Page<User> getUserPage(int page, int size, String keyword, String status, String role, String sortBy, String sortOrder) {
        // 创建分页对象
        Page<User> pageParam = new Page<>(page, size);
        
        // 判断排序方向（默认降序）
        boolean isAsc = "asc".equalsIgnoreCase(sortOrder);

        // 如果按余额排序，需要关联 user_account 表
        if ("balance".equals(sortBy)) {
            return getUserPageWithBalanceSort(pageParam, keyword, status, role, isAsc);
        }

        // 创建查询条件构造器
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();

        // 添加关键字搜索条件（用户名、邮箱或昵称）
        if (StringUtils.hasText(keyword)) {
            queryWrapper.and(wrapper -> wrapper
                    .like(User::getUsername, keyword)
                    .or()
                    .like(User::getEmail, keyword)
                    .or()
                    .like(User::getNickname, keyword));
        }

        // 添加状态筛选条件
        if (StringUtils.hasText(status)) {
            queryWrapper.eq(User::getStatus, Integer.parseInt(status));
        }

        // 添加角色筛选条件
        if (StringUtils.hasText(role)) {
            queryWrapper.eq(User::getRole, role);
        }

        // 根据排序字段排序
        if ("id".equals(sortBy)) {
            if (isAsc) {
                queryWrapper.orderByAsc(User::getUserId);
            } else {
                queryWrapper.orderByDesc(User::getUserId);
            }
        } else if ("createTime".equals(sortBy)) {
            if (isAsc) {
                queryWrapper.orderByAsc(User::getCreateTime);
            } else {
                queryWrapper.orderByDesc(User::getCreateTime);
            }
        } else {
            // 默认按创建时间降序排序
            queryWrapper.orderByDesc(User::getCreateTime);
        }

        // 执行分页查询
        return page(pageParam, queryWrapper);
    }
    
    /**
     * 按余额排序的用户分页查询（需要关联 user_account 表）
     */
    private Page<User> getUserPageWithBalanceSort(Page<User> pageParam, String keyword, String status, String role, boolean isAsc) {
        // 构建基础SQL条件
        StringBuilder whereSql = new StringBuilder("1=1");
        
        if (StringUtils.hasText(keyword)) {
            whereSql.append(" AND (u.username LIKE '%").append(keyword).append("%'")
                    .append(" OR u.email LIKE '%").append(keyword).append("%'")
                    .append(" OR u.nickname LIKE '%").append(keyword).append("%')");
        }
        
        if (StringUtils.hasText(status)) {
            whereSql.append(" AND u.status = ").append(status);
        }
        
        if (StringUtils.hasText(role)) {
            whereSql.append(" AND u.role = '").append(role).append("'");
        }
        
        // 使用原生SQL查询，关联 user_account 表按余额排序
        String orderDirection = isAsc ? "ASC" : "DESC";
        String sql = "SELECT u.* FROM user u " +
                     "LEFT JOIN user_account ua ON u.user_id = ua.user_id " +
                     "WHERE " + whereSql.toString() + " " +
                     "ORDER BY COALESCE(ua.balance, 0) " + orderDirection;
        
        // 使用 MyBatis-Plus 的 baseMapper 执行原生查询
        // 由于复杂度较高，这里使用简化方案：先查询所有符合条件的用户ID，再分页
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        
        if (StringUtils.hasText(keyword)) {
            queryWrapper.and(wrapper -> wrapper
                    .like(User::getUsername, keyword)
                    .or()
                    .like(User::getEmail, keyword)
                    .or()
                    .like(User::getNickname, keyword));
        }
        
        if (StringUtils.hasText(status)) {
            queryWrapper.eq(User::getStatus, Integer.parseInt(status));
        }
        
        if (StringUtils.hasText(role)) {
            queryWrapper.eq(User::getRole, role);
        }
        
        // 使用 last 方法添加自定义排序（关联子查询）
        String balanceOrderSql = "ORDER BY (SELECT COALESCE(ua.balance, 0) FROM user_account ua WHERE ua.user_id = user.user_id) " + orderDirection;
        queryWrapper.last(balanceOrderSql);
        
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
            log.debug("从缓存中获取用户详情: userId={}", userId);
            return (User) cacheResult;
        }

        // 缓存未命中，从数据库查询
        log.debug("缓存未命中，从数据库查询用户详情: userId={}", userId);

        // 从数据库查询用户信息
        User user = getById(userId);

        // 如果用户存在，缓存结果
        if (user != null) {
            redisUtil.set(cacheKey, user, CacheConstants.USER_EXPIRE_TIME);
            log.debug("将用户详情缓存到Redis: userId={}, 过期时间={}秒", userId, CacheConstants.USER_EXPIRE_TIME);
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
        log.debug("开始执行扣款操作: userId={}, amount={}", userId, amountToDeduct);
        
        if (userId == null || amountToDeduct == null || amountToDeduct.compareTo(BigDecimal.ZERO) <= 0) {
            log.error("扣款参数错误: userId={}, amount={}", userId, amountToDeduct);
            throw new BusinessException("参数错误：用户ID和扣款金额不能为空，且金额必须大于0");
        }

        try {
            // 使用UserAccountService获取真实的用户账户余额
            UserAccount userAccount = userAccountService.getUserAccountByUserId(userId);
            log.debug("获取到用户账户: userId={}, accountId={}, balance={}", 
                     userId, userAccount.getId(), userAccount.getBalance());
            
            // 检查余额是否足够
            BigDecimal currentBalance = userAccount.getBalance();
            if (currentBalance == null) {
                currentBalance = BigDecimal.ZERO;
            }
            
            log.debug("余额检查: 当前余额={}, 扣款金额={}, 差额={}", 
                     currentBalance, amountToDeduct, currentBalance.subtract(amountToDeduct));
            
            if (currentBalance.compareTo(amountToDeduct) < 0) {
                log.warn("用户余额不足: userId={}, balance={}, amount={}", 
                         userId, currentBalance, amountToDeduct);
                throw new BusinessException("钱包余额不足，当前余额：" + currentBalance + "，扣款金额：" + amountToDeduct);
            }
            
            // 使用UserAccountService提供的方法扣减余额（通常是针对一个订单）
            // 如果没有orderId，则传null或特殊值如-1，在UserAccountService中特殊处理
            Integer orderId = null; // 这里可能需要修改，取决于业务需求
            
            // 使用adjustUserBalance方法，传入负数金额表示扣减
            BigDecimal negativeAmount = amountToDeduct.negate(); // 转为负数
            userAccountService.adjustUserBalance(userId, negativeAmount, "系统扣款");
            
            log.debug("扣款成功: userId={}, amount={}", userId, amountToDeduct);
            
        } catch (BusinessException e) {
            log.error("扣款业务异常: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("扣款过程发生异常: {}", e.getMessage(), e);
            throw new BusinessException("扣款失败: " + e.getMessage());
        }
    }

    /**
     * 清除用户相关缓存
     *
     * @param user 用户对象
     */
    private void clearUserCache(User user) {
        if (user == null || user.getUserId() == null) {
            return;
        }

        try {
            // 用户详情缓存
            String detailCacheKey = CacheConstants.USER_DETAIL_KEY + user.getUserId();
            redisUtil.del(detailCacheKey);
            log.debug("清除用户详情缓存: userId={}", user.getUserId());

            // 用户名查询缓存（仅当用户名更新时才需要清除）
            if (user.getUsername() != null) {
                String usernameCacheKey = CacheConstants.USER_NAME_KEY + user.getUsername();
                redisUtil.del(usernameCacheKey);
                log.debug("清除用户名查询缓存: username={}", user.getUsername());
            }

            // 邮箱查询缓存（仅当邮箱更新时才需要清除）
            if (user.getEmail() != null) {
                String emailCacheKey = CacheConstants.USER_EMAIL_KEY + user.getEmail();
                redisUtil.del(emailCacheKey);
                log.debug("清除邮箱查询缓存: email={}", user.getEmail());
            }

            // 用户令牌缓存（一般不需要在这里清除，通常在登出时清除）
            // 如果是更改密码或者账号状态变更，可能需要强制清除令牌缓存
            if (user.getPassword() != null || user.getStatus() != null) {
                // 查找并清除该用户的所有令牌缓存
                Set<String> tokenKeys = redisUtil.keys(CacheConstants.USER_TOKEN_KEY + "*");
                if (tokenKeys != null && !tokenKeys.isEmpty()) {
                    // 遍历令牌，检查是否属于当前用户
                    for (String tokenKey : tokenKeys) {
                        try {
                            String token = tokenKey.substring(CacheConstants.USER_TOKEN_KEY.length());
                            // 从令牌中提取用户信息
                            Claims claims = jwtUtils.getClaimsFromToken(token);
                            if (claims != null) {
                                Integer tokenUserId = claims.get("userId", Integer.class);
                                if (tokenUserId != null && tokenUserId.equals(user.getUserId())) {
                                    // 属于当前用户的令牌，清除缓存
                                    redisUtil.del(tokenKey);
                                    log.debug("清除用户令牌缓存: userId={}, token={}", user.getUserId(), token);
                                }
                            }
                        } catch (Exception e) {
                            log.error("解析令牌失败: {}", e.getMessage());
                            // 继续处理下一个令牌
                        }
                    }
                }
            }

            // 清除用户列表缓存 (可能涉及分页查询)
            // 适用于用户信息更新影响列表显示的情况
            // 使用模式匹配删除所有与用户列表相关的缓存
            Set<String> listKeys = redisUtil.keys(CacheConstants.USER_LIST_KEY + "*");
            if (listKeys != null && !listKeys.isEmpty()) {
                redisUtil.del(listKeys.toArray(new String[0]));
                log.debug("清除用户列表缓存，共{}个键", listKeys.size());
            }

            log.debug("已清除用户相关缓存: userId={}", user.getUserId());
        } catch (Exception e) {
            log.error("清除用户缓存失败: userId={}, error={}", user.getUserId(), e.getMessage());
        }
    }

    @Override
    public Map<String, Object> getUserStats(Integer userId) {
        Map<String, Object> stats = new HashMap<>();
        
        log.debug("开始获取用户统计数据: userId={}", userId);
        
        try {
            // 获取用户账户余额
            UserAccount userAccount = userAccountService.getUserAccountByUserId(userId);
            if (userAccount != null) {
                stats.put("balance", userAccount.getBalance() != null ? userAccount.getBalance() : BigDecimal.ZERO);
                log.debug("用户账户余额: userId={}, balance={}", userId, userAccount.getBalance());
            } else {
                stats.put("balance", BigDecimal.ZERO);
                log.warn("用户账户不存在，余额使用默认值: userId={}", userId);
            }
            
            // 从 user_points 表获取积分
            UserPoints userPoints = userPointsMapper.selectOne(
                new LambdaQueryWrapper<UserPoints>()
                    .eq(UserPoints::getUserId, userId)
            );
            if (userPoints != null) {
                stats.put("points", userPoints.getPoints() != null ? userPoints.getPoints() : 0);
                log.debug("用户积分: userId={}, points={}", userId, userPoints.getPoints());
            } else {
                stats.put("points", 0);
                log.warn("用户积分记录不存在，使用默认值: userId={}", userId);
            }
            
            // 从 user_coupon 表统计未使用的优惠券数量
            Long couponCount = userCouponMapper.selectCount(
                new LambdaQueryWrapper<UserCoupon>()
                    .eq(UserCoupon::getUserId, userId)
                    .eq(UserCoupon::getStatus, "UNUSED")
            );
            stats.put("couponCount", couponCount != null ? couponCount.intValue() : 0);
            log.debug("用户优惠券数量: userId={}, couponCount={}", userId, couponCount);
            
            // 获取订单数量
            Long orderCount = orderMapper.selectCount(
                new LambdaQueryWrapper<Order>()
                    .eq(Order::getUserId, userId)
            );
            stats.put("orderCount", orderCount != null ? orderCount.intValue() : 0);
            
            // 获取收藏数量
            Long favoriteCount = favoriteMapper.selectCount(
                new LambdaQueryWrapper<Favorite>()
                    .eq(Favorite::getUserId, userId)
            );
            stats.put("favoriteCount", favoriteCount != null ? favoriteCount.intValue() : 0);
            
            // 获取评价数量
            Long commentCount = commentMapper.selectCount(
                new LambdaQueryWrapper<Comment>()
                    .eq(Comment::getUserId, userId)
            );
            stats.put("commentCount", commentCount != null ? commentCount.intValue() : 0);
            
            // 优化：热点路径使用debug级别日志
            log.debug("用户统计数据获取成功: userId={}", userId);
            
        } catch (Exception e) {
            log.error("获取用户统计数据失败: userId={}, error={}", userId, e.getMessage(), e);
            // 返回默认值
            stats.put("balance", BigDecimal.ZERO);
            stats.put("points", 0);
            stats.put("couponCount", 0);
            stats.put("orderCount", 0);
            stats.put("favoriteCount", 0);
            stats.put("commentCount", 0);
        }
        
        return stats;
    }
}