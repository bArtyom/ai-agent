package com.yupi.yuaiagent;

import com.yupi.yuaiagent.chatmemory.MySQLChatMemory;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
@Slf4j
class ChatMemoryTest {

    @Autowired
    private MySQLChatMemory mySQLChatMemory;

    @Test
    void testMySQLChatMemory() {
        String conversationId = "test_mysql_001";

        // 1. 添加消息
        List<Message> messages = List.of(
            new UserMessage("你好"),
            new AssistantMessage("你好！有什么可以帮你？")
        );
        mySQLChatMemory.add(conversationId, messages);

        // 2. 获取消息
        List<Message> retrieved = mySQLChatMemory.get(conversationId, 10);
        log.info("获取到 {} 条消息", retrieved.size());
        retrieved.forEach(msg -> log.info("{}: {}", msg.getMessageType(), msg));

        // 3. 清空
        mySQLChatMemory.clear(conversationId);
    }
}