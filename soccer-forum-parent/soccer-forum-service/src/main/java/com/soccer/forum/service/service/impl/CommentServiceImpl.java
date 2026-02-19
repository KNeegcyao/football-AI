package com.soccer.forum.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.soccer.forum.common.enums.ServiceErrorCode;
import com.soccer.forum.common.exception.ServiceException;
import com.soccer.forum.domain.entity.Comment;
import com.soccer.forum.domain.entity.Post;
import com.soccer.forum.domain.entity.User;
import com.soccer.forum.service.mapper.CommentMapper;
import com.soccer.forum.service.mapper.PostMapper;
import com.soccer.forum.service.mapper.UserMapper;
import com.soccer.forum.service.model.dto.CommentCreateReq;
import com.soccer.forum.service.model.dto.CommentPageReq;
import com.soccer.forum.service.model.dto.CommentResp;
import com.soccer.forum.service.service.CommentService;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements CommentService {

    private final CommentMapper commentMapper;
    private final UserMapper userMapper;
    private final PostMapper postMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private final com.soccer.forum.service.service.NotificationService notificationService;

    public CommentServiceImpl(CommentMapper commentMapper, UserMapper userMapper, PostMapper postMapper, 
                              RedisTemplate<String, Object> redisTemplate,
                              com.soccer.forum.service.service.NotificationService notificationService) {
        this.commentMapper = commentMapper;
        this.userMapper = userMapper;
        this.postMapper = postMapper;
        this.redisTemplate = redisTemplate;
        this.notificationService = notificationService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createComment(CommentCreateReq req, Long userId) {
        Comment comment = new Comment();
        comment.setPostId(req.getPostId());
        comment.setUserId(userId);
        comment.setContent(req.getContent());
        comment.setLikes(0);
        comment.setStatus(1);

        Post post = postMapper.selectById(req.getPostId());
        if (post == null) {
            throw new ServiceException(ServiceErrorCode.POST_NOT_FOUND);
        }

        if (req.getParentId() != null && req.getParentId() > 0) {
            Comment parent = commentMapper.selectById(req.getParentId());
            if (parent != null) {
                Long rootId = parent.getParentId() == 0 ? parent.getId() : parent.getParentId();
                comment.setParentId(rootId);
                comment.setReplyToUserId(parent.getUserId());
                
                // 发送回复评论通知
                notificationService.sendNotification(parent.getUserId(), userId, 4, req.getPostId(), req.getContent());
            } else {
                comment.setParentId(0L);
            }
        } else {
            comment.setParentId(0L);
            // 发送评论帖子通知
            notificationService.sendNotification(post.getUserId(), userId, 3, post.getId(), req.getContent());
        }

        commentMapper.insert(comment);
        
        // 更新帖子评论数
        post.setCommentCount(post.getCommentCount() + 1);
        postMapper.updateById(post);
        
        // 清除帖子详情缓存
        // String cacheKey = "post:detail:" + req.getPostId();
        // redisTemplate.delete(cacheKey);
    }

    @Override
    public Page<CommentResp> getCommentPage(CommentPageReq req) {
        // 1. 查询根评论
        Page<Comment> page = new Page<>(req.getPage(), req.getSize());
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Comment::getPostId, req.getPostId())
               .eq(Comment::getParentId, 0)
               .eq(Comment::getStatus, 1);
        
        if ("hottest".equalsIgnoreCase(req.getSort())) {
            wrapper.orderByDesc(Comment::getLikes);
        } else {
            wrapper.orderByDesc(Comment::getCreatedAt);
        }
        
        Page<Comment> commentPage = commentMapper.selectPage(page, wrapper);
        
        List<CommentResp> respList = new ArrayList<>();
        if (commentPage.getRecords().isEmpty()) {
            Page<CommentResp> result = new Page<>(req.getPage(), req.getSize());
            result.setTotal(commentPage.getTotal());
            result.setRecords(respList);
            return result;
        }

        // 收集所有涉及的用户ID
        Set<Long> userIds = commentPage.getRecords().stream().map(Comment::getUserId).collect(Collectors.toSet());
        
        // 2. 查询子评论 (每个根评论下的子评论)
        // 这里的策略是：查出当前页所有根评论ID，然后一次性查出这些根评论下的所有子评论
        List<Long> rootIds = commentPage.getRecords().stream().map(Comment::getId).collect(Collectors.toList());
        List<Comment> childComments = commentMapper.selectList(new LambdaQueryWrapper<Comment>()
                .in(Comment::getParentId, rootIds)
                .eq(Comment::getStatus, 1)
                .orderByAsc(Comment::getCreatedAt));
        
        userIds.addAll(childComments.stream().map(Comment::getUserId).collect(Collectors.toSet()));
        userIds.addAll(childComments.stream().map(Comment::getReplyToUserId).collect(Collectors.toSet()));

        // 3. 查询用户信息
        List<User> users = userMapper.selectBatchIds(userIds);
        Map<Long, User> userMap = users.stream().collect(Collectors.toMap(User::getId, u -> u));

        // 4. 组装数据
        for (Comment root : commentPage.getRecords()) {
            CommentResp rootResp = convert(root, userMap);
            
            // 找到该根评论下的子评论
            List<CommentResp> children = childComments.stream()
                    .filter(c -> c.getParentId().equals(root.getId()))
                    .map(c -> convert(c, userMap))
                    .collect(Collectors.toList());
            
            rootResp.setReplies(children);
            respList.add(rootResp);
        }

        Page<CommentResp> result = new Page<>(req.getPage(), req.getSize());
        result.setTotal(commentPage.getTotal());
        result.setRecords(respList);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteComment(Long id, Long userId) {
        Comment comment = commentMapper.selectById(id);
        if (comment == null) {
            throw new ServiceException(ServiceErrorCode.COMMENT_NOT_FOUND);
        }
        if (!comment.getUserId().equals(userId)) {
            throw new ServiceException(ServiceErrorCode.FORBIDDEN);
        }
        
        comment.setStatus(0);
        commentMapper.updateById(comment);
        
        // 更新帖子评论数
        Post post = postMapper.selectById(comment.getPostId());
        if (post != null && post.getCommentCount() > 0) {
            post.setCommentCount(post.getCommentCount() - 1);
            postMapper.updateById(post);
            
            // 清除帖子详情缓存
            String cacheKey = "post:detail:" + comment.getPostId();
            redisTemplate.delete(cacheKey);
        }
    }

    private CommentResp convert(Comment comment, Map<Long, User> userMap) {
        CommentResp resp = new CommentResp();
        BeanUtils.copyProperties(comment, resp);
        
        User user = userMap.get(comment.getUserId());
        if (user != null) {
            resp.setNickname(user.getNickname());
            resp.setAvatar(user.getAvatar());
        }
        
        if (comment.getReplyToUserId() != null && comment.getReplyToUserId() > 0) {
            User replyUser = userMap.get(comment.getReplyToUserId());
            if (replyUser != null) {
                resp.setReplyToNickname(replyUser.getNickname());
            }
        }
        
        return resp;
    }
}
