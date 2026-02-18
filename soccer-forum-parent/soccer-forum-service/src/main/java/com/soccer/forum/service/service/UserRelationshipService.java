package com.soccer.forum.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.soccer.forum.domain.entity.UserRelationship;

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
}
