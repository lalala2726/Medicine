package cn.zhangchuangla.medicine.admin.service;

import cn.zhangchuangla.medicine.admin.mapper.LlmProviderMapper;
import cn.zhangchuangla.medicine.admin.mapper.LlmProviderModelMapper;
import cn.zhangchuangla.medicine.admin.publisher.AgentConfigPublisher;
import cn.zhangchuangla.medicine.common.core.constants.RedisConstants;
import cn.zhangchuangla.medicine.common.core.enums.ResponseCode;
import cn.zhangchuangla.medicine.common.core.exception.ServiceException;
import cn.zhangchuangla.medicine.common.redis.core.RedisCache;
import cn.zhangchuangla.medicine.model.cache.*;
import cn.zhangchuangla.medicine.model.constants.LlmModelTypeConstants;
import cn.zhangchuangla.medicine.model.constants.LlmProviderTypeConstants;
import cn.zhangchuangla.medicine.model.entity.LlmProvider;
import cn.zhangchuangla.medicine.model.entity.LlmProviderModel;
import cn.zhangchuangla.medicine.model.mq.AgentConfigRefreshMessage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Agent 运行时 Redis 与 MQ 联动同步服务。
 */
@Service
@RequiredArgsConstructor
public class AgentConfigRuntimeSyncService {

    private static final String REDIS_KEY = RedisConstants.AgentConfig.ALL_CONFIG_KEY;
    private static final int PROVIDER_STATUS_ENABLED = 1;
    private static final int MODEL_STATUS_ENABLED = 0;
    private static final int CAPABILITY_ENABLED = 1;
    private static final String DEFAULT_OPERATOR = "system";
    private static final String AGENT_CONFIG_REFRESH_MESSAGE_TYPE = "agent_config_refresh";
    private static final String PROVIDER_TYPE_MISSING_MESSAGE = "当前启用的模型提供商未配置类型，请先在模型提供商中补充类型";
    private static final String PROVIDER_DISABLE_MESSAGE = "当前启用的提供商不允许停用，请先切换到其他提供商";
    private static final String PROVIDER_DELETE_MESSAGE = "当前启用的提供商不允许删除，请先切换到其他提供商";
    private static final String PROVIDER_SWITCH_MODEL_MISSING_MESSAGE = "切换失败，目标提供商下不存在模型：%s";
    private static final String KNOWLEDGE_BASE_DISABLE_MESSAGE = "当前知识库已被知识库Agent配置引用，请先移除后再停用";
    private static final String KNOWLEDGE_BASE_DELETE_MESSAGE = "当前知识库已被知识库Agent配置引用，请先移除后再删除";
    private static final String MODEL_DISABLED_MESSAGE = "模型未启用：%s";
    private static final String REASONING_UNSUPPORTED_MESSAGE = "模型不支持深度思考：%s";
    private static final String VISION_UNSUPPORTED_MESSAGE = "模型不支持图片理解：%s";

    private static final List<KnowledgeBaseModelBinding> KNOWLEDGE_BASE_MODEL_BINDINGS = List.of(
            new KnowledgeBaseModelBinding(
                    LlmModelTypeConstants.EMBEDDING,
                    KnowledgeBaseAgentConfig::getEmbeddingModel,
                    KnowledgeBaseAgentConfig::setEmbeddingModel
            ),
            new KnowledgeBaseModelBinding(
                    LlmModelTypeConstants.CHAT,
                    KnowledgeBaseAgentConfig::getRankingModel,
                    AgentConfigRuntimeSyncService::setKnowledgeBaseRankingModel
            )
    );

