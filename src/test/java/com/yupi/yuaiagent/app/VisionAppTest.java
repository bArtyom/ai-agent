package com.yupi.yuaiagent.app;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * 多模态对话测试
 */
@SpringBootTest
@Slf4j
public class VisionAppTest {

    @Autowired
    private VisionApp visionApp;

    @Test
    public void testAnalyzeImage() {
        // 测试分析图片
        String result = visionApp.analyzeImageFromResources(
            "test.jpg", 
            "这张图片里有什么？"
        );
        log.info("分析结果：{}", result);
    }

    @Test
    public void testDescribeImage() {
        // 测试详细描述图片
        String result = visionApp.describeImage("test.png");
        log.info("描述结果：{}", result);
    }

    @Test
    public void testExtractText() {
        // 测试OCR文字识别
        String result = visionApp.extractTextFromImage("test.png");
        log.info("识别文字：{}", result);
    }

    @Test
    public void testIdentifyObjects() {
        // 测试物体识别
        String result = visionApp.identifyObjects("test.png");
        log.info("识别物体：{}", result);
    }

    @Test
    public void testAnalyzeEmotion() {
        // 测试情感分析
        String result = visionApp.analyzeEmotion("test.png");
        log.info("情感分析：{}", result);
    }
}
