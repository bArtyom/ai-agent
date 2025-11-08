package com.yupi.yuaiagent.config;

import org.springframework.context.annotation.Configuration;

/**
 * ChatMemory 配置类
 * 统一管理对话记忆的实例化
 */
@Configuration
public class ChatMemoryConfig {

    /**
     * 当前使用 MySQLChatMemory (已标记 @Primary)
     * 
     * MySQLChatMemory 是 @Component,Spring 会自动创建实例
     * 使用 @Primary 标记后,会作为默认的 ChatMemory 实现
     * 
     * 如果想切换回文件存储,可以:
     * 1. 在 MySQLChatMemory 上移除 @Primary
     * 2. 取消下面的注释,创建 FileBasedChatMemory Bean
     */
    
    // @Bean
    // @Primary
    // public ChatMemory fileBasedChatMemory() {
    //     String storageDir = "chat-memory";
    //     return new FileBasedChatMemory(storageDir);
    // }
    
    // 如果以后想使用 Redis 实现:
    // @Bean
    // @Primary
    // public ChatMemory redisChatMemory(RedisTemplate redisTemplate) {
    //     return new RedisChatMemory(redisTemplate);
    // }
}
