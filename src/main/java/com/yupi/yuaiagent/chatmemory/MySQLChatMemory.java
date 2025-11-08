package com.yupi.yuaiagent.chatmemory;

import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import com.yupi.yuaiagent.mapper.ChatMemoryMapper;
import com.yupi.yuaiagent.model.entity.ChatMemoryEntity;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;


@Slf4j
@Component
public class MySQLChatMemory implements ChatMemory {

    @Autowired
    private ChatMemoryMapper chatMemoryMapper;

    
    @Override
    public void add(String conversationId, List<Message> messages) {
        log.info("保存对话到mysql，conversatonId：{},消息数{}",conversationId,messages.size());

        //获取对话已有的消息数量
        Long count=chatMemoryMapper.selectCount(
            new QueryWrapper<ChatMemoryEntity>()
                .eq("conversation_id",conversationId)
        );
        int startIndex=count.intValue();
        
        //批量插入新消息
        for (int i = 0; i < messages.size(); i++) {
            Message message=messages.get(i);
            ChatMemoryEntity chatMemoryEntity=new ChatMemoryEntity();
            chatMemoryEntity.setConversationId(conversationId);
            chatMemoryEntity.setMessageIndex(startIndex + i);
            chatMemoryEntity.setMessageType(message.getMessageType().getValue());
            chatMemoryEntity.setContent(message.getText());

            chatMemoryMapper.insert(chatMemoryEntity);
        }
    }

    @Override
    public List<Message> get(String conversationId, int lastN) {
        log.info("从 MySQL 获取对话, conversationId: {}, 最近 {} 条", conversationId, lastN);
        List<ChatMemoryEntity> entities = chatMemoryMapper.selectList(
            new QueryWrapper<ChatMemoryEntity>()
                .eq("conversation_id", conversationId)
                .orderByDesc("message_index")
                .last("LIMIT " + lastN)
        );
         // 转换为 Message 对象（注意需要反转顺序）
        List<Message> messages = new ArrayList<>();
        for (int i = entities.size() - 1; i >= 0; i--) {
            ChatMemoryEntity entity = entities.get(i);
            Message message = convertToMessage(entity);
            messages.add(message);
        }
        return messages;
    }

    /**
     * 将实体转换为 Message 对象
     */
    private Message convertToMessage(ChatMemoryEntity entity) {
        String type = entity.getMessageType();
        String content = entity.getContent();
        
        if ("USER".equals(type)) {
            return new UserMessage(content);
        } else if ("ASSISTANT".equals(type)) {
            return new AssistantMessage(content);
        } else {
            return new UserMessage(content);  // 默认返回用户消息
        }
    }

    @Override
    public void clear(String conversationId) {
        log.info("清空 MySQL 对话记录, conversationId: {}", conversationId);
        
        chatMemoryMapper.delete(
            new QueryWrapper<ChatMemoryEntity>()
                .eq("conversation_id", conversationId)
        );
    }



}
