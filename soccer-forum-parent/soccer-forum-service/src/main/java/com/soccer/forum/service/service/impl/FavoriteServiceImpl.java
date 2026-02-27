package com.soccer.forum.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.soccer.forum.common.enums.ServiceErrorCode;
import com.soccer.forum.common.exception.ServiceException;
import com.soccer.forum.domain.entity.Favorite;
import com.soccer.forum.domain.entity.Post;
import com.soccer.forum.domain.entity.News;
import com.soccer.forum.domain.entity.Topic;
import com.soccer.forum.domain.entity.Team;
import com.soccer.forum.domain.entity.User;
import com.soccer.forum.service.mapper.FavoriteMapper;
import com.soccer.forum.service.mapper.PostMapper;
import com.soccer.forum.service.mapper.NewsMapper;
import com.soccer.forum.service.mapper.TopicMapper;
import com.soccer.forum.service.mapper.TeamMapper;
import com.soccer.forum.service.mapper.UserMapper;
import com.soccer.forum.service.model.dto.FavoriteToggleResp;
import com.soccer.forum.service.model.dto.PostDetailResp;
import com.soccer.forum.service.service.FavoriteService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.function.Function;
import org.springframework.beans.BeanUtils;

@Service
public class FavoriteServiceImpl implements FavoriteService {

    private static final Logger log = LoggerFactory.getLogger(FavoriteServiceImpl.class);

    private final FavoriteMapper favoriteMapper;
    private final PostMapper postMapper;
    private final NewsMapper newsMapper;
    private final UserMapper userMapper;
    private final TopicMapper topicMapper;
    private final TeamMapper teamMapper;

