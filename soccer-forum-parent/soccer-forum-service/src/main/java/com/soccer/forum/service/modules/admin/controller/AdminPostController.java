package com.soccer.forum.service.modules.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.soccer.forum.common.R;
import com.soccer.forum.service.modules.admin.dto.PostVO;
import com.soccer.forum.service.modules.admin.service.AdminPostService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/posts")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminPostController {

    private final AdminPostService adminPostService;

    @GetMapping
    public R<Page<PostVO>> getPostList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Long topicId) {
        return R.ok(adminPostService.getPostList(page, size, keyword, status, topicId));
    }

    @PutMapping("/{id}/status")
    public R<Void> updatePostStatus(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        adminPostService.updatePostStatus(id, body.get("status"));
        return R.ok();
    }

    @DeleteMapping("/{id}")
    public R<Void> deletePost(@PathVariable Long id) {
        adminPostService.deletePost(id);
        return R.ok();
    }
}
