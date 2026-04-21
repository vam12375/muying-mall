package com.muyingmall.service.impl;

import com.muyingmall.entity.User;
import com.muyingmall.fixtures.UserFixtures;
import com.muyingmall.mapper.CommentMapper;
import com.muyingmall.mapper.FavoriteMapper;
import com.muyingmall.mapper.OrderMapper;
import com.muyingmall.mapper.UserCouponMapper;
import com.muyingmall.mapper.UserMapper;
import com.muyingmall.mapper.UserPointsMapper;
import com.muyingmall.service.UserAccountService;
import com.muyingmall.util.JwtUtils;
import com.muyingmall.util.RedisUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * 用户服务 · 单元测试。
 * 覆盖：
 *   - getUserById：null 兜底 / 缓存命中 / 缓存未命中 DB 命中并回写 / DB 未命中不写缓存
 *   - verifyPassword：委托 PasswordEncoder
 *   - generateToken：委托 JwtUtils
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@Tag("unit")
@DisplayName("用户服务 · 单元测试")
class UserServiceImplTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtUtils jwtUtils;
    @Mock
    private RedisUtil redisUtil;
    @Mock
    private RedisTemplate<String, Object> redisTemplate;
    @Mock
    private UserAccountService userAccountService;
    @Mock
    private OrderMapper orderMapper;
    @Mock
    private FavoriteMapper favoriteMapper;
    @Mock
    private CommentMapper commentMapper;
    @Mock
    private UserPointsMapper userPointsMapper;
    @Mock
    private UserCouponMapper userCouponMapper;

    @InjectMocks
    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(userService, "baseMapper", userMapper);
    }

    @Nested
    @DisplayName("getUserById(userId)")
    class GetUserById {

        @Test
        @DisplayName("userId 为 null 时直接返回 null")
        void getUserById_shouldReturnNull_whenUserIdNull() {
            assertThat(userService.getUserById(null)).isNull();
            verify(redisUtil, never()).get(anyString());
        }

        @Test
        @DisplayName("缓存命中时应直接返回缓存实例")
        void getUserById_shouldReturnCached_whenCacheHit() {
            // Given
            User cached = UserFixtures.normalUser(9, "alice");
            given(redisUtil.get(anyString())).willReturn(cached);

            // When
            User result = userService.getUserById(9);

            // Then
            assertThat(result).isSameAs(cached);
            verify(userMapper, never()).selectById(any());
        }

        @Test
        @DisplayName("缓存未命中且数据库查到时应返回用户并回写缓存")
        void getUserById_shouldReturnFromDbAndCache_whenDbHit() {
            // Given
            User dbUser = UserFixtures.normalUser(9, "alice");
            given(redisUtil.get(anyString())).willReturn(null);
            given(userMapper.selectById(9)).willReturn(dbUser);

            // When
            User result = userService.getUserById(9);

            // Then
            assertThat(result).isSameAs(dbUser);
            verify(redisUtil).set(anyString(), eq(dbUser), anyLong());
        }

        @Test
        @DisplayName("缓存未命中且数据库未查到时应返回 null 且不写缓存")
        void getUserById_shouldReturnNull_whenDbMiss() {
            // Given
            given(redisUtil.get(anyString())).willReturn(null);
            given(userMapper.selectById(9999)).willReturn(null);

            // When
            User result = userService.getUserById(9999);

            // Then
            assertThat(result).isNull();
            verify(redisUtil, never()).set(anyString(), any(), anyLong());
        }
    }

    @Nested
    @DisplayName("verifyPassword / generateToken")
    class PasswordAndToken {

        @Test
        @DisplayName("verifyPassword 应委托给 PasswordEncoder.matches")
        void verifyPassword_shouldDelegate_toPasswordEncoder() {
            // Given
            User user = UserFixtures.normalUser(9, "alice");
            given(passwordEncoder.matches("raw", user.getPassword())).willReturn(true);

            // When
            boolean ok = userService.verifyPassword(user, "raw");

            // Then
            assertThat(ok).isTrue();
            verify(passwordEncoder).matches("raw", user.getPassword());
        }

        @Test
        @DisplayName("generateToken 应委托给 JwtUtils.generateToken 并返回其结果")
        void generateToken_shouldDelegate_toJwtUtils() {
            // Given
            User user = UserFixtures.normalUser(9, "alice");
            given(jwtUtils.generateToken(eq(9), eq("alice"), eq("user"))).willReturn("jwt-token");

            // When
            String token = userService.generateToken(user);

            // Then
            assertThat(token).isEqualTo("jwt-token");
            verify(jwtUtils).generateToken(9, "alice", "user");
        }
    }
}
