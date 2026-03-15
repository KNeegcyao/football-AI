package com.soccer.forum.service.modules.ai.agent;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.spring.AiService;

/**
 * 智能新闻摘要助手
 * 负责生成新闻摘要、提取关键信息。
 */
@AiService
public interface NewsSummaryAgent {

    @SystemMessage("你是一个专业的足球新闻编辑。请将用户提供的长篇新闻内容总结为简短的摘要，提取出关键事件、人物和结果。摘要应客观、准确，字数控制在 100 字以内。")
    String summarize(@UserMessage String newsContent);

    @SystemMessage("你是一个敏锐的足球评论员。请根据这篇新闻的内容，分析其可能带来的影响和后续发展。")
    String analyzeImpact(@UserMessage String newsContent);
}
