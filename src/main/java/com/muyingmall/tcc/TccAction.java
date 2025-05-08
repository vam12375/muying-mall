package com.muyingmall.tcc;

/**
 * TCC事务操作接口
 * 
 * @param <T> 业务参数类型
 * @param <R> 返回结果类型
 */
public interface TccAction<T, R> {

    /**
     * Try阶段，尝试执行业务
     * 完成所有业务检查，预留必须的业务资源
     *
     * @param params 业务参数
     * @return 业务结果
     */
    R tryAction(T params);

    /**
     * Confirm阶段，确认执行业务
     * 真正执行业务，不作任何业务检查
     * 只使用Try阶段预留的业务资源
     * 操作需要满足幂等性
     *
     * @param params 业务参数
     */
    void confirmAction(T params);

    /**
     * Cancel阶段，取消执行业务
     * 释放Try阶段预留的业务资源
     * 操作需要满足幂等性
     *
     * @param params 业务参数
     */
    void cancelAction(T params);
}