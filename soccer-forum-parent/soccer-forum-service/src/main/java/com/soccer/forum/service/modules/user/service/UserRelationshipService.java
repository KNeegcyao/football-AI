package com.soccer.forum.service.modules.user.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.soccer.forum.domain.entity.UserRelationship;
import com.soccer.forum.service.modules.user.model.UserFollowResp;

public interface UserRelationshipService extends IService<UserRelationship> {
    
    /**
     * 关注用户
     */
    void follow(Long followerId, Long followingId);

    /**
     * 取消关注
     */
    void unfollow(Long followerId, Long followingId);

    /**
     * 是否已关注
     */
    boolean isFollowing(Long followerId, Long followingId);

    /**
     * 是否互相关注
     */
    boolean isMutualFollow(Long userA, Long userB);

    /**
     * 获取关注列表
     */
    IPage<UserFollowResp> getFollowingList(Long userId, Page<UserRelationship> page, Long currentUserId);

    /**
     * 获取粉丝列表
     */
    IPage<UserFollowResp> getFollowersList(Long userId, Page<UserRelationship> page, Long currentUserId);

    /**
     * 获取黑名单列表
     */
    IPage<UserFollowResp> getBlacklist(Long userId, Page<UserRelationship> page);

    /**
     * 设置拉黑状态
     */
    void setBlacklist(Long userId, Long otherUserId, Boolean isBlacklist);

    /**
     * 是否已拉黑
     */
    boolean isBlacklisted(Long userId, Long otherUserId);
}
