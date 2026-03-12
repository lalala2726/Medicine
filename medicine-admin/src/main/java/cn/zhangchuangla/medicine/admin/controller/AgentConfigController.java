package cn.zhangchuangla.medicine.admin.controller;

import cn.zhangchuangla.medicine.admin.model.request.*;
import cn.zhangchuangla.medicine.admin.model.vo.*;
import cn.zhangchuangla.medicine.admin.service.AgentConfigService;
import cn.zhangchuangla.medicine.common.core.base.AjaxResult;
import cn.zhangchuangla.medicine.common.security.base.BaseController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Agent 配置管理控制器。
 */
@RestController
@RequestMapping("/agent/config")
@Validated
@Tag(name = "Agent配置管理", description = "管理端 Agent 配置接口")
@RequiredArgsConstructor
public class AgentConfigController extends BaseController {

    private final AgentConfigService agentConfigService;

    /**
     * 查询知识库 Agent 配置。
     *
     * @return 知识库 Agent 配置
     */
    @GetMapping("/knowledge-base")
    @Operation(summary = "知识库Agent配置详情")
    public AjaxResult<KnowledgeBaseAgentConfigVo> getKnowledgeBaseConfig() {
        return success(agentConfigService.getKnowledgeBaseConfig());
    }

    /**
     * 保存知识库 Agent 配置。
     *
     * @param request 知识库 Agent 配置请求
     * @return 操作结果
     */
    @PutMapping("/knowledge-base")
    @Operation(summary = "保存知识库Agent配置")
    public AjaxResult<Void> saveKnowledgeBaseConfig(@Valid @RequestBody KnowledgeBaseAgentConfigRequest request) {
        return toAjax(agentConfigService.saveKnowledgeBaseConfig(request));
    }

    /**
     * 查询管理端助手 Agent 配置。
     *
     * @return 管理端助手 Agent 配置
     */
    @GetMapping("/admin-assistant")
    @Operation(summary = "管理端助手Agent配置详情")
    public AjaxResult<AdminAssistantAgentConfigVo> getAdminAssistantConfig() {
        return success(agentConfigService.getAdminAssistantConfig());
    }

    /**
     * 保存管理端助手 Agent 配置。
     *
     * @param request 管理端助手 Agent 配置请求
     * @return 操作结果
     */
    @PutMapping("/admin-assistant")
    @Operation(summary = "保存管理端助手Agent配置")
    public AjaxResult<Void> saveAdminAssistantConfig(@Valid @RequestBody AdminAssistantAgentConfigRequest request) {
        return toAjax(agentConfigService.saveAdminAssistantConfig(request));
    }

    /**
     * 查询图片识别 Agent 配置。
     *
     * @return 图片识别 Agent 配置
     */
    @GetMapping("/image-recognition")
    @Operation(summary = "图片识别Agent配置详情")
    public AjaxResult<ImageRecognitionAgentConfigVo> getImageRecognitionConfig() {
        return success(agentConfigService.getImageRecognitionConfig());
    }

    /**
     * 查询豆包语音 Agent 配置。
     *
     * @return 豆包语音 Agent 配置
     */
    @GetMapping("/speech")
    @Operation(summary = "豆包语音Agent配置详情")
    public AjaxResult<SpeechAgentConfigVo> getSpeechConfig() {
        return success(agentConfigService.getSpeechConfig());
    }

    /**
     * 保存图片识别 Agent 配置。
     *
     * @param request 图片识别 Agent 配置请求
     * @return 操作结果
     */
    @PutMapping("/image-recognition")
    @Operation(summary = "保存图片识别Agent配置")
    public AjaxResult<Void> saveImageRecognitionConfig(
            @Valid @RequestBody ImageRecognitionAgentConfigRequest request) {
        return toAjax(agentConfigService.saveImageRecognitionConfig(request));
    }

    /**
     * 保存豆包语音 Agent 配置。
     *
     * @param request 豆包语音 Agent 配置请求
     * @return 操作结果
     */
    @PutMapping("/speech")
    @Operation(summary = "保存豆包语音Agent配置")
    public AjaxResult<Void> saveSpeechConfig(@Valid @RequestBody SpeechAgentConfigRequest request) {
        return toAjax(agentConfigService.saveSpeechConfig(request));
    }

    /**
     * 查询聊天历史总结 Agent 配置。
     *
     * @return 聊天历史总结 Agent 配置
     */
    @GetMapping("/chat-history-summary")
    @Operation(summary = "聊天历史总结Agent配置详情")
    public AjaxResult<ChatHistorySummaryAgentConfigVo> getChatHistorySummaryConfig() {
        return success(agentConfigService.getChatHistorySummaryConfig());
    }

    /**
     * 保存聊天历史总结 Agent 配置。
     *
     * @param request 聊天历史总结 Agent 配置请求
     * @return 操作结果
     */
    @PutMapping("/chat-history-summary")
    @Operation(summary = "保存聊天历史总结Agent配置")
    public AjaxResult<Void> saveChatHistorySummaryConfig(
            @Valid @RequestBody ChatHistorySummaryAgentConfigRequest request) {
        return toAjax(agentConfigService.saveChatHistorySummaryConfig(request));
    }

    /**
     * 查询聊天标题生成 Agent 配置。
     *
     * @return 聊天标题生成 Agent 配置
     */
    @GetMapping("/chat-title")
    @Operation(summary = "聊天标题生成Agent配置详情")
    public AjaxResult<ChatTitleAgentConfigVo> getChatTitleConfig() {
        return success(agentConfigService.getChatTitleConfig());
    }

    /**
     * 保存聊天标题生成 Agent 配置。
     *
     * @param request 聊天标题生成 Agent 配置请求
     * @return 操作结果
     */
    @PutMapping("/chat-title")
    @Operation(summary = "保存聊天标题生成Agent配置")
    public AjaxResult<Void> saveChatTitleConfig(
            @Valid @RequestBody ChatTitleAgentConfigRequest request) {
        return toAjax(agentConfigService.saveChatTitleConfig(request));
    }

    /**
     * 查询向量模型选项。
     *
     * @return 向量模型选项列表
     */
    @GetMapping("/embedding-model/option")
    @Operation(summary = "向量模型选项")
    public AjaxResult<List<AgentModelOptionVo>> listEmbeddingModelOptions() {
        return success(agentConfigService.listEmbeddingModelOptions());
    }

    /**
     * 查询重排模型选项。
     *
     * @return 重排模型选项列表
     */
    @GetMapping("/rerank-model/option")
    @Operation(summary = "重排模型选项")
    public AjaxResult<List<AgentModelOptionVo>> listRerankModelOptions() {
        return success(agentConfigService.listRerankModelOptions());
    }

    /**
     * 查询聊天模型选项。
     *
     * @return 聊天模型选项列表
     */
    @GetMapping("/chat-model/option")
    @Operation(summary = "聊天模型选项")
    public AjaxResult<List<AgentModelOptionVo>> listChatModelOptions() {
        return success(agentConfigService.listChatModelOptions());
    }

    /**
     * 查询图片理解模型选项。
     *
     * @return 图片理解模型选项列表
     */
    @GetMapping("/vision-model/option")
    @Operation(summary = "图片理解模型选项")
    public AjaxResult<List<AgentModelOptionVo>> listVisionModelOptions() {
        return success(agentConfigService.listVisionModelOptions());
    }
}