    private static final List<SlotBinding> SLOT_BINDINGS = List.of(
            new SlotBinding(
                    LlmModelTypeConstants.CHAT,
                    false,
                    cache -> cache.getAdminAssistant() == null ? null : cache.getAdminAssistant().getRouteModel(),
                    (cache, slot) -> ensureAdminAssistant(cache).setRouteModel(slot)
            ),
            new SlotBinding(
                    LlmModelTypeConstants.CHAT,
                    false,
                    cache -> cache.getAdminAssistant() == null ? null : cache.getAdminAssistant().getBusinessNodeSimpleModel(),
                    (cache, slot) -> ensureAdminAssistant(cache).setBusinessNodeSimpleModel(slot)
            ),
            new SlotBinding(
                    LlmModelTypeConstants.CHAT,
                    false,
                    cache -> cache.getAdminAssistant() == null ? null : cache.getAdminAssistant().getBusinessNodeComplexModel(),
                    (cache, slot) -> ensureAdminAssistant(cache).setBusinessNodeComplexModel(slot)
            ),
            new SlotBinding(
                    LlmModelTypeConstants.CHAT,
                    false,
                    cache -> cache.getAdminAssistant() == null ? null : cache.getAdminAssistant().getChatModel(),
                    (cache, slot) -> ensureAdminAssistant(cache).setChatModel(slot)
            ),
            new SlotBinding(
                    LlmModelTypeConstants.CHAT,
                    false,
                    cache -> cache.getClientAssistant() == null ? null : cache.getClientAssistant().getRouteModel(),
                    (cache, slot) -> ensureClientAssistant(cache).setRouteModel(slot)
            ),
            new SlotBinding(
                    LlmModelTypeConstants.CHAT,
                    false,
                    cache -> cache.getClientAssistant() == null ? null : cache.getClientAssistant().getChatModel(),
                    (cache, slot) -> ensureClientAssistant(cache).setChatModel(slot)
            ),
            new SlotBinding(
                    LlmModelTypeConstants.CHAT,
                    false,
                    cache -> cache.getClientAssistant() == null ? null : cache.getClientAssistant().getOrderModel(),
                    (cache, slot) -> ensureClientAssistant(cache).setOrderModel(slot)
            ),
            new SlotBinding(
                    LlmModelTypeConstants.CHAT,
                    false,
                    cache -> cache.getClientAssistant() == null ? null : cache.getClientAssistant().getProductModel(),
                    (cache, slot) -> ensureClientAssistant(cache).setProductModel(slot)
            ),
            new SlotBinding(
                    LlmModelTypeConstants.CHAT,
                    false,
                    cache -> cache.getClientAssistant() == null ? null : cache.getClientAssistant().getAfterSaleModel(),
                    (cache, slot) -> ensureClientAssistant(cache).setAfterSaleModel(slot)
            ),
            new SlotBinding(
                    LlmModelTypeConstants.CHAT,
                    false,
                    cache -> cache.getClientAssistant() == null ? null : cache.getClientAssistant().getConsultationComfortModel(),
                    (cache, slot) -> ensureClientAssistant(cache).setConsultationComfortModel(slot)
            ),
            new SlotBinding(
                    LlmModelTypeConstants.CHAT,
                    false,
                    cache -> cache.getClientAssistant() == null ? null : cache.getClientAssistant().getConsultationQuestionModel(),
                    (cache, slot) -> ensureClientAssistant(cache).setConsultationQuestionModel(slot)
            ),
            new SlotBinding(
                    LlmModelTypeConstants.CHAT,
                    false,
                    cache -> cache.getClientAssistant() == null ? null : cache.getClientAssistant().getConsultationFinalDiagnosisModel(),
                    (cache, slot) -> ensureClientAssistant(cache).setConsultationFinalDiagnosisModel(slot)
            ),
            new SlotBinding(
                    LlmModelTypeConstants.CHAT,
                    true,
                    cache -> cache.getImageRecognition() == null ? null : cache.getImageRecognition().getImageRecognitionModel(),
                    (cache, slot) -> ensureImageRecognition(cache).setImageRecognitionModel(slot)
            ),
            new SlotBinding(
                    LlmModelTypeConstants.CHAT,
                    false,
                    cache -> cache.getChatHistorySummary() == null ? null : cache.getChatHistorySummary().getChatHistorySummaryModel(),
                    (cache, slot) -> ensureChatHistorySummary(cache).setChatHistorySummaryModel(slot)
            ),
            new SlotBinding(
                    LlmModelTypeConstants.CHAT,
                    false,
                    cache -> cache.getChatTitle() == null ? null : cache.getChatTitle().getChatTitleModel(),
                    (cache, slot) -> ensureChatTitle(cache).setChatTitleModel(slot)
            )
    );

