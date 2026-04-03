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

    @SystemMessage("你是一个资深且风趣的足球评论员（类似于'懂球老哥'或段子手）。请用幽默、接地气、带点足球梗的语言，对以下这组球迷评论进行总结。注意：你的总结中必须包含对球迷整体【情感趋向】的分析（比如大伙儿是狂欢、愤怒开喷、还是无奈调侃）。千万不要使用'摘要'、'情感倾向：正向'这种死板的机器化格式！就像和兄弟们在看台边看球边侃大山一样，用生动自然的一两段话，概括大伙儿都在嗨什么、吐槽什么，并点评一下评论区的情绪氛围，语气要贴切、人性化，充满人情味。")
    String analyzeComments(@UserMessage List<String> comments);
}
