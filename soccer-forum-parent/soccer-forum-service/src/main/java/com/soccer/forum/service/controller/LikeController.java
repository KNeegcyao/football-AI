package com.soccer.forum.service.controller;

import com.soccer.forum.service.model.dto.LikeReq;
import com.soccer.forum.service.security.model.LoginUser;
import com.soccer.forum.service.service.LikeService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/likes")
public class LikeController {

    private final LikeService likeService;

    public LikeController(LikeService likeService) {
        this.likeService = likeService;
    }

    @PostMapping
    public boolean toggle(@RequestBody LikeReq req, @AuthenticationPrincipal LoginUser loginUser) {
        return likeService.toggleLike(req.getTargetId(), req.getTargetType(), loginUser.getUser().getId());
    }
}
