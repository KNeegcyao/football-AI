package com.soccer.forum.service.modules.ai.config;

import com.soccer.forum.service.modules.ai.agent.DataQueryAgent;
import com.soccer.forum.service.modules.ai.agent.MatchAnalysisAgent;
import com.soccer.forum.service.modules.ai.tool.SoccerTools;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.model.zhipu.ZhipuAiChatModel;
import java.util.List;
import dev.langchain4j.model.embedding.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * AI 配置类
 * 负责初始化 ChatLanguageModel, EmbeddingModel, EmbeddingStore 等组件
 */
@Configuration
public class AiConfig {

    @Value("${langchain4j.zhipu-ai.chat-model.api-key}")
    private String zhipuApiKey;

    @Value("${langchain4j.zhipu-ai.chat-model.model-name:glm-4-flash}")
    private String zhipuModelName;

    /**
     * 配置智谱 AI 聊天模型
     * 显式定义为 Primary，确保系统中优先使用智谱 AI
     */
    @Bean
    @Primary
    public ChatLanguageModel chatLanguageModel() {
        if (zhipuApiKey == null || zhipuApiKey.trim().isEmpty() || "your-key-here".equals(zhipuApiKey)) {
            System.err.println("警告: 智谱 AI API Key 未配置或为默认值，AI 功能将不可用！");
            return new ChatLanguageModel() {
                @Override
                public String generate(String userMessage) {
                    return "AI 功能未启用，请配置 API Key。";
                }
                @Override
                public Response<AiMessage> generate(List<ChatMessage> messages) {
                    return Response.from(AiMessage.from("AI 功能未启用，请配置 API Key。"));
                }
                @Override
                public Response<AiMessage> generate(List<ChatMessage> messages, List<ToolSpecification> toolSpecifications) {
                    return Response.from(AiMessage.from("AI 功能未启用，请配置 API Key。"));
                }
                @Override
                public Response<AiMessage> generate(List<ChatMessage> messages, ToolSpecification toolSpecification) {
                    return Response.from(AiMessage.from("AI 功能未启用，请配置 API Key。"));
                }
            };
        }
        return ZhipuAiChatModel.builder()
                .apiKey(zhipuApiKey)
                .model(zhipuModelName)
                .logRequests(true)
                .logResponses(true)
                .build();
    }

    /**
     * 配置聊天记忆提供者
     * 这里使用基于消息窗口的简单内存实现，保留最近 10 条消息
     */
    @Bean
    public ChatMemoryProvider chatMemoryProvider() {
        return memoryId -> MessageWindowChatMemory.withMaxMessages(10);
    }

    /**
     * 配置嵌入模型
     * 使用本地轻量级模型 all-minilm-l6-v2，无需 API Key，适合演示和简单 RAG
     */
    @Bean
    public EmbeddingModel embeddingModel() {
        return new AllMiniLmL6V2EmbeddingModel();
    }

    /**
     * 配置向量存储
     * 这里使用内存存储，重启后数据丢失。生产环境建议使用 Pinecone, Milvus, Chroma 等
     */
    @Bean
    public EmbeddingStore<TextSegment> embeddingStore() {
        return new InMemoryEmbeddingStore<>();
    }

    /**
     * 手动构建 DataQueryAgent，绑定 Tools
     */
    @Bean
    public DataQueryAgent dataQueryAgent(ChatLanguageModel chatLanguageModel, SoccerTools soccerTools) {
        return AiServices.builder(DataQueryAgent.class)
                .chatLanguageModel(chatLanguageModel)
                .tools(soccerTools)
                .chatMemoryProvider(chatMemoryProvider())
                .build();
    }

    /**
     * 手动构建 MatchAnalysisAgent，绑定 Tools
     */
    @Bean
    public MatchAnalysisAgent matchAnalysisAgent(ChatLanguageModel chatLanguageModel, SoccerTools soccerTools) {
        return AiServices.builder(MatchAnalysisAgent.class)
                .chatLanguageModel(chatLanguageModel)
                .tools(soccerTools)
                .chatMemoryProvider(chatMemoryProvider())
                .build();
    }
}
