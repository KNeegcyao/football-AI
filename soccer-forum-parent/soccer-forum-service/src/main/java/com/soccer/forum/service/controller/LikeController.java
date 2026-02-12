package com.soccer.forum.service.controller;

import com.soccer.forum.common.core.domain.R;
import com.soccer.forum.service.model.dto.LikeReq;
import com.soccer.forum.service.security.model.LoginUser;
import com.soccer.forum.service.service.LikeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 点赞管理控制器
 * <p>
 * 提供帖子和评论的点赞/取消点赞功能。
 * </p>
 */
@Tag(name = "点赞管理", description = "点赞功能接口")
@RestController
@RequestMapping("/api/likes")
public class LikeController {

    private static final Logger log = LoggerFactory.getLogger(LikeController.class);

    private final LikeService likeService;

    public LikeController(LikeService likeService) {
        this.likeService = likeService;
    }

    /**
     * 点赞/取消点赞接口
     * <p>
     * 根据目标 ID 和类型进行点赞操作，如果已点赞则取消。
     * </p>
     *
     * @param req       点赞请求对象 (targetId, targetType)
     * @param loginUser 当前登录用户
     * @return 操作结果 (liked: true表示已点赞, false表示取消点赞)
     */
    @Operation(summary = "切换点赞状态", description = "对帖子或评论进行点赞或取消点赞")
    @PostMapping
    public R<Map<String, Boolean>> toggle(@Parameter(description = "点赞信息") @Validated @RequestBody LikeReq req,
                                          @Parameter(hidden = true) @AuthenticationPrincipal LoginUser loginUser) {
        log.info("收到点赞切换请求: 用户ID={}, 目标ID={}, 类型={}", 
                loginUser.getUser().getId(), req.getTargetId(), req.getTargetType());
        
        boolean liked = likeService.toggleLike(req.getTargetId(), req.getTargetType(), loginUser.getUser().getId());
        
        String msg = liked ? "点赞成功" : "取消点赞成功";
        log.info("{} : 用户ID={}, 目标ID={}", msg, loginUser.getUser().getId(), req.getTargetId());
        
        return R.ok(Map.of("liked", liked), msg);
    }
}
