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
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final com.soccer.forum.service.service.NotificationService notificationService;

    public LikeServiceImpl(RedisTemplate<String, Object> redisTemplate, LikeMapper likeMapper, 
                           PostMapper postMapper, CommentMapper commentMapper, UserMapper userMapper,
                           com.soccer.forum.service.service.NotificationService notificationService) {
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
        String typeStr = (targetType == 1) ? "post" : "comment";
        String userSetKey = "like:" + typeStr + ":" + targetId + ":users";
        String countKey = "like:" + typeStr + ":" + targetId + ":count";
        String dirtyKey = "like:dirty:" + typeStr + "s";

        // 获取目标所有者ID以便发送通知
        Long targetOwnerId = null;
        String titleOrContent = "";
        if (targetType == 1) {
            Post post = postMapper.selectById(targetId);
            if (post == null) {
                throw new ServiceException(ServiceErrorCode.POST_NOT_FOUND);
            }
            targetOwnerId = post.getUserId();
            titleOrContent = post.getTitle();
        } else {
            Comment comment = commentMapper.selectById(targetId);
            if (comment == null) {
                throw new ServiceException(ServiceErrorCode.COMMENT_NOT_FOUND);
            }
            targetOwnerId = comment.getUserId();
            titleOrContent = comment.getContent();
        }

        // ... existing logic for count initialization ...
        if (Boolean.FALSE.equals(redisTemplate.hasKey(countKey))) {
            Integer dbCount = 0;
            if (targetType == 1) {
                Post post = postMapper.selectById(targetId);
                if (post != null) dbCount = post.getLikes();
            } else {
                Comment comment = commentMapper.selectById(targetId);
                if (comment != null) dbCount = comment.getLikes();
            }
            redisTemplate.opsForValue().set(countKey, dbCount, 1, TimeUnit.DAYS);
        }

        boolean isMember = Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(userSetKey, userId));
        if (!redisTemplate.hasKey(userSetKey)) {
             LambdaQueryWrapper<Like> query = new LambdaQueryWrapper<>();
             query.eq(Like::getTargetId, targetId)
                  .eq(Like::getTargetType, targetType)
                  .eq(Like::getUserId, userId);
             if (likeMapper.exists(query)) {
                 isMember = true;
                 redisTemplate.opsForSet().add(userSetKey, userId);
                 redisTemplate.expire(userSetKey, 1, TimeUnit.DAYS);
             }
        }

        if (isMember) {
            // Unlike logic
            redisTemplate.opsForSet().remove(userSetKey, userId);
            redisTemplate.opsForValue().decrement(countKey);
            redisTemplate.opsForSet().add(dirtyKey, targetId);
            
            LambdaQueryWrapper<Like> deleteQuery = new LambdaQueryWrapper<>();
            deleteQuery.eq(Like::getTargetId, targetId)
                       .eq(Like::getTargetType, targetType)
                       .eq(Like::getUserId, userId);
            likeMapper.delete(deleteQuery);
            
            return false;
        } else {
            // Like logic
            redisTemplate.opsForSet().add(userSetKey, userId);
            redisTemplate.opsForValue().increment(countKey);
            redisTemplate.opsForSet().add(dirtyKey, targetId);
            redisTemplate.expire(userSetKey, 1, TimeUnit.DAYS);
            
            Like newLike = new Like();
            newLike.setTargetId(targetId);
            newLike.setTargetType(targetType);
            newLike.setUserId(userId);
            likeMapper.insert(newLike);

            // 发送点赞通知
            if (targetOwnerId != null) {
                notificationService.sendNotification(targetOwnerId, userId, targetType == 1 ? 1 : 2, targetId, titleOrContent);
            }
            
            return true;
        }
    }

    @Override
    public boolean isLiked(Long targetId, Integer targetType, Long userId) {
        if (userId == null) {
            return false;
        }
        String typeStr = (targetType == 1) ? "post" : "comment";
        String userSetKey = "like:" + typeStr + ":" + targetId + ":users";
        
        // 优先查 Redis
        if (Boolean.TRUE.equals(redisTemplate.hasKey(userSetKey))) {
            return Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(userSetKey, userId));
        }
        
        // Redis 未命中查数据库
        LambdaQueryWrapper<Like> query = new LambdaQueryWrapper<>();
        query.eq(Like::getTargetId, targetId)
             .eq(Like::getTargetType, targetType)
             .eq(Like::getUserId, userId);
        boolean exists = likeMapper.exists(query);
        
        // 回填 Redis (仅当 key 存在时添加，避免缓存击穿问题，或者这里可以不回填，等待 toggle 时处理)
        // 简单处理：如果查到了，说明有点赞
        return exists;
    }

    @Override
    public List<UserSimpleResp> getPostLikers(Long postId, int limit) {
        // Query database for latest likes
        LambdaQueryWrapper<Like> query = new LambdaQueryWrapper<>();
        query.eq(Like::getTargetId, postId)
             .eq(Like::getTargetType, 1) // 1 for post
             .orderByDesc(Like::getCreatedAt)
             .last("LIMIT " + limit);
        
        List<Like> likes = likeMapper.selectList(query);
        if (likes.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> userIds = likes.stream().map(Like::getUserId).collect(Collectors.toList());
        List<User> users = userMapper.selectBatchIds(userIds);
        
        // Map to DTO
        return users.stream()
                .map(u -> new UserSimpleResp(u.getId(), u.getNickname(), u.getAvatar()))
                .collect(Collectors.toList());
    }
}
