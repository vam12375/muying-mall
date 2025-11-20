package com.muyingmall.common.constants;

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
     * 用户缓存前缀
     */
    public static final String USER_KEY_PREFIX = "user:";

    /**
     * 用户详情缓存键
     */
    public static final String USER_DETAIL_KEY = USER_KEY_PREFIX + "detail:";

    /**
     * 用户名查询缓存键
     */
    public static final String USER_NAME_KEY = USER_KEY_PREFIX + "name:";

    /**
     * 用户邮箱查询缓存键
     */
    public static final String USER_EMAIL_KEY = USER_KEY_PREFIX + "email:";

    /**
     * 用户列表缓存键
     */
    public static final String USER_LIST_KEY = USER_KEY_PREFIX + "list:";

    /**
     * 用户令牌缓存键
     */
    public static final String USER_TOKEN_KEY = USER_KEY_PREFIX + "token:";

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
     * 用户信息缓存过期时间（秒）
     */
    public static final long USER_EXPIRE_TIME = 3600; // 1小时

    /**
     * 用户令牌缓存过期时间（秒）
     */
    public static final long USER_TOKEN_EXPIRE_TIME = 86400; // 24小时

    /**
     * 订单缓存前缀
     */
    public static final String ORDER_KEY_PREFIX = "order:";

    /**
     * 订单详情缓存键
     */
    public static final String ORDER_DETAIL_KEY = ORDER_KEY_PREFIX + "detail:";

    /**
     * 用户订单列表缓存键
     */
    public static final String USER_ORDER_LIST_KEY = ORDER_KEY_PREFIX + "user:";

    /**
     * 订单状态缓存键
     */
    public static final String ORDER_STATUS_KEY = ORDER_KEY_PREFIX + "status:";

    /**
     * 订单统计缓存键
     */
    public static final String ORDER_STATS_KEY = ORDER_KEY_PREFIX + "stats:";

    /**
     * 管理员订单列表缓存键
     */
    public static final String ADMIN_ORDER_LIST_KEY = ORDER_KEY_PREFIX + "admin:list:";

    /**
     * 订单信息缓存过期时间（秒）
     */
    public static final long ORDER_EXPIRE_TIME = 1800; // 30分钟

    /**
     * 订单列表缓存过期时间（秒）
     */
    public static final long ORDER_LIST_EXPIRE_TIME = 900; // 15分钟

    /**
     * 订单统计缓存过期时间（秒）
     */
    public static final long ORDER_STATS_EXPIRE_TIME = 3600; // 1小时

    /**
     * 购物车缓存前缀
     */
    public static final String CART_KEY_PREFIX = "cart:";

    /**
     * 用户购物车缓存键
     */
    public static final String USER_CART_KEY = CART_KEY_PREFIX + "user:";

    /**
     * 购物车商品数量缓存键
     */
    public static final String CART_COUNT_KEY = CART_KEY_PREFIX + "count:";

    /**
     * 购物车选中项缓存键
     */
    public static final String CART_SELECTED_KEY = CART_KEY_PREFIX + "selected:";

    /**
     * 购物车缓存过期时间（秒）
     */
    public static final long CART_EXPIRE_TIME = 1800; // 30分钟

    /**
     * 购物车数量缓存过期时间（秒）
     */
    public static final long CART_COUNT_EXPIRE_TIME = 300; // 5分钟

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

    /**
     * 积分系统缓存前缀
     */
    public static final String POINTS_KEY_PREFIX = "points:";

    /**
     * 用户签到状态缓存键
     */
    public static final String USER_SIGNIN_STATUS_KEY = POINTS_KEY_PREFIX + "signin:status:";

    /**
     * 用户积分历史缓存键
     */
    public static final String USER_POINTS_HISTORY_KEY = POINTS_KEY_PREFIX + "history:";

    /**
     * 用户签到日历缓存键
     */
    public static final String USER_SIGNIN_CALENDAR_KEY = POINTS_KEY_PREFIX + "signin:calendar:";

    /**
     * 积分系统缓存过期时间（秒）
     */
    public static final long POINTS_STATUS_EXPIRE_TIME = 300; // 5分钟

    /**
     * 积分历史缓存过期时间（秒）
     */
    public static final long POINTS_HISTORY_EXPIRE_TIME = 600; // 10分钟

    /**
     * 签到日历缓存过期时间（秒）
     */
    public static final long SIGNIN_CALENDAR_EXPIRE_TIME = 3600; // 1小时

    /**
     * 消息系统缓存前缀
     */
    public static final String MESSAGE_KEY_PREFIX = "message:";

    /**
     * 用户未读消息计数缓存键
     */
    public static final String USER_UNREAD_COUNT_KEY = MESSAGE_KEY_PREFIX + "unread:count:";

    /**
     * 用户消息列表缓存键
     */
    public static final String USER_MESSAGES_KEY = MESSAGE_KEY_PREFIX + "list:";

    /**
     * 用户未读消息按类型统计缓存键
     */
    public static final String USER_UNREAD_TYPE_KEY = MESSAGE_KEY_PREFIX + "unread:type:";

    /**
     * 消息计数缓存过期时间（秒）
     */
    public static final long MESSAGE_COUNT_EXPIRE_TIME = 60; // 1分钟

    /**
     * 消息列表缓存过期时间（秒）
     */
    public static final long MESSAGE_LIST_EXPIRE_TIME = 300; // 5分钟

    /**
     * 内容管理缓存前缀
     */
    public static final String CONTENT_KEY_PREFIX = "content:";

    /**
     * 内容详情缓存键
     */
    public static final String CONTENT_DETAIL_KEY = CONTENT_KEY_PREFIX + "detail:";

    /**
     * 内容列表缓存键
     */
    public static final String CONTENT_LIST_KEY = CONTENT_KEY_PREFIX + "list:";

    /**
     * 分析数据缓存前缀
     */
    public static final String ANALYTICS_KEY_PREFIX = "analytics:";

    /**
     * 分析报表缓存键
     */
    public static final String ANALYTICS_REPORT_KEY = ANALYTICS_KEY_PREFIX + "report:";

    /**
     * 分析数据缓存过期时间（秒）
     */
    public static final long ANALYTICS_EXPIRE_TIME = 1800; // 30分钟

    /**
     * 空值缓存标记
     * 用于标识查询不到的数据，防止缓存穿透
     */
    public static final String EMPTY_CACHE_VALUE = "EMPTY_VALUE_PROTECTION";
}
