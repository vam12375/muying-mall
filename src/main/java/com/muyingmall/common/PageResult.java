package com.muyingmall.common;

import lombok.Data;

import java.util.List;

/**
 * 分页结果类
 *
 * @param <T> 数据类型
 */
@Data
public class PageResult<T> {
    /**
     * 总记录数
     */
    private long total;

    /**
     * 当前页数据
     */
    private List<T> list;

    /**
     * 当前页码
     */
    private int pageNum;

    /**
     * 每页大小
     */
    private int pageSize;

    /**
     * 总页数
     */
    private int pages;

    public PageResult() {
    }

    public PageResult(long total, List<T> list, int pageNum, int pageSize) {
        this.total = total;
        this.list = list;
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.pages = (int) Math.ceil((double) total / pageSize);
    }

    /**
     * 创建空的分页结果
     *
     * @param <T> 数据类型
     * @return 空的分页结果
     */
    public static <T> PageResult<T> empty() {
        return new PageResult<>(0, List.of(), 1, 10);
    }
}