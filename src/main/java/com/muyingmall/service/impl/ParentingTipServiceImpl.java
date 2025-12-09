package com.muyingmall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.muyingmall.entity.ParentingTip;
import com.muyingmall.entity.ParentingTipCategory;
import com.muyingmall.entity.ParentingTipComment;
import com.muyingmall.mapper.ParentingTipCategoryMapper;
import com.muyingmall.mapper.ParentingTipCommentMapper;
import com.muyingmall.mapper.ParentingTipMapper;
import com.muyingmall.service.ParentingTipService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ParentingTipServiceImpl extends ServiceImpl<ParentingTipMapper, ParentingTip> implements ParentingTipService {

    private final ParentingTipCategoryMapper categoryMapper;
    private final ParentingTipCommentMapper commentMapper;

    @Override
    public IPage<ParentingTip> getPage(Integer page, Integer pageSize, Integer categoryId, String keyword) {
        LambdaQueryWrapper<ParentingTip> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ParentingTip::getStatus, 1);
        if (categoryId != null) {
            wrapper.eq(ParentingTip::getCategoryId, categoryId);
        }
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(ParentingTip::getTitle, keyword)
                    .or().like(ParentingTip::getSummary, keyword));
        }
        wrapper.orderByDesc(ParentingTip::getIsHot).orderByDesc(ParentingTip::getPublishTime);
        return this.page(new Page<>(page, pageSize), wrapper);
    }

    @Override
    public List<ParentingTip> getHotTips(Integer limit) {
        LambdaQueryWrapper<ParentingTip> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ParentingTip::getStatus, 1)
               .eq(ParentingTip::getIsHot, 1)
               .orderByDesc(ParentingTip::getViewCount)
               .last("LIMIT " + limit);
        return this.list(wrapper);
    }

    @Override
    public List<ParentingTipCategory> getCategories() {
        LambdaQueryWrapper<ParentingTipCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ParentingTipCategory::getStatus, 1).orderByAsc(ParentingTipCategory::getSort);
        return categoryMapper.selectList(wrapper);
    }

    @Override
    public ParentingTip getDetail(Long id) {
        return this.getById(id);
    }

    @Override
    public void increaseViewCount(Long id) {
        baseMapper.increaseViewCount(id);
    }

    @Override
    public List<ParentingTip> getRelatedTips(Integer categoryId, Long excludeId, Integer limit) {
        LambdaQueryWrapper<ParentingTip> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ParentingTip::getStatus, 1)
               .eq(ParentingTip::getCategoryId, categoryId)
               .ne(ParentingTip::getId, excludeId)
               .orderByDesc(ParentingTip::getViewCount)
               .last("LIMIT " + limit);
        return this.list(wrapper);
    }

    @Override
    public IPage<ParentingTipComment> getComments(Long tipId, Integer page, Integer pageSize) {
        return commentMapper.selectPageWithUser(new Page<>(page, pageSize), tipId);
    }

    @Override
    public ParentingTipComment addComment(Long tipId, Integer userId, String content) {
        ParentingTipComment comment = new ParentingTipComment();
        comment.setTipId(tipId);
        comment.setUserId(userId);
        comment.setContent(content);
        comment.setStatus(1);
        comment.setCreateTime(LocalDateTime.now());
        commentMapper.insert(comment);
        
        // 更新评论数
        ParentingTip tip = this.getById(tipId);
        if (tip != null) {
            tip.setCommentCount(tip.getCommentCount() + 1);
            this.updateById(tip);
        }
        return comment;
    }
}
