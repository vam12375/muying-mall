package com.muyingmall.common.constants;

/**
 * API 错误码常量类
 * 统一定义系统中使用的错误码和错误消息
 * 错误码规范：
 * - 1000-1999: 用户相关错误
 * - 2000-2999: 商品相关错误
 * - 3000-3999: 订单相关错误
 * - 4000-4999: 支付相关错误
 * - 5000-5999: 系统相关错误
 * - 6000-6999: 权限相关错误
 * - 7000-7999: 业务逻辑错误
 */
public class ApiErrorCodes {

    // ========== 通用错误码 ==========
    public static final int SUCCESS = 200;
    public static final String SUCCESS_MSG = "操作成功";
    
    public static final int BAD_REQUEST = 400;
    public static final String BAD_REQUEST_MSG = "请求参数错误";
    
    public static final int UNAUTHORIZED = 401;
    public static final String UNAUTHORIZED_MSG = "未授权访问";
    
    public static final int FORBIDDEN = 403;
    public static final String FORBIDDEN_MSG = "禁止访问";
    
    public static final int NOT_FOUND = 404;
    public static final String NOT_FOUND_MSG = "资源不存在";
    
    public static final int INTERNAL_ERROR = 500;
    public static final String INTERNAL_ERROR_MSG = "服务器内部错误";

    // ========== 用户相关错误码 (1000-1999) ==========
    public static final int USER_NOT_FOUND = 1001;
    public static final String USER_NOT_FOUND_MSG = "用户不存在";
    
    public static final int USER_ALREADY_EXISTS = 1002;
    public static final String USER_ALREADY_EXISTS_MSG = "用户已存在";
    
    public static final int INVALID_PASSWORD = 1003;
    public static final String INVALID_PASSWORD_MSG = "密码错误";
    
    public static final int USER_DISABLED = 1004;
    public static final String USER_DISABLED_MSG = "用户已被禁用";
    
    public static final int EMAIL_ALREADY_EXISTS = 1005;
    public static final String EMAIL_ALREADY_EXISTS_MSG = "邮箱已被注册";
    
    public static final int PHONE_ALREADY_EXISTS = 1006;
    public static final String PHONE_ALREADY_EXISTS_MSG = "手机号已被注册";
    
    public static final int INVALID_VERIFICATION_CODE = 1007;
    public static final String INVALID_VERIFICATION_CODE_MSG = "验证码错误或已过期";

    // ========== 商品相关错误码 (2000-2999) ==========
    public static final int PRODUCT_NOT_FOUND = 2001;
    public static final String PRODUCT_NOT_FOUND_MSG = "商品不存在";
    
    public static final int PRODUCT_OUT_OF_STOCK = 2002;
    public static final String PRODUCT_OUT_OF_STOCK_MSG = "商品库存不足";
    
    public static final int PRODUCT_OFFLINE = 2003;
    public static final String PRODUCT_OFFLINE_MSG = "商品已下架";
    
    public static final int CATEGORY_NOT_FOUND = 2004;
    public static final String CATEGORY_NOT_FOUND_MSG = "商品分类不存在";
    
    public static final int BRAND_NOT_FOUND = 2005;
    public static final String BRAND_NOT_FOUND_MSG = "品牌不存在";

    // ========== 订单相关错误码 (3000-3999) ==========
    public static final int ORDER_NOT_FOUND = 3001;
    public static final String ORDER_NOT_FOUND_MSG = "订单不存在";
    
    public static final int ORDER_STATUS_ERROR = 3002;
    public static final String ORDER_STATUS_ERROR_MSG = "订单状态错误";
    
    public static final int ORDER_CANNOT_CANCEL = 3003;
    public static final String ORDER_CANNOT_CANCEL_MSG = "订单无法取消";
    
    public static final int ORDER_ALREADY_PAID = 3004;
    public static final String ORDER_ALREADY_PAID_MSG = "订单已支付";
    
    public static final int CART_EMPTY = 3005;
    public static final String CART_EMPTY_MSG = "购物车为空";
    
    public static final int ADDRESS_NOT_FOUND = 3006;
    public static final String ADDRESS_NOT_FOUND_MSG = "收货地址不存在";

    // ========== 支付相关错误码 (4000-4999) ==========
    public static final int PAYMENT_FAILED = 4001;
    public static final String PAYMENT_FAILED_MSG = "支付失败";
    
    public static final int PAYMENT_TIMEOUT = 4002;
    public static final String PAYMENT_TIMEOUT_MSG = "支付超时";
    
    public static final int INSUFFICIENT_BALANCE = 4003;
    public static final String INSUFFICIENT_BALANCE_MSG = "余额不足";
    
    public static final int INVALID_PAYMENT_METHOD = 4004;
    public static final String INVALID_PAYMENT_METHOD_MSG = "不支持的支付方式";
    
    public static final int REFUND_FAILED = 4005;
    public static final String REFUND_FAILED_MSG = "退款失败";

    // ========== 系统相关错误码 (5000-5999) ==========
    public static final int SYSTEM_BUSY = 5001;
    public static final String SYSTEM_BUSY_MSG = "系统繁忙，请稍后重试";
    
    public static final int RATE_LIMIT_EXCEEDED = 5002;
    public static final String RATE_LIMIT_EXCEEDED_MSG = "请求过于频繁，请稍后重试";
    
    public static final int FILE_UPLOAD_FAILED = 5003;
    public static final String FILE_UPLOAD_FAILED_MSG = "文件上传失败";
    
    public static final int DATABASE_ERROR = 5004;
    public static final String DATABASE_ERROR_MSG = "数据库操作失败";

    // ========== 权限相关错误码 (6000-6999) ==========
    public static final int TOKEN_EXPIRED = 6001;
    public static final String TOKEN_EXPIRED_MSG = "登录已过期，请重新登录";
    
    public static final int INVALID_TOKEN = 6002;
    public static final String INVALID_TOKEN_MSG = "无效的访问令牌";
    
    public static final int PERMISSION_DENIED = 6003;
    public static final String PERMISSION_DENIED_MSG = "权限不足";

    // ========== 业务逻辑错误码 (7000-7999) ==========
    public static final int COUPON_NOT_FOUND = 7001;
    public static final String COUPON_NOT_FOUND_MSG = "优惠券不存在";
    
    public static final int COUPON_EXPIRED = 7002;
    public static final String COUPON_EXPIRED_MSG = "优惠券已过期";
    
    public static final int COUPON_USED = 7003;
    public static final String COUPON_USED_MSG = "优惠券已使用";
    
    public static final int INSUFFICIENT_POINTS = 7004;
    public static final String INSUFFICIENT_POINTS_MSG = "积分不足";
    
    public static final int COMMENT_NOT_FOUND = 7005;
    public static final String COMMENT_NOT_FOUND_MSG = "评论不存在";
}
