package com.yupi.yuaiagent.rag;


import jakarta.annotation.Resource;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;

import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


import java.util.List;

@Configuration
public class LoveAppVectorStoreConfig {

    @Resource
    private LoveAppDocumentLoader loveAppDocumentLoader;

    @Resource
    private MyTokenTextSplitter myTokenTextSplitter;

    @Bean
    VectorStore loveAppVectorStore(@Qualifier("ollamaEmbeddingModel") EmbeddingModel embeddingModel){
        SimpleVectorStore simpleVectorStore = SimpleVectorStore.builder(embeddingModel)
                .build();
        //加载文档
        List<Document> documents = loveAppDocumentLoader.loadMarkdowns();

        //自主切分
        List<Document> splitDocuments = myTokenTextSplitter.splitCustomized(documents);
        simpleVectorStore.add(splitDocuments);
        return simpleVectorStore;
    }

}
