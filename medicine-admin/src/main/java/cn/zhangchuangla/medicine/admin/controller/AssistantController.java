package cn.zhangchuangla.medicine.admin.controller;

import cn.zhangchuangla.medicine.admin.service.AssistantService;
import cn.zhangchuangla.medicine.common.core.base.AjaxResult;
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
@Tag(name = "AI助手")
public class AssistantController extends BaseController {

    private final AssistantService assistantService;

    public AssistantController(AssistantService assistantService) {
        this.assistantService = assistantService;
    }

    @PostMapping("/parse/drug")
    @Operation(summary = "解析药品图片信息")
    public AjaxResult<DrugInfoDto> parseDrugInfoByImage(@RequestBody parseDrugInfoByImageRequest request) {
        DrugInfoDto drugInfoDto = assistantService.parseDrugInfoByImage(request.imageUrl());
        return success(drugInfoDto);
    }

    public record parseDrugInfoByImageRequest(List<String> imageUrl){}

}
