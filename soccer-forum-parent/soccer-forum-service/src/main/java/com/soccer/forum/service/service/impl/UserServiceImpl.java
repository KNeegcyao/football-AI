package com.soccer.forum.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.soccer.forum.common.enums.ServiceErrorCode;
import com.soccer.forum.common.exception.ServiceException;
import com.soccer.forum.domain.entity.Favorite;
import com.soccer.forum.domain.entity.Like;
import com.soccer.forum.domain.entity.Post;
import com.soccer.forum.domain.entity.User;
import com.soccer.forum.domain.entity.UserRelationship;
import com.soccer.forum.service.mapper.FavoriteMapper;
import com.soccer.forum.service.mapper.LikeMapper;
import com.soccer.forum.service.mapper.PostMapper;
import com.soccer.forum.service.mapper.UserMapper;
import com.soccer.forum.service.mapper.UserRelationshipMapper;
import com.soccer.forum.service.model.dto.UserInfoResp;
import com.soccer.forum.service.model.dto.UserStatsResp;
import com.soccer.forum.service.model.dto.UserUpdateReq;
import com.soccer.forum.service.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户管理服务实现类
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final PostMapper postMapper;
    private final LikeMapper likeMapper;
    private final FavoriteMapper favoriteMapper;
    private final UserRelationshipMapper relationshipMapper;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(PostMapper postMapper, LikeMapper likeMapper, FavoriteMapper favoriteMapper, 
                          UserRelationshipMapper relationshipMapper, PasswordEncoder passwordEncoder) {
        this.postMapper = postMapper;
        this.likeMapper = likeMapper;
        this.favoriteMapper = favoriteMapper;
        this.relationshipMapper = relationshipMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserInfoResp getUserInfo(Long userId) {
        User user = this.getById(userId);
        if (user == null) {
            throw new ServiceException(ServiceErrorCode.USER_NOT_FOUND);
        }
        return UserInfoResp.fromEntity(user);
    }

    @Override
    public UserStatsResp getUserStats(Long userId) {
        UserStatsResp stats = new UserStatsResp();

        // 1. 发布帖子数
        Long postCount = postMapper.selectCount(new LambdaQueryWrapper<Post>()
                .eq(Post::getUserId, userId));
        stats.setPostCount(postCount);

        // 2. 收藏帖子数
        Long favoriteCount = favoriteMapper.selectCount(new LambdaQueryWrapper<Favorite>()
                .eq(Favorite::getUserId, userId));
        stats.setFavoriteCount(favoriteCount);

        // 3. 收获点赞数 (该用户发布的所有帖子收到的点赞总和)
        // 获取用户所有帖子ID
        List<Post> userPosts = postMapper.selectList(new LambdaQueryWrapper<Post>()
                .select(Post::getId)
                .eq(Post::getUserId, userId));
        
        if (userPosts.isEmpty()) {
            stats.setLikeReceivedCount(0L);
        } else {
            List<Long> postIds = userPosts.stream().map(Post::getId).collect(Collectors.toList());
            Long likeCount = likeMapper.selectCount(new LambdaQueryWrapper<Like>()
                    .eq(Like::getTargetType, 1) // 1: 帖子
                    .in(Like::getTargetId, postIds));
            stats.setLikeReceivedCount(likeCount);
        }

        // 4. 关注数
        Long followingCount = relationshipMapper.selectCount(new LambdaQueryWrapper<UserRelationship>()
                .eq(UserRelationship::getFollowerId, userId));
        stats.setFollowingCount(followingCount);

        // 5. 粉丝数
        Long followerCount = relationshipMapper.selectCount(new LambdaQueryWrapper<UserRelationship>()
                .eq(UserRelationship::getFollowingId, userId));
        stats.setFollowerCount(followerCount);

        return stats;
    }

    @Override
    @org.springframework.transaction.annotation.Transactional(rollbackFor = Exception.class)
    public void updateUserInfo(Long userId, UserUpdateReq req) {
        User user = this.getById(userId);
        if (user == null) {
            throw new ServiceException(ServiceErrorCode.USER_NOT_FOUND);
        }

        if (req.getNickname() != null) {
            user.setNickname(req.getNickname());
        }
        if (req.getAvatar() != null) {
            user.setAvatar(req.getAvatar());
        }
        if (req.getCover() != null) {
            user.setCover(req.getCover());
        }
        if (req.getEmail() != null) {
            String newEmail = req.getEmail().trim();
            if (newEmail.isEmpty()) {
                user.setEmail(null);
            } else if (!newEmail.equals(user.getEmail())) {
                // 检查邮箱是否已被占用
                User existingUser = this.getOne(new LambdaQueryWrapper<User>()
                        .eq(User::getEmail, newEmail)
                        .ne(User::getId, userId));
                if (existingUser != null) {
                    throw new ServiceException(ServiceErrorCode.EMAIL_ALREADY_EXISTS);
                }
                user.setEmail(newEmail);
            }
        }
        if (req.getPhone() != null) {
            String newPhone = req.getPhone().trim();
            if (newPhone.isEmpty()) {
                user.setPhone(null);
            } else if (!newPhone.equals(user.getPhone())) {
                // 检查手机号是否已被占用
                User existingUser = this.getOne(new LambdaQueryWrapper<User>()
                        .eq(User::getPhone, newPhone)
                        .ne(User::getId, userId));
                if (existingUser != null) {
                    throw new ServiceException(ServiceErrorCode.PHONE_ALREADY_EXISTS);
                }
                user.setPhone(newPhone);
            }
        }
        if (req.getBio() != null) {
            user.setBio(req.getBio());
        }

        this.updateById(user);
    }

    @Override
    @org.springframework.transaction.annotation.Transactional(rollbackFor = Exception.class)
    public void updatePassword(Long userId, String oldPassword, String newPassword) {
        User user = this.getById(userId);
        if (user == null) {
            throw new ServiceException(ServiceErrorCode.USER_NOT_FOUND);
        }

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new ServiceException(ServiceErrorCode.USER_PASSWORD_ERROR);
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        this.updateById(user);
    }

    @Override
    @org.springframework.transaction.annotation.Transactional(rollbackFor = Exception.class)
    public void updateNotificationSetting(Long userId, String replyType) {
        User user = this.getById(userId);
        if (user == null) {
            throw new ServiceException(ServiceErrorCode.USER_NOT_FOUND);
        }
        user.setReplyNotificationType(replyType);
        this.updateById(user);
    }

    @Override
    @org.springframework.transaction.annotation.Transactional(rollbackFor = Exception.class)
    public void updateFanNotificationSetting(Long userId, String fanType) {
        User user = this.getById(userId);
        if (user == null) {
            throw new ServiceException(ServiceErrorCode.USER_NOT_FOUND);
        }
        user.setFanNotificationType(fanType);
        this.updateById(user);
    }

    @Override
    @org.springframework.transaction.annotation.Transactional(rollbackFor = Exception.class)
    public void deleteAccount(Long userId) {
        User user = this.getById(userId);
        if (user == null) {
            throw new ServiceException(ServiceErrorCode.USER_NOT_FOUND);
        }

        // 逻辑删除或物理删除，这里根据业务需求决定。通常建议物理删除或脱敏处理。
        // 这里演示物理删除
        this.removeById(userId);
        
        // 同时删除关联数据（可选，根据外键约束或业务需求）
        postMapper.delete(new LambdaQueryWrapper<Post>().eq(Post::getUserId, userId));
        likeMapper.delete(new LambdaQueryWrapper<Like>().eq(Like::getUserId, userId));
        favoriteMapper.delete(new LambdaQueryWrapper<Favorite>().eq(Favorite::getUserId, userId));
    }
}
