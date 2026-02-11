package com.soccer.forum.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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

    public LikeServiceImpl(RedisTemplate<String, Object> redisTemplate, LikeMapper likeMapper, PostMapper postMapper, CommentMapper commentMapper) {
        this.redisTemplate = redisTemplate;
        this.likeMapper = likeMapper;
        this.postMapper = postMapper;
        this.commentMapper = commentMapper;
    }

    @Override
    public boolean toggleLike(Long targetId, Integer targetType, Long userId) {
        String typeStr = (targetType == 1) ? "post" : "comment";
        String userSetKey = "like:" + typeStr + ":" + targetId + ":users";
        String countKey = "like:" + typeStr + ":" + targetId + ":count";
        String dirtyKey = "like:dirty:" + typeStr + "s";

        // 1. Initialize Count if missing
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

        // 2. Check if user liked (Check Redis first, if missing check DB)
        // Note: For simplicity and performance, we assume if Redis Set is empty but Count > 0, 
        // we might need to load from DB. But loading all users is heavy.
        // Strategy: Use Bloom Filter or just check DB if Redis key missing.
        // For MVP: We check DB if Redis Set key is missing.
        
        boolean isMember = Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(userSetKey, userId));
        
        // If Redis key doesn't exist, we can't be sure if user liked or not unless we check DB.
        // But checking DB for every request defeats Redis purpose.
        // Solution: We assume Redis is the source of truth for "Session" or "Hot Data".
        // But to be safe, we can double check DB if Redis Set is missing (TTL expired).
        if (!redisTemplate.hasKey(userSetKey)) {
             LambdaQueryWrapper<Like> query = new LambdaQueryWrapper<>();
             query.eq(Like::getTargetId, targetId)
                  .eq(Like::getTargetType, targetType)
                  .eq(Like::getUserId, userId);
             if (likeMapper.exists(query)) {
                 isMember = true;
                 // Restore to Redis
                 redisTemplate.opsForSet().add(userSetKey, userId);
                 redisTemplate.expire(userSetKey, 1, TimeUnit.DAYS);
             }
        }

        if (isMember) {
            // Unlike
            redisTemplate.opsForSet().remove(userSetKey, userId);
            redisTemplate.opsForValue().decrement(countKey);
            redisTemplate.opsForSet().add(dirtyKey, targetId);
            
            // Async delete from DB (Handled by Job? No, Job only syncs counts)
            // Job syncs COUNTS. But "Like" record in DB (post_likes table) needs to be deleted too.
            // If we rely on Job to sync counts, `post_likes` table will be out of sync.
            // Requirement: "Final Consistency".
            // We should add to a "Dirty Like Operation" queue?
            // Or just update DB synchronously for the relationship, but Redis for the count?
            // "High Performance" usually implies Count is the bottleneck. Relationship insert/delete is less frequent per post than reads.
            // But for a hot post, insert/delete is also high freq.
            
            // Let's stick to the Plan: "Redis for high freq... Sync to MySQL".
            // This implies we sync BOTH counts AND relationships.
            // But syncing relationships is complex (diffing sets).
            
            // Compromise for MVP: 
            // 1. Update Redis Count & Set.
            // 2. Sync Count to DB (via Job).
            // 3. Update DB Relationship asynchronously (fire and forget, or @Async).
            // Since I don't have @Async set up with thread pool explicitly, I'll do it synchronously for the relationship (it's PK lookup delete, fast enough), 
            // BUT keep the COUNT update in Redis only (to avoid locking the Post row).
            
            LambdaQueryWrapper<Like> deleteQuery = new LambdaQueryWrapper<>();
            deleteQuery.eq(Like::getTargetId, targetId)
                       .eq(Like::getTargetType, targetType)
                       .eq(Like::getUserId, userId);
            likeMapper.delete(deleteQuery);
            
            return false;
        } else {
            // Like
            redisTemplate.opsForSet().add(userSetKey, userId);
            redisTemplate.opsForValue().increment(countKey);
            redisTemplate.opsForSet().add(dirtyKey, targetId);
            redisTemplate.expire(userSetKey, 1, TimeUnit.DAYS);
            
            Like newLike = new Like();
            newLike.setTargetId(targetId);
            newLike.setTargetType(targetType);
            newLike.setUserId(userId);
            likeMapper.insert(newLike);
            
            return true;
        }
    }
}
