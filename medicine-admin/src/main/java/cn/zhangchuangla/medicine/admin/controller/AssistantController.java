package cn.zhangchuangla.medicine.admin.controller;

import cn.zhangchuangla.medicine.admin.service.AdminAssistantService;
import cn.zhangchuangla.medicine.common.security.annotation.IsAdmin;
import cn.zhangchuangla.medicine.common.security.base.BaseController;
import cn.zhangchuangla.medicine.llm.model.request.AssistantChatRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * @author Chuang
 * <p>
 * created on 2025/11/23
 */
@RestController
@RequestMapping("/assistant")
@Tag(name = "AI助手")
@IsAdmin
public class AssistantController extends BaseController {

    private final AdminAssistantService assistantService;

    public AssistantController(AdminAssistantService assistantService) {
        this.assistantService = assistantService;
    }

    /**
     * 智能助手
     */
    @PostMapping(value = "/chat", produces = "text/event-stream")
    @Operation(summary = "智能助手")
    public SseEmitter chat(@RequestBody AssistantChatRequest request) {
        return assistantService.chat(request);
    }

}
