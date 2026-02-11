package com.soccer.forum.service.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.soccer.forum.domain.entity.Post;
import com.soccer.forum.service.model.dto.FavoriteReq;
import com.soccer.forum.service.security.model.LoginUser;
import com.soccer.forum.service.service.FavoriteService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "收藏管理", description = "收藏功能接口")
@RestController
@RequestMapping("/api/favorites")
public class FavoriteController {

    private final FavoriteService favoriteService;

    public FavoriteController(FavoriteService favoriteService) {
        this.favoriteService = favoriteService;
    }

    @PostMapping
    public boolean toggle(@RequestBody FavoriteReq req, @AuthenticationPrincipal LoginUser loginUser) {
        return favoriteService.toggleFavorite(req.getPostId(), loginUser.getUser().getId());
    }

    @GetMapping
    public Page<Post> list(@RequestParam(defaultValue = "1") Integer page,
                           @RequestParam(defaultValue = "10") Integer size,
                           @AuthenticationPrincipal LoginUser loginUser) {
        return favoriteService.getMyFavorites(page, size, loginUser.getUser().getId());
    }
}
