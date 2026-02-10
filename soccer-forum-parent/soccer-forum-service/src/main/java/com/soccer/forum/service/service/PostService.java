package com.soccer.forum.service.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.soccer.forum.domain.entity.Post;
import com.soccer.forum.service.model.dto.PostCreateReq;
import com.soccer.forum.service.model.dto.PostPageReq;

public interface PostService {
    void createPost(PostCreateReq req, Long userId);
    Post getPostById(Long id);
    Page<Post> getPostPage(PostPageReq req);
    void deletePost(Long id, Long userId);
}
