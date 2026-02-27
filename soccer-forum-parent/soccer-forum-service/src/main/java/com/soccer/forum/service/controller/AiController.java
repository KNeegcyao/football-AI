package com.soccer.forum.service.controller;

import com.soccer.forum.common.core.domain.R;
import com.soccer.forum.service.service.FootballAiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@Tag(name = "AI 服务", description = "AI 足球助手相关接口")
public class AiController {

    private static final Logger log = LoggerFactory.getLogger(AiController.class);

    @Autowired
    private FootballAiService aiService;

    @Operation(summary = "自由对话", description = "与 AI 足球专家自由聊天")
    @GetMapping("/chat")
    public R<Map<String, String>> chat(@Parameter(description = "聊天消息内容") @RequestParam String message) {
        log.info("收到 AI 聊天请求: message={}", message);
        String response = aiService.chat(message);
        return R.ok(Map.of("response", response));
    }
}
