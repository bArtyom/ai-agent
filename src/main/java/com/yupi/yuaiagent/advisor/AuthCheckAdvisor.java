package com.yupi.yuaiagent.advisor;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.ai.chat.client.advisor.api.*;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;


@Slf4j
public class AuthCheckAdvisor implements CallAroundAdvisor, StreamAroundAdvisor {
    //违禁词列表
    private static final Set<String> BANNED_WORDS=new HashSet<>(Arrays.asList(
    "暴力", "色情", "赌博", "违法"
    ));

    //用户权限等级
    private static final String PARAM_USER_ROLE = "userRole";
    private static final String PARAM_USER_ID = "userId";


    
    @Override
    public AdvisedResponse aroundCall(AdvisedRequest advisedRequest, CallAroundAdvisorChain chain) {
        log.info("执行权限检查跟内容检查的Advisor");

        //1、权限检查
        if(!checkPermission(advisedRequest)){
            throw new RuntimeException("权限不足，拒绝访问");
        }
        
        //2、违禁词检查
        if(!checkBannedWords(advisedRequest)){
            throw new RuntimeException("请求内容包含违禁词，拒绝访问");
        }

        return chain.nextAroundCall(advisedRequest);

    }

    @Override
    public Flux<AdvisedResponse> aroundStream(AdvisedRequest advisedRequest, StreamAroundAdvisorChain chain) {
        log.info("执行流式权限检查跟内容检查的Advisor");

        //1、权限检查
        if(!checkPermission(advisedRequest)){
            return Flux.error(new RuntimeException("权限不足，拒绝访问"));
        }
        
        //2、违禁词检查
        if(!checkBannedWords(advisedRequest)){
            return Flux.error(new RuntimeException("请求内容包含违禁词，拒绝访问"));
        }

        return chain.nextAroundStream(advisedRequest);
    }


    

    /**
     * 检查用户权限
     * @param advisedRequest
     * @return
     */
    private boolean checkPermission(AdvisedRequest advisedRequest) {
        Map<String,Object> advisorParams=advisedRequest.adviseContext();
        String userRole=(String)advisorParams.get(PARAM_USER_ROLE);
        String userId=(String)advisorParams.get(PARAM_USER_ID);

        log.info("检查用户权限，用户ID：{}，用户角色：{}",userId,userRole);

        if(userRole==null){
            log.warn("用户角色为空，拒绝访问");
            return false;
        }

        if("GUEST".equals(userRole)){
            log.warn("用户角色为GUEST，拒绝访问");
            return false;
        }
        return true;
    }

    /**
     * 检查违禁词
     * @param advisedRequest
     * @return
     */
    private boolean checkBannedWords(AdvisedRequest advisedRequest){
        Map<String,Object> advisorParams=advisedRequest.adviseContext();
        List<String> messages=(List<String>)advisorParams.get("messages");
        for(String message: messages){
            for(String bannedWord:BANNED_WORDS){
                if(message.contains(bannedWord)){
                    log.warn("检测到违禁词：{}，拒绝访问",bannedWord);
                    return false;
                }
            }
        }
        return true;
    }

    


    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public int getOrder() {
        return 0;  // 优先级最高，第一个执行
    }
}
