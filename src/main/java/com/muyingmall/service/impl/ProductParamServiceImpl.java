package com.muyingmall.service.impl;

import com.muyingmall.entity.ProductParam;
import com.muyingmall.mapper.ProductParamMapper;
import com.muyingmall.service.ProductParamService;
import com.muyingmall.util.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 商品参数服务实现类
 */
@Slf4j
@Service
public class ProductParamServiceImpl implements ProductParamService {
    
    @Autowired
    private ProductParamMapper productParamMapper;
    
    @Autowired
    private RedisUtil redisUtil;
    
    private static final String PARAM_CACHE_KEY_PREFIX = "product:params:";
    private static final long CACHE_EXPIRE_TIME = 86400; // 24小时（秒）
    
    @Override
    public List<ProductParam> getParamsByProductId(Integer productId) {
        if (productId == null) {
            return new ArrayList<>();
        }
        
        try {
            // 尝试从缓存获取
            String cacheKey = PARAM_CACHE_KEY_PREFIX + productId;
            Object cachedParams = redisUtil.get(cacheKey);
            
            if (cachedParams != null && cachedParams instanceof List) {
                log.debug("从缓存获取商品参数，商品ID: {}", productId);
                @SuppressWarnings("unchecked")
                List<ProductParam> params = (List<ProductParam>) cachedParams;
                return params;
            }
            
            // 从数据库查询
            List<ProductParam> params = productParamMapper.selectByProductId(productId);
            
            // 存入缓存（24小时）
            if (params != null && !params.isEmpty()) {
                redisUtil.set(cacheKey, params, CACHE_EXPIRE_TIME);
                log.debug("商品参数已缓存，商品ID: {}, 参数数量: {}", productId, params.size());
            }
            
            return params != null ? params : new ArrayList<>();
        } catch (Exception e) {
            log.error("获取商品参数失败，商品ID: {}", productId, e);
            return new ArrayList<>();
        }
    }
}
