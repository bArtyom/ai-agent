package com.yupi.yuaiagent.demo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * 实用工具测试
 * 
 * 运行此测试类来查看工具效果
 */
@SpringBootTest
public class UtilityToolsTest {

    @Autowired
    private UtilityToolsDemo utilityToolsDemo;

    /**
     * 测试时间工具
     */
    @Test
    public void testDateTimeTool() {
        utilityToolsDemo.demoDateTimeTool();
    }

    /**
     * 测试数据库工具
     */
    @Test
    public void testDatabaseTool() {
        utilityToolsDemo.demoDatabaseTool();
    }

    /**
     * 测试邮件工具(需要先配置邮箱)
     */
    @Test
    public void testEmailTool() {
        utilityToolsDemo.demoEmailTool();
    }

    /**
     * 测试组合工具
     */
    @Test
    public void testComboTools() {
        utilityToolsDemo.demoComboTools();
    }

    /**
     * 运行所有演示
     */
    @Test
    public void runAllDemos() {
        utilityToolsDemo.runAllDemos();
    }
}
