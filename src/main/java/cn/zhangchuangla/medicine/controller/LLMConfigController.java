package cn.zhangchuangla.medicine.controller;

import cn.zhangchuangla.medicine.common.base.AjaxResult;
import cn.zhangchuangla.medicine.common.base.BaseController;
import cn.zhangchuangla.medicine.common.base.Option;
import cn.zhangchuangla.medicine.common.base.TableDataResult;
import cn.zhangchuangla.medicine.common.utils.Assert;
import cn.zhangchuangla.medicine.common.utils.BeanCotyUtils;
import cn.zhangchuangla.medicine.llm.service.ModelConfigService;
import cn.zhangchuangla.medicine.llm.service.OpenAiClientFactory;
import cn.zhangchuangla.medicine.model.dto.ModelConfigDto;
import cn.zhangchuangla.medicine.model.entity.LlmConfig;
import cn.zhangchuangla.medicine.model.entity.ModelConfig;
import cn.zhangchuangla.medicine.model.request.llm.LlmConfigAddRequest;
import cn.zhangchuangla.medicine.model.request.llm.LlmConfigListQueryRequest;
import cn.zhangchuangla.medicine.model.request.llm.LlmConfigUpdateRequest;
import cn.zhangchuangla.medicine.model.vo.llm.LLMOptions;
import cn.zhangchuangla.medicine.model.vo.llm.LlmConfigListVo;
import cn.zhangchuangla.medicine.model.vo.llm.LlmConfigVo;
import cn.zhangchuangla.medicine.service.LlmConfigService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * LLM配置控制器
 *
 * @author Chuang
 * <p>
 * created on 2025/9/6 20:39
 */
@RestController
@RequestMapping("/llm/config")
@RequiredArgsConstructor
@Tag(name = "大模型配置接口", description = "提供大模型的管理")
public class LLMConfigController extends BaseController {

    private final LlmConfigService llmConfigService;
    private final ModelConfigService modelConfigService;
    private final OpenAiClientFactory openAiClientFactory;

    /**
     * 获取LLM配置列表
     *
     * @param request LLM配置列表查询参数
     * @return LLM配置列表视图对象集合
     */
    @GetMapping("/list")
    @Operation(summary = "LLM配置列表")
    public AjaxResult<TableDataResult> listLlmConfig(LlmConfigListQueryRequest request) {
        Page<LlmConfig> llmConfigPage = llmConfigService.listLlmConfig(request);
        List<LlmConfigListVo> llmConfigListVos = copyListProperties(llmConfigPage, LlmConfigListVo.class);
        return getTableData(llmConfigPage, llmConfigListVos);
    }

    /**
     * 获取LLM配置详情
     *
     * @param id 配置ID
     * @return LLM配置详情
     */
    @GetMapping("/{id:\\d+}")
    @Operation(summary = "LLM配置详情")
    public AjaxResult<LlmConfigVo> getLlmConfigById(@PathVariable("id") Long id) {
        LlmConfig llmConfig = llmConfigService.getLlmConfigById(id);
        LlmConfigVo llmConfigVo = copyProperties(llmConfig, LlmConfigVo.class);
        return success(llmConfigVo);
    }

    /**
     * 添加LLM配置
     *
     * @param request 添加LLM配置参数
     * @return 添加结果
     */
    @PostMapping
    @Operation(summary = "添加LLM配置")
    public AjaxResult<Void> addLlmConfig(@RequestBody LlmConfigAddRequest request) {
        boolean result = llmConfigService.addLlmConfig(request);
        return toAjax(result);
    }

    /**
     * 获取LLM配置选项
     *
     * @return LLM配置选项
     */
    @GetMapping("/options")
    @Operation(summary = "获取LLM配置选项")
    public AjaxResult<List<LLMOptions>> getLLMOptions() {
        List<LLMOptions> llmOptions = llmConfigService.getLLMOptions();
        return success(llmOptions);
    }

    /**
     * 获取LLM提供商选项
     *
     * @return LLM提供商选项
     */
    @GetMapping("/option/provider")
    @Operation(summary = "获取LLM提供商选项")
    public AjaxResult<List<Option<String>>> getLLMProvider() {
        List<Option<String>> options = llmConfigService.getLLMProvider();
        return success(options);
    }

    /**
     * 获取LLM模型选项
     *
     * @param provider LLM提供商
     * @return LLM模型选项
     */
    @GetMapping("/option/model")
    @Operation(summary = "获取LLM模型选项")
    public AjaxResult<List<Option<String>>> getLLMModel(@RequestParam("provider") String provider) {
        Assert.hasText(provider, "LLM提供商不能为空!");
        List<Option<String>> options = llmConfigService.getLLMModel(provider);
        return success(options);
    }

    /**
     * 修改LLM配置
     *
     * @param request 修改LLM配置参数
     * @return 修改结果
     */
    @PutMapping
    @Operation(summary = "修改LLM配置")
    public AjaxResult<Void> updateLlmConfig(@RequestBody LlmConfigUpdateRequest request) {
        boolean result = llmConfigService.updateLlmConfig(request);
        return toAjax(result);
    }

    /**
     * 删除LLM配置
     *
     * @param ids 配置ID列表
     * @return 删除结果
     */
    @DeleteMapping("/{ids}")
    @Operation(summary = "删除LLM配置")
    public AjaxResult<Void> deleteLlmConfig(@PathVariable("ids") List<Long> ids) {
        boolean result = llmConfigService.deleteLlmConfig(ids);
        return toAjax(result);
    }

    /**
     * 更新模型配置
     *
     * @param modelConfigDto 模型配置
     * @return 响应结果
     */
    @PutMapping("/chat")
    @Operation(summary = "更新模型配置")
    public AjaxResult<Void> updateChatModelConfig(@Validated @RequestBody ModelConfigDto modelConfigDto) {
        modelConfigService.updateChatConfig(modelConfigDto);
        return success();
    }

    /**
     * 获取当前模型配置
     *
     * @return 模型配置
     */
    @GetMapping("/chat")
    @Operation(summary = "获取当前模型配置")
    public AjaxResult<ModelConfigDto> getCurrentChatModelConfig() {
        ModelConfig chatConfig = modelConfigService.getChatConfig();
        ModelConfigDto modelConfigDto = BeanCotyUtils.copyProperties(chatConfig, ModelConfigDto.class);
        return success(modelConfigDto);
    }

}
