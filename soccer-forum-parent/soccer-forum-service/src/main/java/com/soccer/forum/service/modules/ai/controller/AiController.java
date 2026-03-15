package com.soccer.forum.service.modules.ai.controller;

import com.soccer.forum.common.R;
import com.soccer.forum.service.modules.ai.agent.*;
import com.soccer.forum.service.modules.ai.rag.RagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * AI 智能服务控制器
 */
@Tag(name = "AI 智能服务", description = "提供新闻摘要、战报生成、评论分析和规则问答等 AI 能力")
@RestController
@RequestMapping("/api/ai")
public class AiController {

    private final NewsSummaryAgent newsSummaryAgent;
    private final MatchAnalysisAgent matchAnalysisAgent;
    private final CommentAnalysisAgent commentAnalysisAgent;
    private final RuleQaAgent ruleQaAgent;
    private final DataQueryAgent dataQueryAgent;
    private final RagService ragService;

    public AiController(NewsSummaryAgent newsSummaryAgent,
                        MatchAnalysisAgent matchAnalysisAgent,
                        CommentAnalysisAgent commentAnalysisAgent,
                        RuleQaAgent ruleQaAgent,
                        DataQueryAgent dataQueryAgent,
                        RagService ragService) {
        this.newsSummaryAgent = newsSummaryAgent;
        this.matchAnalysisAgent = matchAnalysisAgent;
        this.commentAnalysisAgent = commentAnalysisAgent;
        this.ruleQaAgent = ruleQaAgent;
        this.dataQueryAgent = dataQueryAgent;
        this.ragService = ragService;
    }

    @Operation(summary = "智能新闻摘要")
    @PostMapping("/summary")
    public R<Object> summarizeNews(@RequestBody Map<String, Object> body) {
        String content = (String) body.get("content");
        if (content == null) return R.fail("内容不能为空");
        
        // 检查是否需要深度点评
        Boolean withImpact = (Boolean) body.getOrDefault("withImpact", false);
        
        String summary = newsSummaryAgent.summarize(content);
        
        if (Boolean.TRUE.equals(withImpact)) {
            String impact = newsSummaryAgent.analyzeImpact(content);
            return R.ok(Map.of(
                "summary", summary,
                "impact", impact
            ));
        }
        
        return R.ok(summary);
    }

    @Operation(summary = "新闻深度点评")
    @PostMapping("/news/impact")
    public R<String> analyzeNewsImpact(@RequestBody Map<String, String> body) {
        String content = body.get("content");
        if (content == null) return R.fail("内容不能为空");
        return R.ok(newsSummaryAgent.analyzeImpact(content));
    }

    @Operation(summary = "赛后战报生成")
    @PostMapping("/match/report")
    public R<String> generateMatchReport(@RequestBody Map<String, String> body) {
        String matchData = body.get("matchData");
        if (matchData == null) return R.fail("比赛数据不能为空");
        return R.ok(matchAnalysisAgent.generateReport(matchData));
    }

    @Operation(summary = "胜率预测")
    @PostMapping("/match/predict")
    public R<String> predictMatchOutcome(@RequestBody Map<String, String> body) {
        String teamA = body.get("teamA");
        String teamB = body.get("teamB");
        String recentForm = body.get("recentForm");
        String context = String.format("主队：%s, 客队：%s, 近期状态：%s", teamA, teamB, recentForm);
        return R.ok(matchAnalysisAgent.predictOutcome(context));
    }

    @Operation(summary = "评论情感分析")
    @PostMapping("/comment/analyze")
    public R<String> analyzeComments(@RequestBody List<String> comments) {
        if (comments == null || comments.isEmpty()) return R.fail("评论列表不能为空");
        return R.ok(commentAnalysisAgent.analyzeComments(comments));
    }

    @Operation(summary = "足球规则问答 (RAG)")
    @PostMapping("/qa/rule")
    public R<String> askRule(@RequestBody Map<String, String> body) {
        String question = body.get("question");
        if (question == null) return R.fail("问题不能为空");
        
        // RAG 流程：先检索再回答
        String documents = ragService.retrieveAsString(question);
        return R.ok(ruleQaAgent.answer(question, documents));
    }

    @Operation(summary = "球队/球星数据智能查询 (Tool)")
    @PostMapping("/query/data")
    public R<String> queryData(@RequestBody Map<String, String> body) {
        String question = body.get("question");
        if (question == null) return R.fail("问题不能为空");
        return R.ok(dataQueryAgent.query(question));
    }
}
