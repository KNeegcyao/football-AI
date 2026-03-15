package com.soccer.forum.service.modules.ai.rag;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * RAG 服务
 * 负责知识库的构建、嵌入和检索
 */
@Service
public class RagService {

    private static final Logger log = LoggerFactory.getLogger(RagService.class);

    private final EmbeddingModel embeddingModel;
    private final EmbeddingStore<TextSegment> embeddingStore;
    
    @Value("${soccer.forum.ai.knowledge-base.path:./knowledge-base}")
    private String knowledgeBasePath;

    public RagService(EmbeddingModel embeddingModel, EmbeddingStore<TextSegment> embeddingStore) {
        this.embeddingModel = embeddingModel;
        this.embeddingStore = embeddingStore;
    }

    /**
     * 初始化知识库
     * 加载本地存储的文档
     */
    @PostConstruct
    public void initKnowledgeBase() {
        log.info("初始化知识库，路径: {}", knowledgeBasePath);
        try {
            Path path = Paths.get(knowledgeBasePath);
            if (!Files.exists(path)) {
                Files.createDirectories(path);
                // 初始化默认规则
                initDefaultRules();
            } else {
                // 加载已有文件
                File[] files = path.toFile().listFiles();
                if (files != null) {
                    for (File file : files) {
                        if (file.isFile() && !file.getName().startsWith(".")) {
                            ingestFile(file);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("初始化知识库失败", e);
        }
    }
    
    /**
     * 初始化默认规则
     */
    private void initDefaultRules() {
        String rules = """
                足球比赛规则简介：
                1. 比赛时间：一场标准的足球比赛时长为 90 分钟，分为上半场和下半场，各 45 分钟。中场休息时间不超过 15 分钟。
                2. 球员人数：每队上场球员不得多于 11 名，其中必须有一名守门员。如果任何一队少于 7 人，比赛不得开始或继续。
                3. 越位规则：处于越位位置的队员，在同队队员踢或触及球的一瞬间，被裁判员认为参与了进攻，才被判罚越位。
                   越位位置是指：队员的头、躯干或脚的任何部分比球和倒数第二名对方队员更接近对方球门线。
                4. 点球规则：在禁区内发生的直接任意球犯规，将被判罚点球。点球点距离球门线 11 米（12 码）。
                5. 黄牌和红牌：黄牌表示警告，红牌表示罚令出场。两张黄牌等于一张红牌。
                6. 换人名额：通常正式比赛允许换 5 人，分 3 次进行（中场休息换人不计次数）。
                """;
        Document document = Document.from(rules);
        ingestDocument(document);
    }

    /**
     * 上传并处理文档
     */
    public void uploadDocument(MultipartFile file) throws IOException {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) return;
        
        // 确保目录存在
        Path dirPath = Paths.get(knowledgeBasePath);
        if (!Files.exists(dirPath)) {
            Files.createDirectories(dirPath);
        }
        
        Path targetPath = dirPath.resolve(originalFilename);
        file.transferTo(targetPath.toFile().getAbsoluteFile());
        
        log.info("接收到新文档: {}", originalFilename);
        ingestFile(targetPath.toFile());
    }
    
    /**
     * 处理本地文件
     */
    private void ingestFile(File file) {
        try {
            // Document document = FileSystemDocumentLoader.loadDocument(file.toPath());
            // 如果是 PDF，尝试强制使用 PDFBoxLoader
            Document document;
            if (file.getName().toLowerCase().endsWith(".pdf")) {
                document = dev.langchain4j.data.document.loader.FileSystemDocumentLoader.loadDocument(file.toPath(), new dev.langchain4j.data.document.parser.apache.pdfbox.ApachePdfBoxDocumentParser());
            } else {
                document = FileSystemDocumentLoader.loadDocument(file.toPath());
            }
            
            ingestDocument(document);
            log.info("文档处理完成: {}", file.getName());
        } catch (Exception e) {
            log.error("处理文档失败: " + file.getName(), e);
            throw new RuntimeException("处理文档失败", e);
        }
    }
    
    /**
     * 切分并嵌入文档
     */
    private void ingestDocument(Document document) {
        if (document.text() == null || document.text().trim().isEmpty()) {
            log.warn("文档内容为空，跳过处理");
            return;
        }
        
        // 增加分片大小，避免切分出过小的片段
        DocumentSplitter splitter = DocumentSplitters.recursive(1000, 100);
        EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
                .documentSplitter(splitter)
                .embeddingModel(embeddingModel)
                .embeddingStore(embeddingStore)
                .build();
        ingestor.ingest(document);
    }

    /**
     * 检索相关文档片段
     */
    public List<TextSegment> retrieve(String query) {
        return embeddingStore.findRelevant(embeddingModel.embed(query).content(), 3)
                .stream()
                .map(match -> match.embedded())
                .toList();
    }
    
    /**
     * 将相关文档片段合并为字符串
     */
    public String retrieveAsString(String query) {
        List<TextSegment> segments = retrieve(query);
        if (segments.isEmpty()) {
            return "";
        }
        return String.join("\n\n", segments.stream().map(TextSegment::text).toList());
    }
    
    /**
     * 获取已加载的文件列表
     */
    public String[] listDocuments() {
        File dir = new File(knowledgeBasePath);
        if (dir.exists() && dir.isDirectory()) {
            return dir.list((d, name) -> !name.startsWith("."));
        }
        return new String[0];
    }
    
    /**
     * 清空知识库
     */
    public void clearKnowledgeBase() {
        if (embeddingStore instanceof InMemoryEmbeddingStore) {
             // 内存存储无法直接清空，只能重新初始化（简单粗暴方式）
             // 实际生产建议使用支持 delete 的向量数据库
        }
        
        // 删除本地文件
        try {
            File dir = new File(knowledgeBasePath);
            File[] files = dir.listFiles();
            if (files != null) {
                for (File file : files) {
                    file.delete();
                }
            }
        } catch (Exception e) {
            log.error("清空文件失败", e);
        }
    }
}