    public FavoriteServiceImpl(FavoriteMapper favoriteMapper, PostMapper postMapper, NewsMapper newsMapper, UserMapper userMapper, TopicMapper topicMapper, TeamMapper teamMapper) {
        this.favoriteMapper = favoriteMapper;
        this.postMapper = postMapper;
        this.newsMapper = newsMapper;
        this.userMapper = userMapper;
        this.topicMapper = topicMapper;
        this.teamMapper = teamMapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FavoriteToggleResp toggleFavorite(Long postId, Long newsId, Long playerId, Long userId) {
        if (postId != null) {
            Post post = postMapper.selectById(postId);
             if (post == null) {
                throw new ServiceException(ServiceErrorCode.POST_NOT_FOUND);
            }
            
            LambdaQueryWrapper<Favorite> query = new LambdaQueryWrapper<>();
            query.eq(Favorite::getPostId, postId)
                 .eq(Favorite::getUserId, userId);
            
            Favorite existing = favoriteMapper.selectOne(query);
            if (existing != null) {
                favoriteMapper.deleteById(existing.getId());
                // 同步更新帖子的点赞数（此处假设收藏也影响点赞数，或者以后可以区分）
                int currentLikes = (post.getLikes() == null ? 0 : post.getLikes());
                post.setLikes(Math.max(0, currentLikes - 1));
                postMapper.updateById(post);
                return new FavoriteToggleResp(false, post.getLikes());
            } else {
                Favorite favorite = new Favorite();
                favorite.setPostId(postId);
                favorite.setUserId(userId);
                favorite.setCreatedAt(LocalDateTime.now());
                favoriteMapper.insert(favorite);
                // 同步更新帖子的点赞数
                int currentLikes = (post.getLikes() == null ? 0 : post.getLikes());
                post.setLikes(currentLikes + 1);
                postMapper.updateById(post);
                return new FavoriteToggleResp(true, post.getLikes());
            }
        } else if (newsId != null) {
            News news = newsMapper.selectById(newsId);
            if (news == null) {
                throw new ServiceException(ServiceErrorCode.DATA_NOT_FOUND);
            }
            
            LambdaQueryWrapper<Favorite> query = new LambdaQueryWrapper<>();
            query.eq(Favorite::getNewsId, newsId)
                 .eq(Favorite::getUserId, userId);
            
            Favorite existing = favoriteMapper.selectOne(query);
            if (existing != null) {
                favoriteMapper.deleteById(existing.getId());
                // 同步更新新闻表的收藏数
                // 获取最新真实收藏数并更新到 news 表
                LambdaQueryWrapper<Favorite> countQuery = new LambdaQueryWrapper<>();
                countQuery.eq(Favorite::getNewsId, newsId);
                int latestCount = favoriteMapper.selectCount(countQuery).intValue();
                
                log.info("取消收藏同步更新计数: 新闻ID={}, 新收藏数={}", newsId, latestCount);
                
                // 使用 LambdaUpdateWrapper 局部更新 collect_count，避免全量更新冲突
                LambdaUpdateWrapper<News> updateWrapper = new LambdaUpdateWrapper<>();
                updateWrapper.eq(News::getId, newsId)
                             .set(News::getCollectCount, latestCount);
                int rows = newsMapper.update(null, updateWrapper);
                
                log.info("newsMapper.update (LambdaUpdateWrapper) 返回受影响行数: {}", rows);
                return new FavoriteToggleResp(false, latestCount);
            } else {
                Favorite favorite = new Favorite();
                favorite.setNewsId(newsId);
                favorite.setUserId(userId);
                favorite.setCreatedAt(LocalDateTime.now());
                favoriteMapper.insert(favorite);
                // 同步更新新闻表的收藏数
                // 获取最新真实收藏数并更新到 news 表
                LambdaQueryWrapper<Favorite> countQuery = new LambdaQueryWrapper<>();
                countQuery.eq(Favorite::getNewsId, newsId);
                int latestCount = favoriteMapper.selectCount(countQuery).intValue();
                
                log.info("添加收藏同步更新计数: 新闻ID={}, 新收藏数={}", newsId, latestCount);
                
                // 使用 LambdaUpdateWrapper 局部更新 collect_count
                LambdaUpdateWrapper<News> updateWrapper = new LambdaUpdateWrapper<>();
                updateWrapper.eq(News::getId, newsId)
                             .set(News::getCollectCount, latestCount);
                int rows = newsMapper.update(null, updateWrapper);
                
                log.info("newsMapper.update (LambdaUpdateWrapper) 返回受影响行数: {}", rows);
                return new FavoriteToggleResp(true, latestCount);
            }
        } else if (playerId != null) {
            // 球员收藏逻辑
            log.info("执行球员收藏逻辑: playerId={}, userId={}", playerId, userId);
            LambdaQueryWrapper<Favorite> query = new LambdaQueryWrapper<>();
            query.eq(Favorite::getPlayerId, playerId)
                 .eq(Favorite::getUserId, userId);
            
            Favorite existing = favoriteMapper.selectOne(query);
            if (existing != null) {
                favoriteMapper.deleteById(existing.getId());
                
                LambdaQueryWrapper<Favorite> countQuery = new LambdaQueryWrapper<>();
                countQuery.eq(Favorite::getPlayerId, playerId);
                int latestCount = favoriteMapper.selectCount(countQuery).intValue();
                
                log.info("取消收藏球员成功: 球员ID={}, 最新收藏数={}", playerId, latestCount);
                return new FavoriteToggleResp(false, latestCount);
            } else {
                Favorite favorite = new Favorite();
                favorite.setPlayerId(playerId);
                favorite.setUserId(userId);
                favorite.setCreatedAt(LocalDateTime.now());
                favoriteMapper.insert(favorite);
                
                LambdaQueryWrapper<Favorite> countQuery = new LambdaQueryWrapper<>();
                countQuery.eq(Favorite::getPlayerId, playerId);
                int latestCount = favoriteMapper.selectCount(countQuery).intValue();
                
                log.info("添加收藏球员成功: 球员ID={}, 最新收藏数={}", playerId, latestCount);
                return new FavoriteToggleResp(true, latestCount);
            }
        } else {
            log.warn("收藏切换请求参数不足: userId={}, postId={}, newsId={}, playerId={}", userId, postId, newsId, playerId);
            throw new ServiceException(ServiceErrorCode.PARAM_ERROR);
        }
    }

    @Override
    public Page<PostDetailResp> getMyFavorites(Integer pageNum, Integer pageSize, Long userId) {
        Page<Favorite> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Favorite> query = new LambdaQueryWrapper<>();
        query.eq(Favorite::getUserId, userId)
             .isNotNull(Favorite::getPostId)
             .orderByDesc(Favorite::getCreatedAt);
        
        Page<Favorite> favoritePage = favoriteMapper.selectPage(page, query);
        
        if (favoritePage.getRecords().isEmpty()) {
            Page<PostDetailResp> result = new Page<>(pageNum, pageSize);
            result.setTotal(favoritePage.getTotal());
            result.setRecords(Collections.emptyList());
            return result;
        }

        List<Long> postIds = favoritePage.getRecords().stream()
                .map(Favorite::getPostId)
                .collect(Collectors.toList());
        
        List<Post> posts = postMapper.selectBatchIds(postIds);
        
        // 保持收藏顺序
        List<Post> sortedPosts = favoritePage.getRecords().stream()
                .map(fav -> posts.stream().filter(p -> p.getId().equals(fav.getPostId())).findFirst().orElse(null))
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toList());
        
        // 获取用户信息
        List<Long> userIds = sortedPosts.stream().map(Post::getUserId).collect(Collectors.toList());
        Map<Long, User> userMap = userIds.isEmpty() ? Collections.emptyMap() :
            userMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));

        // 获取话题信息
        List<Long> topicIds = sortedPosts.stream()
                .map(Post::getTopicId)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toList());
        Map<Long, Topic> topicMap = topicIds.isEmpty() ? Collections.emptyMap() :
                topicMapper.selectBatchIds(topicIds).stream()
                        .collect(Collectors.toMap(Topic::getId, Function.identity()));

        // 获取圈子信息
        List<Long> circleIds = sortedPosts.stream()
                .map(Post::getCircleId)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toList());
        Map<Long, Team> teamMap = circleIds.isEmpty() ? Collections.emptyMap() :
                teamMapper.selectBatchIds(circleIds).stream()
                        .collect(Collectors.toMap(Team::getId, Function.identity()));

        // 转换为 PostDetailResp
        List<PostDetailResp> respList = sortedPosts.stream().map(post -> {
            User user = userMap.get(post.getUserId());
            String nickname = (user != null) ? user.getNickname() : "未知用户";
            String avatar = (user != null) ? user.getAvatar() : "";
            
            PostDetailResp resp = PostDetailResp.fromPost(post, nickname, avatar);
            
            // 设置话题名称
            if (post.getTopicId() != null && topicMap.containsKey(post.getTopicId())) {
                resp.setTopicName(topicMap.get(post.getTopicId()).getTitle());
            }
            
            // 设置圈子名称
            if (post.getCircleId() != null && teamMap.containsKey(post.getCircleId())) {
                resp.setCircleName(teamMap.get(post.getCircleId()).getName());
            }

            resp.setIsFavorited(true);
            return resp;
        }).collect(Collectors.toList());

        Page<PostDetailResp> result = new Page<>(pageNum, pageSize);
        result.setTotal(favoritePage.getTotal());
        result.setRecords(respList);
        return result;
    }

    @Override
    public Page<News> getMyFavoriteNews(Integer pageNum, Integer pageSize, Long userId) {
        Page<Favorite> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Favorite> query = new LambdaQueryWrapper<>();
        query.eq(Favorite::getUserId, userId)
             .isNotNull(Favorite::getNewsId)
             .orderByDesc(Favorite::getCreatedAt);
        
        Page<Favorite> favoritePage = favoriteMapper.selectPage(page, query);
        
        if (favoritePage.getRecords().isEmpty()) {
            Page<News> result = new Page<>(pageNum, pageSize);
            result.setTotal(favoritePage.getTotal());
            result.setRecords(Collections.emptyList());
            return result;
        }
        
        List<Long> newsIds = favoritePage.getRecords().stream()
                .map(Favorite::getNewsId)
                .collect(Collectors.toList());
                
        List<News> newsList = newsMapper.selectBatchIds(newsIds);
        
        // Keep order
        List<News> sortedNews = favoritePage.getRecords().stream()
                .map(fav -> newsList.stream().filter(n -> n.getId().equals(fav.getNewsId())).findFirst().orElse(null))
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toList());
                
        Page<News> result = new Page<>(pageNum, pageSize);
        result.setTotal(favoritePage.getTotal());
        result.setRecords(sortedNews);
        return result;
    }

    @Override
    public Page<Map<String, Object>> getMyFavoritePlayers(Integer pageNum, Integer pageSize, Long userId) {
        Page<Favorite> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Favorite> query = new LambdaQueryWrapper<>();
        query.eq(Favorite::getUserId, userId)
             .isNotNull(Favorite::getPlayerId)
             .orderByDesc(Favorite::getCreatedAt);
        
        Page<Favorite> favoritePage = favoriteMapper.selectPage(page, query);
        
        if (favoritePage.getRecords().isEmpty()) {
            Page<Map<String, Object>> result = new Page<>(pageNum, pageSize);
            result.setTotal(favoritePage.getTotal());
            result.setRecords(Collections.emptyList());
            return result;
        }

        // 球员数据目前来自外部 API (SportAPI)，因此收藏列表只返回球员 ID
        // 前端根据 ID 列表自行加载或展示基础信息
        List<Map<String, Object>> records = favoritePage.getRecords().stream().map(fav -> {
            Map<String, Object> map = new java.util.HashMap<>();
            map.put("playerId", fav.getPlayerId());
            map.put("favoriteId", fav.getId());
            map.put("createTime", fav.getCreatedAt());
            return map;
        }).collect(Collectors.toList());

        Page<Map<String, Object>> result = new Page<>(pageNum, pageSize);
        result.setTotal(favoritePage.getTotal());
        result.setRecords(records);
        return result;
    }

    @Override
    public boolean isFavorited(Long postId, Long newsId, Long playerId, Long userId) {
        LambdaQueryWrapper<Favorite> query = new LambdaQueryWrapper<>();
        query.eq(Favorite::getUserId, userId);
        
        if (postId != null) {
            query.eq(Favorite::getPostId, postId);
        } else if (newsId != null) {
            query.eq(Favorite::getNewsId, newsId);
        } else if (playerId != null) {
            query.eq(Favorite::getPlayerId, playerId);
        } else {
            return false;
        }
        
        return favoriteMapper.exists(query);
    }
}
