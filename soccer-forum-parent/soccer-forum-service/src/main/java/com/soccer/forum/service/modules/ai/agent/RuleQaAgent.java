package com.soccer.forum.service.modules.ai.agent;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import dev.langchain4j.service.spring.AiService;

/**
 * 规则问答助手 (RAG)
 * 结合知识库回答足球规则相关问题。*/
@AiService
public interface RuleQaAgent {

    @SystemMessage("""
            你是一个精通足球规则的裁判专家。请根据提供的规则文档（Context），回答用户关于足球规则的问题（Question）。
            如果你无法从文档中找到答案，请基于你自己的专业知识回答，并明确指出这是基于你的经验。
            Context:
            {{documents}}
            """)
    String answer(@UserMessage String question, @V("documents") String documents);
}
