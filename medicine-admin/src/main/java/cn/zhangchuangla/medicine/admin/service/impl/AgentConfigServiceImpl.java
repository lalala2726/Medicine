package cn.zhangchuangla.medicine.admin.service.impl;

import cn.zhangchuangla.medicine.admin.model.request.*;
import cn.zhangchuangla.medicine.admin.model.vo.*;
import cn.zhangchuangla.medicine.admin.service.*;
import cn.zhangchuangla.medicine.admin.support.KnowledgeBaseEmbeddingDimSupport;
import cn.zhangchuangla.medicine.common.core.enums.ResponseCode;
import cn.zhangchuangla.medicine.common.core.exception.ServiceException;
import cn.zhangchuangla.medicine.common.core.utils.Assert;
import cn.zhangchuangla.medicine.common.security.base.BaseService;
import cn.zhangchuangla.medicine.model.cache.*;
import cn.zhangchuangla.medicine.model.constants.LlmModelTypeConstants;
import cn.zhangchuangla.medicine.model.entity.KbBase;
import cn.zhangchuangla.medicine.model.entity.LlmProvider;
import cn.zhangchuangla.medicine.model.entity.LlmProviderModel;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Agent 配置服务实现。
 * <p>
 * 负责将管理端编辑态配置解析为运行时缓存结构，并统一读写 Redis 中的 Agent 全量配置。
 */
@Service
@RequiredArgsConstructor
public class AgentConfigServiceImpl implements AgentConfigService, BaseService {

    private static final int PROVIDER_STATUS_ENABLED = 1;
    private static final int KNOWLEDGE_BASE_MAX_COUNT = 5;
    private static final int KNOWLEDGE_BASE_TOP_K_MIN = 1;
    private static final int KNOWLEDGE_BASE_TOP_K_MAX = 100;
    private static final int MODEL_STATUS_ENABLED = 0;
    private static final int CAPABILITY_ENABLED = 1;
    private static final int ADMIN_ASSISTANT_MAX_TOKENS_MIN = 100;
    private static final int ADMIN_ASSISTANT_MAX_TOKENS_MAX = 10000;
    /**
     * 客户端助手模型槽位允许配置的最小最大 Token 数。
     */
    private static final int CLIENT_ASSISTANT_MAX_TOKENS_MIN = 100;
    private static final int CLIENT_ASSISTANT_MAX_TOKENS_MAX = 10000;
    private static final int IMAGE_RECOGNITION_MAX_TOKENS_MIN = 512;
    private static final int IMAGE_RECOGNITION_MAX_TOKENS_MAX = 10000;
    private static final int CHAT_HISTORY_SUMMARY_MAX_TOKENS_MIN = 100;
    private static final int CHAT_HISTORY_SUMMARY_MAX_TOKENS_MAX = 10000;
    private static final int CHAT_TITLE_MAX_TOKENS_MIN = 1;
    private static final int CHAT_TITLE_MAX_TOKENS_MAX = 50;
    private static final int SPEECH_MAX_TEXT_CHARS_MIN = 1;
    private static final int SPEECH_MAX_TEXT_CHARS_MAX = 3000;
    private static final double TEMPERATURE_MIN = 0D;
    private static final double TEMPERATURE_MAX = 2D;
    private static final String DEFAULT_OPERATOR = "system";
    private static final String SPEECH_PROVIDER = "volcengine";
    private static final String VOLCENGINE_STT_RESOURCE_ID = "volc.seedasr.sauc.duration";
    private static final String VOLCENGINE_TTS_RESOURCE_ID = "seed-tts-2.0";
    private static final String ENABLED_PROVIDER_MISSING_MESSAGE = "当前没有启用的模型提供商";
    private static final String MODEL_DISABLED_MESSAGE = "模型未启用：%s";
    private static final String REASONING_UNSUPPORTED_MESSAGE = "模型不支持深度思考：%s";
    private static final String VISION_UNSUPPORTED_MESSAGE = "模型不支持图片理解：%s";
    private static final String EMBEDDING_MODEL_MISSING_MESSAGE = "当前启用提供商下不存在向量模型：%s";
    private static final String RANKING_MODEL_MISSING_MESSAGE = "当前启用提供商下不存在排序聊天模型：%s";
    private static final String CHAT_MODEL_MISSING_MESSAGE = "当前启用提供商下不存在聊天模型：%s";
    private static final String VISION_MODEL_MISSING_MESSAGE = "当前启用提供商下不存在图片理解模型：%s";
    private static final String KNOWLEDGE_BASE_NAME_REQUIRED_MESSAGE = "知识库名称不能为空";
    private static final String KNOWLEDGE_BASE_DUPLICATE_MESSAGE = "知识库名称不能重复：%s";
    private static final String KNOWLEDGE_BASE_NOT_FOUND_MESSAGE = "启用中的知识库不存在：%s";
    private static final String KNOWLEDGE_BASE_MODEL_MISMATCH_MESSAGE = "知识库向量模型必须与第一个知识库保持一致：%s";
    private static final String KNOWLEDGE_BASE_DIM_MISMATCH_MESSAGE = "知识库向量维度必须与第一个知识库保持一致：%s";
    private static final String KNOWLEDGE_BASE_CONFIG_MODEL_MISMATCH_MESSAGE = "向量模型必须与第一个知识库保持一致";
    private static final String KNOWLEDGE_BASE_CONFIG_DIM_MISMATCH_MESSAGE = "向量维度必须与第一个知识库保持一致";
    private static final String KNOWLEDGE_BASE_RANKING_REQUIRED_MESSAGE = "启用排序时必须选择排序模型";
    private static final String KNOWLEDGE_BASE_RANKING_DISABLED_MESSAGE = "关闭排序时不允许选择排序模型";
    private static final String KNOWLEDGE_BASE_TOP_K_MIN_MESSAGE = "知识库返回条数不能小于1";
    private static final String KNOWLEDGE_BASE_TOP_K_MAX_MESSAGE = "知识库返回条数不能大于100";
    private static final String SPEECH_APP_ID_REQUIRED_MESSAGE = "豆包语音AppId不能为空";
    private static final String SPEECH_ACCESS_TOKEN_REQUIRED_MESSAGE = "豆包语音AccessToken不能为空";
    private static final String SPEECH_TTS_REQUIRED_MESSAGE = "语音合成配置不能为空";
    private static final String SPEECH_TTS_VOICE_TYPE_REQUIRED_MESSAGE = "语音合成VoiceType不能为空";
    private static final String SPEECH_TTS_MAX_TEXT_CHARS_REQUIRED_MESSAGE = "语音合成最大文本长度不能为空";
    private static final String SPEECH_TTS_MAX_TEXT_CHARS_MIN_MESSAGE = "语音合成最大文本长度不能小于1";
    private static final String SPEECH_TTS_MAX_TEXT_CHARS_MAX_MESSAGE = "语音合成最大文本长度不能大于3000";

    private final KbBaseService kbBaseService;
    private final LlmProviderService llmProviderService;
    private final LlmProviderModelService llmProviderModelService;
    private final AgentConfigRuntimeSyncService agentConfigRuntimeSyncService;

