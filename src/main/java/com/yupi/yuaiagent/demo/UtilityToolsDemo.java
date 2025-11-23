package com.yupi.yuaiagent.demo;

import com.yupi.yuaiagent.utils.DatabaseTool;
import com.yupi.yuaiagent.utils.DateTimeTool;
import com.yupi.yuaiagent.utils.EmailTool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.ToolCallbacks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * 实用工具使用示例
 * 
 * 演示如何使用邮件、时间、数据库三个实用工具
 * 
 * @author yupi
 * @since 2025-11-23
 */
@Slf4j
@Component
public class UtilityToolsDemo {

    @Autowired
    private ChatClient.Builder chatClientBuilder;

    @Autowired
    private DateTimeTool dateTimeTool;

    @Autowired
    private DatabaseTool databaseTool;

    @Autowired
    private EmailTool emailTool;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 演示1: 时间工具使用
     */
    public void demoDateTimeTool() {
        log.info("\n========== 时间工具演示 ==========");

        ChatClient chatClient = chatClientBuilder
            .defaultTools(ToolCallbacks.from(dateTimeTool))
            .build();

        // 测试场景1: 获取当前时间
        log.info("\n--- 场景1: 询问当前时间 ---");
        String response1 = chatClient.prompt()
            .user("现在是几点?今天星期几?")
            .call()
            .content();
        log.info("AI回复: {}", response1);

        // 测试场景2: 日期计算
        log.info("\n--- 场景2: 日期计算 ---");
        String response2 = chatClient.prompt()
            .user("3天后是几号?是星期几?")
            .call()
            .content();
        log.info("AI回复: {}", response2);

        // 测试场景3: 日期差值
        log.info("\n--- 场景3: 日期差值 ---");
        String response3 = chatClient.prompt()
            .user("从今天到2025年12月31日还有多少天?")
            .call()
            .content();
        log.info("AI回复: {}", response3);

        // 测试场景4: 时区转换
        log.info("\n--- 场景4: 时区查询 ---");
        String response4 = chatClient.prompt()
            .user("现在纽约几点了?")
            .call()
            .content();
        log.info("AI回复: {}", response4);
    }

    /**
     * 演示2: 数据库工具使用
     */
    public void demoDatabaseTool() {
        log.info("\n========== 数据库工具演示 ==========");

        // 先准备测试数据
        setupTestData();

        ChatClient chatClient = chatClientBuilder
            .defaultTools(ToolCallbacks.from(databaseTool))
            .build();

        // 测试场景1: 列出所有表
        log.info("\n--- 场景1: 列出所有表 ---");
        String response1 = chatClient.prompt()
            .user("数据库里有哪些表?")
            .call()
            .content();
        log.info("AI回复: {}", response1);

        // 测试场景2: 查询数据
        log.info("\n--- 场景2: 查询数据 ---");
        String response2 = chatClient.prompt()
            .user("查询test_users表中所有用户")
            .call()
            .content();
        log.info("AI回复: {}", response2);

        // 测试场景3: 统计数据
        log.info("\n--- 场景3: 统计数据 ---");
        String response3 = chatClient.prompt()
            .user("统计test_users表中有多少个用户?")
            .call()
            .content();
        log.info("AI回复: {}", response3);

        // 测试场景4: 条件查询
        log.info("\n--- 场景4: 条件查询 ---");
        String response4 = chatClient.prompt()
            .user("查询test_users表中status为active的用户")
            .call()
            .content();
        log.info("AI回复: {}", response4);
    }

    /**
     * 演示3: 邮件工具使用(需要先配置邮箱)
     */
    public void demoEmailTool() {
        log.info("\n========== 邮件工具演示 ==========");
        log.info("⚠️ 注意: 邮件发送需要先在 application.yml 中配置邮箱信息");

        ChatClient chatClient = chatClientBuilder
            .defaultTools(ToolCallbacks.from(emailTool))
            .build();

        // 测试场景1: 发送简单邮件
        log.info("\n--- 场景1: 发送通知邮件 ---");
        String response1 = chatClient.prompt()
            .user("发一封邮件给 test@example.com,主题是'会议通知',内容是'明天下午3点在会议室A开会'")
            .call()
            .content();
        log.info("AI回复: {}", response1);

        // 测试场景2: 发送HTML邮件
        log.info("\n--- 场景2: 发送HTML格式邮件 ---");
        String response2 = chatClient.prompt()
            .user("发一封HTML格式的邮件给 admin@example.com,主题是'系统报告',内容包含一个表格")
            .call()
            .content();
        log.info("AI回复: {}", response2);
    }

    /**
     * 演示4: 组合使用多个工具
     */
    public void demoComboTools() {
        log.info("\n========== 组合工具演示 ==========");

        ChatClient chatClient = chatClientBuilder
            .defaultTools(ToolCallbacks.from(dateTimeTool, databaseTool, emailTool))
            .build();

        // 复杂场景: 查询数据 + 生成报告 + 发送邮件
        log.info("\n--- 场景: 自动生成并发送数据报告 ---");
        String response = chatClient.prompt()
            .user("""
                请帮我完成以下任务:
                1. 查询test_users表中的用户总数
                2. 统计status为active的用户数量
                3. 生成一份简单的报告,包含当前时间和统计结果
                4. 将报告发送到 admin@example.com
                """)
            .call()
            .content();
        log.info("AI回复: {}", response);
    }

    /**
     * 准备测试数据
     */
    private void setupTestData() {
        try {
            // 创建测试表
            jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS test_users (
                    id INT PRIMARY KEY AUTO_INCREMENT,
                    name VARCHAR(50) NOT NULL,
                    email VARCHAR(100),
                    status VARCHAR(20) DEFAULT 'active',
                    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                ) COMMENT='测试用户表'
                """);

            // 插入测试数据
            jdbcTemplate.execute("DELETE FROM test_users");
            jdbcTemplate.execute("""
                INSERT INTO test_users (name, email, status) VALUES
                ('张三', 'zhang@test.com', 'active'),
                ('李四', 'li@test.com', 'active'),
                ('王五', 'wang@test.com', 'inactive'),
                ('赵六', 'zhao@test.com', 'active')
                """);

            log.info("✅ 测试数据准备完成");
        } catch (Exception e) {
            log.warn("⚠️ 测试数据准备失败(可能已存在): {}", e.getMessage());
        }
    }

    /**
     * 运行所有演示
     */
    public void runAllDemos() {
        log.info("\n");
        log.info("╔══════════════════════════════════════════════════╗");
        log.info("║          实用工具演示 - Utility Tools Demo       ║");
        log.info("╚══════════════════════════════════════════════════╝");

        try {
            // 1. 时间工具演示
            demoDateTimeTool();

            // 2. 数据库工具演示
            demoDatabaseTool();

            // 3. 邮件工具演示(如果配置了邮箱)
            // demoEmailTool();  // 需要先配置邮箱才能运行

            // 4. 组合工具演示
            // demoComboTools();  // 需要先配置邮箱才能运行

            log.info("\n✅ 所有演示完成!");

        } catch (Exception e) {
            log.error("❌ 演示执行失败", e);
        }
    }
}
