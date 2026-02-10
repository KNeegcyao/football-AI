package com.soccer.forum.service.service;

public interface LikeService {
    /**
     * 点赞/取消点赞
     * @param targetId 目标ID
     * @param targetType 类型: 1帖子, 2评论
     * @param userId 用户ID
     * @return true:点赞成功, false:取消点赞成功
     */
    boolean toggleLike(Long targetId, Integer targetType, Long userId);
}
