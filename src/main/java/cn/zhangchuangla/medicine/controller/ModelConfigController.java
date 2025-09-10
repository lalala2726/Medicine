package cn.zhangchuangla.medicine.controller;

import cn.zhangchuangla.medicine.common.base.AjaxResult;
import cn.zhangchuangla.medicine.common.base.BaseController;
import cn.zhangchuangla.medicine.llm.service.ModelConfigService;
import cn.zhangchuangla.medicine.model.dto.ModelConfigDto;
import cn.zhangchuangla.medicine.model.entity.ModelConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * @author Chuang
 * <p>
 * created on 2025/9/9 11:06
 */
@RestController
@RequestMapping("/llm/model/config")
@RequiredArgsConstructor
@Tag(name = "模型配置接口", description = "模型配置接口")
public class ModelConfigController extends BaseController {

    private final ModelConfigService modelConfigService;

    /**
     * 更新模型配置
     *
     * @param modelConfigDto 模型配置
     * @return 响应结果
     */
    @PostMapping
    @Operation(summary = "更新模型配置")
    public AjaxResult<Void> updateChatModel(@Validated @RequestBody ModelConfigDto modelConfigDto) {
        modelConfigService.updateChatConfig(modelConfigDto);
        return success();
    }

    /**
     * 获取当前模型配置
     *
     * @return 模型配置
     */
    @GetMapping
    @Operation(summary = "获取当前模型配置")
    public AjaxResult<ModelConfig> getCurrentModelConfig() {
        return success(modelConfigService.getChatConfig());
    }

}
