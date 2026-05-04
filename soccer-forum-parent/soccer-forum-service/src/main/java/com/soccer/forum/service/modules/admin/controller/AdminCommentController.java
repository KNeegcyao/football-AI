package com.soccer.forum.service.modules.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.soccer.forum.common.R;
import com.soccer.forum.service.modules.admin.dto.CommentVO;
import com.soccer.forum.service.modules.admin.service.AdminCommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/comments")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminCommentController {

    private final AdminCommentService adminCommentService;

    @GetMapping
    public R<Page<CommentVO>> getCommentList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long postId) {
        return R.ok(adminCommentService.getCommentList(page, size, keyword, postId));
    }

    @DeleteMapping("/{id}")
    public R<Void> deleteComment(@PathVariable Long id) {
        adminCommentService.deleteComment(id);
        return R.ok();
    }
}