    private final RedisCache redisCache;
    private final AgentConfigPublisher agentConfigPublisher;
    private final LlmProviderMapper llmProviderMapper;
    private final LlmProviderModelMapper llmProviderModelMapper;

    private static KnowledgeBaseAgentConfig ensureKnowledgeBase(AgentAllConfigCache cache) {
        KnowledgeBaseAgentConfig config = cache.getKnowledgeBase();
        if (config == null) {
            config = new KnowledgeBaseAgentConfig();
            cache.setKnowledgeBase(config);
        }
        return config;
    }

    /**
     * 设置知识库配置的排序模型。
     * 如果模型名称为空或 null，此方法还会将 rankingEnabled 设置为 false。
     *
     * @param config    知识库 Agent 配置
     * @param modelName 要设置的排序模型名称，若为 null 则清除它
     */
    private static void setKnowledgeBaseRankingModel(KnowledgeBaseAgentConfig config, String modelName) {
        config.setRankingModel(modelName);
        if (!StringUtils.hasText(modelName)) {
            config.setRankingEnabled(false);
        }
    }

    private static AdminAssistantAgentConfig ensureAdminAssistant(AgentAllConfigCache cache) {
        AdminAssistantAgentConfig config = cache.getAdminAssistant();
        if (config == null) {
            config = new AdminAssistantAgentConfig();
            cache.setAdminAssistant(config);
        }
        return config;
    }

    /**
     * 确保缓存中存在客户端助手配置节点。
     *
     * @param cache Agent 全量缓存
     * @return 客户端助手配置节点
     */
    private static ClientAssistantAgentConfig ensureClientAssistant(AgentAllConfigCache cache) {
        ClientAssistantAgentConfig config = cache.getClientAssistant();
        if (config == null) {
            config = new ClientAssistantAgentConfig();
            cache.setClientAssistant(config);
        }
        return config;
    }

    private static ImageRecognitionAgentConfig ensureImageRecognition(AgentAllConfigCache cache) {
        ImageRecognitionAgentConfig config = cache.getImageRecognition();
        if (config == null) {
            config = new ImageRecognitionAgentConfig();
            cache.setImageRecognition(config);
        }
        return config;
    }

    private static ChatHistorySummaryAgentConfig ensureChatHistorySummary(AgentAllConfigCache cache) {
        ChatHistorySummaryAgentConfig config = cache.getChatHistorySummary();
        if (config == null) {
            config = new ChatHistorySummaryAgentConfig();
            cache.setChatHistorySummary(config);
        }
        return config;
    }

    private static ChatTitleAgentConfig ensureChatTitle(AgentAllConfigCache cache) {
        ChatTitleAgentConfig config = cache.getChatTitle();
        if (config == null) {
            config = new ChatTitleAgentConfig();
            cache.setChatTitle(config);
        }
        return config;
    }

    /**
     * 读取并规范化 Agent 缓存。
     *
     * @return Agent 缓存
     */
    public AgentAllConfigCache readCache() {
        AgentAllConfigCache cache = redisCache.getCacheObject(REDIS_KEY);
        return normalizeCache(cache == null ? new AgentAllConfigCache() : cache);
    }

