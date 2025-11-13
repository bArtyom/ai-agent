package com.yupi.yuaiagent.rag;


import jakarta.annotation.Resource;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.KeywordMetadataEnricher;
import org.springframework.stereotype.Component;


import java.util.List;

@Component
public class MyKeywordEnricher {
    @Resource
    private ChatModel ollamaChatModel;

    List<Document> enrichDocuments(List<Document> documents){
        KeywordMetadataEnricher enricher = new KeywordMetadataEnricher(this.ollamaChatModel, 5);
        return enricher.apply(documents);
   }
}
