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

    /**
     * 检查是否已点赞
     * @param targetId 目标ID
     * @param targetType 类型: 1帖子, 2评论
     * @param userId 用户ID
     * @return true:已点赞, false:未点赞
     */
    boolean isLiked(Long targetId, Integer targetType, Long userId);

    /**
     * 获取帖子点赞用户列表
     *
     * @param postId 帖子ID
     * @param limit  限制数量
     * @return 点赞用户列表
     */
    java.util.List<com.soccer.forum.service.model.dto.UserSimpleResp> getPostLikers(Long postId, int limit);
}
