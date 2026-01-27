package cn.zhangchuangla.medicine.admin.controller;

import cn.zhangchuangla.medicine.agent.service.AgentImageParseService;
import cn.zhangchuangla.medicine.common.core.base.AjaxResult;
import cn.zhangchuangla.medicine.common.security.annotation.IsAdmin;
import cn.zhangchuangla.medicine.common.security.base.BaseController;
import cn.zhangchuangla.medicine.llm.model.dto.DrugInfoDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author Chuang
 * <p>
 * created on 2025/11/23
 */
@RestController
@RequestMapping("/assistant")
@Tag(name = "AI Image Parse")
@IsAdmin
public class AssistantImageController extends BaseController {

    private final AgentImageParseService imageParseService;

    public AssistantImageController(AgentImageParseService imageParseService) {
        this.imageParseService = imageParseService;
    }

    /**
     * Parse drug information from images.
     *
     * @param request image URLs
     * @return drug information
     */
    @PostMapping("/parse/drug")
    @Operation(summary = "Parse drug information from images")
    public AjaxResult<DrugInfoDto> parseDrugInfoByImage(@RequestBody ParseDrugInfoByImageRequest request) {
        DrugInfoDto drugInfoDto = imageParseService.parseDrugInfoByImage(request.imageUrl());
        return success(drugInfoDto);
    }

    public record ParseDrugInfoByImageRequest(List<String> imageUrl) {
    }
}
