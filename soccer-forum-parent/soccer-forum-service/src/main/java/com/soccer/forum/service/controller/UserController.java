package com.soccer.forum.service.controller;

import com.soccer.forum.common.core.domain.R;
import com.soccer.forum.service.model.dto.UserInfoResp;
import com.soccer.forum.service.model.dto.UserStatsResp;
import com.soccer.forum.service.model.dto.UserUpdateReq;
import com.soccer.forum.service.security.model.LoginUser;
import com.soccer.forum.service.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 用户资料管理控制器
 */
@Tag(name = "用户资料", description = "用户个人资料管理接口")
@RestController
@RequestMapping("/api/users")
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * 获取个人资料
     */
    @Operation(summary = "获取个人资料", description = "获取当前登录用户的详细个人资料")
    @GetMapping("/profile")
    public R<UserInfoResp> getProfile(@Parameter(hidden = true) @AuthenticationPrincipal LoginUser loginUser) {
        log.info("获取用户资料: 用户ID={}", loginUser.getUser().getId());
        UserInfoResp userInfo = userService.getUserInfo(loginUser.getUser().getId());
        return R.ok(userInfo);
    }

    /**
     * 获取用户统计数据
     */
    @Operation(summary = "获取用户统计数据", description = "获取当前登录用户的发帖数、收获点赞数、收藏数等统计信息")
    @GetMapping("/stats")
    public R<UserStatsResp> getStats(@Parameter(hidden = true) @AuthenticationPrincipal LoginUser loginUser) {
        log.info("获取用户统计数据: 用户ID={}", loginUser.getUser().getId());
        UserStatsResp stats = userService.getUserStats(loginUser.getUser().getId());
        return R.ok(stats);
    }

    /**
     * 更新个人资料
     */
    @Operation(summary = "更新个人资料", description = "修改当前登录用户的昵称、头像、邮箱或手机号")
    @PutMapping("/profile")
    public R<Void> updateProfile(@Parameter(description = "更新内容") @RequestBody UserUpdateReq req,
                                 @Parameter(hidden = true) @AuthenticationPrincipal LoginUser loginUser) {
        log.info("更新用户资料: 用户ID={}", loginUser.getUser().getId());
        userService.updateUserInfo(loginUser.getUser().getId(), req);
        return R.ok(null, "个人资料更新成功");
    }
}
