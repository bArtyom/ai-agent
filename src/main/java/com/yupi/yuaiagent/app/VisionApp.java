package com.yupi.yuaiagent.app;

import com.yupi.yuaiagent.advisor.MyLoggerAdvisor;
import com.yupi.yuaiagent.utils.ImageUtils;


import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Component;
import org.springframework.util.MimeTypeUtils;

import java.util.Base64;

/**
 * 多模态对话助手 - 支持图片理解
 * 可以让 AI 解析图片内容并回答相关问题
 */
@Component
@Slf4j
public class VisionApp {

    private final ChatClient chatClient;

    public VisionApp(@Qualifier("ollamaChatModel") ChatModel chatModel
                   ,@Qualifier("mysqlChatMemory") ChatMemory chatMemory) {
        this.chatClient = ChatClient.builder(chatModel)
        .defaultAdvisors(
            new MessageChatMemoryAdvisor(chatMemory),
            new MyLoggerAdvisor()
        ).build();
    }

    /**
     * 分析 resources/images 目录下的图片
     * 
     * @param imagePath 图片路径（相对于 resources/images）
     * @param question 用户问题
     * @return AI 对图片的分析结果
     */
    public String analyzeImageFromResources(String imagePath, String question) {
        log.info("开始分析图片：{}", imagePath);
        
        // 加载图片并转换为 Base64
        String base64Image = ImageUtils.loadImageAsBase64FromResources(imagePath);
        
        // 解码为字节数组
        byte[] imageBytes = Base64.getDecoder().decode(base64Image);
        
        // 获取图片类型
        String mimeType = ImageUtils.getImageMimeType(imagePath);
        
        // 构建多模态消息
        return analyzeImage(imageBytes, mimeType, question);
    }

    /**
     * 分析项目根目录下的图片
     * 
     * @param imagePath 图片路径（相对于项目根目录/images）
     * @param question 用户问题
     * @return AI 对图片的分析结果
     */
    public String analyzeImageFromProject(String imagePath, String question) {
        log.info("开始分析项目图片：{}", imagePath);
        
        // 加载图片并转换为 Base64
        String base64Image = ImageUtils.loadImageAsBase64FromProject(imagePath);
        
        // 解码为字节数组
        byte[] imageBytes = Base64.getDecoder().decode(base64Image);
        
        // 获取图片类型
        String mimeType = ImageUtils.getImageMimeType(imagePath);
        
        // 构建多模态消息
        return analyzeImage(imageBytes, mimeType, question);
    }

    /**
     * 核心方法：分析图片字节数组
     * 
     * @param imageBytes 图片字节数组
     * @param mimeType 图片 MIME 类型（如 image/jpeg）
     * @param question 用户问题
     * @return AI 分析结果
     */
    private String analyzeImage(byte[] imageBytes, String mimeType, String question) {
        log.info("开始调用 AI 分析图片，MIME 类型：{}", mimeType);
        
        // 使用 ChatClient 的 media 方法传递图片
        String result = chatClient
                .prompt()
                .user(userSpec -> userSpec
                    .text(question)
                    .media(MimeTypeUtils.parseMimeType(mimeType), new ByteArrayResource(imageBytes))
                )
                .call()
                .content();
        
        log.info("图片分析完成，结果长度：{} 字符", result.length());
        return result;
    }

    /**
     * 根据图片生成详细描述
     * 
     * @param imagePath 图片路径
     * @return 详细描述
     */
    public String describeImage(String imagePath) {
        String question = """
            请详细描述这张图片，包括：
            1. 主要内容和主题
            2. 画面中的元素和物体
            3. 色彩和光线特点
            4. 整体氛围和感觉
            5. 可能的拍摄场景或背景
            """;
        return analyzeImageFromResources(imagePath, question);
    }

    /**
     * 从图片中提取文字（OCR）
     * 
     * @param imagePath 图片路径
     * @return 提取的文字内容
     */
    public String extractTextFromImage(String imagePath) {
        String question = "请识别并提取图片中的所有文字内容，按照从上到下、从左到右的顺序列出";
        return analyzeImageFromResources(imagePath, question);
    }
    
    /**
     * 识别图片中的物体
     * 
     * @param imagePath 图片路径
     * @return 物体识别结果
     */
    public String identifyObjects(String imagePath) {
        String question = "请列出图片中所有能识别出的物体和元素，并说明它们的位置关系";
        return analyzeImageFromResources(imagePath, question);
    }
    
    /**
     * 分析图片情感和氛围
     * 
     * @param imagePath 图片路径
     * @return 情感分析结果
     */
    public String analyzeEmotion(String imagePath) {
        String question = "请分析这张图片传达的情感和氛围，包括色调、构图对情绪的影响";
        return analyzeImageFromResources(imagePath, question);
    }
}
