package com.yupi.yuaiagent.rag.service;

import com.yupi.yuaiagent.rag.augmentation.LoveAppRagCustomAdvisorFactory;
import com.yupi.yuaiagent.rag.document.QueryRewriter;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * RAG 服务层
 * 职责：管理所有 RAG 相关的操作（查询改写、知识库检索、Advisor 创建）
 */
@Service
@Slf4j
public class RagService {
    
    @Resource
    private VectorStore loveAppVectorStore;
    
    @Resource
    private QueryRewriter queryRewriter;
    
    @Resource
    private HybridSearchService hybridSearchService;
    
    /**
     * 改写用户查询
     */
    public String rewriteQuery(String originalQuery) {
        log.debug("原始查询: {}", originalQuery);
        String rewritten = queryRewriter.doQueryRewrite(originalQuery);
        log.debug("改写后查询: {}", rewritten);
        return rewritten;
    }
    
    /**
     * 创建基础的问答 Advisor
     */
    public Advisor createQuestionAnswerAdvisor() {
        return new QuestionAnswerAdvisor(loveAppVectorStore);
    }
    
    /**
     * 创建增强的 RAG Advisor
     * @param status 用户状态（单身/恋爱/已婚）
     */
    public Advisor createEnhancedRagAdvisor(String status) {
        log.debug("创建增强 RAG Advisor，状态: {}", status);
        return LoveAppRagCustomAdvisorFactory.createLoveAppRagCustomAdvisor(loveAppVectorStore, status);
    }
    
    /**
     * 获取向量存储实例
     */
    public VectorStore getVectorStore() {
        return loveAppVectorStore;
    }
    
    /**
     * 混合检索文档
     * 优先从向量数据库检索，不足时从 MySQL 补充
     * 
     * @param query 查询文本
     * @param category 分类
     * @param topK 返回数量
     * @param similarityThreshold 相似度阈值
     * @return 检索到的文档列表
     */
    public List<Document> hybridSearchDocuments(
            String query,
            String category,
            int topK,
            double similarityThreshold) {
        return hybridSearchService.hybridSearch(query, category, topK, similarityThreshold);
    }
    
    /**
     * 混合检索文档（简化版）
     */
    public List<Document> hybridSearchDocuments(String query, String category) {
        return hybridSearchService.hybridSearch(query, category);
    }
}