    /**
     * 查询知识库 Agent 配置详情。
     *
     * @return 知识库 Agent 配置
     */
    @Override
    public KnowledgeBaseAgentConfigVo getKnowledgeBaseConfig() {
        KnowledgeBaseAgentConfigVo vo = new KnowledgeBaseAgentConfigVo();
        AgentAllConfigCache cache = agentConfigRuntimeSyncService.readCache();
        KnowledgeBaseAgentConfig config = cache.getKnowledgeBase();
        if (config == null) {
            vo.setEnabled(Boolean.FALSE);
            return vo;
        }
        LlmProvider provider = getEnabledProviderOrNull();
        vo.setEnabled(resolveKnowledgeBaseEnabled(config));
        vo.setKnowledgeNames(copyKnowledgeNames(config.getKnowledgeNames()));
        vo.setEmbeddingDim(config.getEmbeddingDim());
        vo.setTopK(config.getTopK());
        vo.setEmbeddingModel(toKnowledgeBaseModelSelectionVo(provider, config.getEmbeddingModel(),
                LlmModelTypeConstants.EMBEDDING));
        boolean rankingEnabled = resolveKnowledgeBaseRankingEnabled(config);
        vo.setRankingEnabled(rankingEnabled);
        vo.setRankingModel(rankingEnabled
                ? toKnowledgeBaseModelSelectionVo(provider, config.getRankingModel(), LlmModelTypeConstants.CHAT)
                : null);
        return vo;
    }

    /**
     * 保存知识库 Agent 配置。
     *
     * @param request 知识库 Agent 配置请求
     * @return 是否保存成功
     */
    @Override
    public boolean saveKnowledgeBaseConfig(KnowledgeBaseAgentConfigRequest request) {
        Assert.notNull(request, "知识库Agent配置不能为空");
        AgentAllConfigCache cache = agentConfigRuntimeSyncService.readCache();
        boolean enabled = Boolean.TRUE.equals(request.getEnabled());
        KnowledgeBaseAgentConfig config = buildKnowledgeBaseConfig(cache.getKnowledgeBase(), request, enabled);
        cache.setKnowledgeBase(config);
        agentConfigRuntimeSyncService.saveCache(cache, enabled ? getRequiredEnabledProvider() : getEnabledProviderOrNull(),
                currentOperator());
        return true;
    }

    /**
     * 查询知识库下拉选项列表。
     *
     * @return 知识库下拉选项
     */
    @Override
    public List<KnowledgeBaseOptionVo> listKnowledgeBaseOptions() {
        return kbBaseService.listEnabledKnowledgeBases().stream()
                .map(this::toKnowledgeBaseOptionVo)
                .toList();
    }

    /**
     * 查询管理端助手 Agent 配置详情。
     *
     * @return 管理端助手 Agent 配置
     */
    @Override
    public AdminAssistantAgentConfigVo getAdminAssistantConfig() {
        AdminAssistantAgentConfigVo vo = new AdminAssistantAgentConfigVo();
        AgentAllConfigCache cache = agentConfigRuntimeSyncService.readCache();
        AdminAssistantAgentConfig config = cache.getAdminAssistant();
        if (config == null) {
            return vo;
        }
        LlmProvider provider = getEnabledProviderOrNull();
        vo.setRouteModel(toAgentModelSelectionVo(provider, config.getRouteModel(), LlmModelTypeConstants.CHAT));
        vo.setBusinessNodeSimpleModel(toAgentModelSelectionVo(provider, config.getBusinessNodeSimpleModel(),
                LlmModelTypeConstants.CHAT));
        vo.setBusinessNodeComplexModel(toAgentModelSelectionVo(provider, config.getBusinessNodeComplexModel(),
                LlmModelTypeConstants.CHAT));
        vo.setChatModel(toAgentModelSelectionVo(provider, config.getChatModel(), LlmModelTypeConstants.CHAT));
        return vo;
    }

    /**
     * 查询客户端助手 Agent 配置详情。
     *
     * @return 客户端助手 Agent 配置
     */
    @Override
    public ClientAssistantAgentConfigVo getClientAssistantConfig() {
        ClientAssistantAgentConfigVo vo = new ClientAssistantAgentConfigVo();
        AgentAllConfigCache cache = agentConfigRuntimeSyncService.readCache();
        ClientAssistantAgentConfig config = cache.getClientAssistant();
        if (config == null) {
            return vo;
        }
        LlmProvider provider = getEnabledProviderOrNull();
        vo.setRouteModel(toAgentModelSelectionVo(provider, config.getRouteModel(), LlmModelTypeConstants.CHAT));
        vo.setChatModel(toAgentModelSelectionVo(provider, config.getChatModel(), LlmModelTypeConstants.CHAT));
        vo.setOrderModel(toAgentModelSelectionVo(provider, config.getOrderModel(), LlmModelTypeConstants.CHAT));
        vo.setProductModel(toAgentModelSelectionVo(provider, config.getProductModel(), LlmModelTypeConstants.CHAT));
        vo.setAfterSaleModel(toAgentModelSelectionVo(provider, config.getAfterSaleModel(), LlmModelTypeConstants.CHAT));
        vo.setConsultationComfortModel(toAgentModelSelectionVo(provider, config.getConsultationComfortModel(),
                LlmModelTypeConstants.CHAT));
        vo.setConsultationQuestionModel(toAgentModelSelectionVo(provider, config.getConsultationQuestionModel(),
                LlmModelTypeConstants.CHAT));
        vo.setConsultationFinalDiagnosisModel(toAgentModelSelectionVo(provider,
                config.getConsultationFinalDiagnosisModel(), LlmModelTypeConstants.CHAT));
        return vo;
    }

    /**
     * 保存管理端助手 Agent 配置。
     *
     * @param request 管理端助手 Agent 配置请求
     * @return 是否保存成功
     */
    @Override
    public boolean saveAdminAssistantConfig(AdminAssistantAgentConfigRequest request) {
        Assert.notNull(request, "管理端助手Agent配置不能为空");
        validateAdminAssistantRequest(request);

        LlmProvider provider = getRequiredEnabledProvider();
        AdminAssistantAgentConfig config = new AdminAssistantAgentConfig();
        config.setRouteModel(resolveRequiredSlotConfig(provider, request.getRouteModel(),
                LlmModelTypeConstants.CHAT, false, CHAT_MODEL_MISSING_MESSAGE));
        config.setBusinessNodeSimpleModel(resolveRequiredSlotConfig(provider, request.getBusinessNodeSimpleModel(),
                LlmModelTypeConstants.CHAT, false, CHAT_MODEL_MISSING_MESSAGE));
        config.setBusinessNodeComplexModel(resolveRequiredSlotConfig(provider, request.getBusinessNodeComplexModel(),
                LlmModelTypeConstants.CHAT, false, CHAT_MODEL_MISSING_MESSAGE));
        config.setChatModel(resolveRequiredSlotConfig(provider, request.getChatModel(),
                LlmModelTypeConstants.CHAT, false, CHAT_MODEL_MISSING_MESSAGE));

        AgentAllConfigCache cache = agentConfigRuntimeSyncService.readCache();
        cache.setAdminAssistant(config);
        agentConfigRuntimeSyncService.saveCache(cache, provider, currentOperator());
        return true;
    }

