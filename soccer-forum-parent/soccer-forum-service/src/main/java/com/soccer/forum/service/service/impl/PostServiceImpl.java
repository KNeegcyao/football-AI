package com.soccer.forum.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.soccer.forum.domain.entity.Post;
import com.soccer.forum.service.mapper.PostMapper;
import com.soccer.forum.service.model.dto.PostCreateReq;
import com.soccer.forum.service.model.dto.PostPageReq;
import com.soccer.forum.service.service.PostService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class PostServiceImpl implements PostService {

    private final PostMapper postMapper;

    public PostServiceImpl(PostMapper postMapper) {
        this.postMapper = postMapper;
    }

    @Override
    public void createPost(PostCreateReq req, Long userId) {
        Post post = new Post();
        post.setTitle(req.getTitle());
        post.setContent(req.getContent());
        post.setUserId(userId);
        post.setViews(0);
        post.setLikes(0);
        post.setStatus(1);
        postMapper.insert(post);
    }

    @Override
    public Post getPostById(Long id) {
        Post post = postMapper.selectById(id);
        if (post != null) {
            post.setViews(post.getViews() + 1);
            postMapper.updateById(post);
        }
        return post;
    }

    @Override
    public Page<Post> getPostPage(PostPageReq req) {
        Page<Post> page = new Page<>(req.getPage(), req.getSize());
        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Post::getStatus, 1)
               .like(StringUtils.hasText(req.getKeyword()), Post::getTitle, req.getKeyword())
               .orderByDesc(Post::getCreatedAt);
        return postMapper.selectPage(page, wrapper);
    }

    @Override
    public void deletePost(Long id, Long userId) {
        Post post = postMapper.selectById(id);
        if (post != null && post.getUserId().equals(userId)) {
            post.setStatus(0);
            postMapper.updateById(post);
        }
    }
}
