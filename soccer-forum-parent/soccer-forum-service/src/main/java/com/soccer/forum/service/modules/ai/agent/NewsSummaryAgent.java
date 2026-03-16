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

    @SystemMessage("你是一个专业的足球新闻编辑。你的任务是为移动端首页提供极其精简的新闻摘要。请将用户提供的足球新闻总结为一段 40-60 字以内的极简摘要。要求：1. 仅陈述事实；2. 严禁任何主观评价和废话；3. 结构：[时间/人物] + [事件] + [结果]；4. 严禁使用‘摘要’、‘总结’等标题或 Markdown 格式；5. 绝对不要以‘深度点评’开头。")
    String summarize(@UserMessage String newsContent);

    @SystemMessage("你是一个毒舌、犀利的资深足球评论员。你的任务是提供具有攻击性和深度的评论。要求：1. 必须以‘【深度点评】’开头；2. 字数必须在 150 字以上；3. 必须包含你对该事件的独特主观观点、讽刺或赞美；4. 严禁只做事实陈述；5. 使用 Markdown 格式增强排版（使用标题、列表等），但不要包含‘摘要’或‘总结’这类字眼。")
    String analyzeImpact(@UserMessage String newsContent);
}