    /**
     * 保存客户端助手 Agent 配置。
     *
     * @param request 客户端助手 Agent 配置请求
     * @return 是否保存成功
     */
    @Override
    public boolean saveClientAssistantConfig(ClientAssistantAgentConfigRequest request) {
        Assert.notNull(request, "客户端助手Agent配置不能为空");
        validateClientAssistantRequest(request);

        LlmProvider provider = getRequiredEnabledProvider();
        ClientAssistantAgentConfig config = new ClientAssistantAgentConfig();
        config.setRouteModel(resolveRequiredSlotConfig(provider, request.getRouteModel(),
                LlmModelTypeConstants.CHAT, false, CHAT_MODEL_MISSING_MESSAGE));
        config.setChatModel(resolveRequiredSlotConfig(provider, request.getChatModel(),
                LlmModelTypeConstants.CHAT, false, CHAT_MODEL_MISSING_MESSAGE));
        config.setOrderModel(resolveRequiredSlotConfig(provider, request.getOrderModel(),
                LlmModelTypeConstants.CHAT, false, CHAT_MODEL_MISSING_MESSAGE));
        config.setProductModel(resolveRequiredSlotConfig(provider, request.getProductModel(),
                LlmModelTypeConstants.CHAT, false, CHAT_MODEL_MISSING_MESSAGE));
        config.setAfterSaleModel(resolveRequiredSlotConfig(provider, request.getAfterSaleModel(),
                LlmModelTypeConstants.CHAT, false, CHAT_MODEL_MISSING_MESSAGE));
        config.setConsultationComfortModel(resolveRequiredSlotConfig(provider, request.getConsultationComfortModel(),
                LlmModelTypeConstants.CHAT, false, CHAT_MODEL_MISSING_MESSAGE));
        config.setConsultationQuestionModel(resolveRequiredSlotConfig(provider, request.getConsultationQuestionModel(),
                LlmModelTypeConstants.CHAT, false, CHAT_MODEL_MISSING_MESSAGE));
        config.setConsultationFinalDiagnosisModel(resolveRequiredSlotConfig(provider,
                request.getConsultationFinalDiagnosisModel(), LlmModelTypeConstants.CHAT, false,
                CHAT_MODEL_MISSING_MESSAGE));

        AgentAllConfigCache cache = agentConfigRuntimeSyncService.readCache();
        cache.setClientAssistant(config);
        agentConfigRuntimeSyncService.saveCache(cache, provider, currentOperator());
        return true;
    }

    /**
     * 查询图片识别 Agent 配置详情。
     *
     * @return 图片识别 Agent 配置
     */
    @Override
    public ImageRecognitionAgentConfigVo getImageRecognitionConfig() {
        ImageRecognitionAgentConfigVo vo = new ImageRecognitionAgentConfigVo();
        AgentAllConfigCache cache = agentConfigRuntimeSyncService.readCache();
        ImageRecognitionAgentConfig config = cache.getImageRecognition();
        if (config == null) {
            return vo;
        }
        vo.setImageRecognitionModel(toAgentModelSelectionVo(getEnabledProviderOrNull(),
                config.getImageRecognitionModel(), LlmModelTypeConstants.CHAT));
        return vo;
    }

    /**
     * 查询豆包语音 Agent 配置详情。
     *
     * @return 豆包语音 Agent 配置
     */
    @Override
    public SpeechAgentConfigVo getSpeechConfig() {
        return toSpeechConfigVo(agentConfigRuntimeSyncService.readCache().getSpeech());
    }

    /**
     * 保存图片识别 Agent 配置。
     *
     * @param request 图片识别 Agent 配置请求
     * @return 是否保存成功
     */
    @Override
    public boolean saveImageRecognitionConfig(ImageRecognitionAgentConfigRequest request) {
        Assert.notNull(request, "图片识别Agent配置不能为空");
        validateImageRecognitionRequest(request);

        LlmProvider provider = getRequiredEnabledProvider();
        ImageRecognitionAgentConfig config = new ImageRecognitionAgentConfig();
        config.setImageRecognitionModel(resolveRequiredSlotConfig(provider, request.getImageRecognitionModel(),
                LlmModelTypeConstants.CHAT, true, VISION_MODEL_MISSING_MESSAGE));

        AgentAllConfigCache cache = agentConfigRuntimeSyncService.readCache();
        cache.setImageRecognition(config);
        agentConfigRuntimeSyncService.saveCache(cache, provider, currentOperator());
        return true;
    }

    /**
     * 保存豆包语音 Agent 配置。
     *
     * @param request 豆包语音 Agent 配置请求
     * @return 是否保存成功
     */
    @Override
    public boolean saveSpeechConfig(SpeechAgentConfigRequest request) {
        Assert.notNull(request, "豆包语音Agent配置不能为空");

        AgentAllConfigCache cache = agentConfigRuntimeSyncService.readCache();
        SpeechAgentConfig existingConfig = cache.getSpeech();
        validateSpeechRequest(request, existingConfig);

        cache.setSpeech(buildSpeechConfig(request, existingConfig));
        agentConfigRuntimeSyncService.saveCache(cache, getEnabledProviderOrNull(), currentOperator());
        return true;
    }

    /**
     * 查询聊天历史总结 Agent 配置详情。
     *
     * @return 聊天历史总结 Agent 配置
     */
    @Override
    public ChatHistorySummaryAgentConfigVo getChatHistorySummaryConfig() {
        ChatHistorySummaryAgentConfigVo vo = new ChatHistorySummaryAgentConfigVo();
        AgentAllConfigCache cache = agentConfigRuntimeSyncService.readCache();
        ChatHistorySummaryAgentConfig config = cache.getChatHistorySummary();
        if (config == null) {
            return vo;
        }
        vo.setChatHistorySummaryModel(toAgentModelSelectionVo(getEnabledProviderOrNull(),
                config.getChatHistorySummaryModel(), LlmModelTypeConstants.CHAT));
        return vo;
    }

    /**
     * 保存聊天历史总结 Agent 配置。
     *
     * @param request 聊天历史总结 Agent 配置请求
     * @return 是否保存成功
     */
    @Override
    public boolean saveChatHistorySummaryConfig(ChatHistorySummaryAgentConfigRequest request) {
        Assert.notNull(request, "聊天历史总结Agent配置不能为空");
        validateChatHistorySummaryRequest(request);

        LlmProvider provider = getRequiredEnabledProvider();
        ChatHistorySummaryAgentConfig config = new ChatHistorySummaryAgentConfig();
        config.setChatHistorySummaryModel(resolveRequiredSlotConfig(provider, request.getChatHistorySummaryModel(),
                LlmModelTypeConstants.CHAT, false, CHAT_MODEL_MISSING_MESSAGE));

        AgentAllConfigCache cache = agentConfigRuntimeSyncService.readCache();
        cache.setChatHistorySummary(config);
        agentConfigRuntimeSyncService.saveCache(cache, provider, currentOperator());
        return true;
    }

    /**
     * 查询聊天标题生成 Agent 配置详情。
     *
     * @return 聊天标题生成 Agent 配置
     */
    @Override
    public ChatTitleAgentConfigVo getChatTitleConfig() {
        ChatTitleAgentConfigVo vo = new ChatTitleAgentConfigVo();
        AgentAllConfigCache cache = agentConfigRuntimeSyncService.readCache();
        ChatTitleAgentConfig config = cache.getChatTitle();
        if (config == null) {
            return vo;
        }
        vo.setChatTitleModel(toAgentModelSelectionVo(getEnabledProviderOrNull(),
                config.getChatTitleModel(), LlmModelTypeConstants.CHAT));
        return vo;
    }

    /**
     * 保存聊天标题生成 Agent 配置。
     *
     * @param request 聊天标题生成 Agent 配置请求
     * @return 是否保存成功
     */
    @Override
    public boolean saveChatTitleConfig(ChatTitleAgentConfigRequest request) {
        Assert.notNull(request, "聊天标题生成Agent配置不能为空");
        validateChatTitleRequest(request);

        LlmProvider provider = getRequiredEnabledProvider();
        ChatTitleAgentConfig config = new ChatTitleAgentConfig();
        config.setChatTitleModel(resolveRequiredSlotConfig(provider, request.getChatTitleModel(),
                LlmModelTypeConstants.CHAT, false, CHAT_MODEL_MISSING_MESSAGE));

        AgentAllConfigCache cache = agentConfigRuntimeSyncService.readCache();
        cache.setChatTitle(config);
        agentConfigRuntimeSyncService.saveCache(cache, provider, currentOperator());
        return true;
    }

