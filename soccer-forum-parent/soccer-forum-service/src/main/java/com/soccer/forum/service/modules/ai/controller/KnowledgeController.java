package com.soccer.forum.service.modules.ai.controller;

import com.soccer.forum.common.R;
import com.soccer.forum.service.modules.ai.rag.RagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * 知识库管理控制器
 */
@Tag(name = "AI 知识库管理")
@RestController
@RequestMapping("/api/ai/knowledge")
@RequiredArgsConstructor
public class KnowledgeController {

    private final RagService ragService;

    @Operation(summary = "上传文档到知识库")
    @PostMapping("/upload")
    public R<String> uploadDocument(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return R.fail("文件不能为空");
        }
        try {
            ragService.uploadDocument(file);
            return R.ok("文档上传并处理成功");
        } catch (IOException e) {
            return R.fail("文档处理失败: " + e.getMessage());
        }
    }

    @Operation(summary = "获取已加载的文档列表")
    @GetMapping("/list")
    public R<String[]> listDocuments() {
        return R.ok(ragService.listDocuments());
    }

    @Operation(summary = "清空知识库")
    @DeleteMapping("/clear")
    public R<String> clearKnowledgeBase() {
        ragService.clearKnowledgeBase();
        // 注意：目前内存向量库无法真正清空，重启服务后生效
        return R.ok("知识库已清空（部分缓存需重启服务生效）");
    }
}
