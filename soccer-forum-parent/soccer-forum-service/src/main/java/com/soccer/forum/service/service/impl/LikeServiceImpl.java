package com.soccer.forum.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.soccer.forum.domain.entity.Comment;
import com.soccer.forum.domain.entity.Like;
import com.soccer.forum.domain.entity.Post;
import com.soccer.forum.service.mapper.CommentMapper;
import com.soccer.forum.service.mapper.LikeMapper;
import com.soccer.forum.service.mapper.PostMapper;
import com.soccer.forum.service.service.LikeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LikeServiceImpl implements LikeService {

    private final LikeMapper likeMapper;
    private final PostMapper postMapper;
    private final CommentMapper commentMapper;

    public LikeServiceImpl(LikeMapper likeMapper, PostMapper postMapper, CommentMapper commentMapper) {
        this.likeMapper = likeMapper;
        this.postMapper = postMapper;
        this.commentMapper = commentMapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean toggleLike(Long targetId, Integer targetType, Long userId) {
        // 1. Check if like exists
        LambdaQueryWrapper<Like> query = new LambdaQueryWrapper<>();
        query.eq(Like::getTargetId, targetId)
             .eq(Like::getTargetType, targetType)
             .eq(Like::getUserId, userId);
        
        Like existingLike = likeMapper.selectOne(query);
        boolean isLiked;

        if (existingLike != null) {
            // Cancel like
            likeMapper.deleteById(existingLike.getId());
            updateLikesCount(targetId, targetType, -1);
            isLiked = false;
        } else {
            // Add like
            Like newLike = new Like();
            newLike.setTargetId(targetId);
            newLike.setTargetType(targetType);
            newLike.setUserId(userId);
            likeMapper.insert(newLike);
            updateLikesCount(targetId, targetType, 1);
            isLiked = true;
        }
        
        return isLiked;
    }

    private void updateLikesCount(Long targetId, Integer targetType, int delta) {
        String sql = "likes = likes + " + delta;
        if (targetType == 1) { // Post
            UpdateWrapper<Post> update = new UpdateWrapper<>();
            update.setSql(sql).eq("id", targetId);
            postMapper.update(null, update);
        } else if (targetType == 2) { // Comment
            UpdateWrapper<Comment> update = new UpdateWrapper<>();
            update.setSql(sql).eq("id", targetId);
            commentMapper.update(null, update);
        }
    }
}
