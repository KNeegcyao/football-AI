package com.soccer.forum.service.modules.ai.controller;

import com.soccer.forum.common.R;
import com.soccer.forum.service.modules.ai.service.DifyAiService;
import com.soccer.forum.service.modules.ai.model.DifyChatResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import org.springframework.web.multipart.MultipartFile;

/**
 * Dify AI 控制层
 */
@Tag(name = "Dify AI 助手接口")
@RestController
@RequestMapping("/api/ai")
public class DifyAiController {

    private static final Logger log = LoggerFactory.getLogger(DifyAiController.class);

    private final DifyAiService difyAiService;

    public DifyAiController(DifyAiService difyAiService) {
        this.difyAiService = difyAiService;
    }

    @Operation(summary = "Dify 智能对话转发")
    @PostMapping("/chat")
    public R<DifyChatResponse> chat(@RequestBody Map<String, Object> body) {
        String message = body.get("message") != null ? body.get("message").toString() : null;
        String conversationId = body.get("conversationId") != null ? body.get("conversationId").toString() : null;
        String userId = body.get("userId") != null ? body.get("userId").toString() : null;
        List<Map<String, Object>> files = (List<Map<String, Object>>) body.get("files");

        if ((message == null || message.trim().isEmpty()) && (files == null || files.isEmpty())) {
            return R.fail("消息内容不能为空");
        }

        log.info("收到 Dify 对话请求, message: {}, conversationId: {}, userId: {}, files: {}", message, conversationId, userId, files);

        DifyChatResponse response = difyAiService.chat(message, conversationId, userId, files);
        
        return R.ok(response);
    }

    @Operation(summary = "上传文件到 Dify")
    @PostMapping("/files/upload")
    public R<Object> uploadFile(@RequestParam("file") MultipartFile file, @RequestParam("user") String user) {
        log.info("收到 Dify 文件上传请求, user: {}, fileName: {}", user, file.getOriginalFilename());
        try {
            Object result = difyAiService.uploadFile(file, user);
            return R.ok(result);
        } catch (Exception e) {
            log.error("上传文件到 Dify 失败", e);
            return R.fail("上传文件失败: " + e.getMessage());
        }
    }

    @Operation(summary = "停止 Dify 响应")
    @PostMapping("/chat-messages/{taskId}/stop")
    public R<Object> stopResponse(@PathVariable("taskId") String taskId, @RequestBody Map<String, Object> body) {
        String user = body.get("user") != null ? body.get("user").toString() : null;
        log.info("收到 Dify 停止响应请求, taskId: {}, user: {}", taskId, user);
        try {
            Object result = difyAiService.stopResponse(taskId, user);
            return R.ok(result);
        } catch (Exception e) {
            log.error("停止 Dify 响应失败", e);
            return R.fail("停止响应失败: " + e.getMessage());
        }
    }

    @Operation(summary = "获取 Dify 会话历史消息")
    @GetMapping("/messages")
    public R<Object> getMessages(
            @RequestParam("conversation_id") String conversationId,
            @RequestParam("user") String user,
            @RequestParam(value = "first_id", required = false) String firstId,
            @RequestParam(value = "limit", required = false) Integer limit) {
        log.info("收到 Dify 获取历史消息请求, conversationId: {}, user: {}, limit: {}", conversationId, user, limit);
        try {
            Object result = difyAiService.getMessages(conversationId, user, firstId, limit);
            return R.ok(result);
        } catch (Exception e) {
            log.error("获取 Dify 历史消息失败", e);
            return R.fail("获取历史消息失败: " + e.getMessage());
        }
    }

    @Operation(summary = "获取 Dify 会话列表")
    @GetMapping("/conversations")
    public R<Object> getConversations(
            @RequestParam("user") String user,
            @RequestParam(value = "last_id", required = false) String lastId,
            @RequestParam(value = "limit", required = false) Integer limit,
            @RequestParam(value = "sort_by", required = false) String sortBy) {
        log.info("收到 Dify 获取会话列表请求, user: {}, limit: {}", user, limit);
        try {
            Object result = difyAiService.getConversations(user, lastId, limit, sortBy);
            return R.ok(result);
        } catch (Exception e) {
            log.error("获取 Dify 会话列表失败", e);
            return R.fail("获取会话列表失败: " + e.getMessage());
        }
    }

    @Operation(summary = "删除 Dify 会话")
    @DeleteMapping("/conversations/{conversationId}")
    public R<Object> deleteConversation(
            @PathVariable("conversationId") String conversationId,
            @RequestBody Map<String, String> body) {
        String user = body.get("user");
        log.info("收到 Dify 删除会话请求, conversationId: {}, user: {}", conversationId, user);
        try {
            Object result = difyAiService.deleteConversation(conversationId, user);
            return R.ok(result);
        } catch (Exception e) {
            log.error("删除 Dify 会话失败", e);
            return R.fail("删除会话失败: " + e.getMessage());
        }
    }
}
