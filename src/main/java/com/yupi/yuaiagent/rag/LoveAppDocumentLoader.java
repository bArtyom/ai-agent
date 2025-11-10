package com.yupi.yuaiagent.rag;


import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;

import javax.swing.text.Document;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class LoveAppDocumentLoader {

    private final ResourcePatternResolver resourcePatternResolver;

    LoveAppDocumentLoader(ResourcePatternResolver resourcePatternResolver) {
        this.resourcePatternResolver = resourcePatternResolver;
    }

    public List<Document> loadMarkdowns(){
        List<Document> allDocuments=new ArrayList<>();




    }

}
