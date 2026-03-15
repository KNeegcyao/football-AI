package com.soccer.forum.service.modules.ai.agent;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

/**
 * 比赛分析与预测助手
 * 负责生成战报总结、预测比赛胜率
 * 注意：此接口通过 AiConfig 手动注册为 Bean，以绑定 Tools
 */
public interface MatchAnalysisAgent {

    @SystemMessage("你是一个专业的足球解说员。请根据提供的比赛数据和关键事件，撰写一篇激情洋溢的赛后战报总结。重点关注进球、红黄牌等关键时刻，语言风格要专业且富有感染力。")
    String generateReport(@UserMessage String matchData);

    @SystemMessage("你是一个足球大数据分析师。请根据两支球队的历史交锋记录、近期状态和伤停情况，详细分析并预测下一场比赛的胜率分布。请给出主胜、平局、客胜的概率预测，并简要说明理由。")
    String predictOutcome(@UserMessage String matchContext);
}
