package com.soccer.forum.service.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.soccer.forum.domain.entity.Post;
import com.soccer.forum.service.model.dto.PostCreateReq;
import com.soccer.forum.service.model.dto.PostPageReq;
import com.soccer.forum.service.security.model.LoginUser;
import com.soccer.forum.service.service.PostService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @PostMapping
    public String create(@RequestBody PostCreateReq req, @AuthenticationPrincipal LoginUser loginUser) {
        postService.createPost(req, loginUser.getUser().getId());
        return "success";
    }

    @GetMapping("/{id}")
    public Post get(@PathVariable Long id) {
        return postService.getPostById(id);
    }

    @GetMapping
    public Page<Post> list(PostPageReq req) {
        return postService.getPostPage(req);
    }

    @DeleteMapping("/{id}")
    public String delete(@PathVariable Long id, @AuthenticationPrincipal LoginUser loginUser) {
        postService.deletePost(id, loginUser.getUser().getId());
        return "success";
    }
}
