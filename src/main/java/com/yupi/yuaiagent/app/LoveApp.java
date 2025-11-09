package com.yupi.yuaiagent.app;

import com.yupi.yuaiagent.advisor.MyLoggerAdvisor;
import com.yupi.yuaiagent.template.PromptTemplateLoader;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;
import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY;

@Component
@Slf4j
public class LoveApp {

    private final ChatClient chatClient;
    private final ChatMemory chatMemory;
    
    @Resource
    private PromptTemplateLoader promptTemplateLoader;

    // 模板文件路径（相对于 resources 目录）
    private static final String TEMPLATE_PATH = "promptTemplate/SystemTemplate";

    /**
     * 构造器注入
     * Spring 会自动注入 ChatModel 和 ChatMemory
     * 
     * @param ollamaChatModel Spring 自动装配的 ChatModel
     * @param chatMemory Spring 自动装配的 ChatMemory
     *                   使用 @Qualifier 指定要注入的实现：
     *                   - "mysqlChatMemory": MySQL 数据库存储
     *                   - "fileChatMemory": 文件存储
     */
    public LoveApp(ChatModel ollamaChatModel, 
                   @Qualifier("mysqlChatMemory") ChatMemory chatMemory) {
        this.chatMemory = chatMemory;
        
        // 构建 ChatClient，不设置 defaultSystem，改为动态加载
        chatClient = ChatClient.builder(ollamaChatModel)
                .defaultAdvisors(
                        new MessageChatMemoryAdvisor(chatMemory),
                        new MyLoggerAdvisor()
                )
                .build();
                
        log.info("LoveApp 初始化完成，使用 ChatMemory: {}", chatMemory.getClass().getSimpleName());
    }
    
    /**
     * 使用模板进行对话
     * 
     * @param message 用户消息
     * @param chatId 对话 ID
     * @return AI 响应
     */
    public String doChat(String message, String chatId) {
        return doChatWithUser(message, chatId, "用户", "程序员");
    }
    
    /**
     * 使用模板进行对话（完整版）
     * 
     * @param message 用户消息
     * @param chatId 对话 ID
     * @param userName 用户名称
     * @param userProfession 用户职业
     * @return AI 响应
     */
    public String doChatWithUser(String message, String chatId, String userName, String userProfession) {
        // 准备模板变量
        Map<String, String> variables = new HashMap<>();
        variables.put("advisorName", "心灵导师小爱");
        variables.put("profession", "程序员");
        variables.put("userName", userName);
        variables.put("userProfession", userProfession);
        variables.put("problemType", "情感咨询");
        variables.put("tone", "温暖而专业");
        variables.put("question", message);
        variables.put("maxWords", "300");
        
        // 加载并填充模板
        String systemPrompt = promptTemplateLoader.loadAndFill(TEMPLATE_PATH, variables);
        
        log.info("使用动态生成的 System Prompt:{}", systemPrompt);
        
        // 发送请求
        ChatResponse response = chatClient
                .prompt()
                .system(systemPrompt)  // 使用动态生成的 system prompt
                .user(message)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                .call()
                .chatResponse();
        String content = response.getResult().getOutput().getText();
        log.info("content: {}", content);
        return content;
    }

     public record LoveReport(String title, List<String> suggestions) {
    }

    /**
     * 使用模板生成恋爱报告
     * 
     * @param message 用户消息
     * @param chatId 对话 ID
     * @return 恋爱报告
     */
    public LoveReport doChatWithReport(String message, String chatId) {
        return doChatWithReportForUser(message, chatId, "用户", "程序员");
    }
    
    /**
     * 使用模板生成恋爱报告（完整版）
     * 
     * @param message 用户消息
     * @param chatId 对话 ID
     * @param userName 用户名称
     * @param userProfession 用户职业
     * @return 恋爱报告
     */
    public LoveReport doChatWithReportForUser(String message, String chatId, String userName, String userProfession) {
        // 准备模板变量
        Map<String, String> variables = new HashMap<>();
        variables.put("advisorName", "心灵导师小爱");
        variables.put("profession", "程序员");
        variables.put("userName", userName);
        variables.put("userProfession", userProfession);
        variables.put("problemType", "情感咨询");
        variables.put("tone", "温暖而专业");
        variables.put("question", message);
        variables.put("maxWords", "300");
        
        // 加载并填充模板
        String systemPrompt = promptTemplateLoader.loadAndFill(TEMPLATE_PATH, variables);
        
        // 添加报告生成指令
        String formatInstruction = """
               你必须生成符合下面格式的回答：
               使用Json格式，例如
                {"title": "恋爱报告：程序员鱼皮的爱情指南",
                  "suggestions": [
                       "拓展社交圈：积极参与社区活动、线上社群，扩大社交圈子。",
                       "提升个人魅力：注重仪容仪表、提升内在修养，展现个人魅力。",
                        "培养共同兴趣：找到共同爱好，增加互动和话题，培养情感联系。",
                      "练习沟通技巧：清晰表达需求，积极倾听，表达真挚情感。",
                      "寻求专业帮助：考虑心理咨询师，更好地了解自我和爱情。",
                    ]
                 }
                """;
        
        String fullSystemPrompt = systemPrompt + "\n\n每次对话后都要生成恋爱结果，标题为用户名的恋爱报告，内容为建议列表。\n" + formatInstruction;
        
        LoveReport loveReport = chatClient
                .prompt()
                .system(fullSystemPrompt)
                .user(message)
                .advisors(spec-> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                .call()
                .entity(LoveReport.class);
        log.info("loveReport: {}", loveReport);
        return loveReport;
    }
}
