package com.yupi.yuaiagent.advisor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.advisor.api.*;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import reactor.core.publisher.Flux;

import java.util.*;

/**
 * 权限校验和违禁词检验 Advisor
 */
@Slf4j
public class AuthCheckAdvisor implements CallAroundAdvisor, StreamAroundAdvisor {

    // 违禁词列表
    private static final Set<String> BANNED_WORDS = new HashSet<>(Arrays.asList(
        "暴力", "色情", "赌博", "毒品", "政治敏感", "违法"
    ));

    // 白名单用户ID
    private static final Set<String> WHITELIST_USERS = new HashSet<>(Arrays.asList(
        "admin", "vip-user"
    ));
    
    private AdvisedRequest before(AdvisedRequest advisedRequest) {
        //1、获取用户信息
        String userId=getUserId(advisedRequest);
        log.info("处理用户请求，userId:{}",userId);

        //2、权限校验
        checkPermission(userId);

        //3、违禁词检查
        checkBannedWords(advisedRequest);

        //4、记录审查日志
        logAudit(userId,advisedRequest);

        return advisedRequest;
    }

    /**
     * 获取用户id
     * @param advisedRequest
     * @return
     */
    private String getUserId(AdvisedRequest advisedRequest){
        Map<String,Object> adviseContext=advisedRequest.adviseContext();

        //从上下文中获取用户id
        Object userId=adviseContext.get("userId");
        if(userId==null)
            userId=adviseContext.get("user_id");
        return userId!=null?userId.toString():"anonymous";
    }
    
    /**
     * 用户权限校验
     * @param userId
     */
    private void checkPermission(String userId){
        if("banned-user".equals(userId)){
            throw new SecurityException("用户已被封禁，无法使用服务");
        }

        if(userId.startsWith("guest-") && !WHITELIST_USERS.contains(userId)){
            throw new SecurityException("游客权限不足，请先登录");
        }
        log.debug("权限校验通过:{}",userId);
    }

    /**
     * 违禁词检查
     * @param advisedRequest
     */
    private void checkBannedWords(AdvisedRequest advisedRequest){
        // 直接从 AdvisedRequest 获取消息列表
        List<Message> messages = advisedRequest.messages();
        
        for(Message message: messages){
            if(message instanceof UserMessage userMessage){
                String content = userMessage.getText();

                //检查违禁词
                for(String bannedWord: BANNED_WORDS){
                    if(content.contains(bannedWord)){
                        log.warn("检测到违禁词：{} in message :{}",bannedWord,content);
                        throw new SecurityException("输入内容包含违禁词："+bannedWord);
                    }
                }
         }
       }
       log.debug("违禁词检查通过");
    }
    
    /**
     * 记录审计日志
     */
    private void logAudit(String userId, AdvisedRequest advisedRequest) {
        // 直接从 AdvisedRequest 获取消息列表
        List<Message> messages = advisedRequest.messages();
        
        StringBuilder messageContent = new StringBuilder();
        for (Message message : messages) {
            if (message instanceof UserMessage userMessage) {
                messageContent.append(userMessage.getText()).append(" ");
            }
        }
        
        if (messageContent.length() > 0) {
            log.info("审计日志 - 用户: {}, 消息: {}", userId, 
                    messageContent.toString().substring(0, Math.min(50, messageContent.length())));
        }
    }

    @Override
    public AdvisedResponse aroundCall(AdvisedRequest advisedRequest, CallAroundAdvisorChain chain) {
            // 前置处理：权限检查和违禁词检查
            AdvisedRequest checkedRequest = this.before(advisedRequest);
            
            // 继续调用链（调用下一个 Advisor 或 LLM）
            AdvisedResponse response = chain.nextAroundCall(checkedRequest);
            
            log.debug("{}：同步调用完成", this.getName());
            return response;
    }

    @Override
    public Flux<AdvisedResponse> aroundStream(AdvisedRequest advisedRequest, StreamAroundAdvisorChain chain) {
        // 前置检查
        AdvisedRequest checkedRequest = this.before(advisedRequest);
        // 继续流式调用链
        return chain.nextAroundStream(checkedRequest);
    }
    

    @Override
    public int getOrder() {
        return 0;
    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }
}
