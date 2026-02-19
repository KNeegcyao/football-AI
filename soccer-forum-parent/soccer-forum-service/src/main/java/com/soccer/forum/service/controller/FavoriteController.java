package com.soccer.forum.service.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.soccer.forum.common.core.domain.R;
import com.soccer.forum.domain.entity.Post;
import com.soccer.forum.service.model.dto.FavoriteReq;
import com.soccer.forum.service.security.model.LoginUser;
import com.soccer.forum.service.service.FavoriteService;
import com.soccer.forum.service.model.dto.PostDetailResp;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 收藏管理控制器
 * <p>
 * 提供帖子的收藏和取消收藏功能，以及查询个人收藏列表。
 * </p>
 */
@Tag(name = "收藏管理", description = "收藏功能接口")
@RestController
@RequestMapping("/api/favorites")
public class FavoriteController {

    private static final Logger log = LoggerFactory.getLogger(FavoriteController.class);

    private final FavoriteService favoriteService;

    public FavoriteController(FavoriteService favoriteService) {
        this.favoriteService = favoriteService;
    }

    /**
     * 切换收藏状态接口
     * <p>
     * 对指定帖子进行收藏操作，如果已收藏则取消收藏。
     * </p>
     *
     * @param req       收藏请求对象 (postId)
     * @param loginUser 当前登录用户
     * @return 操作结果 (favorited: true表示已收藏, false表示取消收藏)
     */
    @Operation(summary = "切换收藏状态", description = "用户收藏或取消收藏帖子")
    @PostMapping("/toggle")
    public R<Map<String, Boolean>> toggle(@Parameter(description = "收藏信息") @Validated @RequestBody FavoriteReq req,
                                          @Parameter(hidden = true) @AuthenticationPrincipal LoginUser loginUser) {
        log.info("收到收藏切换请求: 用户ID={}, 帖子ID={}", loginUser.getUser().getId(), req.getPostId());
        
        try {
            boolean favorited = favoriteService.toggleFavorite(req.getPostId(), loginUser.getUser().getId());
            
            String msg = favorited ? "收藏成功" : "取消收藏成功";
            log.info("{} : 用户ID={}, 帖子ID={}", msg, loginUser.getUser().getId(), req.getPostId());
            
            return R.ok(Map.of("favorited", favorited), msg);
        } catch (Exception e) {
            log.error("收藏操作失败: postId=" + req.getPostId() + ", userId=" + loginUser.getUser().getId(), e);
            throw e;
        }
    }


    /**
     * 查询个人收藏列表接口
     *
     * @param page      页码 (默认1)
     * @param size      每页大小 (默认10)
     * @param loginUser 当前登录用户
     * @return 收藏的帖子列表分页数据
     */
    @Operation(summary = "获取我的收藏", description = "分页查询当前用户的收藏帖子列表")
    @GetMapping
    public R<Page<PostDetailResp>> list(@Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
                              @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Integer size,
                              @Parameter(hidden = true) @AuthenticationPrincipal LoginUser loginUser) {
        log.info("查询收藏列表: 用户ID={}, page={}, size={}", loginUser.getUser().getId(), page, size);
        Page<PostDetailResp> result = favoriteService.getMyFavorites(page, size, loginUser.getUser().getId());
        return R.ok(result);
    }
}
