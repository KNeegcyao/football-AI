package com.soccer.forum.service.modules.ai.agent;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.spring.AiService;

import java.util.List;

/**
 * 话题评论分析助手
 * 负责对话题下的评论进行情感分析、观点总结
 */
@AiService
public interface CommentAnalysisAgent {

    @SystemMessage("你是一个舆情分析师。请对以下的一组用户评论进行总结，概括主要观点、情感倾向（积极/消极/中立）以及争议焦点。输出应包含摘要和关键论点。")
    String analyzeComments(@UserMessage List<String> comments);
}
