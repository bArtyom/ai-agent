-- 创建数据库
CREATE DATABASE IF NOT EXISTS ai_agent 
    DEFAULT CHARACTER SET utf8mb4 
    DEFAULT COLLATE utf8mb4_general_ci;

USE ai_agent;

-- 创建对话记忆表
CREATE TABLE `chat_memory` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `conversation_id` VARCHAR(100) NOT NULL COMMENT '对话ID',
  `message_index` INT NOT NULL COMMENT '消息索引（顺序）',
  `message_type` VARCHAR(20) NOT NULL COMMENT '消息类型：USER/ASSISTANT/SYSTEM',
  `content` TEXT NOT NULL COMMENT '消息内容',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_conversation_index` (`conversation_id`, `message_index`),
  KEY `idx_conversation_id` (`conversation_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='对话记忆表';