    /**
     * 查询向量模型选项列表。
     *
     * @return 向量模型选项
     */
    @Override
    public List<AgentModelOptionVo> listEmbeddingModelOptions() {
        return listModelOptions(LlmModelTypeConstants.EMBEDDING, false);
    }

    /**
     * 查询聊天模型选项列表。
     *
     * @return 聊天模型选项
     */
    @Override
    public List<AgentModelOptionVo> listChatModelOptions() {
        return listModelOptions(LlmModelTypeConstants.CHAT, false);
    }

    /**
     * 查询图片理解模型选项列表。
     *
     * @return 图片理解模型选项
     */
    @Override
    public List<AgentModelOptionVo> listVisionModelOptions() {
        return listModelOptions(LlmModelTypeConstants.CHAT, true);
    }

    /**
     * 将运行时槽位配置转换为编辑态视图对象。
     *
     * @param provider   当前启用的模型提供商
     * @param slotConfig 运行时槽位配置
     * @param modelType  模型类型
     * @return 编辑态视图对象
     */
    private AgentModelSelectionVo toAgentModelSelectionVo(LlmProvider provider,
                                                          AgentModelSlotConfig slotConfig,
                                                          String modelType) {
        if (slotConfig == null) {
            return null;
        }
        AgentModelSelectionVo vo = new AgentModelSelectionVo();
        vo.setModelName(slotConfig.getModelName());
        vo.setReasoningEnabled(slotConfig.getReasoningEnabled());
        fillModelCapabilities(vo, provider, slotConfig.getModelName(), modelType);
        vo.setMaxTokens(slotConfig.getMaxTokens());
        vo.setTemperature(slotConfig.getTemperature());
        return vo;
    }

    /**
     * 将知识库中仅保存模型名称的配置转换为编辑态视图对象。
     *
     * @param provider  当前启用的模型提供商
     * @param modelName 模型名称
     * @param modelType 模型类型
     * @return 编辑态视图对象；模型名称为空时返回 null
     */
    private AgentModelSelectionVo toKnowledgeBaseModelSelectionVo(LlmProvider provider,
                                                                  String modelName,
                                                                  String modelType) {
        if (!StringUtils.hasText(modelName)) {
            return null;
        }
        AgentModelSelectionVo vo = new AgentModelSelectionVo();
        vo.setModelName(modelName);
        vo.setReasoningEnabled(false);
        fillModelCapabilities(vo, provider, modelName, modelType);
        return vo;
    }

    /**
     * 为模型选择视图对象补充能力信息。
     * <p>
     * 根据提供商、模型名称和模型类型查询模型元数据，并回填深度思考与图片理解能力标记。
     *
     * @param vo        待回填能力信息的视图对象
     * @param provider  当前启用的模型提供商
     * @param modelName 模型名称
     * @param modelType 模型类型
     */
    private void fillModelCapabilities(AgentModelSelectionVo vo, LlmProvider provider, String modelName, String modelType) {
        if (provider == null || !StringUtils.hasText(modelName)) {
            return;
        }
        List<LlmProviderModel> models = llmProviderModelService.lambdaQuery()
                .eq(LlmProviderModel::getProviderId, provider.getId())
                .eq(LlmProviderModel::getModelType, modelType)
                .eq(LlmProviderModel::getModelName, modelName)
                .orderByAsc(LlmProviderModel::getSort, LlmProviderModel::getId)
                .list();
        if (models.isEmpty()) {
            return;
        }
        LlmProviderModel model = models.getFirst();
        vo.setSupportReasoning(isCapabilityEnabled(model.getSupportReasoning()));
        vo.setSupportVision(isCapabilityEnabled(model.getSupportVision()));
    }

    /**
     * 将豆包语音运行时配置转换为详情视图对象。
     *
     * @param config 豆包语音运行时配置
     * @return 豆包语音详情视图对象
     */
    private SpeechAgentConfigVo toSpeechConfigVo(SpeechAgentConfig config) {
        SpeechAgentConfigVo vo = new SpeechAgentConfigVo();
        if (config == null) {
            return vo;
        }
        vo.setAppId(config.getAppId());
        vo.setAccessToken(null);
        vo.setTextToSpeech(toTextToSpeechConfigVo(config.getTextToSpeech()));
        return vo;
    }

    /**
     * 将语音合成运行时配置转换为详情视图对象。
     *
     * @param config 语音合成运行时配置
     * @return 语音合成详情视图对象
     */
    private TextToSpeechConfigVo toTextToSpeechConfigVo(TextToSpeechAgentConfig config) {
        if (config == null) {
            return null;
        }
        TextToSpeechConfigVo vo = new TextToSpeechConfigVo();
        vo.setVoiceType(config.getVoiceType());
        vo.setMaxTextChars(config.getMaxTextChars());
        return vo;
    }

    /**
     * 查询指定模型类型的下拉选项。
     *
     * @param modelType      模型类型
     * @param visionRequired 是否要求支持图片理解
     * @return 模型选项列表
     */
    private List<AgentModelOptionVo> listModelOptions(String modelType, boolean visionRequired) {
        LlmProvider provider = getEnabledProviderOrNull();
        if (provider == null) {
            return List.of();
        }
        return listEnabledProviderModels(provider.getId(), modelType, visionRequired)
                .stream()
                .map(this::toAgentModelOptionVo)
                .toList();
    }

    /**
     * 将提供商模型实体转换为带能力信息的下拉选项视图对象。
     *
     * @param providerModel 提供商模型实体
     * @return 模型下拉选项视图对象
     */
    private AgentModelOptionVo toAgentModelOptionVo(LlmProviderModel providerModel) {
        AgentModelOptionVo vo = new AgentModelOptionVo();
        vo.setLabel(providerModel.getModelName());
        vo.setValue(providerModel.getModelName());
        vo.setSupportReasoning(isCapabilityEnabled(providerModel.getSupportReasoning()));
        vo.setSupportVision(isCapabilityEnabled(providerModel.getSupportVision()));
        return vo;
    }

    /**
     * 将知识库实体转换为下拉选项视图对象。
     *
     * @param kbBase 知识库实体
     * @return 知识库下拉选项视图对象
     */
    private KnowledgeBaseOptionVo toKnowledgeBaseOptionVo(KbBase kbBase) {
        KnowledgeBaseOptionVo vo = new KnowledgeBaseOptionVo();
        vo.setKnowledgeName(kbBase.getKnowledgeName());
        vo.setDisplayName(kbBase.getDisplayName());
        vo.setEmbeddingModel(kbBase.getEmbeddingModel());
        vo.setEmbeddingDim(kbBase.getEmbeddingDim());
        return vo;
    }

    /**
     * 解析必填模型槽位配置。
     *
     * @param provider                启用提供商
     * @param request                 槽位请求
     * @param modelType               期望模型类型
     * @param visionRequired          是否要求支持图片理解
     * @param modelMissingMessageForm 模型不存在提示模板
     * @return 运行时槽位配置
     */
    private AgentModelSlotConfig resolveRequiredSlotConfig(LlmProvider provider,
                                                           AgentModelSelectionRequest request,
                                                           String modelType,
                                                           boolean visionRequired,
                                                           String modelMissingMessageForm) {
        Assert.notNull(request, "模型配置不能为空");
        return resolveSlotConfig(provider, request, modelType, visionRequired, modelMissingMessageForm);
    }

