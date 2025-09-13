package com.muyingmall.common.dto;

import lombok.Data;
import org.springframework.data.domain.Page;

import java.io.Serializable;
import java.util.List;

/**
 * 分页结果封装类
 * 解决Spring Data Page对象序列化问题
 */
@Data
public class PageResult<T> implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 数据列表
     */
    private List<T> content;
    
    /**
     * 当前页码（从0开始）
     */
    private int page;
    
    /**
     * 每页大小
     */
    private int size;
    
    /**
     * 总记录数
     */
    private long total;
    
    /**
     * 总页数
     */
    private int totalPages;
    
    /**
     * 是否为第一页
     */
    private boolean first;
    
    /**
     * 是否为最后一页
     */
    private boolean last;
    
    /**
     * 是否有下一页
     */
    private boolean hasNext;
    
    /**
     * 是否有上一页
     */
    private boolean hasPrevious;
    
    /**
     * 当前页的元素数量
     */
    private int numberOfElements;
    
    /**
     * 是否为空页
     */
    private boolean empty;
    
    public PageResult() {
    }
    
    public PageResult(List<T> content, int page, int size, long total) {
        this.content = content;
        this.page = page;
        this.size = size;
        this.total = total;
        this.totalPages = size == 0 ? 1 : (int) Math.ceil((double) total / (double) size);
        this.numberOfElements = content != null ? content.size() : 0;
        this.first = page == 0;
        this.last = page >= totalPages - 1;
        this.hasNext = page < totalPages - 1;
        this.hasPrevious = page > 0;
        this.empty = numberOfElements == 0;
    }
    
    /**
     * 从Spring Data Page对象转换
     */
    public static <T> PageResult<T> from(Page<T> page) {
        if (page == null) {
            return new PageResult<>();
        }
        
        PageResult<T> result = new PageResult<>();
        result.content = page.getContent();
        result.page = page.getNumber();
        result.size = page.getSize();
        result.total = page.getTotalElements();
        result.totalPages = page.getTotalPages();
        result.numberOfElements = page.getNumberOfElements();
        result.first = page.isFirst();
        result.last = page.isLast();
        result.hasNext = page.hasNext();
        result.hasPrevious = page.hasPrevious();
        result.empty = page.isEmpty();
        
        return result;
    }
    
    /**
     * 创建空的分页结果
     */
    public static <T> PageResult<T> empty() {
        return new PageResult<>();
    }
    
    /**
     * 创建空的分页结果，指定页码和大小
     */
    public static <T> PageResult<T> empty(int page, int size) {
        return new PageResult<>(List.of(), page, size, 0);
    }
}
