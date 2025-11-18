-- 知识库表（作为向量数据库的降级方案）
use ai_agent;
CREATE TABLE IF NOT EXISTS `knowledge_base` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `title` VARCHAR(200) NOT NULL COMMENT '知识标题',
  `content` TEXT NOT NULL COMMENT '知识内容',
  `category` VARCHAR(50) DEFAULT NULL COMMENT '分类（single/dating/married）',
  `keywords` VARCHAR(500) DEFAULT NULL COMMENT '关键词，用逗号分隔',
  `source` VARCHAR(100) DEFAULT NULL COMMENT '来源',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_category` (`category`),
  FULLTEXT KEY `idx_content` (`title`, `content`) WITH PARSER ngram
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='知识库表';

-- 插入示例数据
INSERT INTO `knowledge_base` (`title`, `content`, `category`, `keywords`, `source`) VALUES
('如何开始一段恋爱', '主动示好、真诚沟通、展现自己的优点、了解对方的兴趣爱好', 'single', '示好,沟通,兴趣', '恋爱常见问题-单身篇'),
('初次约会注意事项', '选择合适的地点、注意仪表、准时到达、礼貌待人、倾听对方', 'single', '约会,礼貌,倾听', '恋爱常见问题-单身篇'),
('恋爱中的沟通技巧', '表达真实感受、避免指责、学会倾听、及时解决矛盾', 'dating', '沟通,倾听,矛盾', '恋爱常见问题-恋爱篇'),
('如何维持长久的感情', '相互信任、共同成长、保持新鲜感、理解包容', 'dating', '信任,成长,新鲜感', '恋爱常见问题-恋爱篇'),
('婚姻中的相处之道', '相互尊重、分担责任、保持沟通、共同面对挑战', 'married', '尊重,责任,沟通', '恋爱常见问题-已婚篇');