    /**
     * 解析知识库排序模型名称。
     * <p>
     * 当排序关闭时校验不能传入排序模型；当排序开启时校验排序模型必填且必须是可用聊天模型。
     *
     * @param provider 启用的模型提供商
     * @param request  知识库配置请求
     * @return 排序模型名称；关闭排序时返回 null
     */
    private String resolveKnowledgeBaseRankingModel(LlmProvider provider,
                                                    KnowledgeBaseAgentConfigRequest request) {
        boolean rankingEnabled = Boolean.TRUE.equals(request.getRankingEnabled());
        AgentModelSelectionRequest rankingRequest = request.getRankingModel();
        if (!rankingEnabled) {
            Assert.isParamTrue(!hasSelectedModel(rankingRequest), KNOWLEDGE_BASE_RANKING_DISABLED_MESSAGE);
            return null;
        }
        Assert.isParamTrue(hasSelectedModel(rankingRequest), KNOWLEDGE_BASE_RANKING_REQUIRED_MESSAGE);
        return resolveSlotConfig(provider, rankingRequest, LlmModelTypeConstants.CHAT, false,
                RANKING_MODEL_MISSING_MESSAGE).getModelName();
    }

    /**
     * 解析单个模型槽位的最终运行时配置。
     *
     * @param provider                启用提供商
     * @param request                 槽位请求
     * @param modelType               期望模型类型
     * @param visionRequired          是否要求支持图片理解
     * @param modelMissingMessageForm 模型不存在提示模板
     * @return 运行时槽位配置
     */
    private AgentModelSlotConfig resolveSlotConfig(LlmProvider provider,
                                                   AgentModelSelectionRequest request,
                                                   String modelType,
                                                   boolean visionRequired,
                                                   String modelMissingMessageForm) {
        String modelName = normalizeRequiredModelName(request);
        LlmProviderModel providerModel = getProviderModel(provider, modelName, modelType, modelMissingMessageForm);
        validateProviderModelEnabled(providerModel);
        validateReasoningCapability(request, providerModel);
        validateVisionCapability(modelName, providerModel, visionRequired);
        return buildSlotConfig(providerModel, request);
    }

    /**
     * 构建知识库运行时配置。
     * <p>
     * 启用状态下会完成知识库、向量模型、向量维度和排序模型的完整校验；禁用状态下转为保留历史配置的轻量更新。
     *
     * @param existingConfig 现有知识库运行时配置
     * @param request        知识库编辑态请求
     * @param enabled        是否启用知识库能力
     * @return 最终写入缓存的知识库运行时配置
     */
    private KnowledgeBaseAgentConfig buildKnowledgeBaseConfig(KnowledgeBaseAgentConfig existingConfig,
                                                              KnowledgeBaseAgentConfigRequest request,
                                                              boolean enabled) {
        if (!enabled) {
            return buildDisabledKnowledgeBaseConfig(existingConfig, request);
        }

        validateEmbeddingDim(request.getEmbeddingDim());

        List<String> knowledgeNames = normalizeKnowledgeNames(request.getKnowledgeNames());
        Integer topK = normalizeKnowledgeBaseTopK(request.getTopK());
        LlmProvider provider = getRequiredEnabledProvider();
        List<KbBase> knowledgeBases = loadEnabledKnowledgeBases(knowledgeNames);
        KbBase baseline = knowledgeBases.getFirst();
        AgentModelSlotConfig embeddingModel = resolveRequiredSlotConfig(provider, request.getEmbeddingModel(),
                LlmModelTypeConstants.EMBEDDING, false, EMBEDDING_MODEL_MISSING_MESSAGE);
        validateKnowledgeBasesAgainstBaseline(knowledgeBases, baseline);
        validateKnowledgeBaseCommonConfig(embeddingModel, request.getEmbeddingDim(), baseline);
        String rankingModel = resolveKnowledgeBaseRankingModel(provider, request);

        KnowledgeBaseAgentConfig config = new KnowledgeBaseAgentConfig();
        config.setEnabled(Boolean.TRUE);
        config.setKnowledgeNames(knowledgeNames);
        config.setEmbeddingDim(request.getEmbeddingDim());
        config.setTopK(topK);
        config.setEmbeddingModel(embeddingModel.getModelName());
        config.setRankingEnabled(Boolean.TRUE.equals(request.getRankingEnabled()));
        config.setRankingModel(rankingModel);
        return config;
    }

    /**
     * 构建禁用状态下的知识库运行时配置。
     * <p>
     * 该方法以现有配置为基准复制出新对象，并按请求覆盖允许更新的字段，同时强制将 enabled 置为 false。
     *
     * @param existingConfig 现有知识库运行时配置
     * @param request        知识库编辑态请求
     * @return 禁用状态下的知识库运行时配置
     */
    private KnowledgeBaseAgentConfig buildDisabledKnowledgeBaseConfig(KnowledgeBaseAgentConfig existingConfig,
                                                                      KnowledgeBaseAgentConfigRequest request) {
        KnowledgeBaseAgentConfig config = copyKnowledgeBaseConfig(existingConfig);
        config.setEnabled(Boolean.FALSE);
        if (request.getKnowledgeNames() != null) {
            config.setKnowledgeNames(copyKnowledgeNames(request.getKnowledgeNames()));
        }
        if (request.getEmbeddingDim() != null || request.getEmbeddingModel() != null || request.getTopK() != null
                || request.getRankingEnabled() != null || request.getRankingModel() != null) {
            config.setEmbeddingDim(request.getEmbeddingDim());
            config.setTopK(normalizeKnowledgeBaseTopK(request.getTopK()));
            config.setEmbeddingModel(normalizeOptionalModelName(request.getEmbeddingModel()));
            config.setRankingEnabled(request.getRankingEnabled());
            config.setRankingModel(normalizeOptionalModelName(request.getRankingModel()));
        }
        return config;
    }

    /**
     * 拷贝知识库运行时配置，避免直接修改原对象。
     *
     * @param existingConfig 原始知识库运行时配置
     * @return 拷贝后的知识库运行时配置；原配置为空时返回空对象
     */
    private KnowledgeBaseAgentConfig copyKnowledgeBaseConfig(KnowledgeBaseAgentConfig existingConfig) {
        KnowledgeBaseAgentConfig config = new KnowledgeBaseAgentConfig();
        if (existingConfig == null) {
            return config;
        }
        config.setEnabled(existingConfig.getEnabled());
        config.setKnowledgeNames(copyKnowledgeNames(existingConfig.getKnowledgeNames()));
        config.setEmbeddingDim(existingConfig.getEmbeddingDim());
        config.setTopK(existingConfig.getTopK());
        config.setEmbeddingModel(existingConfig.getEmbeddingModel());
        config.setRankingEnabled(existingConfig.getRankingEnabled());
        config.setRankingModel(existingConfig.getRankingModel());
        return config;
    }

    /**
     * 按提供商、名称和类型查询模型。
     *
     * @param provider                启用提供商
     * @param modelName               模型名称
     * @param modelType               模型类型
     * @param modelMissingMessageForm 模型不存在提示模板
     * @return 提供商模型实体
     */
    private LlmProviderModel getProviderModel(LlmProvider provider,
                                              String modelName,
                                              String modelType,
                                              String modelMissingMessageForm) {
        List<LlmProviderModel> models = llmProviderModelService.lambdaQuery()
                .eq(LlmProviderModel::getProviderId, provider.getId())
                .eq(LlmProviderModel::getModelType, modelType)
                .eq(LlmProviderModel::getModelName, modelName)
                .orderByAsc(LlmProviderModel::getSort, LlmProviderModel::getId)
                .list();
        if (models.isEmpty()) {
            throw new ServiceException(ResponseCode.OPERATION_ERROR, modelMissingMessageForm.formatted(modelName));
        }
        return models.getFirst();
    }

