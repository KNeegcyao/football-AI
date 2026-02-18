package com.soccer.forum.service.controller;

import com.soccer.forum.common.core.domain.R;
import com.soccer.forum.service.security.model.LoginUser;
import com.soccer.forum.service.service.UserRelationshipService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户关系控制器
 */
@Tag(name = "用户关系", description = "关注/取消关注接口")
@RestController
@RequestMapping("/api/relationships")
public class UserRelationshipController {

    private static final Logger log = LoggerFactory.getLogger(UserRelationshipController.class);

    private final UserRelationshipService relationshipService;

    public UserRelationshipController(UserRelationshipService relationshipService) {
        this.relationshipService = relationshipService;
    }

    @Operation(summary = "关注用户")
    @PostMapping("/follow/{userId}")
    public R<Void> follow(@Parameter(description = "被关注用户ID") @PathVariable Long userId,
                         @Parameter(hidden = true) @AuthenticationPrincipal LoginUser loginUser) {
        log.info("用户 {} 关注用户 {}", loginUser.getUser().getId(), userId);
        relationshipService.follow(loginUser.getUser().getId(), userId);
        return R.ok(null, "关注成功");
    }

    @Operation(summary = "取消关注")
    @PostMapping("/unfollow/{userId}")
    public R<Void> unfollow(@Parameter(description = "取消关注用户ID") @PathVariable Long userId,
                           @Parameter(hidden = true) @AuthenticationPrincipal LoginUser loginUser) {
        log.info("用户 {} 取消关注用户 {}", loginUser.getUser().getId(), userId);
        relationshipService.unfollow(loginUser.getUser().getId(), userId);
        return R.ok(null, "取消关注成功");
    }

    @Operation(summary = "查询是否已关注")
    @GetMapping("/isFollowing/{userId}")
    public R<Boolean> isFollowing(@Parameter(description = "目标用户ID") @PathVariable Long userId,
                                 @Parameter(hidden = true) @AuthenticationPrincipal LoginUser loginUser) {
        if (loginUser == null) {
            return R.ok(false);
        }
        boolean isFollowing = relationshipService.isFollowing(loginUser.getUser().getId(), userId);
        return R.ok(isFollowing);
    }
}
