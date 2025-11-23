package com.yupi.yuaiagent.advisor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.advisor.api.*;
import org.springframework.ai.chat.model.MessageAggregator;
import reactor.core.publisher.Flux;

@Slf4j
public class MyLoggerAdvisor implements CallAroundAdvisor, StreamAroundAdvisor {
    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public int getOrder() {
        return 1;
    }
    private AdvisedRequest before(AdvisedRequest request){
        log.info("AI Request:{}",request.userText());
         // 打印系统消息（可能包含检索到的文档）
        if (request.systemText() != null && !request.systemText().isEmpty()) {
            log.info("📚 [MyLoggerAdvisor] 系统消息长度: {} 字符", request.systemText().length());
            log.info("📚 [MyLoggerAdvisor] 系统消息预览: {}", 
                request.systemText().substring(0, Math.min(200, request.systemText().length())));
        }
        return request;
    }

    private void observeAfter(AdvisedResponse advisedResponse){
        String fullResponse = advisedResponse.response().getResult().getOutput().getText();
        // 只输出前100个字符的摘要,避免日志重复
        String summary = fullResponse.length() > 100 
            ? fullResponse.substring(0, 100) + "..." 
            : fullResponse;
        log.info("✅ [MyLoggerAdvisor] AI 响应摘要: {}", summary);
        log.debug("完整响应: {}", fullResponse); // 完整内容放在 debug 级别
    }

    @Override
    public AdvisedResponse aroundCall(AdvisedRequest advisedRequest, CallAroundAdvisorChain chain) {
        advisedRequest=this.before(advisedRequest);
        AdvisedResponse advisedResponse=chain.nextAroundCall(advisedRequest);
        this.observeAfter(advisedResponse);
        return advisedResponse;
    }

    @Override
    public Flux<AdvisedResponse> aroundStream(AdvisedRequest advisedRequest, StreamAroundAdvisorChain chain) {
       advisedRequest=this.before(advisedRequest);
       Flux<AdvisedResponse> advisedResponse=chain.nextAroundStream(advisedRequest);
       return (new MessageAggregator()).aggregateAdvisedResponse(advisedResponse,this::observeAfter);
    }
}
