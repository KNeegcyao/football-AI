package com.soccer.forum.service.modules.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.soccer.forum.common.R;
import com.soccer.forum.service.modules.admin.dto.NewsCreateDTO;
import com.soccer.forum.service.modules.admin.dto.NewsUpdateDTO;
import com.soccer.forum.service.modules.admin.dto.NewsVO;
import com.soccer.forum.service.modules.admin.service.AdminNewsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/news")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminNewsController {

    private final AdminNewsService adminNewsService;

    @GetMapping
    public R<Page<NewsVO>> getNewsList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category) {
        return R.ok(adminNewsService.getNewsList(page, size, keyword, category));
    }

    @PostMapping
    public R<NewsVO> createNews(@Valid @RequestBody NewsCreateDTO dto) {
        return R.ok(adminNewsService.createNews(dto));
    }

    @PutMapping("/{id}")
    public R<NewsVO> updateNews(@PathVariable Long id, @Valid @RequestBody NewsUpdateDTO dto) {
        return R.ok(adminNewsService.updateNews(id, dto));
    }

    @DeleteMapping("/{id}")
    public R<Void> deleteNews(@PathVariable Long id) {
        adminNewsService.deleteNews(id);
        return R.ok();
    }
}
