package com.yupi.yuaiagent.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yupi.yuaiagent.model.entity.KnowledgeBase;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 知识库 Mapper
 */
@Mapper
public interface KnowledgeBaseMapper extends BaseMapper<KnowledgeBase> {
    
    /**
     * 全文搜索知识库
     * 使用 MySQL 的 FULLTEXT 索引进行全文搜索
     * 
     * @param keyword 搜索关键词
     * @param category 分类（可选）
     * @param limit 返回数量限制
     * @return 匹配的知识列表
     */
    @Select({
        "<script>",
        "SELECT * FROM knowledge_base",
        "WHERE MATCH(title, content) AGAINST(#{keyword} IN NATURAL LANGUAGE MODE)",
        "<if test='category != null and category != \"\"'>",
        "  AND category = #{category}",
        "</if>",
        "ORDER BY MATCH(title, content) AGAINST(#{keyword} IN NATURAL LANGUAGE MODE) DESC",
        "LIMIT #{limit}",
        "</script>"
    })
    List<KnowledgeBase> fullTextSearch(
        @Param("keyword") String keyword,
        @Param("category") String category,
        @Param("limit") int limit
    );
    
    /**
     * 模糊搜索（降级方案，不需要全文索引）
     * 
     * @param keyword 搜索关键词
     * @param category 分类（可选）
     * @param limit 返回数量限制
     * @return 匹配的知识列表
     */
    @Select({
        "<script>",
        "SELECT * FROM knowledge_base",
        "WHERE (title LIKE CONCAT('%', #{keyword}, '%')",
        "   OR content LIKE CONCAT('%', #{keyword}, '%')",
        "   OR keywords LIKE CONCAT('%', #{keyword}, '%'))",
        "<if test='category != null and category != \"\"'>",
        "  AND category = #{category}",
        "</if>",
        "ORDER BY create_time DESC",
        "LIMIT #{limit}",
        "</script>"
    })
    List<KnowledgeBase> likeSearch(
        @Param("keyword") String keyword,
        @Param("category") String category,
        @Param("limit") int limit
    );
}