    /**
     * 保存 Agent 缓存并广播刷新消息。
     *
     * @param cache           最新缓存
     * @param enabledProvider 当前启用提供商
     * @param operator        操作人
     */
    public void saveCache(AgentAllConfigCache cache, LlmProvider enabledProvider, String operator) {
        AgentAllConfigCache normalizedCache = normalizeCache(cache == null ? new AgentAllConfigCache() : cache);
        normalizedCache.setLlm(buildLlmConfig(enabledProvider));
        normalizedCache.setUpdatedAt(OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        normalizedCache.setUpdatedBy(normalizeOperator(operator));
        redisCache.setCacheObject(REDIS_KEY, normalizedCache);
        agentConfigPublisher.publishRefresh(buildRefreshMessage(normalizedCache));
    }

    /**
     * 当前启用提供商信息变更后刷新 Redis 的 llm 节点。
     *
     * @param provider 当前启用提供商
     * @param operator 操作人
     */
    public void syncEnabledProviderChange(LlmProvider provider, String operator) {
        if (!isProviderEnabled(provider)) {
            return;
        }
        saveCache(readCache(), provider, operator);
    }

    /**
     * 基于数据库中当前启用 provider 刷新 Redis llm 节点。
     *
     * @param operator 操作人
     */
    public void syncCurrentEnabledProviderSnapshot(String operator) {
        saveCache(readCache(), getEnabledProviderOrNull(), operator);
    }

    /**
     * 切换启用 provider 后清空业务 Agent 模型配置，但保留独立的语音配置。
     *
     * @param operator 操作人
     */
    public void clearAgentConfigsForProviderSwitch(String operator) {
        AgentAllConfigCache cache = readCache();
        cache.setAgentConfigs(new AgentConfigsCache());
        saveCache(cache, getEnabledProviderOrNull(), operator);
    }

    /**
     * 当前启用 provider 的模型快照发生整体变化后刷新缓存。
     *
     * @param provider 当前 provider
     * @param operator 操作人
     */
    public void syncActiveProviderSnapshot(LlmProvider provider, String operator) {
        if (!isProviderEnabled(provider)) {
            return;
        }
        AgentAllConfigCache cache = readCache();
        reconcileSlotsWithModels(cache, listProviderModels(provider.getId()));
        saveCache(cache, provider, operator);
    }

    /**
     * 启用 provider 前校验当前运行配置是否与目标 provider 兼容。
     *
     * @param provider 目标 provider
     */
    public void validateProviderSwitchCompatibility(LlmProvider provider) {
        if (provider == null) {
            return;
        }
        AgentAllConfigCache cache = readCache();
        List<LlmProviderModel> models = listProviderModels(provider.getId());
        validateKnowledgeBaseModelCompatibility(cache.getKnowledgeBase(), models);
        for (SlotBinding binding : SLOT_BINDINGS) {
            AgentModelSlotConfig slot = binding.getter().apply(cache);
            if (!hasSelectedModel(slot)) {
                continue;
            }
            LlmProviderModel model = findMatchingModel(models, binding, slot.getModelName());
            if (model == null) {
                throw new ServiceException(ResponseCode.OPERATION_ERROR,
                        PROVIDER_SWITCH_MODEL_MISSING_MESSAGE.formatted(slot.getModelName()));
            }
            validateModelUsableForSlot(slot, binding, model);
        }
    }

    /**
     * 校验当前启用 provider 是否允许停用。
     *
     * @param provider provider
     */
    public void assertProviderCanDisable(LlmProvider provider) {
        if (isProviderEnabled(provider)) {
            throw new ServiceException(ResponseCode.OPERATION_ERROR, PROVIDER_DISABLE_MESSAGE);
        }
    }

    /**
     * 校验当前启用 provider 是否允许删除。
     *
     * @param provider provider
     */
    public void assertProviderCanDelete(LlmProvider provider) {
        if (isProviderEnabled(provider)) {
            throw new ServiceException(ResponseCode.OPERATION_ERROR, PROVIDER_DELETE_MESSAGE);
        }
    }

    /**
     * 校验知识库是否允许停用。
     *
     * @param knowledgeName 知识库业务名称
     */
    public void assertKnowledgeBaseCanDisable(String knowledgeName) {
        if (isKnowledgeBaseReferenced(knowledgeName)) {
            throw new ServiceException(ResponseCode.OPERATION_ERROR, KNOWLEDGE_BASE_DISABLE_MESSAGE);
        }
    }

    /**
     * 校验知识库是否允许删除。
     *
     * @param knowledgeName 知识库业务名称
     */
    public void assertKnowledgeBaseCanDelete(String knowledgeName) {
        if (isKnowledgeBaseReferenced(knowledgeName)) {
            throw new ServiceException(ResponseCode.OPERATION_ERROR, KNOWLEDGE_BASE_DELETE_MESSAGE);
        }
    }

    /**
     * 单个模型更新后同步 Redis 中引用该模型的槽位。
     *
     * @param existing 更新前模型
     * @param updated  更新后模型
     * @param operator 操作人
     */
    public void syncAfterModelUpdate(LlmProviderModel existing, LlmProviderModel updated, String operator) {
        LlmProvider enabledProvider = getEnabledProviderOrNull();
        if (enabledProvider == null || existing == null || !Objects.equals(existing.getProviderId(), enabledProvider.getId())) {
            return;
        }

        AgentAllConfigCache cache = readCache();
        boolean changed = syncKnowledgeBaseModelUpdate(cache.getKnowledgeBase(), existing, updated);
        for (SlotBinding binding : SLOT_BINDINGS) {
            AgentModelSlotConfig slot = binding.getter().apply(cache);
            if (!matchesSlot(slot, binding, existing.getModelName(), existing.getModelType())) {
                continue;
            }
            if (shouldClearSlot(slot, binding, existing, updated)) {
                binding.setter().accept(cache, null);
                changed = true;
                continue;
            }
            if (!Objects.equals(slot.getModelName(), updated.getModelName())) {
                slot.setModelName(updated.getModelName());
                binding.setter().accept(cache, slot);
                changed = true;
            }
        }
        if (changed) {
            saveCache(cache, enabledProvider, operator);
        }
    }

    /**
     * 单个模型删除后清理 Redis 中引用该模型的槽位。
     *
     * @param existing 删除前模型
     * @param operator 操作人
     */
    public void syncAfterModelDelete(LlmProviderModel existing, String operator) {
        LlmProvider enabledProvider = getEnabledProviderOrNull();
        if (enabledProvider == null || existing == null || !Objects.equals(existing.getProviderId(), enabledProvider.getId())) {
            return;
        }
        AgentAllConfigCache cache = readCache();
        boolean changed = clearKnowledgeBaseModelReferences(cache.getKnowledgeBase(),
                existing.getModelName(), existing.getModelType());
        changed |= clearSlotsReferencing(cache, existing.getModelName(), existing.getModelType());
        if (changed) {
            saveCache(cache, enabledProvider, operator);
        }
    }

    private AgentAllConfigCache normalizeCache(AgentAllConfigCache cache) {
        cache.setSchemaVersion(AgentAllConfigCache.CURRENT_SCHEMA_VERSION);
        if (cache.getAgentConfigs() == null) {
            cache.setAgentConfigs(new AgentConfigsCache());
        }
        return cache;
    }

    private AgentLlmConfig buildLlmConfig(LlmProvider provider) {
        if (provider == null) {
            return null;
        }
        String providerType = normalizeNullableText(provider.getProviderType());
        if (!StringUtils.hasText(providerType) || !LlmProviderTypeConstants.ALL.contains(providerType)) {
            throw new ServiceException(ResponseCode.OPERATION_ERROR, PROVIDER_TYPE_MISSING_MESSAGE);
        }
        AgentLlmConfig config = new AgentLlmConfig();
        config.setProviderType(providerType);
        config.setBaseUrl(normalizeNullableText(provider.getBaseUrl()));
        config.setApiKey(normalizeNullableText(provider.getApiKey()));
        return config;
    }

    private AgentConfigRefreshMessage buildRefreshMessage(AgentAllConfigCache cache) {
        return AgentConfigRefreshMessage.builder()
                .message_type(AGENT_CONFIG_REFRESH_MESSAGE_TYPE)
                .redis_key(REDIS_KEY)
                .updated_at(cache.getUpdatedAt())
                .updated_by(cache.getUpdatedBy())
                .created_at(OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
                .build();
    }

    private void reconcileSlotsWithModels(AgentAllConfigCache cache, List<LlmProviderModel> models) {
        reconcileKnowledgeBaseModelReferences(cache.getKnowledgeBase(), models);
        for (SlotBinding binding : SLOT_BINDINGS) {
            AgentModelSlotConfig slot = binding.getter().apply(cache);
            if (!hasSelectedModel(slot)) {
                continue;
            }
            LlmProviderModel model = findMatchingModel(models, binding, slot.getModelName());
            if (!isModelUsableForSlot(slot, binding, model)) {
                binding.setter().accept(cache, null);
            }
        }
    }

    private boolean clearSlotsReferencing(AgentAllConfigCache cache, String modelName, String modelType) {
        boolean changed = false;
        for (SlotBinding binding : SLOT_BINDINGS) {
            AgentModelSlotConfig slot = binding.getter().apply(cache);
            if (!matchesSlot(slot, binding, modelName, modelType)) {
                continue;
            }
            binding.setter().accept(cache, null);
            changed = true;
        }
        return changed;
    }

    private void validateKnowledgeBaseModelCompatibility(KnowledgeBaseAgentConfig knowledgeBase,
                                                         List<LlmProviderModel> models) {
        if (knowledgeBase == null) {
            return;
        }
        for (KnowledgeBaseModelBinding binding : KNOWLEDGE_BASE_MODEL_BINDINGS) {
            String modelName = binding.getter().apply(knowledgeBase);
            if (!StringUtils.hasText(modelName)) {
                continue;
            }
            LlmProviderModel model = findMatchingModel(models, binding.modelType(), modelName);
            if (model == null) {
                throw new ServiceException(ResponseCode.OPERATION_ERROR,
                        PROVIDER_SWITCH_MODEL_MISSING_MESSAGE.formatted(modelName));
            }
            if (!isModelEnabled(model)) {
                throw new ServiceException(ResponseCode.OPERATION_ERROR,
                        MODEL_DISABLED_MESSAGE.formatted(model.getModelName()));
            }
        }
    }

    private void reconcileKnowledgeBaseModelReferences(KnowledgeBaseAgentConfig knowledgeBase,
                                                       List<LlmProviderModel> models) {
        if (knowledgeBase == null) {
            return;
        }
        for (KnowledgeBaseModelBinding binding : KNOWLEDGE_BASE_MODEL_BINDINGS) {
            String modelName = binding.getter().apply(knowledgeBase);
            if (!StringUtils.hasText(modelName)) {
                continue;
            }
            LlmProviderModel model = findMatchingModel(models, binding.modelType(), modelName);
            if (model == null || !isModelEnabled(model)) {
                binding.setter().accept(knowledgeBase, null);
            }
        }
    }

    private boolean syncKnowledgeBaseModelUpdate(KnowledgeBaseAgentConfig knowledgeBase,
                                                 LlmProviderModel existing,
                                                 LlmProviderModel updated) {
        if (knowledgeBase == null) {
            return false;
        }
        boolean changed = false;
        for (KnowledgeBaseModelBinding binding : KNOWLEDGE_BASE_MODEL_BINDINGS) {
            String modelName = binding.getter().apply(knowledgeBase);
            if (!matchesKnowledgeBaseModel(binding, modelName, existing.getModelName(), existing.getModelType())) {
                continue;
            }
            if (shouldClearKnowledgeBaseModel(existing, updated)) {
                binding.setter().accept(knowledgeBase, null);
                changed = true;
                continue;
            }
            if (!Objects.equals(modelName, updated.getModelName())) {
                binding.setter().accept(knowledgeBase, updated.getModelName());
                changed = true;
            }
        }
        return changed;
    }

    private boolean clearKnowledgeBaseModelReferences(KnowledgeBaseAgentConfig knowledgeBase,
                                                      String modelName,
                                                      String modelType) {
        if (knowledgeBase == null) {
            return false;
        }
        boolean changed = false;
        for (KnowledgeBaseModelBinding binding : KNOWLEDGE_BASE_MODEL_BINDINGS) {
            String selectedModelName = binding.getter().apply(knowledgeBase);
            if (!matchesKnowledgeBaseModel(binding, selectedModelName, modelName, modelType)) {
                continue;
            }
            binding.setter().accept(knowledgeBase, null);
            changed = true;
        }
        return changed;
    }

    private boolean shouldClearSlot(AgentModelSlotConfig slot,
                                    SlotBinding binding,
                                    LlmProviderModel existing,
                                    LlmProviderModel updated) {
        if (updated == null) {
            return true;
        }
        if (!Objects.equals(updated.getProviderId(), existing.getProviderId())) {
            return true;
        }
        if (!Objects.equals(updated.getModelType(), existing.getModelType())) {
            return true;
        }
        return !isModelUsableForSlot(slot, binding, updated);
    }

    private boolean shouldClearKnowledgeBaseModel(LlmProviderModel existing, LlmProviderModel updated) {
        if (updated == null) {
            return true;
        }
        if (!Objects.equals(updated.getProviderId(), existing.getProviderId())) {
            return true;
        }
        if (!Objects.equals(updated.getModelType(), existing.getModelType())) {
            return true;
        }
        return !isModelEnabled(updated);
    }

    private boolean isModelUsableForSlot(AgentModelSlotConfig slot, SlotBinding binding, LlmProviderModel model) {
        if (slot == null || model == null || !isModelEnabled(model)) {
            return false;
        }
        if (Boolean.TRUE.equals(slot.getReasoningEnabled()) && !supportsReasoning(model)) {
            return false;
        }
        return !binding.visionRequired() || supportsVision(model);
    }

    private void validateModelUsableForSlot(AgentModelSlotConfig slot, SlotBinding binding, LlmProviderModel model) {
        if (!isModelEnabled(model)) {
            throw new ServiceException(ResponseCode.OPERATION_ERROR,
                    MODEL_DISABLED_MESSAGE.formatted(model.getModelName()));
        }
        if (Boolean.TRUE.equals(slot.getReasoningEnabled()) && !supportsReasoning(model)) {
            throw new ServiceException(ResponseCode.OPERATION_ERROR,
                    REASONING_UNSUPPORTED_MESSAGE.formatted(model.getModelName()));
        }
        if (binding.visionRequired() && !supportsVision(model)) {
            throw new ServiceException(ResponseCode.OPERATION_ERROR,
                    VISION_UNSUPPORTED_MESSAGE.formatted(model.getModelName()));
        }
    }

    private LlmProvider getEnabledProviderOrNull() {
        List<LlmProvider> providers = llmProviderMapper.selectList(Wrappers.<LlmProvider>lambdaQuery()
                .eq(LlmProvider::getStatus, PROVIDER_STATUS_ENABLED)
                .orderByAsc(LlmProvider::getSort, LlmProvider::getId));
        return providers.isEmpty() ? null : providers.getFirst();
    }

    private List<LlmProviderModel> listProviderModels(Long providerId) {
        return llmProviderModelMapper.selectList(Wrappers.<LlmProviderModel>lambdaQuery()
                .eq(LlmProviderModel::getProviderId, providerId)
                .orderByAsc(LlmProviderModel::getSort, LlmProviderModel::getId));
    }

    private LlmProviderModel findMatchingModel(List<LlmProviderModel> models, SlotBinding binding, String modelName) {
        if (!StringUtils.hasText(modelName)) {
            return null;
        }
        return models.stream()
                .filter(model -> Objects.equals(binding.modelType(), model.getModelType()))
                .filter(model -> Objects.equals(modelName, model.getModelName()))
                .findFirst()
                .orElse(null);
    }

    private LlmProviderModel findMatchingModel(List<LlmProviderModel> models, String modelType, String modelName) {
        if (!StringUtils.hasText(modelName)) {
            return null;
        }
        return models.stream()
                .filter(model -> Objects.equals(modelType, model.getModelType()))
                .filter(model -> Objects.equals(modelName, model.getModelName()))
                .findFirst()
                .orElse(null);
    }

    private boolean matchesSlot(AgentModelSlotConfig slot, SlotBinding binding, String modelName, String modelType) {
        return hasSelectedModel(slot)
                && Objects.equals(binding.modelType(), modelType)
                && Objects.equals(slot.getModelName(), modelName);
    }

    private boolean matchesKnowledgeBaseModel(KnowledgeBaseModelBinding binding,
                                              String selectedModelName,
                                              String modelName,
                                              String modelType) {
        return StringUtils.hasText(selectedModelName)
                && Objects.equals(binding.modelType(), modelType)
                && Objects.equals(selectedModelName, modelName);
    }

    private boolean hasSelectedModel(AgentModelSlotConfig slot) {
        return slot != null && StringUtils.hasText(slot.getModelName());
    }

    private boolean isKnowledgeBaseReferenced(String knowledgeName) {
        if (!StringUtils.hasText(knowledgeName)) {
            return false;
        }
        AgentAllConfigCache cache = readCache();
        KnowledgeBaseAgentConfig knowledgeBase = cache.getKnowledgeBase();
        return isKnowledgeBaseEnabled(knowledgeBase)
                && knowledgeBase.getKnowledgeNames() != null
                && knowledgeBase.getKnowledgeNames().stream()
                .filter(StringUtils::hasText)
                .anyMatch(knowledgeName::equals);
    }

    private boolean isKnowledgeBaseEnabled(KnowledgeBaseAgentConfig knowledgeBase) {
        if (knowledgeBase == null) {
            return false;
        }
        if (knowledgeBase.getEnabled() != null) {
            return knowledgeBase.getEnabled();
        }
        return knowledgeBase.getKnowledgeNames() != null && !knowledgeBase.getKnowledgeNames().isEmpty();
    }

    private boolean isProviderEnabled(LlmProvider provider) {
        return provider != null && provider.getStatus() != null && provider.getStatus() == PROVIDER_STATUS_ENABLED;
    }

    private boolean isModelEnabled(LlmProviderModel model) {
        return model.getEnabled() != null && model.getEnabled() == MODEL_STATUS_ENABLED;
    }

    private boolean supportsReasoning(LlmProviderModel model) {
        return model.getSupportReasoning() != null && model.getSupportReasoning() == CAPABILITY_ENABLED;
    }

    private boolean supportsVision(LlmProviderModel model) {
        return model.getSupportVision() != null && model.getSupportVision() == CAPABILITY_ENABLED;
    }

    private String normalizeOperator(String operator) {
        String normalized = normalizeNullableText(operator);
        return normalized != null ? normalized : DEFAULT_OPERATOR;
    }

    private String normalizeNullableText(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }

    private record SlotBinding(String modelType,
                               boolean visionRequired,
                               Function<AgentAllConfigCache, AgentModelSlotConfig> getter,
                               BiConsumer<AgentAllConfigCache, AgentModelSlotConfig> setter) {
    }

    private record KnowledgeBaseModelBinding(String modelType,
                                             Function<KnowledgeBaseAgentConfig, String> getter,
                                             BiConsumer<KnowledgeBaseAgentConfig, String> setter) {
    }
}
