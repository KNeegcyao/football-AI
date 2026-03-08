package com.soccer.forum.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.soccer.forum.common.enums.ServiceErrorCode;
import com.soccer.forum.common.exception.ServiceException;
import com.soccer.forum.domain.entity.User;
import com.soccer.forum.domain.entity.UserRelationship;
import com.soccer.forum.service.mapper.UserRelationshipMapper;
import com.soccer.forum.service.model.dto.UserFollowResp;
import com.soccer.forum.service.service.UserRelationshipService;
import com.soccer.forum.service.service.UserService;
import com.soccer.forum.service.service.NotificationService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserRelationshipServiceImpl extends ServiceImpl<UserRelationshipMapper, UserRelationship> implements UserRelationshipService {

    private final NotificationService notificationService;
    private final UserService userService;

    public UserRelationshipServiceImpl(NotificationService notificationService, @Lazy UserService userService) {
        this.notificationService = notificationService;
        this.userService = userService;
    }

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
        
        // 发送关注通知 (使用类型 5:系统通知/关注通知)
        notificationService.sendNotification(followingId, followerId, 5, 0L, "关注了你");
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
        if (followerId == null || followingId == null) return false;
        LambdaQueryWrapper<UserRelationship> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserRelationship::getFollowerId, followerId)
               .eq(UserRelationship::getFollowingId, followingId);
        return this.count(wrapper) > 0;
    }

    @Override
    public boolean isMutualFollow(Long userA, Long userB) {
        return isFollowing(userA, userB) && isFollowing(userB, userA);
    }

    @Override
    public IPage<UserFollowResp> getFollowingList(Long userId, Page<UserRelationship> page, Long currentUserId) {
        LambdaQueryWrapper<UserRelationship> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserRelationship::getFollowerId, userId)
               .orderByDesc(UserRelationship::getCreatedAt);
        
        IPage<UserRelationship> relationshipPage = this.page(page, wrapper);
        
        IPage<UserFollowResp> resultPage = new Page<>(relationshipPage.getCurrent(), relationshipPage.getSize(), relationshipPage.getTotal());
        
        List<Long> followingIds = relationshipPage.getRecords().stream()
                .map(UserRelationship::getFollowingId)
                .collect(Collectors.toList());
        
        if (followingIds.isEmpty()) {
            return resultPage;
        }
        
        List<User> users = userService.listByIds(followingIds);
        List<UserFollowResp> records = users.stream().map(user -> {
            UserFollowResp resp = new UserFollowResp(
                    user.getId(),
                    user.getUsername(),
                    user.getNickname(),
                    user.getAvatar(),
                    user.getBio(),
                    false // isVerified 默认 false
            );
            // 补充关注状态
            if (currentUserId != null) {
                resp.setIsFollowing(isFollowing(currentUserId, user.getId()));
                resp.setIsFollower(isFollowing(user.getId(), currentUserId));
            } else {
                resp.setIsFollowing(false);
                resp.setIsFollower(false);
            }
            return resp;
        }).collect(Collectors.toList());
        
        resultPage.setRecords(records);
        return resultPage;
    }

    @Override
    public IPage<UserFollowResp> getFollowersList(Long userId, Page<UserRelationship> page, Long currentUserId) {
        LambdaQueryWrapper<UserRelationship> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserRelationship::getFollowingId, userId)
               .orderByDesc(UserRelationship::getCreatedAt);
        
        IPage<UserRelationship> relationshipPage = this.page(page, wrapper);
        
        IPage<UserFollowResp> resultPage = new Page<>(relationshipPage.getCurrent(), relationshipPage.getSize(), relationshipPage.getTotal());
        
        List<Long> followerIds = relationshipPage.getRecords().stream()
                .map(UserRelationship::getFollowerId)
                .collect(Collectors.toList());
        
        if (followerIds.isEmpty()) {
            return resultPage;
        }
        
        List<User> users = userService.listByIds(followerIds);
        List<UserFollowResp> records = users.stream().map(user -> {
            UserFollowResp resp = new UserFollowResp(
                    user.getId(),
                    user.getUsername(),
                    user.getNickname(),
                    user.getAvatar(),
                    user.getBio(),
                    false // isVerified 默认 false
            );
            // 补充关注状态
            if (currentUserId != null) {
                resp.setIsFollowing(isFollowing(currentUserId, user.getId()));
                resp.setIsFollower(isFollowing(user.getId(), currentUserId));
            } else {
                resp.setIsFollowing(false);
                resp.setIsFollower(false);
            }
            return resp;
        }).collect(Collectors.toList());
        
        resultPage.setRecords(records);
        return resultPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setBlacklist(Long userId, Long otherUserId, Boolean isBlacklist) {
        UserRelationship relationship = this.getOne(new LambdaQueryWrapper<UserRelationship>()
                .eq(UserRelationship::getFollowerId, userId)
                .eq(UserRelationship::getFollowingId, otherUserId));

        if (relationship == null) {
            relationship = new UserRelationship();
            relationship.setFollowerId(userId);
            relationship.setFollowingId(otherUserId);
            relationship.setIsBlacklisted(isBlacklist ? 1 : 0);
            this.save(relationship);
        } else {
            relationship.setIsBlacklisted(isBlacklist ? 1 : 0);
            this.updateById(relationship);
        }
    }

    @Override
    public boolean isBlacklisted(Long userId, Long otherUserId) {
        UserRelationship relationship = this.getOne(new LambdaQueryWrapper<UserRelationship>()
                .eq(UserRelationship::getFollowerId, userId)
                .eq(UserRelationship::getFollowingId, otherUserId));
        return relationship != null && Integer.valueOf(1).equals(relationship.getIsBlacklisted());
    }
}
