package com.muyingmall.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

/**
 * 带标签的评价数据传输对象
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CommentWithTagsDTO extends CommentDTO {

    private static final long serialVersionUID = 1L;

    /**
     * 评价标签列表
     */
    private List<CommentTagDTO> tags = new ArrayList<>();
}