package com.soccer.forum.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.soccer.forum.domain.entity.Post;
import com.soccer.forum.service.mapper.PostMapper;
import com.soccer.forum.service.model.dto.PostCreateReq;
import com.soccer.forum.service.model.dto.PostPageReq;
import com.soccer.forum.service.service.PostService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

/**
 * 帖子服务实现类
 * <p>
 * 实现帖子管理的具体业务逻辑。
 * </p>
 *
 * @author Soccer Forum Dev Team
 * @version 1.0
 */
@Service
public class PostServiceImpl implements PostService {

    private static final Logger log = LoggerFactory.getLogger(PostServiceImpl.class);

    private final PostMapper postMapper;
    private final org.springframework.data.redis.core.StringRedisTemplate redisTemplate;

    public PostServiceImpl(PostMapper postMapper, org.springframework.data.redis.core.StringRedisTemplate redisTemplate) {
        this.postMapper = postMapper;
        this.redisTemplate = redisTemplate;
    }

    /**
     * 创建帖子实现
     * <p>
     * 初始化帖子状态（正常），设置创建时间，持久化到数据库。
     * </p>
     *
     * @param req 帖子创建请求参数
     * @param userId 发布用户的 ID
     * @return 新创建帖子的 ID
     */
    @Override
    public Long createPost(PostCreateReq req, Long userId) {
        log.debug("创建帖子: 用户ID={}, 标题={}", userId, req.getTitle());
        Post post = new Post();
        post.setTitle(req.getTitle());
        post.setContent(req.getContent());
        post.setUserId(userId);
        post.setViews(0);
        post.setLikes(0);
        post.setStatus(1); // 1: Normal
        post.setCreatedAt(LocalDateTime.now());
        post.setUpdatedAt(LocalDateTime.now());
        
        postMapper.insert(post);
        log.info("帖子创建成功: id={}", post.getId());
        return post.getId();
    }

    /**
     * 获取帖子详情实现
     * <p>
     * 查询帖子信息，若存在则同步增加浏览量。
     * 使用 Redis 计数，每 10 次访问同步一次数据库。
     * </p>
     *
     * @param id 帖子 ID
     * @return 帖子实体对象
     */
    @Override
    public Post getPostById(Long id) {
        log.debug("获取帖子详情: id={}", id);
        Post post = postMapper.selectById(id);
        if (post != null) {
            // 使用 Redis 增加浏览量
            String key = "post:views:" + id;
            Long views = redisTemplate.opsForValue().increment(key);
            
            // 每 10 次访问同步一次数据库，减轻数据库压力
            if (views != null && views % 10 == 0) {
                post.setViews(post.getViews() + 10);
                postMapper.updateById(post);
                log.debug("同步帖子浏览量到数据库: id={}, views={}", id, post.getViews());
            } else {
                // 仅在内存对象中增加，用于返回给前端展示实时数据
                // 注意：这里只是临时显示，数据库中可能滞后
                post.setViews(post.getViews() + (views == null ? 0 : views.intValue() % 10));
            }
        }
        return post;
    }

    /**
     * 分页查询帖子列表实现
     * <p>
     * 仅查询状态正常的帖子，支持按标题模糊搜索，按创建时间倒序排列。
     * </p>
     *
     * @param req 分页及查询参数
     * @return 帖子分页对象
     */
    @Override
    public Page<Post> getPostPage(PostPageReq req) {
        log.debug("分页查询帖子: 页码={}, 大小={}", req.getPage(), req.getSize());
        Page<Post> page = new Page<>(req.getPage(), req.getSize());
        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<>();
        
        // Only show normal status posts
        wrapper.eq(Post::getStatus, 1);
        
        if (StringUtils.hasText(req.getKeyword())) {
            wrapper.like(Post::getTitle, req.getKeyword());
        }
        
        wrapper.orderByDesc(Post::getCreatedAt);
        return postMapper.selectPage(page, wrapper);
    }

    /**
     * 删除帖子实现
     * <p>
     * 校验操作权限（仅作者可删），执行逻辑删除（状态置为0）。
     * </p>
     *
     * @param id 帖子 ID
     * @param userId 操作用户的 ID
     * @throws RuntimeException 当帖子不存在或无权删除时抛出
     */
    @Override
    public void deletePost(Long id, Long userId) {
        log.debug("删除帖子: 帖子ID={}, 用户ID={}", id, userId);
        Post post = postMapper.selectById(id);
        if (post != null) {
            if (post.getUserId().equals(userId)) {
                post.setStatus(0); // 0: Deleted
                postMapper.updateById(post);
                log.info("帖子删除成功(逻辑删除): id={}", id);
            } else {
                log.warn("删除失败: 用户 {} 尝试删除非本人帖子 {}", userId, id);
                throw new RuntimeException("权限不足: 只能删除自己的帖子");
            }
        } else {
            log.warn("删除失败: 未找到帖子 id={}", id);
            throw new RuntimeException("未找到帖子");
        }
    }
}
