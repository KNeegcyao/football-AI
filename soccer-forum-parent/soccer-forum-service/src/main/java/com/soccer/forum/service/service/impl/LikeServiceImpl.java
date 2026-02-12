package com.soccer.forum.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.soccer.forum.common.enums.ServiceErrorCode;
import com.soccer.forum.common.exception.ServiceException;
import com.soccer.forum.domain.entity.Comment;
import com.soccer.forum.domain.entity.Like;
import com.soccer.forum.domain.entity.Post;
import com.soccer.forum.service.mapper.CommentMapper;
import com.soccer.forum.service.mapper.LikeMapper;
import com.soccer.forum.service.mapper.PostMapper;
import com.soccer.forum.service.service.LikeService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class LikeServiceImpl implements LikeService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final LikeMapper likeMapper;
    private final PostMapper postMapper;
    private final CommentMapper commentMapper;
    private final com.soccer.forum.service.service.NotificationService notificationService;

    public LikeServiceImpl(RedisTemplate<String, Object> redisTemplate, LikeMapper likeMapper, 
                           PostMapper postMapper, CommentMapper commentMapper,
                           com.soccer.forum.service.service.NotificationService notificationService) {
        this.redisTemplate = redisTemplate;
        this.likeMapper = likeMapper;
        this.postMapper = postMapper;
        this.commentMapper = commentMapper;
        this.notificationService = notificationService;
    }

    @Override
    @org.springframework.transaction.annotation.Transactional(rollbackFor = Exception.class)
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
}
