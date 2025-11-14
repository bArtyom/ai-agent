package com.yupi.yuaiagent.rag.augmentation;

import org.springframework.ai.chat.client.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LoveAppRagCustomAdvisorFactory {
    public static Advisor createLoveAppRagCustomAdvisor(VectorStore vectorStore,String status){
            Filter.Expression expression=new FilterExpressionBuilder()
            .eq("status", status)
            .build();
            VectorStoreDocumentRetriever documentRetrieverFromVectorStore = VectorStoreDocumentRetriever.builder()
            .vectorStore(vectorStore)
            .filterExpression(expression)
            .similarityThreshold(0.5)
            .topK(3)
            .build();
        return RetrievalAugmentationAdvisor.builder()
        .documentRetriever(documentRetrieverFromVectorStore)
        .build();
    }
}
