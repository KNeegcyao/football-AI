package com.soccer.forum.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.soccer.forum.common.enums.ServiceErrorCode;
import com.soccer.forum.common.exception.ServiceException;
import com.soccer.forum.domain.entity.Favorite;
import com.soccer.forum.domain.entity.Like;
import com.soccer.forum.domain.entity.Post;
import com.soccer.forum.domain.entity.User;
import com.soccer.forum.service.mapper.FavoriteMapper;
import com.soccer.forum.service.mapper.LikeMapper;
import com.soccer.forum.service.mapper.PostMapper;
import com.soccer.forum.service.mapper.UserMapper;
import com.soccer.forum.service.model.dto.UserInfoResp;
import com.soccer.forum.service.model.dto.UserStatsResp;
import com.soccer.forum.service.model.dto.UserUpdateReq;
import com.soccer.forum.service.service.UserService;
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

    public UserServiceImpl(PostMapper postMapper, LikeMapper likeMapper, FavoriteMapper favoriteMapper) {
        this.postMapper = postMapper;
        this.likeMapper = likeMapper;
        this.favoriteMapper = favoriteMapper;
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
        if (req.getEmail() != null) {
            user.setEmail(req.getEmail());
        }
        if (req.getPhone() != null) {
            user.setPhone(req.getPhone());
        }

        this.updateById(user);
    }
}
