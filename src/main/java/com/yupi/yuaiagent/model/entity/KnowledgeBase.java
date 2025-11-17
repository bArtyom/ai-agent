package com.yupi.yuaiagent.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 知识库实体类
 */
@Data
@TableName("knowledge_base")
public class KnowledgeBase implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 知识标题
     */
    private String title;
    
    /**
     * 知识内容
     */
    private String content;
    
    /**
     * 分类（single/dating/married）
     */
    private String category;
    
    /**
     * 关键词，用逗号分隔
     */
    private String keywords;
    
    /**
     * 来源
     */
    private String source;
    
    /**
     * 创建时间
     */
    private Date createTime;
    
    /**
     * 更新时间
     */
    private Date updateTime;
}
