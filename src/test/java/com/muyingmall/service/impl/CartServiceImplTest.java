package com.muyingmall.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.muyingmall.common.exception.BusinessException;
import com.muyingmall.dto.CartAddDTO;
import com.muyingmall.entity.Cart;
import com.muyingmall.entity.Product;
import com.muyingmall.mapper.CartMapper;
import com.muyingmall.service.ProductService;
import com.muyingmall.util.RedisUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * 购物车服务 · 单元测试。
 * 被测方法：{@link CartServiceImpl#addCart(Integer, CartAddDTO)}
 * 覆盖：
 *   - 黄金路径：商品存在且上架、库存充足、购物车无重复项 → 插入新购物车
 *   - 累加路径：已存在相同 SKU 购物车项 → 数量累加
 *   - 边界：商品不存在 / 商品下架 / 库存不足 → 抛 BusinessException
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@Tag("unit")
@DisplayName("购物车服务 · 单元测试")
class CartServiceImplTest {

    @Mock
    private CartMapper cartMapper;

    @Mock
    private ProductService productService;
    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private RedisUtil redisUtil;
    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @InjectMocks
    private CartServiceImpl cartService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(cartService, "baseMapper", cartMapper);
    }

    private Product onSaleProduct(Integer productId, int stock) {
        Product product = new Product();
        product.setProductId(productId);
        product.setProductName("测试商品-" + productId);
        product.setPriceNew(new BigDecimal("99.00"));
        product.setStock(stock);
        product.setProductStatus("上架");
        return product;
    }

    private CartAddDTO cartAddDto(Integer productId, Long skuId, Integer quantity) {
        CartAddDTO dto = new CartAddDTO();
        dto.setProductId(productId);
        dto.setSkuId(skuId);
        dto.setSkuName("默认规格");
        dto.setQuantity(quantity);
        dto.setSelected(1);
        dto.setPrice(new BigDecimal("88.00"));
        return dto;
    }

    @Nested
    @DisplayName("addCart(userId, dto) · 黄金路径")
    class GoldenPaths {

        @Test
        @DisplayName("无重复项时应插入新购物车并清缓存")
        void addCart_shouldInsertNewCart_whenNoDuplicate() {
            // Given
            given(productService.getById(100)).willReturn(onSaleProduct(100, 10));
            given(cartMapper.selectOne(any(Wrapper.class), anyBoolean())).willReturn(null);

            // When
            Cart result = cartService.addCart(9, cartAddDto(100, 555L, 2));

            // Then
            ArgumentCaptor<Cart> captor = ArgumentCaptor.forClass(Cart.class);
            verify(cartMapper).insert(captor.capture());
            Cart persisted = captor.getValue();
            assertThat(persisted.getUserId()).isEqualTo(9);
            assertThat(persisted.getProductId()).isEqualTo(100);
            assertThat(persisted.getSkuId()).isEqualTo(555L);
            assertThat(persisted.getQuantity()).isEqualTo(2);
            assertThat(persisted.getSpecsHash()).isEqualTo("sku_555");
            assertThat(persisted.getStatus()).isEqualTo(1);
            // DTO 价格优先
            assertThat(persisted.getPriceSnapshot()).isEqualByComparingTo("88.00");

            assertThat(result).isSameAs(persisted);
            // 缓存清理（redisUtil.del 至少调用一次）
            verify(redisUtil, org.mockito.Mockito.atLeastOnce()).del(org.mockito.ArgumentMatchers.any(String[].class));
        }

        @Test
        @DisplayName("已存在同 SKU 购物车时应累加数量并 updateById")
        void addCart_shouldAccumulateQuantity_whenDuplicateExists() {
            // Given
            given(productService.getById(100)).willReturn(onSaleProduct(100, 10));
            Cart exist = new Cart();
            exist.setCartId(77);
            exist.setUserId(9);
            exist.setProductId(100);
            exist.setSkuId(555L);
            exist.setQuantity(1);
            exist.setSelected(0);
            given(cartMapper.selectOne(any(Wrapper.class), anyBoolean())).willReturn(exist);
            given(cartMapper.updateById(any(Cart.class))).willReturn(1);

            // When
            Cart result = cartService.addCart(9, cartAddDto(100, 555L, 3));

            // Then
            assertThat(result).isSameAs(exist);
            assertThat(result.getQuantity()).isEqualTo(4); // 1 + 3
            assertThat(result.getSelected()).isEqualTo(1);
            verify(cartMapper, never()).insert(any(Cart.class));
            verify(cartMapper).updateById(exist);
        }
    }

    @Nested
    @DisplayName("addCart(userId, dto) · 边界")
    class BoundaryPaths {

        @Test
        @DisplayName("商品不存在时应抛 BusinessException")
        void addCart_shouldThrow_whenProductNotFound() {
            given(productService.getById(9999)).willReturn(null);

            assertThatThrownBy(() -> cartService.addCart(9, cartAddDto(9999, 1L, 1)))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("商品不存在");

            verify(cartMapper, never()).insert(any(Cart.class));
        }

        @Test
        @DisplayName("商品下架时应抛 BusinessException")
        void addCart_shouldThrow_whenProductOffShelf() {
            Product offShelf = onSaleProduct(100, 10);
            offShelf.setProductStatus("下架");
            given(productService.getById(100)).willReturn(offShelf);

            assertThatThrownBy(() -> cartService.addCart(9, cartAddDto(100, 1L, 1)))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("已下架");

            verify(cartMapper, never()).insert(any(Cart.class));
        }

        @Test
        @DisplayName("库存不足时应抛 BusinessException")
        void addCart_shouldThrow_whenStockInsufficient() {
            given(productService.getById(100)).willReturn(onSaleProduct(100, 2));

            assertThatThrownBy(() -> cartService.addCart(9, cartAddDto(100, 1L, 10)))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("库存不足");

            verify(cartMapper, never()).insert(any(Cart.class));
        }
    }
}
