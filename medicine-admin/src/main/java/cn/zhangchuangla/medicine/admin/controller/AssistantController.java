package cn.zhangchuangla.medicine.admin.controller;

import cn.zhangchuangla.medicine.admin.service.AssistantService;
import cn.zhangchuangla.medicine.common.core.base.AjaxResult;
import cn.zhangchuangla.medicine.common.security.annotation.IsAdmin;
import cn.zhangchuangla.medicine.common.security.base.BaseController;
import cn.zhangchuangla.medicine.llm.model.dto.DrugInfoDto;
import cn.zhangchuangla.medicine.llm.model.response.AssistantChatResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.List;

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

    private final AssistantService assistantService;

    public AssistantController(AssistantService assistantService) {
        this.assistantService = assistantService;
    }

    /**
     * 解析药品图片信息
     *
     * @param request 图片URL
     * @return 药品信息
     */
    @PostMapping("/parse/drug")
    @Operation(summary = "解析药品图片信息")
    public AjaxResult<DrugInfoDto> parseDrugInfoByImage(@RequestBody parseDrugInfoByImageRequest request) {
        DrugInfoDto drugInfoDto = assistantService.parseDrugInfoByImage(request.imageUrl());
        return success(drugInfoDto);
    }

    /**
     * 智能助手
     */
    @PostMapping(value = "/chat", produces = "text/event-stream")
    @Operation(summary = "智能助手")
    public Flux<AssistantChatResponse> chat(@RequestBody AssistantChatRequest request) {
        return assistantService.chat(request.message());
    }

    public record parseDrugInfoByImageRequest(List<String> imageUrl) {
    }

    public record AssistantChatRequest(String message) {
    }

}
