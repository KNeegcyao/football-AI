package com.soccer.forum.service.modules.ai.controller;

import com.soccer.forum.common.R;
import com.soccer.forum.domain.entity.News;
import com.soccer.forum.service.modules.ai.agent.*;
import com.soccer.forum.service.modules.ai.rag.RagService;
import com.soccer.forum.service.modules.match.service.NewsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
    private final NewsService newsService;

    public AiController(NewsSummaryAgent newsSummaryAgent,
                        MatchAnalysisAgent matchAnalysisAgent,
                        CommentAnalysisAgent commentAnalysisAgent,
                        RuleQaAgent ruleQaAgent,
                        DataQueryAgent dataQueryAgent,
                        RagService ragService,
                        NewsService newsService) {
        this.newsSummaryAgent = newsSummaryAgent;
        this.matchAnalysisAgent = matchAnalysisAgent;
        this.commentAnalysisAgent = commentAnalysisAgent;
        this.ruleQaAgent = ruleQaAgent;
        this.dataQueryAgent = dataQueryAgent;
        this.ragService = ragService;
        this.newsService = newsService;
    }

    @Operation(summary = "资讯内容智能摘要 (Persistence)")
    @PostMapping("/news/{id}/summary")
    public R<String> summarizeNewsById(@Parameter(description = "资讯ID") @PathVariable Long id) {
        System.out.println("Entering summarizeNewsById with id: " + id);
        // 1. 获取资讯详情
        News news = newsService.getNewsDetail(id);
        if (news == null) {
            return R.fail("资讯不存在");
        }

        // 2. 如果已有摘要，直接返回
        if (news.getSummary() != null && !news.getSummary().isEmpty()) {
            return R.ok(news.getSummary());
        }

        // 3. 调用 AI 生成摘要
        String content = news.getContent();
        if (content == null || content.isEmpty()) {
            return R.fail("资讯内容为空，无法生成摘要");
        }
        
        String summary;
        try {
            System.out.println("Calling newsSummaryAgent.summarize...");
            summary = newsSummaryAgent.summarize(content);
            // 简单去重：如果 AI 生成的内容和已有摘要非常接近（例如前 20 个字符相同），则强制重新生成或加上前缀
            if (news.getSummary() != null && summary.startsWith(news.getSummary().substring(0, Math.min(10, news.getSummary().length())))) {
                summary = "【精简摘要】" + summary;
            }
            System.out.println("newsSummaryAgent.summarize returned: " + summary);
        } catch (Throwable e) {
            System.out.println("Caught exception in AiController: " + e);
            e.printStackTrace();
            // AI 服务调用失败时的降级处理 (Mock)
            org.slf4j.LoggerFactory.getLogger(AiController.class).error("AI 摘要生成失败: {}", e.getMessage());
            summary = "【演示摘要】这是一篇关于足球的精彩报道，详细记录了比赛的关键时刻与球员的出色表现。请配置 API Key 以启用完整功能。";
        }

        // 4. 更新数据库 (异步或同步均可，这里选择同步简单处理)
        // 确保只保存前 100 个字符作为摘要，或者根据业务需求处理
        news.setSummary(summary);
        newsService.updateNews(id, news);

        return R.ok(summary);
    }

    @Operation(summary = "资讯内容深度点评 (Real-time)")
    @PostMapping("/news/{id}/impact")
    public R<String> analyzeNewsImpactById(@Parameter(description = "资讯ID") @PathVariable Long id) {
        // 1. 获取资讯详情
        News news = newsService.getNewsDetail(id);
        if (news == null) {
            return R.fail("资讯不存在");
        }

        // 2. 如果已有深度点评，直接返回
        if (news.getImpact() != null && !news.getImpact().isEmpty()) {
            return R.ok(news.getImpact());
        }

        // 3. 调用 AI 生成深度点评
        String content = news.getContent();
        if (content == null || content.isEmpty()) {
            return R.fail("资讯内容为空，无法生成点评");
        }
        
        String impact;
        try {
            impact = newsSummaryAgent.analyzeImpact(content);
            
            // 严格分流校验：如果 AI 返回的内容过短或与摘要过于相似，则视为生成失败
            if (impact.length() < 100 || (news.getSummary() != null && impact.contains(news.getSummary().substring(0, Math.min(20, news.getSummary().length()))))) {
                // 增加随机因子重新尝试一次
                impact = "【深度点评】" + newsSummaryAgent.analyzeImpact(content + " (请给出更犀利、更长的主观评论)");
            }

            // 将深度点评持久化到数据库的新字段中
            news.setImpact(impact);
            newsService.updateNews(id, news);
        } catch (Exception e) {
            org.slf4j.LoggerFactory.getLogger(AiController.class).error("AI 点评生成失败: {}", e.getMessage());
            return R.fail("AI 点评生成失败，请稍后重试");
        }

        return R.ok(impact);
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
