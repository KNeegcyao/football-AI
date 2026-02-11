package com.soccer.forum.service.job;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.soccer.forum.domain.entity.Comment;
import com.soccer.forum.domain.entity.Post;
import com.soccer.forum.service.mapper.CommentMapper;
import com.soccer.forum.service.mapper.PostMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * 点赞数据同步任务
 * <p>
 * 定时将 Redis 中的点赞数据同步到 MySQL 数据库。
 * </p>
 */
@Component
public class LikeSyncJob {

    private static final Logger log = LoggerFactory.getLogger(LikeSyncJob.class);

    private final RedisTemplate<String, Object> redisTemplate;
    private final PostMapper postMapper;
    private final CommentMapper commentMapper;

    public LikeSyncJob(RedisTemplate<String, Object> redisTemplate, PostMapper postMapper, CommentMapper commentMapper) {
        this.redisTemplate = redisTemplate;
        this.postMapper = postMapper;
        this.commentMapper = commentMapper;
    }

    /**
     * 同步帖子点赞数
     * 每 5 分钟执行一次
     */
    @Scheduled(fixedRate = 300000)
    public void syncPostLikes() {
        log.info("开始同步帖子点赞数据...");
        String dirtyKey = "like:dirty:posts";
        Set<Object> dirtyIds = redisTemplate.opsForSet().members(dirtyKey);

        if (dirtyIds == null || dirtyIds.isEmpty()) {
            log.info("没有需要同步的帖子点赞数据");
            return;
        }

        for (Object idObj : dirtyIds) {
            Long postId = Long.valueOf(idObj.toString());
            String countKey = "like:post:" + postId + ":count";
            Object countObj = redisTemplate.opsForValue().get(countKey);

            if (countObj != null) {
                Integer count = Integer.valueOf(countObj.toString());
                
                // 更新数据库
                UpdateWrapper<Post> update = new UpdateWrapper<>();
                update.set("likes", count).eq("id", postId);
                postMapper.update(null, update);
                
                log.debug("同步帖子点赞: id={}, count={}", postId, count);
                
                // 从脏集合中移除
                redisTemplate.opsForSet().remove(dirtyKey, idObj);
            }
        }
        log.info("帖子点赞数据同步完成");
    }

    /**
     * 同步评论点赞数
     * 每 5 分钟执行一次
     */
    @Scheduled(fixedRate = 300000)
    public void syncCommentLikes() {
        log.info("开始同步评论点赞数据...");
        String dirtyKey = "like:dirty:comments";
        Set<Object> dirtyIds = redisTemplate.opsForSet().members(dirtyKey);

        if (dirtyIds == null || dirtyIds.isEmpty()) {
            log.info("没有需要同步的评论点赞数据");
            return;
        }

        for (Object idObj : dirtyIds) {
            Long commentId = Long.valueOf(idObj.toString());
            String countKey = "like:comment:" + commentId + ":count";
            Object countObj = redisTemplate.opsForValue().get(countKey);

            if (countObj != null) {
                Integer count = Integer.valueOf(countObj.toString());
                
                // 更新数据库
                UpdateWrapper<Comment> update = new UpdateWrapper<>();
                update.set("likes", count).eq("id", commentId);
                commentMapper.update(null, update);
                
                log.debug("同步评论点赞: id={}, count={}", commentId, count);
                
                // 从脏集合中移除
                redisTemplate.opsForSet().remove(dirtyKey, idObj);
            }
        }
        log.info("评论点赞数据同步完成");
    }
}