    /**
     * 校验模型是否启用。
     *
     * @param providerModel 提供商模型实体
     */
    private void validateProviderModelEnabled(LlmProviderModel providerModel) {
        if (providerModel.getEnabled() == null || providerModel.getEnabled() != MODEL_STATUS_ENABLED) {
            throw new ServiceException(ResponseCode.OPERATION_ERROR,
                    MODEL_DISABLED_MESSAGE.formatted(providerModel.getModelName()));
        }
    }

    /**
     * 校验模型是否支持深度思考。
     *
     * @param request       模型选择请求
     * @param providerModel 提供商模型实体
     */
    private void validateReasoningCapability(AgentModelSelectionRequest request, LlmProviderModel providerModel) {
        if (Boolean.TRUE.equals(request.getReasoningEnabled())
                && !isCapabilityEnabled(providerModel.getSupportReasoning())) {
            throw new ServiceException(ResponseCode.OPERATION_ERROR,
                    REASONING_UNSUPPORTED_MESSAGE.formatted(providerModel.getModelName()));
        }
    }

    /**
     * 校验模型是否支持图片理解。
     *
     * @param modelName      模型名称
     * @param providerModel  提供商模型实体
     * @param visionRequired 是否要求图片理解能力
     */
    private void validateVisionCapability(String modelName, LlmProviderModel providerModel, boolean visionRequired) {
        if (visionRequired && !isCapabilityEnabled(providerModel.getSupportVision())) {
            throw new ServiceException(ResponseCode.OPERATION_ERROR, VISION_UNSUPPORTED_MESSAGE.formatted(modelName));
        }
    }

    /**
     * 构建运行时槽位配置。
     *
     * @param providerModel 提供商模型实体
     * @param request       编辑态请求
     * @return 运行时槽位配置
     */
    private AgentModelSlotConfig buildSlotConfig(LlmProviderModel providerModel,
                                                 AgentModelSelectionRequest request) {
        AgentModelSlotConfig slotConfig = new AgentModelSlotConfig();
        slotConfig.setModelName(providerModel.getModelName());
        slotConfig.setReasoningEnabled(request.getReasoningEnabled());
        slotConfig.setMaxTokens(request.getMaxTokens());
        slotConfig.setTemperature(request.getTemperature());
        return slotConfig;
    }

    /**
     * 归一化知识库名称列表，并校验非空、数量上限与重复项。
     *
     * @param knowledgeNames 原始知识库名称列表
     * @return 归一化后的知识库名称列表
     */
    private List<String> normalizeKnowledgeNames(List<String> knowledgeNames) {
        Assert.notEmpty(knowledgeNames, "知识库名称列表不能为空");
        Assert.isParamTrue(knowledgeNames.size() <= KNOWLEDGE_BASE_MAX_COUNT, "知识库最多支持5个");

        List<String> normalizedNames = new ArrayList<>(knowledgeNames.size());
        LinkedHashSet<String> uniqueNames = new LinkedHashSet<>();
        for (String knowledgeName : knowledgeNames) {
            String normalizedName = normalizeNullableText(knowledgeName);
            Assert.notEmpty(normalizedName, KNOWLEDGE_BASE_NAME_REQUIRED_MESSAGE);
            Assert.isParamTrue(uniqueNames.add(normalizedName),
                    KNOWLEDGE_BASE_DUPLICATE_MESSAGE.formatted(normalizedName));
            normalizedNames.add(normalizedName);
        }
        return normalizedNames;
    }

    /**
     * 按名称加载启用中的知识库，并保持与请求一致的顺序。
     *
     * @param knowledgeNames 知识库名称列表
     * @return 启用中的知识库列表
     */
    private List<KbBase> loadEnabledKnowledgeBases(List<String> knowledgeNames) {
        List<KbBase> knowledgeBases = kbBaseService.listEnabledKnowledgeBasesByNames(knowledgeNames);
        Map<String, KbBase> knowledgeBaseMap = knowledgeBases.stream()
                .collect(Collectors.toMap(KbBase::getKnowledgeName, Function.identity()));
        List<KbBase> orderedKnowledgeBases = new ArrayList<>(knowledgeNames.size());
        for (String knowledgeName : knowledgeNames) {
            KbBase kbBase = knowledgeBaseMap.get(knowledgeName);
            if (kbBase == null) {
                throw new ServiceException(ResponseCode.OPERATION_ERROR,
                        KNOWLEDGE_BASE_NOT_FOUND_MESSAGE.formatted(knowledgeName));
            }
            orderedKnowledgeBases.add(kbBase);
        }
        return orderedKnowledgeBases;
    }

    /**
     * 校验多个知识库的公共向量配置是否与基准知识库一致。
     *
     * @param knowledgeBases 待校验的知识库列表
     * @param baseline       作为基准的第一个知识库
     */
    private void validateKnowledgeBasesAgainstBaseline(List<KbBase> knowledgeBases, KbBase baseline) {
        for (int index = 1; index < knowledgeBases.size(); index++) {
            KbBase kbBase = knowledgeBases.get(index);
            if (!Objects.equals(normalizeNullableText(kbBase.getEmbeddingModel()),
                    normalizeNullableText(baseline.getEmbeddingModel()))) {
                throw new ServiceException(ResponseCode.OPERATION_ERROR,
                        KNOWLEDGE_BASE_MODEL_MISMATCH_MESSAGE.formatted(kbBase.getKnowledgeName()));
            }
            if (!Objects.equals(kbBase.getEmbeddingDim(), baseline.getEmbeddingDim())) {
                throw new ServiceException(ResponseCode.OPERATION_ERROR,
                        KNOWLEDGE_BASE_DIM_MISMATCH_MESSAGE.formatted(kbBase.getKnowledgeName()));
            }
        }
    }

    /**
     * 校验请求中的知识库公共配置是否与基准知识库一致。
     *
     * @param embeddingModel 请求中的向量模型配置
     * @param embeddingDim   请求中的向量维度
     * @param baseline       作为基准的知识库
     */
    private void validateKnowledgeBaseCommonConfig(AgentModelSlotConfig embeddingModel, Integer embeddingDim, KbBase baseline) {
        if (!Objects.equals(normalizeNullableText(baseline.getEmbeddingModel()), embeddingModel.getModelName())) {
            throw new ServiceException(ResponseCode.OPERATION_ERROR, KNOWLEDGE_BASE_CONFIG_MODEL_MISMATCH_MESSAGE);
        }
        if (!Objects.equals(baseline.getEmbeddingDim(), embeddingDim)) {
            throw new ServiceException(ResponseCode.OPERATION_ERROR, KNOWLEDGE_BASE_CONFIG_DIM_MISMATCH_MESSAGE);
        }
    }

    /**
     * 校验知识库向量维度是否属于支持集合。
     *
     * @param embeddingDim 向量维度
     */
    private void validateEmbeddingDim(Integer embeddingDim) {
        Assert.notNull(embeddingDim, "向量维度不能为空");
        Assert.isParamTrue(KnowledgeBaseEmbeddingDimSupport.isSupported(embeddingDim),
                KnowledgeBaseEmbeddingDimSupport.SUPPORTED_DIM_MESSAGE);
    }

    /**
     * 归一化知识库召回条数。
     * <p>
     * 当值为 null 或 0 时视为未配置；其余值会校验范围后原样返回。
     *
     * @param topK 原始召回条数
     * @return 归一化后的召回条数；未配置时返回 null
     */
    private Integer normalizeKnowledgeBaseTopK(Integer topK) {
        if (topK == null || topK == 0) {
            return null;
        }
        Assert.isParamTrue(topK >= KNOWLEDGE_BASE_TOP_K_MIN, KNOWLEDGE_BASE_TOP_K_MIN_MESSAGE);
        Assert.isParamTrue(topK <= KNOWLEDGE_BASE_TOP_K_MAX, KNOWLEDGE_BASE_TOP_K_MAX_MESSAGE);
        return topK;
    }

