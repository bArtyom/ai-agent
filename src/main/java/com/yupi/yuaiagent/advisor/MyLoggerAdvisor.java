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
        log.info("AI Response:{}",advisedResponse.response().getResult().getOutput().getText());
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
