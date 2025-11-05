package com.yupi.yuaiagent.app;

import com.yupi.yuaiagent.advisor.MyLoggerAdvisor;
import com.yupi.yuaiagent.advisor.ReReadingAdvisor;

import com.yupi.yuaiagent.chatmemory.FileBasedChatMemory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;
import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY;

@Component
@Slf4j
public class LoveApp {

    private final ChatClient chatClient;

    private static final String SYSTEM_PROMPT = "扮演深耕恋爱心理领域的专家。开场向用户表明身份，告知用户可倾诉恋爱难题。" +
            "围绕单身、恋爱、已婚三种状态提问：单身状态询问社交圈拓展及追求心仪对象的困扰；" +
            "恋爱状态询问沟通、习惯差异引发的矛盾；已婚状态询问家庭责任与亲属关系处理的问题。" +
            "引导用户详述事情经过、对方反应及自身想法，以便给出专属解决方案。";

    public LoveApp(ChatModel ollamaChatModel) {
        // 初始化基于内存的对话记忆
        //初始化基于文件的对话记忆
        //user.dir 获取当前项目的根目录
        String fileDir=System.getProperty("user.dir")+"/chat-memory";
        ChatMemory chatMemory=new FileBasedChatMemory(fileDir);
        chatClient=ChatClient.builder(ollamaChatModel)
                .defaultSystem(SYSTEM_PROMPT)
                .defaultAdvisors(
                        new MessageChatMemoryAdvisor(chatMemory)
                )
                .build();
    }
    public String doChat(String message, String chatId) {
        ChatResponse response = chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                .call()
                .chatResponse();
        String content = response.getResult().getOutput().getText();
        log.info("content: {}", content);
        return content;
    }

    record LoveReport(String title, List<String> suggestions) {
    }

    public LoveReport doChatWithReport(String message,String chatId){
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
        
        LoveReport loveReport=chatClient
                .prompt()
                .system(SYSTEM_PROMPT + "每次对话后都要生成恋爱结果，标题为用户名的恋爱报告，内容为建议列表。" + formatInstruction)
                .user(message)
                .advisors(spec-> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY,chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY,10))
                .call()
                .entity(LoveReport.class);
        log.info("loveReport: {}", loveReport);
        return loveReport;
    }
}
