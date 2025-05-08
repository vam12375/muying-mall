package com.muyingmall.common;

/**
 * 缓存常量类
 * 定义缓存键前缀和过期时间
 */
public class CacheConstants {

    /**
     * 商品缓存前缀
     */
    public static final String PRODUCT_KEY_PREFIX = "product:";

    /**
     * 商品详情缓存键
     */
    public static final String PRODUCT_DETAIL_KEY = PRODUCT_KEY_PREFIX + "detail:";

    /**
     * 商品列表缓存键
     */
    public static final String PRODUCT_LIST_KEY = PRODUCT_KEY_PREFIX + "list:";

    /**
     * 管理后台商品列表缓存键
     */
    public static final String PRODUCT_ADMIN_LIST_KEY = PRODUCT_KEY_PREFIX + "admin:list:";

    /**
     * 热门商品缓存键
     */
    public static final String PRODUCT_HOT_KEY = PRODUCT_KEY_PREFIX + "hot";

    /**
     * 新品商品缓存键
     */
    public static final String PRODUCT_NEW_KEY = PRODUCT_KEY_PREFIX + "new";

    /**
     * 推荐商品缓存键
     */
    public static final String PRODUCT_RECOMMEND_KEY = PRODUCT_KEY_PREFIX + "recommend";

    /**
     * 分类商品缓存键
     */
    public static final String PRODUCT_CATEGORY_KEY = PRODUCT_KEY_PREFIX + "category:";

    /**
     * 积分商品缓存前缀
     */
    public static final String POINTS_PRODUCT_KEY_PREFIX = "points_product:";

    /**
     * 积分商品详情缓存键
     */
    public static final String POINTS_PRODUCT_DETAIL_KEY = POINTS_PRODUCT_KEY_PREFIX + "detail:";

    /**
     * 积分商品列表缓存键
     */
    public static final String POINTS_PRODUCT_LIST_KEY = POINTS_PRODUCT_KEY_PREFIX + "list:";

    /**
     * 积分热门商品缓存键
     */
    public static final String POINTS_PRODUCT_HOT_KEY = POINTS_PRODUCT_KEY_PREFIX + "hot";

    /**
     * 积分推荐商品缓存键
     */
    public static final String POINTS_PRODUCT_RECOMMEND_KEY = POINTS_PRODUCT_KEY_PREFIX + "recommend";

    /**
     * 分类缓存前缀
     */
    public static final String CATEGORY_KEY_PREFIX = "category:";

    /**
     * 品牌缓存前缀
     */
    public static final String BRAND_KEY_PREFIX = "brand:";

    /**
     * 商品缓存过期时间（秒）
     */
    public static final long PRODUCT_EXPIRE_TIME = 7200; // 2小时

    /**
     * 热门商品缓存过期时间（秒）
     */
    public static final long PRODUCT_HOT_EXPIRE_TIME = 1800; // 30分钟

    /**
     * 分类缓存过期时间（秒）
     */
    public static final long CATEGORY_EXPIRE_TIME = 86400; // 24小时

    /**
     * 品牌缓存过期时间（秒）
     */
    public static final long BRAND_EXPIRE_TIME = 86400; // 24小时

    /**
     * 通用短时缓存过期时间（秒）
     */
    public static final long SHORT_EXPIRE_TIME = 300; // 5分钟

    /**
     * 通用中时缓存过期时间（秒）
     */
    public static final long MEDIUM_EXPIRE_TIME = 1800; // 30分钟

    /**
     * 通用长时缓存过期时间（秒）
     */
    public static final long LONG_EXPIRE_TIME = 86400; // 24小时
}