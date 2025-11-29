package cn.zhangchuangla.medicine.client.controller;

import cn.zhangchuangla.medicine.llm.model.response.ClientChatResponse;
import cn.zhangchuangla.medicine.llm.service.ConsultationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

/**
 * @author Chuang
 * <p>
 * created on 2025/11/22
 */
@Slf4j
@RestController
@RequestMapping("/consultation")
@Tag(name = "咨询管理", description = "咨询管理接口")
public class ConsultationController {

    private final ConsultationService consultationService;

    public ConsultationController(ConsultationService consultationService) {
        this.consultationService = consultationService;
    }


    @PostMapping(value = "/chat", produces = "text/event-stream")
    @Operation(summary = "简单咨询", description = "简单的医疗咨询接口，返回 ClientChatResponse SSE 消息（文本或卡片）")
    public Flux<ClientChatResponse> simpleConsultation(@RequestBody ConsultationRequest request) {
        log.info("咨询问题：{}", request.question);
        return consultationService.simpleConsultation(request.question());
    }


    /**
     * 咨询请求参数
     *
     * @param question 咨询问题内容
     */
    public record ConsultationRequest(
            String question
    ) {
    }
}
