package com.muyingmall.service;

import java.util.Map;

/**
 * 搜索索引管理服务接口
 */
public interface SearchIndexService {

    /**
     * 创建商品索引
     * @return 创建结果
     */
    boolean createProductIndex();

    /**
     * 删除商品索引
     * @return 删除结果
     */
    boolean deleteProductIndex();

    /**
     * 检查索引是否存在
     * @param indexName 索引名称
     * @return 是否存在
     */
    boolean indexExists(String indexName);

    /**
     * 获取索引信息
     * @param indexName 索引名称
     * @return 索引信息
     */
    Map<String, Object> getIndexInfo(String indexName);

    /**
     * 获取索引统计信息
     * @param indexName 索引名称
     * @return 统计信息
     */
    Map<String, Object> getIndexStats(String indexName);

    /**
     * 刷新索引
     * @param indexName 索引名称
     * @return 刷新结果
     */
    boolean refreshIndex(String indexName);

    /**
     * 优化索引
     * @param indexName 索引名称
     * @return 优化结果
     */
    boolean optimizeIndex(String indexName);

    /**
     * 设置索引别名
     * @param indexName 索引名称
     * @param aliasName 别名
     * @return 设置结果
     */
    boolean setIndexAlias(String indexName, String aliasName);

    /**
     * 删除索引别名
     * @param indexName 索引名称
     * @param aliasName 别名
     * @return 删除结果
     */
    boolean removeIndexAlias(String indexName, String aliasName);
}
