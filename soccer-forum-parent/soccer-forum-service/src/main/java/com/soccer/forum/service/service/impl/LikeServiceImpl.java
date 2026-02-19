package com.soccer.forum.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.soccer.forum.common.enums.ServiceErrorCode;
import com.soccer.forum.common.exception.ServiceException;
import com.soccer.forum.domain.entity.Comment;
import com.soccer.forum.domain.entity.Like;
import com.soccer.forum.domain.entity.Post;
import com.soccer.forum.domain.entity.User;
import com.soccer.forum.service.mapper.CommentMapper;
import com.soccer.forum.service.mapper.LikeMapper;
import com.soccer.forum.service.mapper.PostMapper;
import com.soccer.forum.service.mapper.UserMapper;
import com.soccer.forum.service.model.dto.UserSimpleResp;
import com.soccer.forum.service.service.LikeService;
import com.soccer.forum.service.service.NotificationService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class LikeServiceImpl implements LikeService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final LikeMapper likeMapper;
    private final PostMapper postMapper;
    private final CommentMapper commentMapper;
    private final UserMapper userMapper;
    private final NotificationService notificationService;

    public LikeServiceImpl(RedisTemplate<String, Object> redisTemplate, LikeMapper likeMapper, 
                           PostMapper postMapper, CommentMapper commentMapper, UserMapper userMapper,
                           NotificationService notificationService) {
        this.redisTemplate = redisTemplate;
        this.likeMapper = likeMapper;
        this.postMapper = postMapper;
        this.commentMapper = commentMapper;
        this.userMapper = userMapper;
        this.notificationService = notificationService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean toggleLike(Long targetId, Integer targetType, Long userId) {
        // 直接数据库操作，移除 Redis
        
        LambdaQueryWrapper<Like> query = new LambdaQueryWrapper<>();
        query.eq(Like::getTargetId, targetId)
             .eq(Like::getTargetType, targetType)
             .eq(Like::getUserId, userId);
             
        Like existing = likeMapper.selectOne(query);
        
        boolean isLiked;
        if (existing != null) {
            // 取消点赞
            likeMapper.deleteById(existing.getId());
            isLiked = false;
        } else {
            // 点赞
            Like like = new Like();
            like.setTargetId(targetId);
            like.setTargetType(targetType);
            like.setUserId(userId);
            like.setCreatedAt(LocalDateTime.now());
            likeMapper.insert(like);
            isLiked = true;

            // 发送通知
            if (targetType == 1) {
                Post post = postMapper.selectById(targetId);
                if (post != null) {
                    notificationService.sendNotification(post.getUserId(), userId, 1, targetId, null);
                }
            } else {
                Comment comment = commentMapper.selectById(targetId);
                if (comment != null) {
                    notificationService.sendNotification(comment.getUserId(), userId, 2, targetId, null);
                }
            }
        }
        
        // 更新统计 (同步)
        if (targetType == 1) {
            Post post = postMapper.selectById(targetId);
            if (post != null) {
                post.setLikes(isLiked ? post.getLikes() + 1 : Math.max(0, post.getLikes() - 1));
                postMapper.updateById(post);
            }
        } else {
            Comment comment = commentMapper.selectById(targetId);
            if (comment != null) {
                comment.setLikes(isLiked ? comment.getLikes() + 1 : Math.max(0, comment.getLikes() - 1));
                commentMapper.updateById(comment);
            }
        }
        
        return isLiked;
    }

    // 简化其他方法，移除 Redis
    public Integer getLikeCount(Long targetId, Integer targetType) {
        if (targetType == 1) {
            Post post = postMapper.selectById(targetId);
            return post != null ? post.getLikes() : 0;
        } else {
            Comment comment = commentMapper.selectById(targetId);
            return comment != null ? comment.getLikes() : 0;
        }
    }

    public boolean isLiked(Long targetId, Integer targetType, Long userId) {
        LambdaQueryWrapper<Like> query = new LambdaQueryWrapper<>();
        query.eq(Like::getTargetId, targetId)
             .eq(Like::getTargetType, targetType)
             .eq(Like::getUserId, userId);
        return likeMapper.exists(query);
    }

    @Override
    public List<UserSimpleResp> getPostLikers(Long postId, int limit) {
        LambdaQueryWrapper<Like> query = new LambdaQueryWrapper<>();
        query.eq(Like::getTargetId, postId)
             .eq(Like::getTargetType, 1) // Post
             .orderByDesc(Like::getCreatedAt)
             .last("LIMIT " + limit);
        
        List<Like> likes = likeMapper.selectList(query);
        if (likes.isEmpty()) {
            return Collections.emptyList();
        }
        
        List<Long> userIds = likes.stream().map(Like::getUserId).collect(Collectors.toList());
        List<User> users = userMapper.selectBatchIds(userIds);
        
        return users.stream().map(user -> {
            UserSimpleResp resp = new UserSimpleResp();
            resp.setId(user.getId());
            resp.setNickname(user.getNickname());
            resp.setAvatar(user.getAvatar());
            return resp;
        }).collect(Collectors.toList());
    }
}