    /**
     * 校验管理端助手请求中的高级参数范围。
     *
     * @param request 管理端助手配置请求
     */
    private void validateAdminAssistantRequest(AdminAssistantAgentConfigRequest request) {
        validateSlotAdvancedParams(request.getRouteModel(), ADMIN_ASSISTANT_MAX_TOKENS_MIN,
                ADMIN_ASSISTANT_MAX_TOKENS_MAX, "路由模型");
        validateSlotAdvancedParams(request.getBusinessNodeSimpleModel(), ADMIN_ASSISTANT_MAX_TOKENS_MIN,
                ADMIN_ASSISTANT_MAX_TOKENS_MAX, "业务节点普通模型");
        validateSlotAdvancedParams(request.getBusinessNodeComplexModel(), ADMIN_ASSISTANT_MAX_TOKENS_MIN,
                ADMIN_ASSISTANT_MAX_TOKENS_MAX, "业务节点复杂模型");
        validateSlotAdvancedParams(request.getChatModel(), ADMIN_ASSISTANT_MAX_TOKENS_MIN,
                ADMIN_ASSISTANT_MAX_TOKENS_MAX, "聊天界面模型");
    }

    /**
     * 校验客户端助手请求中的高级参数范围。
     *
     * @param request 客户端助手配置请求
     */
    private void validateClientAssistantRequest(ClientAssistantAgentConfigRequest request) {
        validateSlotAdvancedParams(request.getRouteModel(), CLIENT_ASSISTANT_MAX_TOKENS_MIN,
                CLIENT_ASSISTANT_MAX_TOKENS_MAX, "客户端路由模型");
        validateSlotAdvancedParams(request.getChatModel(), CLIENT_ASSISTANT_MAX_TOKENS_MIN,
                CLIENT_ASSISTANT_MAX_TOKENS_MAX, "客户端聊天模型");
        validateSlotAdvancedParams(request.getOrderModel(), CLIENT_ASSISTANT_MAX_TOKENS_MIN,
                CLIENT_ASSISTANT_MAX_TOKENS_MAX, "客户端订单模型");
        validateSlotAdvancedParams(request.getProductModel(), CLIENT_ASSISTANT_MAX_TOKENS_MIN,
                CLIENT_ASSISTANT_MAX_TOKENS_MAX, "客户端商品模型");
        validateSlotAdvancedParams(request.getAfterSaleModel(), CLIENT_ASSISTANT_MAX_TOKENS_MIN,
                CLIENT_ASSISTANT_MAX_TOKENS_MAX, "客户端售后模型");
        validateSlotAdvancedParams(request.getConsultationComfortModel(), CLIENT_ASSISTANT_MAX_TOKENS_MIN,
                CLIENT_ASSISTANT_MAX_TOKENS_MAX, "问诊安抚模型");
        validateSlotAdvancedParams(request.getConsultationQuestionModel(), CLIENT_ASSISTANT_MAX_TOKENS_MIN,
                CLIENT_ASSISTANT_MAX_TOKENS_MAX, "问诊追问模型");
        validateSlotAdvancedParams(request.getConsultationFinalDiagnosisModel(), CLIENT_ASSISTANT_MAX_TOKENS_MIN,
                CLIENT_ASSISTANT_MAX_TOKENS_MAX, "问诊最终诊断模型");
    }

    /**
     * 校验图片识别请求中的高级参数范围。
     *
     * @param request 图片识别配置请求
     */
    private void validateImageRecognitionRequest(ImageRecognitionAgentConfigRequest request) {
        validateSlotAdvancedParams(request.getImageRecognitionModel(), IMAGE_RECOGNITION_MAX_TOKENS_MIN,
                IMAGE_RECOGNITION_MAX_TOKENS_MAX, "图片识别模型");
    }

    /**
     * 校验聊天历史总结请求中的高级参数范围。
     *
     * @param request 聊天历史总结配置请求
     */
    private void validateChatHistorySummaryRequest(ChatHistorySummaryAgentConfigRequest request) {
        validateSlotAdvancedParams(request.getChatHistorySummaryModel(), CHAT_HISTORY_SUMMARY_MAX_TOKENS_MIN,
                CHAT_HISTORY_SUMMARY_MAX_TOKENS_MAX, "聊天历史总结模型");
    }

    /**
     * 校验聊天标题生成请求中的高级参数范围。
     *
     * @param request 聊天标题生成配置请求
     */
    private void validateChatTitleRequest(ChatTitleAgentConfigRequest request) {
        validateSlotAdvancedParams(request.getChatTitleModel(), CHAT_TITLE_MAX_TOKENS_MIN,
                CHAT_TITLE_MAX_TOKENS_MAX, "聊天标题生成模型");
    }

    /**
     * 校验豆包语音请求字段与文本长度范围。
     *
     * @param request        豆包语音配置请求
     * @param existingConfig 现有豆包语音配置
     */
    private void validateSpeechRequest(SpeechAgentConfigRequest request, SpeechAgentConfig existingConfig) {
        Assert.notEmpty(normalizeNullableText(request.getAppId()), SPEECH_APP_ID_REQUIRED_MESSAGE);

        TextToSpeechConfigRequest textToSpeech = request.getTextToSpeech();
        Assert.notNull(textToSpeech, SPEECH_TTS_REQUIRED_MESSAGE);
        Assert.notEmpty(normalizeNullableText(textToSpeech.getVoiceType()), SPEECH_TTS_VOICE_TYPE_REQUIRED_MESSAGE);

        Integer maxTextChars = textToSpeech.getMaxTextChars();
        Assert.notNull(maxTextChars, SPEECH_TTS_MAX_TEXT_CHARS_REQUIRED_MESSAGE);
        Assert.isParamTrue(maxTextChars >= SPEECH_MAX_TEXT_CHARS_MIN, SPEECH_TTS_MAX_TEXT_CHARS_MIN_MESSAGE);
        Assert.isParamTrue(maxTextChars <= SPEECH_MAX_TEXT_CHARS_MAX, SPEECH_TTS_MAX_TEXT_CHARS_MAX_MESSAGE);

        resolveSpeechAccessToken(existingConfig, request.getAccessToken());
    }

    /**
     * 校验单个槽位的最大 token 与温度范围。
     *
     * @param request   模型槽位请求
     * @param minTokens 最大 token 最小值
     * @param maxTokens 最大 token 最大值
     * @param slotName  槽位名称
     */
    private void validateSlotAdvancedParams(AgentModelSelectionRequest request,
                                            int minTokens,
                                            int maxTokens,
                                            String slotName) {
        if (request == null) {
            return;
        }
        Integer maxTokensValue = request.getMaxTokens();
        if (maxTokensValue != null) {
            Assert.isParamTrue(maxTokensValue >= minTokens,
                    "%s最大token数不能小于%d".formatted(slotName, minTokens));
            Assert.isParamTrue(maxTokensValue <= maxTokens,
                    "%s最大token数不能大于%d".formatted(slotName, maxTokens));
        }

        Double temperature = request.getTemperature();
        if (temperature != null) {
            Assert.isParamTrue(temperature >= TEMPERATURE_MIN,
                    "%s温度不能小于0".formatted(slotName));
            Assert.isParamTrue(temperature <= TEMPERATURE_MAX,
                    "%s温度不能大于2".formatted(slotName));
        }
    }

