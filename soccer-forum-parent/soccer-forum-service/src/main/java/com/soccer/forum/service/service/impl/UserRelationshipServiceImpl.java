package com.soccer.forum.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.soccer.forum.common.enums.ServiceErrorCode;
import com.soccer.forum.common.exception.ServiceException;
import com.soccer.forum.domain.entity.UserRelationship;
import com.soccer.forum.service.mapper.UserRelationshipMapper;
import com.soccer.forum.service.service.UserRelationshipService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserRelationshipServiceImpl extends ServiceImpl<UserRelationshipMapper, UserRelationship> implements UserRelationshipService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void follow(Long followerId, Long followingId) {
        if (followerId.equals(followingId)) {
            throw new ServiceException("不能关注自己", ServiceErrorCode.PARAM_ERROR.getCode());
        }
        
        // 检查是否已关注
        if (isFollowing(followerId, followingId)) {
            return;
        }
        
        UserRelationship relationship = new UserRelationship();
        relationship.setFollowerId(followerId);
        relationship.setFollowingId(followingId);
        this.save(relationship);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unfollow(Long followerId, Long followingId) {
        LambdaQueryWrapper<UserRelationship> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserRelationship::getFollowerId, followerId)
               .eq(UserRelationship::getFollowingId, followingId);
        this.remove(wrapper);
    }

    @Override
    public boolean isFollowing(Long followerId, Long followingId) {
        LambdaQueryWrapper<UserRelationship> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserRelationship::getFollowerId, followerId)
               .eq(UserRelationship::getFollowingId, followingId);
        return this.count(wrapper) > 0;
    }
}
