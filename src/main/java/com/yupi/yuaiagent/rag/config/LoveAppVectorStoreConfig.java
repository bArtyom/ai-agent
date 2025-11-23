package com.yupi.yuaiagent.rag.config;


import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;

import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.yupi.yuaiagent.rag.document.LoveAppDocumentLoader;
import com.yupi.yuaiagent.rag.document.MyKeywordEnricher;
import com.yupi.yuaiagent.rag.document.MyTokenTextSplitter;

import java.util.List;

@Slf4j
@Configuration
public class LoveAppVectorStoreConfig {

    @Resource
    private LoveAppDocumentLoader loveAppDocumentLoader;

    @Resource
    private MyTokenTextSplitter myTokenTextSplitter;

    @Resource
    private MyKeywordEnricher myKeywordEnricher;

    @Bean
    VectorStore loveAppVectorStore(@Qualifier("ollamaEmbeddingModel") EmbeddingModel embeddingModel){
        log.info("========== 开始初始化 LoveApp VectorStore ==========");
        
        log.info("步骤1: 创建 SimpleVectorStore 实例");
        SimpleVectorStore simpleVectorStore = SimpleVectorStore.builder(embeddingModel)
                .build();
        log.info("步骤1完成: SimpleVectorStore 创建成功");
        
        //加载文档
        log.info("步骤2: 开始加载 Markdown 文档");
        List<Document> documents = loveAppDocumentLoader.loadMarkdowns();
        log.info("步骤2完成: 成功加载 {} 个文档", documents != null ? documents.size() : 0);

        //自主切分
        log.info("步骤3: 开始切分文档");
        List<Document> splitDocuments = myTokenTextSplitter.splitCustomized(documents);
        log.info("步骤3完成: 文档切分完成,共 {} 个分块", splitDocuments != null ? splitDocuments.size() : 0);

        //自动补充元信息 (暂时禁用,避免启动时调用30次ChatModel导致卡顿)
        log.info("步骤4: 开始丰富文档元信息 (已跳过,直接使用切分后的文档)");
        List<Document> enrichedDocuments = splitDocuments; // 暂时跳过关键词提取
        // List<Document> enrichedDocuments = myKeywordEnricher.enrichDocuments(splitDocuments);
        log.info("步骤4完成: 文档元信息丰富完成,共 {} 个文档块", enrichedDocuments != null ? enrichedDocuments.size() : 0);
        
        log.info("步骤5: 开始将文档向量化并添加到 VectorStore (这一步可能需要较长时间)");
        log.info("提示: 正在调用 Ollama 嵌入服务对 {} 个文档块进行向量化...", enrichedDocuments != null ? enrichedDocuments.size() : 0);
        simpleVectorStore.add(enrichedDocuments);
        log.info("步骤5完成: 文档向量化并添加成功");
        
        log.info("========== LoveApp VectorStore 初始化完成 ==========");
        return simpleVectorStore;
    }

}
