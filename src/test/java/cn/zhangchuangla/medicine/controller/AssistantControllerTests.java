package cn.zhangchuangla.medicine.controller;

import cn.zhangchuangla.medicine.enums.ChatStageEnum;
import cn.zhangchuangla.medicine.model.vo.chat.StreamChatResponse;
import cn.zhangchuangla.medicine.model.vo.llm.chat.UserMessageRequest;
import cn.zhangchuangla.medicine.service.AssistantService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AssistantControllerTests {

    private AssistantService assistantService;
    private WebTestClient webTestClient;

    @BeforeEach
    void setUp() {
        assistantService = mock(AssistantService.class);
        AssistantController controller = new AssistantController(assistantService);
        webTestClient = WebTestClient.bindToController(controller).build();
    }

    @Test
    void testChatShouldSendStartStageImmediately() {
        StreamChatResponse start = StreamChatResponse.builder()
                .uuid("test-uuid")
                .stage(ChatStageEnum.WORKFLOW_START.getCode())
                .content("测试流程启动")
                .finished(Boolean.FALSE)
                .build();

        StreamChatResponse analysis = StreamChatResponse.builder()
                .uuid("test-uuid")
                .stage(ChatStageEnum.ROUTE_CONSULT.getCode())
                .content("进入分析阶段")
                .finished(Boolean.TRUE)
                .build();

        when(assistantService.testChat(any(UserMessageRequest.class)))
                .thenReturn(Flux.just(start, analysis));

        Flux<StreamChatResponse> responseFlux = webTestClient.post()
                .uri("/assistant/chat/test")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {"message":"hello","uuid":"test-uuid"}
                        """)
                .exchange()
                .expectStatus().isOk()
                .returnResult(StreamChatResponse.class)
                .getResponseBody();

        StepVerifier.create(responseFlux)
                .expectNextMatches(resp -> ChatStageEnum.WORKFLOW_START.getCode().equals(resp.getStage())
                        && Boolean.FALSE.equals(resp.getFinished()))
                .expectNextMatches(resp -> ChatStageEnum.ROUTE_CONSULT.getCode().equals(resp.getStage())
                        && Boolean.TRUE.equals(resp.getFinished()))
                .verifyComplete();

        verify(assistantService).testChat(any(UserMessageRequest.class));
    }
}
