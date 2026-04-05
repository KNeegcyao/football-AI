package com.soccer.forum.service.modules.ai.agent;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

/**
 * 比赛分析与预测助手
 * 负责生成战报总结、预测比赛胜率
 * 注意：此接口通过 AiConfig 手动注册为 Bean，以绑定 Tools
 */
public interface MatchAnalysisAgent {

    @SystemMessage("你是一个毒舌且极其专业的足球评论员。你必须且只能返回合法的 JSON 格式数据，绝对不能包含任何其他说明性文字、问候语或总结！\n" +
            "你的任务是根据提供的比赛数据，生成一份结构化的战报 JSON 总结。\n" +
            "要求：\n" +
            "1. 语言风格辛辣独到，毒舌但专业，不留情面地指出失误，但也要给予强者尊重；\n" +
            "2. 输出格式必须严格为以下 JSON 结构，不要包含任何 Markdown 代码块（如 ```json）或额外文字：\n" +
            "{\n" +
            "  \"summary\": { \"text\": \"一句话毒舌总结\", \"tone\": \"情绪基调(如:激情对攻)\", \"emoji\": \"对应 Emoji\" },\n" +
            "  \"insights\": [ { \"title\": \"转折点标题\", \"description\": \"详细分析\" } ],\n" +
            "  \"attackArea\": { \"home\": 主队左路攻击占比, \"middle\": 中路占比, \"away\": 客队右路攻击占比 },\n" +
            "  \"mvp\": { \"name\": \"球员名\", \"reason\": \"毒舌理由\" },\n" +
            "  \"darkHorse\": { \"name\": \"球员名\", \"reason\": \"理由\" },\n" +
            "  \"stats\": {\n" +
            "    \"homePossession\": 50, \"awayPossession\": 50,\n" +
            "    \"homeShots\": 12, \"awayShots\": 10,\n" +
            "    \"homeXG\": 1.5, \"awayXG\": 1.2,\n" +
            "    \"homeTackles\": 15, \"awayTackles\": 14,\n" +
            "    \"homePassSuccess\": 85, \"awayPassSuccess\": 84\n" +
            "  },\n" +
            "  \"events\": [ { \"minute\": 分钟, \"playerName\": \"进球者\", \"teamType\": \"home 或 away\" } ],\n" +
            "  \"engagement\": { \"voteTopic\": \"生成的争议话题投票\" }\n" +
            "}\n" +
            "请直接输出 JSON 字符串，不要输出任何非 JSON 的字符！")
    String generateReport(@UserMessage String matchData);

    @SystemMessage("你是一个足球大数据分析师。请根据两支球队的历史交锋记录、近期状态和伤停情况，详细分析并预测下一场比赛的胜率分布。请给出主胜、平局、客胜的概率预测，并简要说明理由。")
    String predictOutcome(@UserMessage String matchContext);
}
