package com.soccer.forum.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.soccer.forum.domain.entity.TeamFollow;

public interface TeamFollowService extends IService<TeamFollow> {
    
    /**
     * 关注球队
     * @param userId 用户ID
     * @param teamId 球队ID
     * @return 是否成功（true: 关注成功, false: 已关注）
     */
    boolean follow(Long userId, Long teamId);

    /**
     * 取消关注球队
     * @param userId 用户ID
     * @param teamId 球队ID
     */
    void unfollow(Long userId, Long teamId);

    /**
     * 检查是否已关注
     * @param userId 用户ID
     * @param teamId 球队ID
     * @return 是否已关注
     */
    boolean isFollowing(Long userId, Long teamId);
}
