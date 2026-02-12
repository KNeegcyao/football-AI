package com.soccer.forum.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.soccer.forum.common.enums.ServiceErrorCode;
import com.soccer.forum.common.exception.ServiceException;
import com.soccer.forum.domain.entity.Favorite;
import com.soccer.forum.domain.entity.Post;
import com.soccer.forum.service.mapper.FavoriteMapper;
import com.soccer.forum.service.mapper.PostMapper;
import com.soccer.forum.service.service.FavoriteService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FavoriteServiceImpl implements FavoriteService {

    private final FavoriteMapper favoriteMapper;
    private final PostMapper postMapper;

    public FavoriteServiceImpl(FavoriteMapper favoriteMapper, PostMapper postMapper) {
        this.favoriteMapper = favoriteMapper;
        this.postMapper = postMapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean toggleFavorite(Long postId, Long userId) {
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
            return false;
        } else {
            Favorite favorite = new Favorite();
            favorite.setPostId(postId);
            favorite.setUserId(userId);
            favorite.setCreatedAt(LocalDateTime.now());
            favoriteMapper.insert(favorite);
            return true;
        }
    }

    @Override
    public Page<Post> getMyFavorites(Integer pageNum, Integer pageSize, Long userId) {
        Page<Favorite> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Favorite> query = new LambdaQueryWrapper<>();
        query.eq(Favorite::getUserId, userId)
             .orderByDesc(Favorite::getCreatedAt);
        
        Page<Favorite> favoritePage = favoriteMapper.selectPage(page, query);
        
        if (favoritePage.getRecords().isEmpty()) {
            Page<Post> result = new Page<>(pageNum, pageSize);
            result.setTotal(favoritePage.getTotal());
            result.setRecords(Collections.emptyList());
            return result;
        }

        List<Long> postIds = favoritePage.getRecords().stream()
                .map(Favorite::getPostId)
                .collect(Collectors.toList());
        
        List<Post> posts = postMapper.selectBatchIds(postIds);
        
        // 保持收藏顺序（selectBatchIds 不保证顺序）
        // 这里需要重新排序，或者前端不介意。为了体验，最好按收藏时间倒序（即 favoritePage 的顺序）。
        // 简单的内存排序：
        List<Post> sortedPosts = favoritePage.getRecords().stream()
                .map(fav -> posts.stream().filter(p -> p.getId().equals(fav.getPostId())).findFirst().orElse(null))
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toList());

        Page<Post> result = new Page<>(pageNum, pageSize);
        result.setTotal(favoritePage.getTotal());
        result.setRecords(sortedPosts);
        return result;
    }
}
