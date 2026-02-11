package com.soccer.forum.service.service;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.spring.AiService;

/**
 * 足球 AI 服务接口
 * <p>
 * 基于 LangChain4j 提供智能对话、评论生成和专业问答功能。
 * </p>
 */
@AiService
public interface FootballAiService {

    /**
     * 自由对话模式
     *
     * @param message 用户消息
     * @return AI 回复
     */
    @SystemMessage("你是一个资深的足球评论员和战术分析师，名字叫'懂球帝AI'。你的语言风格幽默、犀利，对世界足坛的历史、战术和球星如数家珍。请用中文回答用户的问题。")
    String chat(@UserMessage String message);

    /**
     * 生成帖子评论
     *
     * @param postContent 帖子内容
     * @return AI 生成的评论
     */
    @SystemMessage("你是一个热情的足球社区用户。请阅读以下帖子内容，并给出一个简短、有趣且切中要害的评论。评论长度控制在 50 字以内。")
    String generateComment(@UserMessage String postContent);

    /**
     * 足球专业问答
     *
     * @param question 用户的问题
     * @return 专业解答
     */
    @SystemMessage("你是一个足球百科全书。请准确、客观地回答用户的足球相关问题。如果问题涉及具体的比赛数据或历史记录，请确保准确性。如果不知道，请诚实回答。")
    String answerQuestion(@UserMessage String question);

    /**
     * 生成比赛战报
     *
     * @param matchData 比赛数据（如比分、进球时间、红黄牌等）
     * @return AI 生成的战报
     */
    @SystemMessage("你是一名专业的体育记者。请根据提供的比赛数据（比分、事件等），写一篇激情澎湃、细节丰富的比赛战报。战报应包含引人注目的标题、比赛综述、关键时刻回顾和总结。")
    String generateMatchReport(@UserMessage String matchData);

    /**
     * 战术分析
     *
     * @param tacticalInfo 球队阵容或战术描述
     * @return 战术分析报告
     */
    @SystemMessage("你是一名顶级的足球战术分析师。请根据提供的球队阵容或战术描述，深度分析其优缺点、核心打法以及可能的克制策略。输出内容应专业、条理清晰。")
    String analyzeTactics(@UserMessage String tacticalInfo);
}