    /**
     * 解析知识库配置的启用状态。
     * <p>
     * 优先使用显式 enabled 值；未显式设置时，根据是否存在任意知识库配置项推断是否视为启用。
     *
     * @param config 知识库运行时配置
     * @return 是否启用知识库能力
     */
    private boolean resolveKnowledgeBaseEnabled(KnowledgeBaseAgentConfig config) {
        if (config.getEnabled() != null) {
            return config.getEnabled();
        }
        return !copyKnowledgeNames(config.getKnowledgeNames()).isEmpty()
                || config.getEmbeddingDim() != null
                || config.getTopK() != null
                || StringUtils.hasText(config.getEmbeddingModel())
                || config.getRankingEnabled() != null
                || StringUtils.hasText(config.getRankingModel());
    }

    /**
     * 解析知识库排序能力的启用状态。
     * <p>
     * 优先使用显式 rankingEnabled 值；未显式设置时，根据是否配置了排序模型推断。
     *
     * @param config 知识库运行时配置
     * @return 是否启用排序能力
     */
    private boolean resolveKnowledgeBaseRankingEnabled(KnowledgeBaseAgentConfig config) {
        if (config.getRankingEnabled() != null) {
            return config.getRankingEnabled();
        }
        return StringUtils.hasText(config.getRankingModel());
    }

    /**
     * 复制知识库名称列表，避免外部修改内部集合。
     *
     * @param knowledgeNames 原始知识库名称列表
     * @return 不可变的知识库名称列表；为空时返回空列表
     */
    private List<String> copyKnowledgeNames(List<String> knowledgeNames) {
        if (knowledgeNames == null || knowledgeNames.isEmpty()) {
            return List.of();
        }
        return List.copyOf(knowledgeNames);
    }

    /**
     * 构建豆包语音运行时配置。
     *
     * @param request        豆包语音编辑态请求
     * @param existingConfig 现有豆包语音运行时配置
     * @return 豆包语音运行时配置
     */
    private SpeechAgentConfig buildSpeechConfig(SpeechAgentConfigRequest request, SpeechAgentConfig existingConfig) {
        SpeechAgentConfig config = new SpeechAgentConfig();
        config.setProvider(SPEECH_PROVIDER);
        config.setAppId(normalizeNullableText(request.getAppId()));
        config.setAccessToken(resolveSpeechAccessToken(existingConfig, request.getAccessToken()));
        config.setSpeechRecognition(buildSpeechRecognitionConfig());
        config.setTextToSpeech(buildTextToSpeechConfig(request.getTextToSpeech()));
        return config;
    }

    /**
     * 构建语音识别运行时配置。
     * <p>
     * STT ResourceId 固定写入 `volc.seedasr.sauc.duration`。
     *
     * @return 语音识别运行时配置
     */
    private SpeechRecognitionAgentConfig buildSpeechRecognitionConfig() {
        SpeechRecognitionAgentConfig config = new SpeechRecognitionAgentConfig();
        config.setResourceId(VOLCENGINE_STT_RESOURCE_ID);
        return config;
    }

    /**
     * 构建语音合成运行时配置。
     * <p>
     * TTS ResourceId 固定写入 `seed-tts-2.0`。
     *
     * @param request 语音合成编辑态请求
     * @return 语音合成运行时配置
     */
    private TextToSpeechAgentConfig buildTextToSpeechConfig(TextToSpeechConfigRequest request) {
        TextToSpeechAgentConfig config = new TextToSpeechAgentConfig();
        config.setResourceId(VOLCENGINE_TTS_RESOURCE_ID);
        config.setVoiceType(normalizeNullableText(request.getVoiceType()));
        config.setMaxTextChars(request.getMaxTextChars());
        return config;
    }

    /**
     * 解析本次应写入 Redis 的语音访问令牌。
     *
     * @param existingConfig 现有豆包语音配置
     * @param accessToken    本次请求中的访问令牌
     * @return 最终写入的访问令牌
     */
    private String resolveSpeechAccessToken(SpeechAgentConfig existingConfig, String accessToken) {
        String normalizedToken = normalizeNullableText(accessToken);
        if (normalizedToken != null) {
            return normalizedToken;
        }
        String existingToken = existingConfig == null ? null : normalizeNullableText(existingConfig.getAccessToken());
        Assert.notEmpty(existingToken, SPEECH_ACCESS_TOKEN_REQUIRED_MESSAGE);
        return existingToken;
    }

    /**
     * 查询当前启用提供商，不存在时返回 null。
     *
     * @return 启用提供商；不存在时返回 null
     */
    private LlmProvider getEnabledProviderOrNull() {
        List<LlmProvider> providers = llmProviderService.lambdaQuery()
                .eq(LlmProvider::getStatus, PROVIDER_STATUS_ENABLED)
                .orderByAsc(LlmProvider::getSort, LlmProvider::getId)
                .list();
        return providers.isEmpty() ? null : providers.getFirst();
    }

    /**
     * 查询当前启用提供商，不存在时抛出异常。
     *
     * @return 启用提供商
     */
    private LlmProvider getRequiredEnabledProvider() {
        LlmProvider provider = getEnabledProviderOrNull();
        if (provider == null) {
            throw new ServiceException(ResponseCode.OPERATION_ERROR, ENABLED_PROVIDER_MISSING_MESSAGE);
        }
        return provider;
    }

    /**
     * 判断请求中是否已经选择了模型。
     *
     * @param request 模型选择请求
     * @return true 表示已选择模型名称
     */
    private boolean hasSelectedModel(AgentModelSelectionRequest request) {
        return request != null && StringUtils.hasText(normalizeNullableText(request.getModelName()));
    }

    /**
     * 归一化可选模型名称。
     *
     * @param request 模型选择请求
     * @return 归一化后的模型名称；未选择时返回 null
     */
    private String normalizeOptionalModelName(AgentModelSelectionRequest request) {
        if (request == null) {
            return null;
        }
        return normalizeNullableText(request.getModelName());
    }

    /**
     * 查询当前启用提供商下的启用模型列表。
     *
     * @param providerId     提供商ID
     * @param modelType      模型类型
     * @param visionRequired 是否要求支持图片理解
     * @return 启用模型列表
     */
    private List<LlmProviderModel> listEnabledProviderModels(Long providerId, String modelType, boolean visionRequired) {
        LambdaQueryChainWrapper<LlmProviderModel> wrapper = llmProviderModelService.lambdaQuery()
                .eq(LlmProviderModel::getProviderId, providerId)
                .eq(LlmProviderModel::getModelType, modelType)
                .eq(LlmProviderModel::getEnabled, MODEL_STATUS_ENABLED)
                .orderByAsc(LlmProviderModel::getSort, LlmProviderModel::getId);
        if (visionRequired) {
            wrapper.eq(LlmProviderModel::getSupportVision, CAPABILITY_ENABLED);
        }
        return wrapper.list();
    }

    /**
     * 归一化并返回必填模型名称。
     *
     * @param request 模型选择请求
     * @return 模型名称
     */
    private String normalizeRequiredModelName(AgentModelSelectionRequest request) {
        String modelName = normalizeNullableText(request.getModelName());
        Assert.notEmpty(modelName, "模型名称不能为空");
        return modelName;
    }

    /**
     * 判断能力开关是否已启用。
     *
     * @param capability 能力值
     * @return 是否启用
     */
    private boolean isCapabilityEnabled(Integer capability) {
        return capability != null && capability == CAPABILITY_ENABLED;
    }

    /**
     * 获取当前操作人。
     *
     * @return 操作人账号
     */
    private String currentOperator() {
        try {
            String username = normalizeNullableText(getUsername());
            return username != null ? username : DEFAULT_OPERATOR;
        } catch (RuntimeException ex) {
            return DEFAULT_OPERATOR;
        }
    }

    /**
     * 归一化可空文本。
     *
     * @param value 原始文本
     * @return 去首尾空白后的文本；为空时返回 null
     */
    private String normalizeNullableText(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }
}
