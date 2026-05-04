package com.soccer.forum.service.modules.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.soccer.forum.common.R;
import com.soccer.forum.service.modules.admin.dto.TopicCreateDTO;
import com.soccer.forum.service.modules.admin.dto.TopicUpdateDTO;
import com.soccer.forum.service.modules.admin.dto.TopicVO;
import com.soccer.forum.service.modules.admin.service.AdminTopicService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/topics")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminTopicController {

    private final AdminTopicService adminTopicService;

    @GetMapping
    public R<Page<TopicVO>> getTopicList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {
        return R.ok(adminTopicService.getTopicList(page, size, keyword));
    }

    @PostMapping
    public R<TopicVO> createTopic(@Valid @RequestBody TopicCreateDTO dto) {
        return R.ok(adminTopicService.createTopic(dto));
    }

    @PutMapping("/{id}")
    public R<TopicVO> updateTopic(@PathVariable Long id, @Valid @RequestBody TopicUpdateDTO dto) {
        return R.ok(adminTopicService.updateTopic(id, dto));
    }

    @DeleteMapping("/{id}")
    public R<Void> deleteTopic(@PathVariable Long id) {
        adminTopicService.deleteTopic(id);
        return R.ok();
    }
}
