package com.yupi.yuaiagent.app.service;

import com.yupi.yuaiagent.app.config.LoveAppConstants;
import com.yupi.yuaiagent.template.PromptTemplateLoader;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 提示词服务
 * 职责：管理提示词的加载和变量填充
 */
@Service
@Slf4j
public class PromptService {
    
    @Resource
    private PromptTemplateLoader promptTemplateLoader;
    
    /**
     * 构建系统提示词
     * @param userName 用户名称
     * @param userProfession 用户职业
     * @param question 用户问题
     */
    public String buildSystemPrompt(String userName, String userProfession, String question) {
        Map<String, String> variables = buildDefaultVariables();
        variables.put("userName", userName);
        variables.put("userProfession", userProfession);
        variables.put("question", question);
        
        String prompt = promptTemplateLoader.loadAndFill(
            LoveAppConstants.SYSTEM_TEMPLATE_PATH, 
            variables
        );
        
        log.debug("生成的系统提示词长度: {}", prompt.length());
        return prompt;
    }
    
    /**
     * 构建报告生成的完整提示词
     */
    public String buildReportPrompt(String userName, String userProfession, String question) {
        String basePrompt = buildSystemPrompt(userName, userProfession, question);
        return basePrompt 
            + LoveAppConstants.REPORT_GENERATION_INSTRUCTION 
            + LoveAppConstants.REPORT_FORMAT_INSTRUCTION;
    }
    
    /**
     * 构建默认变量映射
     */
    private Map<String, String> buildDefaultVariables() {
        Map<String, String> variables = new HashMap<>();
        variables.put("advisorName", LoveAppConstants.DEFAULT_ADVISOR_NAME);
        variables.put("profession", LoveAppConstants.DEFAULT_PROFESSION);
        variables.put("problemType", LoveAppConstants.DEFAULT_PROBLEM_TYPE);
        variables.put("tone", LoveAppConstants.DEFAULT_TONE);
        variables.put("maxWords", LoveAppConstants.DEFAULT_MAX_WORDS);
        return variables;
    }
}
