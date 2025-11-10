package com.yupi.yuaiagent.app;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

@SpringBootTest
class LoveAppTest {

    @Resource
    private LoveApp loveApp;

    @Test
    void testChat() {
        String chatId = UUID.randomUUID().toString();
        // 第一轮
        String message = "你好，我是程序员鱼皮";
        String answer = loveApp.doChat(message, chatId);
        Assertions.assertNotNull(answer);
        // 第二轮
        message = "我想让另一半（编程导航）更爱我";
        answer = loveApp.doChat(message, chatId);
        Assertions.assertNotNull(answer);
        // 第三轮
        message = "我的另一半叫什么来着？刚跟你说过，帮我回忆一下";
        answer = loveApp.doChat(message, chatId);
        Assertions.assertNotNull(answer);
    }
    
    /**
     * 测试使用自定义用户信息
     */
    @Test
    void testChatWithCustomUser() {
        String chatId = UUID.randomUUID().toString();
        
        // 使用自定义用户名和职业
        String answer = loveApp.doChatWithUser(
            "我最近和女朋友因为工作加班太多经常吵架，该怎么办？",
            chatId,
            "张三",           // 自定义用户名
            "Java后端工程师"   // 自定义职业
        );
        
        System.out.println("AI 回答：\n" + answer);
        Assertions.assertNotNull(answer);
    }
    
    /**
     * 测试生成恋爱报告
     */
    @Test
    void testChatWithReport() {
        String chatId = UUID.randomUUID().toString();

        LoveApp.LoveReport report = loveApp.doChatWithReportForUser(
                "我是一名程序员，想找女朋友但不知道从何开始",
                chatId,
                "王五",
                "前端工程师"
        );

        System.out.println("报告标题：" + report.title());
        System.out.println("建议列表：");
        report.suggestions().forEach(s -> System.out.println("  - " + s));

        Assertions.assertNotNull(report);
        Assertions.assertNotNull(report.title());
        Assertions.assertFalse(report.suggestions().isEmpty());
    }

    @Test
    void doChatWithRag() {
        String chatId = UUID.randomUUID().toString();
        String message = "我已经结婚了，但是婚后关系不太亲密，怎么办？";
        String answer =  loveApp.doChatWithRag(message, chatId);
        Assertions.assertNotNull(answer);
    }

}

