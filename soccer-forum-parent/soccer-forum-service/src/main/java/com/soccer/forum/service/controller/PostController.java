package com.soccer.forum.service.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.soccer.forum.domain.entity.Post;
import com.soccer.forum.service.model.dto.PostCreateReq;
import com.soccer.forum.service.model.dto.PostPageReq;
import com.soccer.forum.service.security.model.LoginUser;
import com.soccer.forum.service.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 帖子相关接口控制器
 * <p>
 * 提供帖子的发布、查询、详情获取及删除功能。
 * </p>
 *
 * @author Soccer Forum Dev Team
 * @version 1.0
 */
@Tag(name = "帖子管理", description = "社区帖子管理接口")
@RestController
@RequestMapping("/api/posts")
public class PostController {

    private static final Logger log = LoggerFactory.getLogger(PostController.class);

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    /**
     * 发布帖子接口
     * <p>
     * 接收帖子标题和内容，关联当前登录用户进行发布。
     * </p>
     *
     * @param req 帖子创建请求对象（标题、内容）
     * @param loginUser 当前登录用户信息（自动注入）
     * @return 包含新帖子 ID 的响应结果
     */
    @Operation(summary = "发布帖子", description = "用户发布新帖子")
    @PostMapping
    public Map<String, Object> create(@RequestBody PostCreateReq req, 
                                      @Parameter(hidden = true) @AuthenticationPrincipal LoginUser loginUser) {
        log.info("收到发布帖子请求: 用户ID={}, 标题={}", loginUser.getUser().getId(), req.getTitle());
        try {
            Long postId = postService.createPost(req, loginUser.getUser().getId());
            log.info("帖子发布成功: id={}", postId);
            
            Map<String, Object> result = new HashMap<>();
            result.put("code", 200);
            result.put("msg", "success");
            result.put("data", Map.of("id", postId));
            return result;
        } catch (Exception e) {
            log.error("帖子发布失败", e);
            throw e;
        }
    }

    /**
     * 获取帖子详情接口
     * <p>
     * 根据帖子 ID 获取详细内容，同时会增加浏览量。
     * </p>
     *
     * @param id 帖子 ID
     * @return 包含帖子详情的响应结果
     */
    @Operation(summary = "获取帖子详情", description = "根据ID获取帖子详情，并增加浏览量")
    @GetMapping("/{id}")
    public Map<String, Object> get(@PathVariable Long id) {
        log.info("收到获取帖子详情请求: id={}", id);
        Post post = postService.getPostById(id);
        
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("msg", "success");
        result.put("data", post);
        return result;
    }

    /**
     * 分页获取帖子列表接口
     * <p>
     * 支持按关键词模糊搜索标题。
     * </p>
     *
     * @param req 分页及查询参数对象
     * @return 包含分页数据的响应结果
     */
    @Operation(summary = "分页获取帖子列表", description = "支持按关键词搜索")
    @GetMapping
    public Map<String, Object> list(PostPageReq req) {
        log.info("收到分页获取帖子列表请求: 页码={}, 大小={}, 关键词={}", req.getPage(), req.getSize(), req.getKeyword());
        Page<Post> page = postService.getPostPage(req);
        
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("msg", "success");
        result.put("data", page);
        return result;
    }

    /**
     * 删除帖子接口
     * <p>
     * 用户仅可删除自己发布的帖子（逻辑删除）。
     * </p>
     *
     * @param id 帖子 ID
     * @param loginUser 当前登录用户信息
     * @return 操作结果
     */
    @Operation(summary = "删除帖子", description = "用户删除自己的帖子")
    @DeleteMapping("/{id}")
    public Map<String, Object> delete(@PathVariable Long id, 
                                      @Parameter(hidden = true) @AuthenticationPrincipal LoginUser loginUser) {
        log.info("收到删除帖子请求: 帖子ID={}, 用户ID={}", id, loginUser.getUser().getId());
        try {
            postService.deletePost(id, loginUser.getUser().getId());
            log.info("帖子删除成功(逻辑删除): id={}", id);
            
            Map<String, Object> result = new HashMap<>();
            result.put("code", 200);
            result.put("msg", "success");
            return result;
        } catch (Exception e) {
            log.error("帖子删除失败: id={}", id, e);
            throw e;
        }
    }
}
