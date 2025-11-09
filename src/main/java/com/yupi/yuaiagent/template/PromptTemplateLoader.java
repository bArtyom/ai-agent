package com.yupi.yuaiagent.template;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

import cn.hutool.core.io.resource.ClassPathResource;
import lombok.extern.slf4j.Slf4j;

/**
 * Prompt 模板加载器
 */
@Slf4j
@Component
public class PromptTemplateLoader {
    private static final Pattern VARIABLE_PATTERN=Pattern.compile("\\{([^}]+)}");

    /**
     * 加载 Prompt 模板内容
     * @param templatePath
     * @return
     */
    public String loadTemplate(String templatePath){
        try  {
            ClassPathResource resource=new ClassPathResource(templatePath);
            InputStream inputStream = resource.getStream();
            String template=StreamUtils.copyToString(inputStream,StandardCharsets.UTF_8);
            log.info("成功加载模板：{}",template);
            return template;
        } catch (IOException e) {
            log.error("加载模板失败：{}",templatePath,e);
            throw new RuntimeException("加载模板失败："+templatePath, e);
        }

    }

    /**
     * 填充模板变量
     * @param template
     * @param variables
     * @return
     */
    public String fillTemplate(String template,Map<String,String> variables){
        String result=template;

        //用正则表达式替换所有变量
        Matcher matcher=VARIABLE_PATTERN.matcher(template);
        StringBuffer sb=new StringBuffer();

        while(matcher.find()){
            String varName=matcher.group(1);
            String varValue=variables.getOrDefault(varName,"{"+ varName+"}"); 
            matcher.appendReplacement(sb,Matcher.quoteReplacement(varValue));
        }
        matcher.appendTail(sb);

        log.debug("模板变量替换完成");
        return sb.toString();
    }

    /**
     * 加载并填充模板
     * @param templatePath
     * @param variables
     * @return
     */
    public String loadAndFill(String templatePath,Map<String,String> variables){
        String template=loadTemplate(templatePath);
        return fillTemplate(template,variables);
    }

}
