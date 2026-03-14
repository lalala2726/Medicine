package cn.zhangchuangla.medicine.admin.service;

import cn.zhangchuangla.medicine.admin.mapper.LlmProviderMapper;
import cn.zhangchuangla.medicine.admin.mapper.LlmProviderModelMapper;
import cn.zhangchuangla.medicine.admin.publisher.AgentConfigPublisher;
import cn.zhangchuangla.medicine.common.core.constants.RedisConstants;
import cn.zhangchuangla.medicine.common.core.exception.ServiceException;
import cn.zhangchuangla.medicine.common.redis.core.RedisCache;
import cn.zhangchuangla.medicine.model.cache.*;
import cn.zhangchuangla.medicine.model.constants.LlmModelTypeConstants;
import cn.zhangchuangla.medicine.model.entity.LlmProvider;
import cn.zhangchuangla.medicine.model.entity.LlmProviderModel;
import cn.zhangchuangla.medicine.model.mq.AgentConfigRefreshMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AgentConfigRuntimeSyncServiceTests {

    @Mock
    private RedisTemplate<Object, Object> redisTemplate;

    @Mock
    @SuppressWarnings("rawtypes")
    private ValueOperations valueOperations;

    @Mock
    private AgentConfigPublisher agentConfigPublisher;

    @Mock
    private LlmProviderMapper llmProviderMapper;

    @Mock
    private LlmProviderModelMapper llmProviderModelMapper;

    private AgentConfigRuntimeSyncService syncService;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        syncService = new AgentConfigRuntimeSyncService(
                new RedisCache(redisTemplate),
                agentConfigPublisher,
                llmProviderMapper,
                llmProviderModelMapper
        );
    }

    @Test
    void syncEnabledProviderChange_ShouldWriteLlmNodeAndPublish() {
        AgentAllConfigCache cache = new AgentAllConfigCache();
        cache.setSpeech(buildSpeech());
        when(valueOperations.get(RedisConstants.AgentConfig.ALL_CONFIG_KEY)).thenReturn(cache);

        syncService.syncEnabledProviderChange(buildEnabledProvider(), "admin");

        ArgumentCaptor<AgentAllConfigCache> cacheCaptor = ArgumentCaptor.forClass(AgentAllConfigCache.class);
        verify(valueOperations).set(eq(RedisConstants.AgentConfig.ALL_CONFIG_KEY), cacheCaptor.capture());
        AgentAllConfigCache saved = cacheCaptor.getValue();
        assertEquals(3, saved.getSchemaVersion());
        assertEquals("openai", saved.getLlm().getProviderType());
        assertEquals("https://api.openai.com/v1", saved.getLlm().getBaseUrl());
        assertEquals("sk-openai", saved.getLlm().getApiKey());
        assertNotNull(saved.getSpeech());
        verify(agentConfigPublisher).publishRefresh(any(AgentConfigRefreshMessage.class));
    }

    @Test
    void validateProviderSwitchCompatibility_WhenTargetModelMissing_ShouldThrowServiceException() {
        AgentAllConfigCache cache = new AgentAllConfigCache();
        KnowledgeBaseAgentConfig knowledgeBase = new KnowledgeBaseAgentConfig();
        knowledgeBase.setEmbeddingModel("text-embedding-3-large");
        cache.setKnowledgeBase(knowledgeBase);
        when(valueOperations.get(RedisConstants.AgentConfig.ALL_CONFIG_KEY)).thenReturn(cache);
        when(llmProviderModelMapper.selectList(any())).thenReturn(List.of());

        ServiceException exception = assertThrows(ServiceException.class,
                () -> syncService.validateProviderSwitchCompatibility(buildTargetProvider()));

        assertEquals("切换失败，目标提供商下不存在模型：text-embedding-3-large", exception.getMessage());
    }

    @Test
    void syncAfterModelUpdate_ShouldRenameKnowledgeBaseReferencedModelAndPublish() {
        AgentAllConfigCache cache = new AgentAllConfigCache();
        KnowledgeBaseAgentConfig knowledgeBase = new KnowledgeBaseAgentConfig();
        knowledgeBase.setEmbeddingModel("text-embedding-3-large");
        cache.setKnowledgeBase(knowledgeBase);
        when(valueOperations.get(RedisConstants.AgentConfig.ALL_CONFIG_KEY)).thenReturn(cache);
        when(llmProviderMapper.selectList(any())).thenReturn(List.of(buildEnabledProvider()));

        LlmProviderModel existing = buildModel(1L, "text-embedding-3-large", LlmModelTypeConstants.EMBEDDING,
                0, 0, 0);
        LlmProviderModel updated = buildModel(1L, "text-embedding-3-large-v2", LlmModelTypeConstants.EMBEDDING,
                0, 0, 0);

        syncService.syncAfterModelUpdate(existing, updated, "tester");

        ArgumentCaptor<AgentAllConfigCache> cacheCaptor = ArgumentCaptor.forClass(AgentAllConfigCache.class);
        verify(valueOperations).set(eq(RedisConstants.AgentConfig.ALL_CONFIG_KEY), cacheCaptor.capture());
        assertEquals("text-embedding-3-large-v2", cacheCaptor.getValue().getKnowledgeBase().getEmbeddingModel());
        verify(agentConfigPublisher).publishRefresh(any(AgentConfigRefreshMessage.class));
    }

    @Test
    void syncAfterModelUpdate_ShouldRenameReferencedSlotAndPublish() {
        AgentAllConfigCache cache = new AgentAllConfigCache();
        ChatHistorySummaryAgentConfig chatHistorySummary = new ChatHistorySummaryAgentConfig();
        chatHistorySummary.setChatHistorySummaryModel(buildSlot("gpt-4.1-mini", false, 4096, 0.3));
        cache.setChatHistorySummary(chatHistorySummary);
        when(valueOperations.get(RedisConstants.AgentConfig.ALL_CONFIG_KEY)).thenReturn(cache);
        when(llmProviderMapper.selectList(any())).thenReturn(List.of(buildEnabledProvider()));

        LlmProviderModel existing = buildModel(1L, "gpt-4.1-mini", LlmModelTypeConstants.CHAT, 0, 1, 0);
        LlmProviderModel updated = buildModel(1L, "gpt-4.1-mini-renamed", LlmModelTypeConstants.CHAT, 0, 1, 0);

        syncService.syncAfterModelUpdate(existing, updated, "tester");

        ArgumentCaptor<AgentAllConfigCache> cacheCaptor = ArgumentCaptor.forClass(AgentAllConfigCache.class);
        verify(valueOperations).set(eq(RedisConstants.AgentConfig.ALL_CONFIG_KEY), cacheCaptor.capture());
        assertEquals("gpt-4.1-mini-renamed",
                cacheCaptor.getValue().getChatHistorySummary().getChatHistorySummaryModel().getModelName());
        verify(agentConfigPublisher).publishRefresh(any(AgentConfigRefreshMessage.class));
    }

    @Test
    void syncAfterModelDelete_ShouldClearKnowledgeBaseRankingModelAndDisableRanking() {
        AgentAllConfigCache cache = new AgentAllConfigCache();
        KnowledgeBaseAgentConfig knowledgeBase = new KnowledgeBaseAgentConfig();
        knowledgeBase.setRankingEnabled(true);
        knowledgeBase.setRankingModel("gpt-4.1-mini");
        cache.setKnowledgeBase(knowledgeBase);
        when(valueOperations.get(RedisConstants.AgentConfig.ALL_CONFIG_KEY)).thenReturn(cache);
        when(llmProviderMapper.selectList(any())).thenReturn(List.of(buildEnabledProvider()));

        syncService.syncAfterModelDelete(
                buildModel(1L, "gpt-4.1-mini", LlmModelTypeConstants.CHAT, 0, 0, 0),
                "tester"
        );

        ArgumentCaptor<AgentAllConfigCache> cacheCaptor = ArgumentCaptor.forClass(AgentAllConfigCache.class);
        verify(valueOperations).set(eq(RedisConstants.AgentConfig.ALL_CONFIG_KEY), cacheCaptor.capture());
        assertNull(cacheCaptor.getValue().getKnowledgeBase().getRankingModel());
        assertEquals(Boolean.FALSE, cacheCaptor.getValue().getKnowledgeBase().getRankingEnabled());
        verify(agentConfigPublisher).publishRefresh(any(AgentConfigRefreshMessage.class));
    }

    @Test
    void syncAfterModelDelete_ShouldClearReferencedSlotAndPublish() {
        AgentAllConfigCache cache = new AgentAllConfigCache();
        ImageRecognitionAgentConfig imageRecognition = new ImageRecognitionAgentConfig();
        imageRecognition.setImageRecognitionModel(buildSlot("qwen-vl-max", true, 4096, 0.2));
        cache.setImageRecognition(imageRecognition);
        when(valueOperations.get(RedisConstants.AgentConfig.ALL_CONFIG_KEY)).thenReturn(cache);
        when(llmProviderMapper.selectList(any())).thenReturn(List.of(buildEnabledProvider()));

        syncService.syncAfterModelDelete(
                buildModel(1L, "qwen-vl-max", LlmModelTypeConstants.CHAT, 0, 1, 1),
                "tester"
        );

        ArgumentCaptor<AgentAllConfigCache> cacheCaptor = ArgumentCaptor.forClass(AgentAllConfigCache.class);
        verify(valueOperations).set(eq(RedisConstants.AgentConfig.ALL_CONFIG_KEY), cacheCaptor.capture());
        assertNull(cacheCaptor.getValue().getImageRecognition().getImageRecognitionModel());
        verify(agentConfigPublisher).publishRefresh(any(AgentConfigRefreshMessage.class));
    }

    @Test
    void syncActiveProviderSnapshot_ShouldClearIncompatibleSlotsAndRefreshLlm() {
        AgentAllConfigCache cache = new AgentAllConfigCache();
        AdminAssistantAgentConfig adminAssistant = new AdminAssistantAgentConfig();
        adminAssistant.setChatModel(buildSlot("gpt-4.1-mini", true, 8192, 0.7));
        cache.setAdminAssistant(adminAssistant);
        ImageRecognitionAgentConfig imageRecognition = new ImageRecognitionAgentConfig();
        imageRecognition.setImageRecognitionModel(buildSlot("qwen-vl-max", true, 4096, 0.2));
        cache.setImageRecognition(imageRecognition);
        when(valueOperations.get(RedisConstants.AgentConfig.ALL_CONFIG_KEY)).thenReturn(cache);
        when(llmProviderModelMapper.selectList(any())).thenReturn(List.of(
                buildModel(1L, "gpt-4.1-mini", LlmModelTypeConstants.CHAT, 0, 1, 0),
                buildModel(1L, "qwen-vl-max", LlmModelTypeConstants.CHAT, 0, 1, 0)
        ));

        syncService.syncActiveProviderSnapshot(buildEnabledProvider(), "tester");

        ArgumentCaptor<AgentAllConfigCache> cacheCaptor = ArgumentCaptor.forClass(AgentAllConfigCache.class);
        verify(valueOperations).set(eq(RedisConstants.AgentConfig.ALL_CONFIG_KEY), cacheCaptor.capture());
        AgentAllConfigCache saved = cacheCaptor.getValue();
        assertEquals("openai", saved.getLlm().getProviderType());
        assertNotNull(saved.getAdminAssistant().getChatModel());
        assertNull(saved.getImageRecognition().getImageRecognitionModel());
    }

    private LlmProvider buildEnabledProvider() {
        return LlmProvider.builder()
                .id(1L)
                .providerType("openai")
                .baseUrl("https://api.openai.com/v1")
                .apiKey("sk-openai")
                .status(1)
                .build();
    }

    private LlmProvider buildTargetProvider() {
        return LlmProvider.builder()
                .id(2L)
                .providerType("aliyun")
                .baseUrl("https://dashscope.aliyuncs.com/compatible-mode/v1")
                .apiKey("sk-aliyun")
                .status(1)
                .build();
    }

    private LlmProviderModel buildModel(Long providerId,
                                        String modelName,
                                        String modelType,
                                        Integer enabled,
                                        Integer supportReasoning,
                                        Integer supportVision) {
        return LlmProviderModel.builder()
                .providerId(providerId)
                .modelName(modelName)
                .modelType(modelType)
                .enabled(enabled)
                .supportReasoning(supportReasoning)
                .supportVision(supportVision)
                .build();
    }

    private AgentModelSlotConfig buildSlot(String modelName,
                                           boolean reasoningEnabled,
                                           Integer maxTokens,
                                           Double temperature) {
        AgentModelSlotConfig slotConfig = new AgentModelSlotConfig();
        slotConfig.setModelName(modelName);
        slotConfig.setReasoningEnabled(reasoningEnabled);
        slotConfig.setMaxTokens(maxTokens);
        slotConfig.setTemperature(temperature);
        return slotConfig;
    }

    private SpeechAgentConfig buildSpeech() {
        SpeechAgentConfig config = new SpeechAgentConfig();
        config.setProvider("volcengine");
        config.setAppId("speech-app-id");
        config.setAccessToken("speech-token");
        return config;
    }
}
