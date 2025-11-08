package com.yupi.yuaiagent.model.entity;

import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

@Data
@TableName("chat_memory")
public class ChatMemoryEntity {
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 对话ID（对应 ChatMemory 的 conversationId）
     */
    private String conversationId;
    
    /**
     * 消息索引（保证顺序）
     */
    private Integer messageIndex;
    
    /**
     * 消息类型：USER/ASSISTANT/SYSTEM
     */
    private String messageType;
    
    /**
     * 消息内容（对应 Message.getContent()）
     */
    private String content;
    
    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
