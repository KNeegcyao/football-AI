package com.soccer.forum.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.soccer.forum.common.enums.ServiceErrorCode;
import com.soccer.forum.common.exception.ServiceException;
import com.soccer.forum.domain.entity.Post;
import com.soccer.forum.service.mapper.PostMapper;
import com.soccer.forum.service.model.dto.PostCreateReq;
import com.soccer.forum.service.model.dto.PostPageReq;
import com.soccer.forum.domain.entity.Team;
import com.soccer.forum.domain.entity.Topic;
import com.soccer.forum.domain.entity.User;
import com.soccer.forum.service.mapper.TeamMapper;
import com.soccer.forum.service.mapper.TopicMapper;
import com.soccer.forum.service.mapper.UserMapper;
import com.soccer.forum.service.model.dto.PostDetailResp;
import com.soccer.forum.service.service.LikeService;
import com.soccer.forum.service.service.PostService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

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
    private final UserMapper userMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private final LikeService likeService;
    private final TopicMapper topicMapper;
    private final TeamMapper teamMapper;

    public PostServiceImpl(PostMapper postMapper, UserMapper userMapper, RedisTemplate<String, Object> redisTemplate, LikeService likeService, TopicMapper topicMapper, TeamMapper teamMapper) {
        this.postMapper = postMapper;
        this.userMapper = userMapper;
        this.redisTemplate = redisTemplate;
        this.likeService = likeService;
        this.topicMapper = topicMapper;
        this.teamMapper = teamMapper;
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
    @Transactional(rollbackFor = Exception.class)
    public Long createPost(PostCreateReq req, Long userId) {
        log.debug("创建帖子: 用户ID={}, 标题={}", userId, req.getTitle());
        Post post = new Post();
        post.setTitle(req.getTitle());
        post.setContent(req.getContent());
        post.setImages(req.getImages());
        post.setCircleId(req.getCircleId());
        post.setTopicId(req.getTopicId());
        post.setUserId(userId);
        post.setViews(0);
        post.setLikes(0);
        post.setCommentCount(0);
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
        
        // 1. 查缓存
        String cacheKey = "post:detail:" + id;
        Object cachedPost = redisTemplate.opsForValue().get(cacheKey);
        if (cachedPost instanceof Post) {
            log.debug("帖子详情命中缓存: id={}", id);
            Post post = (Post) cachedPost;
            
            // 异步增加浏览量 (Redis)
            String viewKey = "post:views:" + id;
            redisTemplate.opsForValue().increment(viewKey);
            
            // 简单处理：返回时合并实时浏览量
            // 注意：这里没有从Redis读最新的view count覆盖缓存里的，
            // 而是为了性能直接返回缓存。浏览量更新逻辑在下面。
            return post;
        }

        // 2. 查数据库
        Post post = postMapper.selectById(id);
        if (post != null) {
            // 3. 写缓存 (TTL 10分钟)
            redisTemplate.opsForValue().set(cacheKey, post, 10, TimeUnit.MINUTES);
            
            // 使用 Redis 增加浏览量
            String viewKey = "post:views:" + id;
            Long views = redisTemplate.opsForValue().increment(viewKey);
            
            // 每 10 次访问同步一次数据库，减轻数据库压力
            if (views != null && views % 10 == 0) {
                post.setViews(post.getViews() + 10);
                postMapper.updateById(post);
                log.debug("同步帖子浏览量到数据库: id={}, views={}", id, post.getViews());
                
                // 更新数据库后，更新缓存
                redisTemplate.opsForValue().set(cacheKey, post, 10, TimeUnit.MINUTES);
            } else {
                // 仅在内存对象中增加，用于返回给前端展示实时数据
                post.setViews(post.getViews() + (views == null ? 0 : views.intValue() % 10));
            }
        } else {
            log.warn("获取帖子失败, 帖子不存在: id={}", id);
            throw new ServiceException(ServiceErrorCode.POST_NOT_FOUND);
        }
        return post;
    }

    /**
     * 根据 ID 获取帖子详情（包含用户信息）
     *
     * @param id 帖子 ID
     * @param userId 当前用户ID (可选)
     * @return 帖子详情对象
     */
    @Override
    public PostDetailResp getPostDetail(Long id, Long userId) {
        Post post = getPostById(id);
        
        // 获取用户信息
        User user = userMapper.selectById(post.getUserId());
        String nickname = "未知用户";
        String avatar = "";
        
        if (user != null) {
            nickname = user.getNickname();
            avatar = user.getAvatar();
        }
        
        PostDetailResp resp = PostDetailResp.fromPost(post, nickname, avatar);
        
        // 设置话题名称
        if (post.getTopicId() != null) {
            Topic topic = topicMapper.selectById(post.getTopicId());
            if (topic != null) {
                resp.setTopicName(topic.getTitle());
            }
        }
        
        // 设置圈子名称
        if (post.getCircleId() != null) {
            Team team = teamMapper.selectById(post.getCircleId());
            if (team != null) {
                resp.setCircleName(team.getName());
            }
        }
        
        // 设置点赞状态
        if (userId != null) {
            resp.setIsLiked(likeService.isLiked(id, 1, userId));
        } else {
            resp.setIsLiked(false);
        }
        
        // 获取最近点赞用户
        resp.setRecentLikes(likeService.getPostLikers(id, 3));

        return resp;
    }

    /**
     * 分页查询帖子列表实现
     * <p>
     * 仅查询状态正常的帖子，支持按标题模糊搜索，按创建时间倒序排列。
     * 并填充用户信息和点赞状态。
     * </p>
     *
     * @param req 分页及查询参数
     * @param currentUserId 当前登录用户ID（可为null）
     * @return 帖子分页对象
     */
    @Override
    public Page<PostDetailResp> getPostPage(PostPageReq req, Long currentUserId) {
        log.debug("分页查询帖子: 页码={}, 大小={}", req.getPage(), req.getSize());
        Page<Post> page = new Page<>(req.getPage(), req.getSize());
        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<>();
        
        // Only show normal status posts
        wrapper.eq(Post::getStatus, 1);
        
        if (req.getCircleId() != null) {
            wrapper.eq(Post::getCircleId, req.getCircleId());
        }
        
        if (req.getTopicId() != null) {
            wrapper.eq(Post::getTopicId, req.getTopicId());
        }
        
        if (StringUtils.hasText(req.getKeyword())) {
            wrapper.like(Post::getTitle, req.getKeyword());
        }
        
        wrapper.orderByDesc(Post::getCreatedAt);
        postMapper.selectPage(page, wrapper);

        // Map to PostDetailResp
        Page<PostDetailResp> respPage = new Page<>();
        BeanUtils.copyProperties(page, respPage, "records");

        if (page.getRecords().isEmpty()) {
            return respPage;
        }

        // Fetch User Info
        Set<Long> userIds = page.getRecords().stream().map(Post::getUserId).collect(Collectors.toSet());
        Map<Long, User> userMap = userIds.isEmpty() ? java.util.Collections.emptyMap() : userMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));
        
        // Fetch Topics
        Set<Long> topicIds = page.getRecords().stream()
            .map(Post::getTopicId)
            .filter(id -> id != null)
            .collect(Collectors.toSet());
        Map<Long, Topic> topicMap = topicIds.isEmpty() ? java.util.Collections.emptyMap() : 
            topicMapper.selectBatchIds(topicIds).stream()
                .collect(Collectors.toMap(Topic::getId, Function.identity()));
                
        // Fetch Teams (Circles)
        Set<Long> circleIds = page.getRecords().stream()
            .map(Post::getCircleId)
            .filter(id -> id != null)
            .collect(Collectors.toSet());
        Map<Long, Team> teamMap = circleIds.isEmpty() ? java.util.Collections.emptyMap() : 
            teamMapper.selectBatchIds(circleIds).stream()
                .collect(Collectors.toMap(Team::getId, Function.identity()));

        // Map records
        List<PostDetailResp> resps = page.getRecords().stream().map(post -> {
            User user = userMap.get(post.getUserId());
            String nickname = (user != null) ? user.getNickname() : "未知用户";
            String avatar = (user != null) ? user.getAvatar() : "";
            
            PostDetailResp resp = PostDetailResp.fromPost(post, nickname, avatar);
            
            // Set Topic Name
            if (post.getTopicId() != null && topicMap.containsKey(post.getTopicId())) {
                resp.setTopicName(topicMap.get(post.getTopicId()).getTitle());
            }
            
            // Set Circle Name
            if (post.getCircleId() != null && teamMap.containsKey(post.getCircleId())) {
                resp.setCircleName(teamMap.get(post.getCircleId()).getName());
            }
            
            // Set Like Status
            if (currentUserId != null) {
                resp.setIsLiked(likeService.isLiked(post.getId(), 1, currentUserId));
            } else {
                resp.setIsLiked(false);
            }
            return resp;
        }).collect(Collectors.toList());

        respPage.setRecords(resps);
        return respPage;
    }

    /**
     * 更新帖子实现
     *
     * @param id 帖子 ID
     * @param req 更新请求参数
     * @param userId 操作用户 ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePost(Long id, PostCreateReq req, Long userId) {
        log.debug("更新帖子: id={}, 用户ID={}", id, userId);
        Post post = postMapper.selectById(id);
        if (post == null) {
            throw new ServiceException(ServiceErrorCode.POST_NOT_FOUND);
        }
        if (!post.getUserId().equals(userId)) {
            throw new ServiceException(ServiceErrorCode.FORBIDDEN);
        }

        post.setTitle(req.getTitle());
        post.setContent(req.getContent());
        post.setUpdatedAt(LocalDateTime.now());
        
        postMapper.updateById(post);
        
        // 清除缓存
        String cacheKey = "post:detail:" + id;
        redisTemplate.delete(cacheKey);
        log.debug("更新帖子并清除缓存: id={}", id);
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
    @Transactional(rollbackFor = Exception.class)
    public void deletePost(Long id, Long userId) {
        log.debug("删除帖子: 帖子ID={}, 用户ID={}", id, userId);
        Post post = postMapper.selectById(id);
        if (post == null) {
            throw new ServiceException(ServiceErrorCode.POST_NOT_FOUND);
        }
        if (!post.getUserId().equals(userId)) {
            log.warn("删除失败: 用户 {} 尝试删除非本人帖子 {}", userId, id);
            throw new ServiceException(ServiceErrorCode.FORBIDDEN);
        }
        
        post.setStatus(0); // 0: Deleted
        post.setUpdatedAt(LocalDateTime.now());
        postMapper.updateById(post);
        
        // 清除缓存
        String cacheKey = "post:detail:" + id;
        redisTemplate.delete(cacheKey);
        log.info("帖子删除成功: id={}", id);
    }
}
