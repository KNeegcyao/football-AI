package com.soccer.forum.service.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.soccer.forum.domain.entity.Post;

import com.soccer.forum.service.model.dto.PostDetailResp;

public interface FavoriteService {
    /**
     * 收藏/取消收藏
     * @param postId 帖子ID
     * @param newsId 新闻ID
     * @param userId 用户ID
     * @return true:收藏成功, false:取消收藏成功
     */
    boolean toggleFavorite(Long postId, Long newsId, Long userId);

    /**
     * 获取用户收藏帖子列表
     */
    Page<PostDetailResp> getMyFavorites(Integer page, Integer size, Long userId);

    /**
     * 获取用户收藏新闻列表
     */
    Page<com.soccer.forum.domain.entity.News> getMyFavoriteNews(Integer page, Integer size, Long userId);

    /**
     * 检查是否已收藏
     * @param postId 帖子ID (可选)
     * @param newsId 新闻ID (可选)
     * @param userId 用户ID
     * @return true:已收藏, false:未收藏
     */
    boolean isFavorited(Long postId, Long newsId, Long userId);
}
