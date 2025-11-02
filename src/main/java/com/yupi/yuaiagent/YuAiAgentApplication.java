package com.yupi.yuaiagent;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Service;

@SpringBootApplication
public class YuAiAgentApplication {

    public static void main(String[] args) {
        SpringApplication.run(YuAiAgentApplication.class, args);
    }

    // 方式1：使用构造器注入
    @Service
    public class ChatService {
        private final ChatClient chatClient;

        public ChatService(ChatClient.Builder builder) {
            this.chatClient = builder
                    .defaultSystem("你是恋爱顾问")
                    .build();
        }
    }
}
