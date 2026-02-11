package com.soccer.forum.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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

@Service
public class CommentServiceImpl implements CommentService {

    private final CommentMapper commentMapper;
    private final UserMapper userMapper;
    private final PostMapper postMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    public CommentServiceImpl(CommentMapper commentMapper, UserMapper userMapper, PostMapper postMapper, RedisTemplate<String, Object> redisTemplate) {
        this.commentMapper = commentMapper;
        this.userMapper = userMapper;
        this.postMapper = postMapper;
        this.redisTemplate = redisTemplate;
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

        if (req.getParentId() != null && req.getParentId() > 0) {
            Comment parent = commentMapper.selectById(req.getParentId());
            if (parent != null) {
                // 如果父评论本身就是子评论(parentId != 0)，则新评论的parentId应该指向根评论
                // 这里我们简化模型：两级评论。
                // 如果 parent.getParentId() == 0，说明 parent 是根评论。
                // 如果 parent.getParentId() != 0，说明 parent 是子评论，那新评论的 parentId 应该取 parent.getParentId()。
                
                Long rootId = parent.getParentId() == 0 ? parent.getId() : parent.getParentId();
                comment.setParentId(rootId);
                
                // 设置被回复的人
                comment.setReplyToUserId(parent.getUserId());
            } else {
                comment.setParentId(0L);
            }
        } else {
            comment.setParentId(0L);
        }

        commentMapper.insert(comment);
        
        // 更新帖子评论数
        Post post = postMapper.selectById(req.getPostId());
        if (post != null) {
            post.setCommentCount(post.getCommentCount() + 1);
            postMapper.updateById(post);
            
            // 清除帖子详情缓存
            String cacheKey = "post:detail:" + req.getPostId();
            redisTemplate.delete(cacheKey);
        }
    }

    @Override
    public Page<CommentResp> getCommentPage(CommentPageReq req) {
        // 1. 查询根评论
        Page<Comment> page = new Page<>(req.getPage(), req.getSize());
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Comment::getPostId, req.getPostId())
               .eq(Comment::getParentId, 0)
               .eq(Comment::getStatus, 1)
               .orderByDesc(Comment::getCreatedAt);
        
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
        if (comment != null && comment.getUserId().equals(userId)) {
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
