package com.soccer.forum.service.controller;

import com.soccer.forum.common.core.domain.R;
import com.soccer.forum.service.service.FootballAiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * AI 智能助手控制器
 * <p>
 * 提供 AI 聊天、问答和自动评论生成接口。
 * </p>
 */
@Tag(name = "AI助手", description = "AI智能助手接口")
@RestController
@RequestMapping("/api/ai")
public class AiController {

    private static final Logger log = LoggerFactory.getLogger(AiController.class);

    private final FootballAiService aiService;

    public AiController(FootballAiService aiService) {
        this.aiService = aiService;
    }

    /**
     * 自由对话接口
     *
     * @param message 用户消息
     * @return AI 回复
     */
    @Operation(summary = "自由对话", description = "与 AI 足球专家自由聊天")
    @GetMapping("/chat")
    public R<Map<String, String>> chat(@Parameter(description = "聊天消息内容") @RequestParam String message) {
        log.info("收到 AI 聊天请求: message={}", message);
        String response = aiService.chat(message);
        return R.ok(Map.of("response", response));
    }

    /**
     * 足球专业问答接口
     *
     * @param question 用户问题
     * @return 专业解答
     */
    @Operation(summary = "专业问答", description = "向 AI 提问足球相关知识")
    @GetMapping("/ask")
    public R<Map<String, String>> ask(@Parameter(description = "提问的问题内容") @RequestParam String question) {
        log.info("收到 AI 问答请求: question={}", question);
        String response = aiService.answerQuestion(question);
        return R.ok(Map.of("response", response));
    }

    /**
     * 生成帖子评论接口
     *
     * @param body 包含内容的对象
     * @return AI 生成的评论
     */
    @Operation(summary = "生成评论", description = "AI 根据帖子内容生成评论")
    @PostMapping("/comment/generate")
    public R<Map<String, String>> generateComment(@Parameter(description = "包含 content 字段的 JSON 对象") @RequestBody Map<String, String> body) {
        String content = body.get("content");
        log.info("收到生成评论请求: content={}", content);
        String comment = aiService.generateComment(content);
        return R.ok(Map.of("comment", comment));
    }

    /**
     * 生成比赛战报接口
     *
     * @param body 比赛数据
     * @return AI 生成的战报
     */
    @Operation(summary = "生成战报", description = "根据比赛数据生成专业战报")
    @PostMapping("/match/report")
    public R<Map<String, String>> generateMatchReport(@Parameter(description = "包含 matchData 字段的 JSON 对象") @RequestBody Map<String, String> body) {
        String matchData = body.get("matchData");
        log.info("收到生成战报请求: matchData={}", matchData);
        String report = aiService.generateMatchReport(matchData);
        return R.ok(Map.of("report", report));
    }

    /**
     * 战术分析接口
     *
     * @param body 战术/阵容信息
     * @return 战术分析结果
     */
    @Operation(summary = "战术分析", description = "根据阵容或描述进行战术分析")
    @PostMapping("/tactics/analyze")
    public R<Map<String, String>> analyzeTactics(@Parameter(description = "包含 tacticalInfo 字段的 JSON 对象") @RequestBody Map<String, String> body) {
        String tacticalInfo = body.get("tacticalInfo");
        log.info("收到战术分析请求: tacticalInfo={}", tacticalInfo);
        String analysis = aiService.analyzeTactics(tacticalInfo);
        return R.ok(Map.of("analysis", analysis));
    }
}
