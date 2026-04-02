package com.soccer.forum.service.modules.ai.config;

import com.soccer.forum.service.modules.ai.agent.AssistantAgent;
import com.soccer.forum.service.modules.ai.agent.CommentAnalysisAgent;
import com.soccer.forum.service.modules.ai.agent.DataQueryAgent;
import com.soccer.forum.service.modules.ai.agent.MatchAnalysisAgent;
import com.soccer.forum.service.modules.ai.agent.NewsSummaryAgent;
import com.soccer.forum.service.modules.ai.agent.RuleQaAgent;
import com.soccer.forum.service.modules.ai.service.FootballAiService;
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
import java.time.Duration;
import java.util.List;
import dev.langchain4j.model.embedding.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.data.embedding.Embedding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger log = LoggerFactory.getLogger(AiConfig.class);

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
            log.warn("智谱 AI API Key 未配置或为默认值，AI 功能将不可用！");
            return new ChatLanguageModel() {
                @Override
                public String generate(String userMessage) {
                    return "AI 功能未启用，请配置有效的 API Key。";
                }
                @Override
                public Response<AiMessage> generate(List<ChatMessage> messages) {
                    return Response.from(AiMessage.from("AI 功能未启用，请配置有效的 API Key。"));
                }
                @Override
                public Response<AiMessage> generate(List<ChatMessage> messages, List<ToolSpecification> toolSpecifications) {
                    return Response.from(AiMessage.from("AI 功能未启用，请配置有效的 API Key。"));
                }
                @Override
                public Response<AiMessage> generate(List<ChatMessage> messages, ToolSpecification toolSpecification) {
                    return Response.from(AiMessage.from("AI 功能未启用，请配置有效的 API Key。"));
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
     * 使用本地轻量级模型 all-minilm-l6-v2
     * 增加异常捕获，如果本地模型加载失败，返回一个 Mock 实现，避免整个系统启动失败或崩溃
     */
    @Bean
    public EmbeddingModel embeddingModel() {
        try {
            log.info("正在加载本地嵌入模型 all-minilm-l6-v2...");
            return new AllMiniLmL6V2EmbeddingModel();
        } catch (Exception e) {
            log.error("本地嵌入模型加载失败，切换到 Mock 模式: {}", e.getMessage());
            return new EmbeddingModel() {
                @Override
                public Response<Embedding> embed(String text) {
                    return Response.from(Embedding.from(new float[384])); // 返回全 0 向量
                }

                @Override
                public Response<Embedding> embed(TextSegment textSegment) {
                    return Response.from(Embedding.from(new float[384]));
                }

                @Override
                public Response<List<Embedding>> embedAll(List<TextSegment> textSegments) {
                    return Response.from(textSegments.stream()
                            .map(s -> Embedding.from(new float[384]))
                            .toList());
                }
            };
        }
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
    public DataQueryAgent dataQueryAgent(ChatLanguageModel chatLanguageModel, SoccerTools soccerTools, ChatMemoryProvider chatMemoryProvider) {
        return AiServices.builder(DataQueryAgent.class)
                .chatLanguageModel(chatLanguageModel)
                .tools(soccerTools)
                .chatMemoryProvider(chatMemoryProvider)
                .build();
    }

    /**
     * 手动构建 AssistantAgent (RAG + Tools)
     */
    @Bean
    public AssistantAgent assistantAgent(ChatLanguageModel chatLanguageModel, SoccerTools soccerTools, ChatMemoryProvider chatMemoryProvider) {
        return AiServices.builder(AssistantAgent.class)
                .chatLanguageModel(chatLanguageModel)
                .tools(soccerTools)
                .chatMemoryProvider(chatMemoryProvider)
                .build();
    }

    /**
     * 手动构建 MatchAnalysisAgent，绑定 Tools
     */
    @Bean
    public MatchAnalysisAgent matchAnalysisAgent(ChatLanguageModel chatLanguageModel, SoccerTools soccerTools, ChatMemoryProvider chatMemoryProvider) {
        return AiServices.builder(MatchAnalysisAgent.class)
                .chatLanguageModel(chatLanguageModel)
                .tools(soccerTools)
                .chatMemoryProvider(chatMemoryProvider)
                .build();
    }

    /**
     * 手动构建 NewsSummaryAgent
     */
    @Bean
    public NewsSummaryAgent newsSummaryAgent(ChatLanguageModel chatLanguageModel) {
        return AiServices.builder(NewsSummaryAgent.class)
                .chatLanguageModel(chatLanguageModel)
                .build();
    }

    /**
     * 手动构建 FootballAiService
     */
    @Bean
    public FootballAiService footballAiService(ChatLanguageModel chatLanguageModel) {
        return AiServices.builder(FootballAiService.class)
                .chatLanguageModel(chatLanguageModel)
                .build();
    }

    /**
     * 手动构建 CommentAnalysisAgent
     */
    @Bean
    public CommentAnalysisAgent commentAnalysisAgent(ChatLanguageModel chatLanguageModel) {
        return AiServices.builder(CommentAnalysisAgent.class)
                .chatLanguageModel(chatLanguageModel)
                .build();
    }

    /**
     * 手动构建 RuleQaAgent
     */
    @Bean
    public RuleQaAgent ruleQaAgent(ChatLanguageModel chatLanguageModel) {
        return AiServices.builder(RuleQaAgent.class)
                .chatLanguageModel(chatLanguageModel)
                .build();
    }
}
