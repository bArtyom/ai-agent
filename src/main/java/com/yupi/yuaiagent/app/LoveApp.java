package com.yupi.yuaiagent.app;

import com.yupi.yuaiagent.advisor.MyLoggerAdvisor;
import com.yupi.yuaiagent.app.config.LoveAppConstants;
import com.yupi.yuaiagent.app.service.PromptService;
import com.yupi.yuaiagent.rag.service.RagService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;
import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY;

/**
 * 爱情应用主组件
 * 职责：
 * 1. 管理 ChatClient 的生命周期
 * 2. 提供多种对话方式（基础对话、模板对话、RAG 增强对话、报告生成）
 * 3. 协调各个服务层的调用
 * 
 * 设计原则：
 * - 高内聚：Chat 相关的功能聚合在一起
 * - 低耦合：依赖抽象的服务层，而非具体实现
 * - 单一职责：只负责对话流程的编排，具体逻辑委托给服务层
 */
@Component
@Slf4j
public class LoveApp {

    // ==================== 依赖注入 ====================
    
    private final ChatModel chatModel;
    private final ChatMemory chatMemory;
    
    @Resource
    private RagService ragService;
    
    @Resource
    private PromptService promptService;
    
    // ChatClient 延迟初始化，避免循环依赖
    private ChatClient chatClient;

    /**
     * 构造器注入（推荐方式）
     * @param chatModel Spring 自动装配的 ChatModel
     * @param chatMemory Spring 自动装配的 ChatMemory
     */
    public LoveApp(
            @Qualifier("ollamaChatModel") ChatModel chatModel,
            @Qualifier("mysqlChatMemory") ChatMemory chatMemory) {
        this.chatModel = chatModel;
        this.chatMemory = chatMemory;
    }
    
    /**
     * 初始化 ChatClient
     * 使用 @PostConstruct 延迟初始化，避免构造函数中的循环依赖
     */
    @PostConstruct
    public void init() {
        this.chatClient = ChatClient.builder(chatModel)
                .defaultAdvisors(
                        new MessageChatMemoryAdvisor(chatMemory),
                        new MyLoggerAdvisor()
                )
                .build();
        
        log.info("LoveApp 初始化完成，ChatModel: {}, ChatMemory: {}", 
                chatModel.getClass().getSimpleName(), 
                chatMemory.getClass().getSimpleName());
    }
    
    // ==================== 公共方法 ====================
    
    /**
     * 基础对话（使用默认用户信息）
     */
    public String doChat(String message, String chatId) {
        return doChatWithUser(message, chatId, 
            LoveAppConstants.DEFAULT_USER_NAME, 
            LoveAppConstants.DEFAULT_PROFESSION);
    }
    
    /**
     * 使用自定义用户信息的对话
     * @param message 用户消息
     * @param chatId 对话 ID
     * @param userName 用户名称
     * @param userProfession 用户职业
     * @return AI 响应
     */
    public String doChatWithUser(String message, String chatId, String userName, String userProfession) {
        String systemPrompt = promptService.buildSystemPrompt(userName, userProfession, message);
        
        log.debug("系统提示词生成完成，长度: {}", systemPrompt.length());
        
        ChatResponse response = chatClient.prompt()
                .system(systemPrompt)
                .user(message)
                .advisors(spec -> {
                    spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId);
                    spec.param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10);
                    // 添加 RAG 知识库检索
                    spec.advisors(ragService.createQuestionAnswerAdvisor());
                })
                .call()
                .chatResponse();
        
        String content = response.getResult().getOutput().getText();
        log.info("对话响应: {}", content);
        return content;
    }
    
    // ==================== 报告生成 ====================
    
    /**
     * 恋爱报告记录
     */
    public record LoveReport(String title, List<String> suggestions) {
    }

    /**
     * 生成恋爱报告（使用默认用户信息）
     */
    public LoveReport doChatWithReport(String message, String chatId) {
        return doChatWithReportForUser(message, chatId, 
            LoveAppConstants.DEFAULT_USER_NAME, 
            LoveAppConstants.DEFAULT_PROFESSION);
    }
    
    /**
     * 生成恋爱报告（使用自定义用户信息）
     * @param message 用户消息
     * @param chatId 对话 ID
     * @param userName 用户名称
     * @param userProfession 用户职业
     * @return 结构化的恋爱报告
     */
    public LoveReport doChatWithReportForUser(String message, String chatId, String userName, String userProfession) {
        String fullSystemPrompt = promptService.buildReportPrompt(userName, userProfession, message);
        
        LoveReport loveReport = chatClient.prompt()
                .system(fullSystemPrompt)
                .user(message)
                .advisors(spec -> {
                    spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId);
                    spec.param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10);
                })
                .call()
                .entity(LoveReport.class);
        
        log.info("恋爱报告生成完成，标题: {}, 建议数: {}", 
                loveReport.title(), loveReport.suggestions().size());
        return loveReport;
    }
    
    // ==================== RAG 增强对话 ====================
    
    /**
     * 基础 RAG 对话
     * @param message 用户消息
     * @param chatId 对话 ID
     * @return AI 响应
     */
    public String doChatWithRag(String message, String chatId) {
        String rewrittenMessage = ragService.rewriteQuery(message);
        
        ChatResponse chatResponse = chatClient.prompt()
                .user(rewrittenMessage)
                .advisors(spec -> {
                    spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId);
                    spec.param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10);
                    // 应用知识库问答
                    spec.advisors(ragService.createQuestionAnswerAdvisor());
                })
                .call()
                .chatResponse();
        
        String content = chatResponse.getResult().getOutput().getText();
        log.info("RAG 对话响应: {}", content);
        return content;
    }

    /**
     * 增强的 RAG 对话（支持状态过滤）
     * @param message 用户消息
     * @param chatId 对话 ID
     * @param status 用户状态（单身/恋爱/已婚）
     * @return AI 响应
     */
    public String doChatWithEnhancedRag(String message, String chatId, String status) {
        String rewrittenMessage = ragService.rewriteQuery(message);
        
        ChatResponse chatResponse = chatClient.prompt()
                .user(rewrittenMessage)
                .advisors(spec -> {
                    spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId);
                    spec.param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10);
                    // 使用增强的 RAG Advisor（支持状态过滤）
                    spec.advisors(ragService.createEnhancedRagAdvisor(status));
                })
                .call()
                .chatResponse();
        
        String content = chatResponse.getResult().getOutput().getText();
        log.info("增强 RAG 对话响应（状态: {}）: {}", status, content);
        return content;
    }

    @Resource
    private ToolCallback[] allTools;

    /**
     * 使用工具对话
     * @param message 用户消息
     * @param chatId 对话 ID
     * @return AI 响应
     */
    public String doChatWithTools(String message, String chatId) {
        ChatResponse response = chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                // 开启日志，便于观察效果
                .advisors(new MyLoggerAdvisor())
                .tools(allTools)
                .call()
                .chatResponse();
        String content = response.getResult().getOutput().getText();
        log.info("content: {}", content);
        return content;
    }

}
