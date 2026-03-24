package com.soccer.forum.service.modules.ai.controller;

import com.soccer.forum.common.R;
import com.soccer.forum.domain.entity.News;
import com.soccer.forum.service.modules.ai.agent.*;
import com.soccer.forum.service.modules.ai.rag.RagService;
import com.soccer.forum.service.modules.ai.service.FootballAiService;
import com.soccer.forum.service.modules.ai.service.SttService;
import com.soccer.forum.service.modules.match.service.NewsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

import com.soccer.forum.service.modules.user.model.LoginUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * AI 控制层
 */
@Tag(name = "AI 助手接口 (V1)")
@RestController
@RequestMapping("/api/ai")
public class AiController {

    private static final Logger log = LoggerFactory.getLogger(AiController.class);

    private final NewsSummaryAgent newsSummaryAgent;
    private final MatchAnalysisAgent matchAnalysisAgent;
    private final CommentAnalysisAgent commentAnalysisAgent;
    private final RuleQaAgent ruleQaAgent;
    private final DataQueryAgent dataQueryAgent;
    private final AssistantAgent assistantAgent;
    private final FootballAiService footballAiService;
    private final RagService ragService;
    private final NewsService newsService;
    private final SttService sttService;

    public AiController(NewsSummaryAgent newsSummaryAgent,
                        MatchAnalysisAgent matchAnalysisAgent,
                        CommentAnalysisAgent commentAnalysisAgent,
                        RuleQaAgent ruleQaAgent,
                        DataQueryAgent dataQueryAgent,
                        AssistantAgent assistantAgent,
                        FootballAiService footballAiService,
                        RagService ragService,
                        NewsService newsService,
                        SttService sttService) {
        this.newsSummaryAgent = newsSummaryAgent;
        this.matchAnalysisAgent = matchAnalysisAgent;
        this.commentAnalysisAgent = commentAnalysisAgent;
        this.ruleQaAgent = ruleQaAgent;
        this.dataQueryAgent = dataQueryAgent;
        this.assistantAgent = assistantAgent;
        this.footballAiService = footballAiService;
        this.ragService = ragService;
        this.newsService = newsService;
        this.sttService = sttService;
    }

    @Operation(summary = "语音转文字 (STT)")
    @PostMapping(value = "/stt", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public R<String> transcribe(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) return R.fail("文件不能为空");
        log.info("收到语音文件: {}, 大小: {} bytes", file.getOriginalFilename(), file.getSize());
        String text = sttService.transcribe(file);
        return R.ok(text);
    }

    @Operation(summary = "通用智能对话 (RAG)")
    @PostMapping("/chat-rag")
    public R<String> chat(@RequestBody Map<String, String> body, jakarta.servlet.http.HttpServletRequest request) {
        String question = body.get("question");
        if (question == null) return R.fail("问题不能为空");
        
        // 获取当前用户ID或会话ID作为记忆ID
        String memoryIdStr = getMemoryId(request);
        // 如果是数字ID，转为 Long；否则转为 Hash 值（Long）
        Long memoryId = parseMemoryId(memoryIdStr);
        
        log.info("用户 {} 发起 AI 对话: {}", memoryId, question);

        try {
            // 1. RAG 流程：检索相关知识
            String context = "";
            // 增加 RAG 触发阈值：只有问题长度 > 3 且不是简单的确认性词汇时才触发
            if (shouldTriggerRag(question)) {
                try {
                    context = ragService.retrieveAsString(question);
                } catch (Throwable t) {
                    log.error("RAG 检索异常: {}", t.getMessage());
                }
            } else {
                log.info("问题过短或为确认词，跳过 RAG 检索: {}", question);
            }
            
            // 2. 调用 AI 助手（传递 memoryId 以启用记忆功能）
            log.info("开始调用 AssistantAgent.chat, memoryId: {}, question: {}, context length: {}", memoryId, question, context.length());
            String response = assistantAgent.chat(memoryId, question, context);
            log.info("AssistantAgent.chat 调用成功, response length: {}", response != null ? response.length() : 0);
            return R.ok(response);
        } catch (Throwable e) {
            log.error("AI 对话失败, 异常类型: {}, 错误消息: {}", e.getClass().getName(), e.getMessage(), e);
            
            // 3. 容错处理：尝试使用基础的百科问答 Agent 兜底
             try {
                 log.info("尝试使用 FootballAiService 兜底回答, question: {}", question);
                 String fallbackResponse = footballAiService.answerQuestion(question);
                 log.info("FootballAiService 兜底回答成功");
                 return R.ok(fallbackResponse);
             } catch (Throwable ex) {
                 log.error("兜底回答也失败了, 异常类型: {}, 错误消息: {}", ex.getClass().getName(), ex.getMessage(), ex);
                 return R.fail("抱歉，我现在遇到了一点问题，请稍后再试。详细原因: " + e.getMessage());
             }
        }
    }

    /**
     * 获取记忆ID，优先使用用户ID，未登录则使用 Session ID
     */
    private String getMemoryId(jakarta.servlet.http.HttpServletRequest request) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() instanceof LoginUser) {
                return ((LoginUser) auth.getPrincipal()).getUser().getId().toString();
            }
        } catch (Exception e) {
            log.warn("获取登录用户ID失败，回退到 Session ID: {}", e.getMessage());
        }
        // 使用 Session ID 保证未登录用户也有独立的对话记忆
        return request.getSession().getId();
    }

    /**
     * 将 String 类型的 memoryId 转换为 Long
     */
    private Long parseMemoryId(String id) {
        try {
            return Long.parseLong(id);
        } catch (NumberFormatException e) {
            // 如果不是纯数字（如 Session ID），使用其 hashCode 作为 Long ID
            return (long) id.hashCode();
        }
    }

    /**
     * 判断是否应该触发 RAG 检索
     * 避免“是的”、“好”、“谢谢”等短句触发无关的背景知识检索导致 AI 分心
     */
    private boolean shouldTriggerRag(String question) {
        if (question == null || question.trim().length() < 3) return false;

        // 排除常见的确认性词汇
        String lowerQ = question.trim().toLowerCase();
        List<String> skipWords = java.util.Arrays.asList("是的", "是的。", "对", "对的", "好的", "好的。", "ok", "确认", "谢谢", "不客气", "再见", "你好");
        if (skipWords.contains(lowerQ)) return false;

        return true;
    }

    @Operation(summary = "生成机智回复")
    @PostMapping("/comment/generate")
    public R<String> generateComment(@RequestBody Map<String, String> body) {
        String content = body.get("content");
        if (content == null || content.isEmpty()) {
            content = "这是一场非常精彩的比赛！";
        }
        return R.ok(footballAiService.generateComment(content));
    }

    @Operation(summary = "战术深度分析")
    @PostMapping("/tactics/analyze")
    public R<String> analyzeTactics(@RequestBody Map<String, String> body) {
        String info = body.get("info");
        if (info == null) return R.fail("战术信息不能为空");
        return R.ok(footballAiService.analyzeTactics(info));
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

        // 4. 更新数据库
        // 按照用户绝对要求：20 字总结，强制截断 20 字符
        if (summary != null && summary.length() > 20) {
            summary = summary.substring(0, 20);
        }
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
