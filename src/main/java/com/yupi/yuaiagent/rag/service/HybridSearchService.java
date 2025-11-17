package com.yupi.yuaiagent.rag.service;

import com.yupi.yuaiagent.mapper.KnowledgeBaseMapper;
import com.yupi.yuaiagent.model.entity.KnowledgeBase;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 混合检索服务
 * 实现向量数据库和关系型数据库的混合检索
 * 
 * 检索策略:
 * 1. 优先从向量数据库（VectorStore）检索
 * 2. 如果向量数据库检索结果不足，从 MySQL 补充
 * 3. 如果向量数据库检索失败，降级到 MySQL
 */
@Service
@Slf4j
public class HybridSearchService {
    
    @Resource
    private VectorStore loveAppVectorStore;
    
    @Resource
    private KnowledgeBaseMapper knowledgeBaseMapper;
    
    /**
     * 初始化时打印数据源信息
     */
    @jakarta.annotation.PostConstruct
    public void init() {
        log.info("✅ HybridSearchService 初始化完成");
        log.info("   - VectorStore: {}", loveAppVectorStore.getClass().getSimpleName());
        log.info("   - KnowledgeBaseMapper: 已注入（连接到 MySQL）");
    }
    
    /**
     * 混合检索
     * 
     * @param query 查询文本
     * @param category 分类（single/dating/married）
     * @param topK 返回的文档数量
     * @param similarityThreshold 向量相似度阈值
     * @return 检索到的文档列表
     */
    public List<Document> hybridSearch(
            String query,
            String category,
            int topK,
            double similarityThreshold) {
        
        log.info("开始混合检索 - 查询: {}, 分类: {}, topK: {}, 阈值: {}", 
            query, category, topK, similarityThreshold);
        
        List<Document> results = new ArrayList<>();
        
        try {
            // 1. 先从向量数据库检索
            List<Document> vectorResults = searchFromVectorStore(
                query, 
                category, 
                topK, 
                similarityThreshold
            );
            
            if (!vectorResults.isEmpty()) {
                log.info("✅ 向量数据库检索到 {} 个结果", vectorResults.size());
                results.addAll(vectorResults);
                
                // 如果结果数量足够，直接返回
                if (results.size() >= topK) {
                    return results.subList(0, topK);
                }
            } else {
                log.warn("⚠️ 向量数据库未检索到结果");
            }
            
            // 2. 如果向量数据库结果不足，从 MySQL 补充
            int remainingCount = topK - results.size();
            if (remainingCount > 0) {
                log.info("向量数据库结果不足，从 MySQL 补充 {} 条", remainingCount);
                List<Document> mysqlResults = searchFromMySQL(query, category, remainingCount);
                
                if (!mysqlResults.isEmpty()) {
                    log.info("✅ MySQL 补充了 {} 个结果", mysqlResults.size());
                    results.addAll(mysqlResults);
                }
            }
            
        } catch (Exception e) {
            log.error("❌ 向量数据库检索失败，降级到 MySQL", e);
            
            // 3. 降级方案：直接从 MySQL 检索
            List<Document> mysqlResults = searchFromMySQL(query, category, topK);
            if (!mysqlResults.isEmpty()) {
                log.info("✅ MySQL 降级检索到 {} 个结果", mysqlResults.size());
                results.addAll(mysqlResults);
            }
        }
        
        log.info("混合检索完成，共返回 {} 个结果", results.size());
        return results;
    }
    
    /**
     * 从向量数据库检索
     */
    private List<Document> searchFromVectorStore(
            String query,
            String category,
            int topK,
            double similarityThreshold) {
        
        try {
            // 构建过滤表达式
            SearchRequest.Builder requestBuilder = SearchRequest.builder()
                .query(query)
                .topK(topK)
                .similarityThreshold(similarityThreshold);
            
            // 如果指定了分类，添加过滤条件
            if (category != null && !category.isEmpty()) {
                Filter.Expression expression = new FilterExpressionBuilder()
                    .eq("status", category)
                    .build();
                requestBuilder.filterExpression(expression);
            }
            
            List<Document> documents = loveAppVectorStore.similaritySearch(
                requestBuilder.build()
            );
            
            return documents != null ? documents : Collections.emptyList();
            
        } catch (Exception e) {
            log.error("向量数据库检索失败", e);
            return Collections.emptyList();
        }
    }
    
    /**
     * 从 MySQL 检索
     */
    private List<Document> searchFromMySQL(String query, String category, int limit) {
        try {
            // 尝试全文搜索
            List<KnowledgeBase> knowledgeList = knowledgeBaseMapper.fullTextSearch(
                query, 
                category, 
                limit
            );
            
            // 如果全文搜索没结果，使用模糊搜索
            if (knowledgeList.isEmpty()) {
                log.debug("全文搜索无结果，使用模糊搜索");
                knowledgeList = knowledgeBaseMapper.likeSearch(query, category, limit);
            }
            
            // 转换为 Document 对象
            return knowledgeList.stream()
                .map(this::convertToDocument)
                .collect(Collectors.toList());
            
        } catch (Exception e) {
            log.error("MySQL 检索失败", e);
            return Collections.emptyList();
        }
    }
    
    /**
     * 将 KnowledgeBase 转换为 Document
     */
    private Document convertToDocument(KnowledgeBase knowledge) {
        String content = String.format("""
            # %s
            
            %s
            
            来源: %s
            """,
            knowledge.getTitle(),
            knowledge.getContent(),
            knowledge.getSource()
        );
        
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("source", "mysql");
        metadata.put("id", knowledge.getId());
        metadata.put("title", knowledge.getTitle());
        metadata.put("category", knowledge.getCategory());
        metadata.put("keywords", knowledge.getKeywords());
        
        return new Document(content, metadata);
    }
    
    /**
     * 简化版混合检索（使用默认参数）
     */
    public List<Document> hybridSearch(String query, String category) {
        return hybridSearch(query, category, 3, 0.5);
    }
}
