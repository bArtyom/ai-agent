package com.yupi.yuaiagent.rag.config;


import jakarta.annotation.Resource;
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
        SimpleVectorStore simpleVectorStore = SimpleVectorStore.builder(embeddingModel)
                .build();
        //加载文档
        List<Document> documents = loveAppDocumentLoader.loadMarkdowns();

        //自主切分
        List<Document> splitDocuments = myTokenTextSplitter.splitCustomized(documents);

        //自动补充元信息
        List<Document> enrichedDocuments = myKeywordEnricher.enrichDocuments(splitDocuments);
        simpleVectorStore.add(enrichedDocuments);
        return simpleVectorStore;
    }

}
