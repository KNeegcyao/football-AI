package com.soccer.forum.service.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.soccer.forum.domain.entity.Post;

public interface FavoriteService {
    /**
     * 收藏/取消收藏
     * @param postId 帖子ID
     * @param userId 用户ID
     * @return true:收藏成功, false:取消收藏成功
     */
    boolean toggleFavorite(Long postId, Long userId);

    /**
     * 获取用户收藏列表
     */
    Page<Post> getMyFavorites(Integer page, Integer size, Long userId);
}
