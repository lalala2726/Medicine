package cn.zhangchuangla.medicine.admin.service.impl;

import cn.zhangchuangla.medicine.admin.model.request.*;
import cn.zhangchuangla.medicine.admin.publisher.AgentConfigPublisher;
import cn.zhangchuangla.medicine.admin.service.LlmProviderModelService;
import cn.zhangchuangla.medicine.admin.service.LlmProviderService;
import cn.zhangchuangla.medicine.common.core.constants.RedisConstants;
import cn.zhangchuangla.medicine.common.core.exception.ServiceException;
import cn.zhangchuangla.medicine.common.redis.core.RedisCache;
import cn.zhangchuangla.medicine.model.cache.*;
import cn.zhangchuangla.medicine.model.constants.LlmModelTypeConstants;
import cn.zhangchuangla.medicine.model.entity.LlmProvider;
import cn.zhangchuangla.medicine.model.entity.LlmProviderModel;
import cn.zhangchuangla.medicine.model.mq.AgentConfigRefreshMessage;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AgentConfigServiceImplTests {

    @Mock
    private LlmProviderService llmProviderService;

    @Mock
    private LlmProviderModelService llmProviderModelService;

    @Mock
    private RedisTemplate<Object, Object> redisTemplate;

    @Mock
    private AgentConfigPublisher agentConfigPublisher;

    @Mock
    @SuppressWarnings("rawtypes")
    private ValueOperations valueOperations;

    private AgentConfigServiceImpl agentConfigService;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        RedisCache redisCache = new RedisCache(redisTemplate);
        agentConfigService = new AgentConfigServiceImpl(
                llmProviderService,
                llmProviderModelService,
                redisCache,
                agentConfigPublisher
        );
    }

    @Test
    void getKnowledgeBaseConfig_ShouldReturnEmptyVo_WhenCacheMissing() {
        when(valueOperations.get(RedisConstants.AgentConfig.ALL_CONFIG_KEY)).thenReturn(null);

        var result = agentConfigService.getKnowledgeBaseConfig();

        assertNull(result.getEmbeddingDim());
        assertNull(result.getEmbeddingModel());
        assertNull(result.getRerankModel());
    }

    @Test
    void getKnowledgeBaseConfig_ShouldMapRuntimeToSelectionVo() {
        AgentAllConfigCache cache = new AgentAllConfigCache();
        KnowledgeBaseAgentConfig knowledgeBase = new KnowledgeBaseAgentConfig();
        knowledgeBase.setEmbeddingDim(1024);
        knowledgeBase.setEmbeddingModel(buildRuntimeSlot("text-embedding-3-large", false, 2048, 0.0));
        knowledgeBase.setRerankModel(buildRuntimeSlot("gte-rerank-v2", false, 512, 0.0));
        cache.setKnowledgeBase(knowledgeBase);
        when(valueOperations.get(RedisConstants.AgentConfig.ALL_CONFIG_KEY)).thenReturn(cache);

        var result = agentConfigService.getKnowledgeBaseConfig();

        assertEquals(1024, result.getEmbeddingDim());
        assertNotNull(result.getEmbeddingModel());
        assertEquals("text-embedding-3-large", result.getEmbeddingModel().getModelName());
        assertEquals(Boolean.FALSE, result.getEmbeddingModel().getSupportReasoning());
        assertEquals(Boolean.FALSE, result.getEmbeddingModel().getSupportVision());
        assertNotNull(result.getRerankModel());
        assertEquals("gte-rerank-v2", result.getRerankModel().getModelName());
        assertEquals(Boolean.FALSE, result.getRerankModel().getSupportReasoning());
    }

    @Test
    void getAdminAssistantConfig_ShouldMapRuntimeToSelectionVo() {
        AgentAllConfigCache cache = new AgentAllConfigCache();
        AdminAssistantAgentConfig adminAssistant = new AdminAssistantAgentConfig();
        adminAssistant.setChatModel(buildRuntimeSlot("gpt-4.1", true, 8192, 0.7));
        cache.setAdminAssistant(adminAssistant);
        when(valueOperations.get(RedisConstants.AgentConfig.ALL_CONFIG_KEY)).thenReturn(cache);

        var result = agentConfigService.getAdminAssistantConfig();

        assertNotNull(result.getChatModel());
        assertEquals("gpt-4.1", result.getChatModel().getModelName());
        assertEquals(Boolean.TRUE, result.getChatModel().getReasoningEnabled());
        assertEquals(Boolean.TRUE, result.getChatModel().getSupportReasoning());
        assertEquals(Boolean.FALSE, result.getChatModel().getSupportVision());
        assertEquals(8192, result.getChatModel().getMaxTokens());
        assertEquals(0.7, result.getChatModel().getTemperature());
    }

    @Test
    void getImageRecognitionConfig_ShouldMapRuntimeToSelectionVo() {
        AgentAllConfigCache cache = new AgentAllConfigCache();
        ImageRecognitionAgentConfig imageRecognition = new ImageRecognitionAgentConfig();
        imageRecognition.setImageRecognitionModel(buildRuntimeSlot("qwen-vl-max", true, 4096, 0.2));
        cache.setImageRecognition(imageRecognition);
        when(valueOperations.get(RedisConstants.AgentConfig.ALL_CONFIG_KEY)).thenReturn(cache);

        var result = agentConfigService.getImageRecognitionConfig();

        assertNotNull(result.getImageRecognitionModel());
        assertEquals("qwen-vl-max", result.getImageRecognitionModel().getModelName());
        assertEquals(Boolean.TRUE, result.getImageRecognitionModel().getReasoningEnabled());
        assertEquals(Boolean.TRUE, result.getImageRecognitionModel().getSupportReasoning());
        assertEquals(Boolean.TRUE, result.getImageRecognitionModel().getSupportVision());
        assertEquals(4096, result.getImageRecognitionModel().getMaxTokens());
        assertEquals(0.2, result.getImageRecognitionModel().getTemperature());
    }

    @Test
    void getSpeechConfig_ShouldHideAccessTokenAndMapEditableFields() {
        AgentAllConfigCache cache = new AgentAllConfigCache();
        SpeechAgentConfig speech = new SpeechAgentConfig();
        speech.setProvider("volcengine");
        speech.setAppId("speech-app-id");
        speech.setAccessToken("secret-token");
        SpeechRecognitionAgentConfig speechRecognition = new SpeechRecognitionAgentConfig();
        speechRecognition.setResourceId("volc.seedasr.sauc.duration");
        speech.setSpeechRecognition(speechRecognition);
        TextToSpeechAgentConfig textToSpeech = new TextToSpeechAgentConfig();
        textToSpeech.setResourceId("seed-tts-2.0");
        textToSpeech.setVoiceType("zh_female_xiaohe_uranus_bigtts");
        textToSpeech.setMaxTextChars(300);
        speech.setTextToSpeech(textToSpeech);
        cache.setSpeech(speech);
        when(valueOperations.get(RedisConstants.AgentConfig.ALL_CONFIG_KEY)).thenReturn(cache);

        var result = agentConfigService.getSpeechConfig();

        assertEquals("speech-app-id", result.getAppId());
        assertNull(result.getAccessToken());
        assertNotNull(result.getTextToSpeech());
        assertEquals("zh_female_xiaohe_uranus_bigtts", result.getTextToSpeech().getVoiceType());
        assertEquals(300, result.getTextToSpeech().getMaxTextChars());
    }

    @Test
    void getChatHistorySummaryConfig_ShouldMapRuntimeToSelectionVo() {
        AgentAllConfigCache cache = new AgentAllConfigCache();
        ChatHistorySummaryAgentConfig chatHistorySummary = new ChatHistorySummaryAgentConfig();
        chatHistorySummary.setChatHistorySummaryModel(buildRuntimeSlot("gpt-4.1-mini", false, 4096, 0.3));
        cache.setChatHistorySummary(chatHistorySummary);
        when(valueOperations.get(RedisConstants.AgentConfig.ALL_CONFIG_KEY)).thenReturn(cache);

        var result = agentConfigService.getChatHistorySummaryConfig();

        assertNotNull(result.getChatHistorySummaryModel());
        assertEquals("gpt-4.1-mini", result.getChatHistorySummaryModel().getModelName());
        assertEquals(Boolean.FALSE, result.getChatHistorySummaryModel().getReasoningEnabled());
        assertEquals(4096, result.getChatHistorySummaryModel().getMaxTokens());
        assertEquals(0.3, result.getChatHistorySummaryModel().getTemperature());
    }

    @Test
    void getChatTitleConfig_ShouldMapRuntimeToSelectionVo() {
        AgentAllConfigCache cache = new AgentAllConfigCache();
        ChatTitleAgentConfig chatTitle = new ChatTitleAgentConfig();
        chatTitle.setChatTitleModel(buildRuntimeSlot("gpt-4.1-mini", false, 32, 0.2));
        cache.setChatTitle(chatTitle);
        when(valueOperations.get(RedisConstants.AgentConfig.ALL_CONFIG_KEY)).thenReturn(cache);

        var result = agentConfigService.getChatTitleConfig();

        assertNotNull(result.getChatTitleModel());
        assertEquals("gpt-4.1-mini", result.getChatTitleModel().getModelName());
        assertEquals(Boolean.FALSE, result.getChatTitleModel().getReasoningEnabled());
        assertEquals(32, result.getChatTitleModel().getMaxTokens());
        assertEquals(0.2, result.getChatTitleModel().getTemperature());
    }

    @Test
    void listEmbeddingModelOptions_WhenNoEnabledProvider_ShouldReturnEmptyList() {
        when(llmProviderService.lambdaQuery()).thenReturn(mockProviderWrapper(List.of()));

        var result = agentConfigService.listEmbeddingModelOptions();

        assertTrue(result.isEmpty());
    }

    @Test
    void listRerankModelOptions_ShouldReturnOnlyEnabledRerankModels() {
        when(llmProviderService.lambdaQuery()).thenReturn(mockProviderWrapper(List.of(buildEnabledProvider())));
        when(llmProviderModelService.lambdaQuery()).thenReturn(mockModelWrapper(List.of(
                buildModel("gte-rerank-v2", LlmModelTypeConstants.RERANK, 0, 0, 0)
        )));

        var result = agentConfigService.listRerankModelOptions();

        assertEquals(1, result.size());
        assertEquals("gte-rerank-v2", result.getFirst().getValue());
        assertEquals("gte-rerank-v2", result.getFirst().getLabel());
        assertEquals(Boolean.FALSE, result.getFirst().getSupportReasoning());
        assertEquals(Boolean.FALSE, result.getFirst().getSupportVision());
    }

    @Test
    void listChatModelOptions_ShouldReturnOnlyEnabledChatModels() {
        when(llmProviderService.lambdaQuery()).thenReturn(mockProviderWrapper(List.of(buildEnabledProvider())));
        when(llmProviderModelService.lambdaQuery()).thenReturn(mockModelWrapper(List.of(
                buildModel("gpt-4.1", LlmModelTypeConstants.CHAT, 0, 1, 0)
        )));

        var result = agentConfigService.listChatModelOptions();

        assertEquals(1, result.size());
        assertEquals("gpt-4.1", result.getFirst().getValue());
        assertEquals("gpt-4.1", result.getFirst().getLabel());
        assertEquals(Boolean.TRUE, result.getFirst().getSupportReasoning());
        assertEquals(Boolean.FALSE, result.getFirst().getSupportVision());
    }

    @Test
    void listVisionModelOptions_ShouldReturnOnlyVisionModels() {
        when(llmProviderService.lambdaQuery()).thenReturn(mockProviderWrapper(List.of(buildEnabledProvider())));
        when(llmProviderModelService.lambdaQuery()).thenReturn(mockModelWrapper(List.of(
                buildModel("qwen-vl-max", LlmModelTypeConstants.CHAT, 0, 1, 1)
        )));

        var result = agentConfigService.listVisionModelOptions();

        assertEquals(1, result.size());
        assertEquals("qwen-vl-max", result.getFirst().getValue());
        assertEquals("qwen-vl-max", result.getFirst().getLabel());
        assertEquals(Boolean.TRUE, result.getFirst().getSupportReasoning());
        assertEquals(Boolean.TRUE, result.getFirst().getSupportVision());
    }

    @Test
    void saveKnowledgeBaseConfig_ShouldPersistResolvedCache_WhenRerankNull() {
        AgentAllConfigCache existingCache = new AgentAllConfigCache();
        existingCache.setAdminAssistant(new AdminAssistantAgentConfig());
        when(valueOperations.get(RedisConstants.AgentConfig.ALL_CONFIG_KEY)).thenReturn(existingCache);
        when(llmProviderService.lambdaQuery()).thenReturn(mockProviderWrapper(List.of(buildEnabledProvider())));
        when(llmProviderModelService.lambdaQuery()).thenReturn(mockModelWrapper(List.of(
                buildModel("text-embedding-3-large", LlmModelTypeConstants.EMBEDDING, 0, 0, 0)
        )));

        KnowledgeBaseAgentConfigRequest request = new KnowledgeBaseAgentConfigRequest();
        request.setEmbeddingDim(1024);
        request.setEmbeddingModel(buildSelection("text-embedding-3-large", false, 2048, 0.0));

        boolean result = agentConfigService.saveKnowledgeBaseConfig(request);

        assertTrue(result);
        ArgumentCaptor<AgentAllConfigCache> captor = ArgumentCaptor.forClass(AgentAllConfigCache.class);
        verify(valueOperations).set(eq(RedisConstants.AgentConfig.ALL_CONFIG_KEY), captor.capture());
        AgentAllConfigCache saved = captor.getValue();
        ArgumentCaptor<AgentConfigRefreshMessage> messageCaptor = ArgumentCaptor.forClass(AgentConfigRefreshMessage.class);
        verify(agentConfigPublisher).publishRefresh(messageCaptor.capture());
        AgentConfigRefreshMessage refreshMessage = messageCaptor.getValue();
        assertNotNull(saved.getUpdatedAt());
        assertEquals("system", saved.getUpdatedBy());
        assertEquals(RedisConstants.AgentConfig.ALL_CONFIG_KEY, refreshMessage.getRedis_key());
        assertEquals(saved.getUpdatedAt(), refreshMessage.getUpdated_at());
        assertEquals(saved.getUpdatedBy(), refreshMessage.getUpdated_by());
        assertNotNull(saved.getAdminAssistant());
        KnowledgeBaseAgentConfig knowledgeBase = saved.getKnowledgeBase();
        assertEquals(1024, knowledgeBase.getEmbeddingDim());
        assertNull(knowledgeBase.getRerankModel());
        assertEquals("openai", knowledgeBase.getEmbeddingModel().getModel().getProvider());
        assertEquals("text-embedding-3-large", knowledgeBase.getEmbeddingModel().getModel().getModel());
        assertEquals(LlmModelTypeConstants.EMBEDDING, knowledgeBase.getEmbeddingModel().getModel().getModelType());
        assertEquals("https://api.openai.com/v1", knowledgeBase.getEmbeddingModel().getModel().getBaseUrl());
        assertEquals("sk-openai", knowledgeBase.getEmbeddingModel().getModel().getApiKey());
    }

    @Test
    void saveKnowledgeBaseConfig_WhenEnabledProviderTypeMissing_ShouldThrowServiceException() {
        LlmProvider provider = buildEnabledProvider();
        provider.setProviderType(null);
        when(llmProviderService.lambdaQuery()).thenReturn(mockProviderWrapper(List.of(provider)));
        when(llmProviderModelService.lambdaQuery()).thenReturn(mockModelWrapper(List.of(
                buildModel("text-embedding-3-large", LlmModelTypeConstants.EMBEDDING, 0, 0, 0)
        )));

        KnowledgeBaseAgentConfigRequest request = new KnowledgeBaseAgentConfigRequest();
        request.setEmbeddingDim(1024);
        request.setEmbeddingModel(buildSelection("text-embedding-3-large", false, 2048, 0.0));

        ServiceException exception = assertThrows(ServiceException.class,
                () -> agentConfigService.saveKnowledgeBaseConfig(request));

        assertEquals("当前启用的模型提供商未配置类型，请先在模型提供商中补充类型", exception.getMessage());
    }

    @Test
    void saveKnowledgeBaseConfig_WhenNoEnabledProvider_ShouldThrowServiceException() {
        when(llmProviderService.lambdaQuery()).thenReturn(mockProviderWrapper(List.of()));

        KnowledgeBaseAgentConfigRequest request = new KnowledgeBaseAgentConfigRequest();
        request.setEmbeddingDim(1024);
        request.setEmbeddingModel(buildSelection("text-embedding-3-large", false, 2048, 0.0));

        ServiceException exception = assertThrows(ServiceException.class,
                () -> agentConfigService.saveKnowledgeBaseConfig(request));

        assertEquals("当前没有启用的模型提供商", exception.getMessage());
        verify(valueOperations, never()).set(any(), any());
    }

    @Test
    void saveKnowledgeBaseConfig_WhenEmbeddingModelMissing_ShouldThrowServiceException() {
        when(llmProviderService.lambdaQuery()).thenReturn(mockProviderWrapper(List.of(buildEnabledProvider())));
        when(llmProviderModelService.lambdaQuery()).thenReturn(mockModelWrapper(List.of()));

        KnowledgeBaseAgentConfigRequest request = new KnowledgeBaseAgentConfigRequest();
        request.setEmbeddingDim(1024);
        request.setEmbeddingModel(buildSelection("missing-embedding", false, 2048, 0.0));

        ServiceException exception = assertThrows(ServiceException.class,
                () -> agentConfigService.saveKnowledgeBaseConfig(request));

        assertEquals("当前启用提供商下不存在向量模型：missing-embedding", exception.getMessage());
    }

    @Test
    void saveKnowledgeBaseConfig_WhenRerankModelMissing_ShouldThrowServiceException() {
        when(llmProviderService.lambdaQuery()).thenReturn(mockProviderWrapper(List.of(buildEnabledProvider())));
        when(llmProviderModelService.lambdaQuery()).thenReturn(
                mockModelWrapper(List.of(buildModel("text-embedding-3-large", LlmModelTypeConstants.EMBEDDING, 0, 0, 0))),
                mockModelWrapper(List.of())
        );

        KnowledgeBaseAgentConfigRequest request = new KnowledgeBaseAgentConfigRequest();
        request.setEmbeddingDim(1024);
        request.setEmbeddingModel(buildSelection("text-embedding-3-large", false, 2048, 0.0));
        request.setRerankModel(buildSelection("missing-rerank", false, 512, 0.0));

        ServiceException exception = assertThrows(ServiceException.class,
                () -> agentConfigService.saveKnowledgeBaseConfig(request));

        assertEquals("当前启用提供商下不存在重排模型：missing-rerank", exception.getMessage());
    }

    @Test
    void saveKnowledgeBaseConfig_WhenModelDisabled_ShouldThrowServiceException() {
        when(llmProviderService.lambdaQuery()).thenReturn(mockProviderWrapper(List.of(buildEnabledProvider())));
        when(llmProviderModelService.lambdaQuery()).thenReturn(mockModelWrapper(List.of(
                buildModel("text-embedding-3-large", LlmModelTypeConstants.EMBEDDING, 1, 0, 0)
        )));

        KnowledgeBaseAgentConfigRequest request = new KnowledgeBaseAgentConfigRequest();
        request.setEmbeddingDim(1024);
        request.setEmbeddingModel(buildSelection("text-embedding-3-large", false, 2048, 0.0));

        ServiceException exception = assertThrows(ServiceException.class,
                () -> agentConfigService.saveKnowledgeBaseConfig(request));

        assertEquals("模型未启用：text-embedding-3-large", exception.getMessage());
    }

    @Test
    void saveKnowledgeBaseConfig_WhenReasoningUnsupported_ShouldThrowServiceException() {
        when(llmProviderService.lambdaQuery()).thenReturn(mockProviderWrapper(List.of(buildEnabledProvider())));
        when(llmProviderModelService.lambdaQuery()).thenReturn(mockModelWrapper(List.of(
                buildModel("text-embedding-3-large", LlmModelTypeConstants.EMBEDDING, 0, 0, 0)
        )));

        KnowledgeBaseAgentConfigRequest request = new KnowledgeBaseAgentConfigRequest();
        request.setEmbeddingDim(1024);
        request.setEmbeddingModel(buildSelection("text-embedding-3-large", true, 2048, 0.0));

        ServiceException exception = assertThrows(ServiceException.class,
                () -> agentConfigService.saveKnowledgeBaseConfig(request));

        assertEquals("模型不支持深度思考：text-embedding-3-large", exception.getMessage());
    }

    @Test
    void saveKnowledgeBaseConfig_WhenEmbeddingDimIsNotPowerOfTwo_ShouldThrowServiceException() {
        KnowledgeBaseAgentConfigRequest request = new KnowledgeBaseAgentConfigRequest();
        request.setEmbeddingDim(130);
        request.setEmbeddingModel(buildSelection("text-embedding-3-large", false, 2048, 0.0));

        ServiceException exception = assertThrows(ServiceException.class,
                () -> agentConfigService.saveKnowledgeBaseConfig(request));

        assertEquals("向量维度必须是2的次方", exception.getMessage());
    }

    @Test
    void saveAdminAssistantConfig_ShouldPersistAllChatSlots() {
        when(valueOperations.get(RedisConstants.AgentConfig.ALL_CONFIG_KEY)).thenReturn(null);
        when(llmProviderService.lambdaQuery()).thenReturn(mockProviderWrapper(List.of(buildEnabledProvider())));
        when(llmProviderModelService.lambdaQuery()).thenReturn(
                mockModelWrapper(List.of(buildModel("gpt-4.1-mini", LlmModelTypeConstants.CHAT, 0, 1, 0))),
                mockModelWrapper(List.of(buildModel("gpt-4.1-mini", LlmModelTypeConstants.CHAT, 0, 1, 0))),
                mockModelWrapper(List.of(buildModel("gpt-4.1", LlmModelTypeConstants.CHAT, 0, 1, 0))),
                mockModelWrapper(List.of(buildModel("gpt-4.1", LlmModelTypeConstants.CHAT, 0, 1, 1)))
        );

        AdminAssistantAgentConfigRequest request = new AdminAssistantAgentConfigRequest();
        request.setRouteModel(buildSelection("gpt-4.1-mini", false, 1024, 0.0));
        request.setBusinessNodeSimpleModel(buildSelection("gpt-4.1-mini", false, 2048, 0.3));
        request.setBusinessNodeComplexModel(buildSelection("gpt-4.1", true, 8192, 0.2));
        request.setChatModel(buildSelection("gpt-4.1", true, 8192, 0.7));

        boolean result = agentConfigService.saveAdminAssistantConfig(request);

        assertTrue(result);
        ArgumentCaptor<AgentAllConfigCache> captor = ArgumentCaptor.forClass(AgentAllConfigCache.class);
        verify(valueOperations).set(eq(RedisConstants.AgentConfig.ALL_CONFIG_KEY), captor.capture());
        verify(agentConfigPublisher).publishRefresh(any(AgentConfigRefreshMessage.class));
        assertEquals("gpt-4.1", captor.getValue().getAdminAssistant().getChatModel().getModel().getModel());
        assertEquals(Boolean.TRUE, captor.getValue().getAdminAssistant().getChatModel().getReasoningEnabled());
    }

    @Test
    void saveAdminAssistantConfig_WhenChatModelMissing_ShouldThrowServiceException() {
        when(llmProviderService.lambdaQuery()).thenReturn(mockProviderWrapper(List.of(buildEnabledProvider())));
        when(llmProviderModelService.lambdaQuery()).thenReturn(mockModelWrapper(List.of()));

        AdminAssistantAgentConfigRequest request = new AdminAssistantAgentConfigRequest();
        request.setRouteModel(buildSelection("missing-chat", false, 1024, 0.0));
        request.setBusinessNodeSimpleModel(buildSelection("missing-chat", false, 1024, 0.0));
        request.setBusinessNodeComplexModel(buildSelection("missing-chat", false, 1024, 0.0));
        request.setChatModel(buildSelection("missing-chat", false, 1024, 0.0));

        ServiceException exception = assertThrows(ServiceException.class,
                () -> agentConfigService.saveAdminAssistantConfig(request));

        assertEquals("当前启用提供商下不存在聊天模型：missing-chat", exception.getMessage());
    }

    @Test
    void saveAdminAssistantConfig_WhenMaxTokensTooSmall_ShouldThrowServiceException() {
        AdminAssistantAgentConfigRequest request = new AdminAssistantAgentConfigRequest();
        request.setRouteModel(buildSelection("gpt-4.1-mini", false, 99, 0.0));
        request.setBusinessNodeSimpleModel(buildSelection("gpt-4.1-mini", false, 2048, 0.3));
        request.setBusinessNodeComplexModel(buildSelection("gpt-4.1", true, 8192, 0.2));
        request.setChatModel(buildSelection("gpt-4.1", true, 8192, 0.7));

        ServiceException exception = assertThrows(ServiceException.class,
                () -> agentConfigService.saveAdminAssistantConfig(request));

        assertEquals("路由模型最大token数不能小于100", exception.getMessage());
    }

    @Test
    void saveAdminAssistantConfig_WhenTemperatureTooLarge_ShouldThrowServiceException() {
        AdminAssistantAgentConfigRequest request = new AdminAssistantAgentConfigRequest();
        request.setRouteModel(buildSelection("gpt-4.1-mini", false, 1024, 2.1));
        request.setBusinessNodeSimpleModel(buildSelection("gpt-4.1-mini", false, 2048, 0.3));
        request.setBusinessNodeComplexModel(buildSelection("gpt-4.1", true, 8192, 0.2));
        request.setChatModel(buildSelection("gpt-4.1", true, 8192, 0.7));

        ServiceException exception = assertThrows(ServiceException.class,
                () -> agentConfigService.saveAdminAssistantConfig(request));

        assertEquals("路由模型温度不能大于2", exception.getMessage());
    }

    @Test
    void saveImageRecognitionConfig_ShouldPersistVisionModel() {
        when(valueOperations.get(RedisConstants.AgentConfig.ALL_CONFIG_KEY)).thenReturn(null);
        when(llmProviderService.lambdaQuery()).thenReturn(mockProviderWrapper(List.of(buildEnabledProvider())));
        when(llmProviderModelService.lambdaQuery()).thenReturn(mockModelWrapper(List.of(
                buildModel("qwen-vl-max", LlmModelTypeConstants.CHAT, 0, 1, 1)
        )));

        ImageRecognitionAgentConfigRequest request = new ImageRecognitionAgentConfigRequest();
        request.setImageRecognitionModel(buildSelection("qwen-vl-max", true, 4096, 0.2));

        boolean result = agentConfigService.saveImageRecognitionConfig(request);

        assertTrue(result);
        ArgumentCaptor<AgentAllConfigCache> captor = ArgumentCaptor.forClass(AgentAllConfigCache.class);
        verify(valueOperations).set(eq(RedisConstants.AgentConfig.ALL_CONFIG_KEY), captor.capture());
        verify(agentConfigPublisher).publishRefresh(any(AgentConfigRefreshMessage.class));
        assertEquals("qwen-vl-max", captor.getValue().getImageRecognition().getImageRecognitionModel().getModel().getModel());
        assertTrue(captor.getValue().getImageRecognition().getImageRecognitionModel().getModel().getSupportVision());
    }

    @Test
    void saveKnowledgeBaseConfig_WhenPublisherFails_ShouldThrowServiceException() {
        when(valueOperations.get(RedisConstants.AgentConfig.ALL_CONFIG_KEY)).thenReturn(null);
        when(llmProviderService.lambdaQuery()).thenReturn(mockProviderWrapper(List.of(buildEnabledProvider())));
        when(llmProviderModelService.lambdaQuery()).thenReturn(mockModelWrapper(List.of(
                buildModel("text-embedding-3-large", LlmModelTypeConstants.EMBEDDING, 0, 0, 0)
        )));
        doThrow(new ServiceException("发送Agent配置刷新消息失败: mq down"))
                .when(agentConfigPublisher)
                .publishRefresh(any(AgentConfigRefreshMessage.class));

        KnowledgeBaseAgentConfigRequest request = new KnowledgeBaseAgentConfigRequest();
        request.setEmbeddingDim(1024);
        request.setEmbeddingModel(buildSelection("text-embedding-3-large", false, 2048, 0.0));

        ServiceException exception = assertThrows(ServiceException.class,
                () -> agentConfigService.saveKnowledgeBaseConfig(request));

        assertEquals("发送Agent配置刷新消息失败: mq down", exception.getMessage());
        verify(valueOperations).set(eq(RedisConstants.AgentConfig.ALL_CONFIG_KEY), any(AgentAllConfigCache.class));
        verify(agentConfigPublisher).publishRefresh(any(AgentConfigRefreshMessage.class));
    }

    @Test
    void saveImageRecognitionConfig_WhenVisionUnsupported_ShouldThrowServiceException() {
        when(llmProviderService.lambdaQuery()).thenReturn(mockProviderWrapper(List.of(buildEnabledProvider())));
        when(llmProviderModelService.lambdaQuery()).thenReturn(mockModelWrapper(List.of(
                buildModel("qwen-chat", LlmModelTypeConstants.CHAT, 0, 1, 0)
        )));

        ImageRecognitionAgentConfigRequest request = new ImageRecognitionAgentConfigRequest();
        request.setImageRecognitionModel(buildSelection("qwen-chat", false, 4096, 0.2));

        ServiceException exception = assertThrows(ServiceException.class,
                () -> agentConfigService.saveImageRecognitionConfig(request));

        assertEquals("模型不支持图片理解：qwen-chat", exception.getMessage());
    }

    @Test
    void saveImageRecognitionConfig_WhenMaxTokensTooSmall_ShouldThrowServiceException() {
        ImageRecognitionAgentConfigRequest request = new ImageRecognitionAgentConfigRequest();
        request.setImageRecognitionModel(buildSelection("qwen-vl-max", true, 511, 0.2));

        ServiceException exception = assertThrows(ServiceException.class,
                () -> agentConfigService.saveImageRecognitionConfig(request));

        assertEquals("图片识别模型最大token数不能小于512", exception.getMessage());
    }

    @Test
    void saveImageRecognitionConfig_WhenReasoningUnsupported_ShouldThrowServiceException() {
        when(llmProviderService.lambdaQuery()).thenReturn(mockProviderWrapper(List.of(buildEnabledProvider())));
        when(llmProviderModelService.lambdaQuery()).thenReturn(mockModelWrapper(List.of(
                buildModel("qwen-vl-max", LlmModelTypeConstants.CHAT, 0, 0, 1)
        )));

        ImageRecognitionAgentConfigRequest request = new ImageRecognitionAgentConfigRequest();
        request.setImageRecognitionModel(buildSelection("qwen-vl-max", true, 4096, 0.2));

        ServiceException exception = assertThrows(ServiceException.class,
                () -> agentConfigService.saveImageRecognitionConfig(request));

        assertEquals("模型不支持深度思考：qwen-vl-max", exception.getMessage());
    }

    @Test
    void saveSpeechConfig_ShouldPersistSpeechSectionAndPublishRefresh() {
        when(valueOperations.get(RedisConstants.AgentConfig.ALL_CONFIG_KEY)).thenReturn(null);

        SpeechAgentConfigRequest request = buildSpeechRequest("speech-app-id", "speech-token",
                "zh_female_xiaohe_uranus_bigtts", 300);

        boolean result = agentConfigService.saveSpeechConfig(request);

        assertTrue(result);
        ArgumentCaptor<AgentAllConfigCache> captor = ArgumentCaptor.forClass(AgentAllConfigCache.class);
        verify(valueOperations).set(eq(RedisConstants.AgentConfig.ALL_CONFIG_KEY), captor.capture());
        verify(agentConfigPublisher).publishRefresh(any(AgentConfigRefreshMessage.class));
        SpeechAgentConfig savedSpeech = captor.getValue().getSpeech();
        assertNotNull(savedSpeech);
        assertEquals("volcengine", savedSpeech.getProvider());
        assertEquals("speech-app-id", savedSpeech.getAppId());
        assertEquals("speech-token", savedSpeech.getAccessToken());
        assertEquals("volc.seedasr.sauc.duration", savedSpeech.getSpeechRecognition().getResourceId());
        assertEquals("seed-tts-2.0", savedSpeech.getTextToSpeech().getResourceId());
        assertEquals("zh_female_xiaohe_uranus_bigtts", savedSpeech.getTextToSpeech().getVoiceType());
        assertEquals(300, savedSpeech.getTextToSpeech().getMaxTextChars());
    }

    @Test
    void saveSpeechConfig_WhenExistingTokenAndRequestTokenBlank_ShouldPreserveOldToken() {
        AgentAllConfigCache cache = new AgentAllConfigCache();
        SpeechAgentConfig existingSpeech = new SpeechAgentConfig();
        existingSpeech.setProvider("volcengine");
        existingSpeech.setAppId("old-app-id");
        existingSpeech.setAccessToken("old-token");
        cache.setSpeech(existingSpeech);
        when(valueOperations.get(RedisConstants.AgentConfig.ALL_CONFIG_KEY)).thenReturn(cache);

        SpeechAgentConfigRequest request = buildSpeechRequest("speech-app-id", "   ",
                "zh_female_xiaohe_uranus_bigtts", 300);

        boolean result = agentConfigService.saveSpeechConfig(request);

        assertTrue(result);
        ArgumentCaptor<AgentAllConfigCache> captor = ArgumentCaptor.forClass(AgentAllConfigCache.class);
        verify(valueOperations).set(eq(RedisConstants.AgentConfig.ALL_CONFIG_KEY), captor.capture());
        assertEquals("old-token", captor.getValue().getSpeech().getAccessToken());
    }

    @Test
    void saveSpeechConfig_WhenNoExistingTokenAndRequestTokenBlank_ShouldThrowServiceException() {
        when(valueOperations.get(RedisConstants.AgentConfig.ALL_CONFIG_KEY)).thenReturn(null);

        SpeechAgentConfigRequest request = buildSpeechRequest("speech-app-id", null,
                "zh_female_xiaohe_uranus_bigtts", 300);

        ServiceException exception = assertThrows(ServiceException.class, () -> agentConfigService.saveSpeechConfig(request));

        assertEquals("豆包语音AccessToken不能为空", exception.getMessage());
        verify(valueOperations, never()).set(any(), any());
        verify(agentConfigPublisher, never()).publishRefresh(any());
    }

    @Test
    void saveSpeechConfig_WhenMaxTextCharsTooLarge_ShouldThrowServiceException() {
        when(valueOperations.get(RedisConstants.AgentConfig.ALL_CONFIG_KEY)).thenReturn(null);

        SpeechAgentConfigRequest request = buildSpeechRequest("speech-app-id", "speech-token",
                "zh_female_xiaohe_uranus_bigtts", 3001);

        ServiceException exception = assertThrows(ServiceException.class, () -> agentConfigService.saveSpeechConfig(request));

        assertEquals("语音合成最大文本长度不能大于3000", exception.getMessage());
    }

    @Test
    void saveChatHistorySummaryConfig_ShouldPersistSummaryModel() {
        when(valueOperations.get(RedisConstants.AgentConfig.ALL_CONFIG_KEY)).thenReturn(null);
        when(llmProviderService.lambdaQuery()).thenReturn(mockProviderWrapper(List.of(buildEnabledProvider())));
        when(llmProviderModelService.lambdaQuery()).thenReturn(mockModelWrapper(List.of(
                buildModel("gpt-4.1-mini", LlmModelTypeConstants.CHAT, 0, 1, 0)
        )));

        ChatHistorySummaryAgentConfigRequest request = new ChatHistorySummaryAgentConfigRequest();
        request.setChatHistorySummaryModel(buildSelection("gpt-4.1-mini", false, 4096, 0.3));

        boolean result = agentConfigService.saveChatHistorySummaryConfig(request);

        assertTrue(result);
        ArgumentCaptor<AgentAllConfigCache> captor = ArgumentCaptor.forClass(AgentAllConfigCache.class);
        verify(valueOperations).set(eq(RedisConstants.AgentConfig.ALL_CONFIG_KEY), captor.capture());
        verify(agentConfigPublisher).publishRefresh(any(AgentConfigRefreshMessage.class));
        assertEquals("gpt-4.1-mini",
                captor.getValue().getChatHistorySummary().getChatHistorySummaryModel().getModel().getModel());
        assertEquals(4096, captor.getValue().getChatHistorySummary().getChatHistorySummaryModel().getMaxTokens());
    }

    @Test
    void saveChatHistorySummaryConfig_WhenMaxTokensTooSmall_ShouldThrowServiceException() {
        ChatHistorySummaryAgentConfigRequest request = new ChatHistorySummaryAgentConfigRequest();
        request.setChatHistorySummaryModel(buildSelection("gpt-4.1-mini", false, 99, 0.3));

        ServiceException exception = assertThrows(ServiceException.class,
                () -> agentConfigService.saveChatHistorySummaryConfig(request));

        assertEquals("聊天历史总结模型最大token数不能小于100", exception.getMessage());
    }

    @Test
    void saveChatTitleConfig_ShouldPersistTitleModel() {
        when(valueOperations.get(RedisConstants.AgentConfig.ALL_CONFIG_KEY)).thenReturn(null);
        when(llmProviderService.lambdaQuery()).thenReturn(mockProviderWrapper(List.of(buildEnabledProvider())));
        when(llmProviderModelService.lambdaQuery()).thenReturn(mockModelWrapper(List.of(
                buildModel("gpt-4.1-mini", LlmModelTypeConstants.CHAT, 0, 1, 0)
        )));

        ChatTitleAgentConfigRequest request = new ChatTitleAgentConfigRequest();
        request.setChatTitleModel(buildSelection("gpt-4.1-mini", false, 32, 0.2));

        boolean result = agentConfigService.saveChatTitleConfig(request);

        assertTrue(result);
        ArgumentCaptor<AgentAllConfigCache> captor = ArgumentCaptor.forClass(AgentAllConfigCache.class);
        verify(valueOperations).set(eq(RedisConstants.AgentConfig.ALL_CONFIG_KEY), captor.capture());
        verify(agentConfigPublisher).publishRefresh(any(AgentConfigRefreshMessage.class));
        assertEquals("gpt-4.1-mini",
                captor.getValue().getChatTitle().getChatTitleModel().getModel().getModel());
        assertEquals(32, captor.getValue().getChatTitle().getChatTitleModel().getMaxTokens());
    }

    @Test
    void saveChatTitleConfig_WhenMaxTokensTooLarge_ShouldThrowServiceException() {
        ChatTitleAgentConfigRequest request = new ChatTitleAgentConfigRequest();
        request.setChatTitleModel(buildSelection("gpt-4.1-mini", false, 51, 0.2));

        ServiceException exception = assertThrows(ServiceException.class,
                () -> agentConfigService.saveChatTitleConfig(request));

        assertEquals("聊天标题生成模型最大token数不能大于50", exception.getMessage());
    }

    @SuppressWarnings("unchecked")
    private LambdaQueryChainWrapper<LlmProvider> mockProviderWrapper(List<LlmProvider> providers) {
        LambdaQueryChainWrapper<LlmProvider> wrapper = mock(LambdaQueryChainWrapper.class, RETURNS_SELF);
        when(wrapper.list()).thenReturn(providers);
        return wrapper;
    }

    @SuppressWarnings("unchecked")
    private LambdaQueryChainWrapper<LlmProviderModel> mockModelWrapper(List<LlmProviderModel> models) {
        LambdaQueryChainWrapper<LlmProviderModel> wrapper = mock(LambdaQueryChainWrapper.class, RETURNS_SELF);
        when(wrapper.list()).thenReturn(models);
        return wrapper;
    }

    private LlmProvider buildEnabledProvider() {
        return LlmProvider.builder()
                .id(1L)
                .providerName("OpenAI")
                .providerType("openai")
                .baseUrl("https://api.openai.com/v1")
                .apiKey("sk-openai")
                .status(1)
                .sort(1)
                .build();
    }

    private LlmProviderModel buildModel(String modelName, String modelType, Integer enabled,
                                        Integer supportReasoning, Integer supportVision) {
        return LlmProviderModel.builder()
                .id(1L)
                .providerId(1L)
                .modelName(modelName)
                .modelType(modelType)
                .enabled(enabled)
                .supportReasoning(supportReasoning)
                .supportVision(supportVision)
                .sort(1)
                .build();
    }

    private AgentModelSelectionRequest buildSelection(String modelName,
                                                      boolean reasoningEnabled,
                                                      Integer maxTokens,
                                                      Double temperature) {
        AgentModelSelectionRequest request = new AgentModelSelectionRequest();
        request.setModelName(modelName);
        request.setReasoningEnabled(reasoningEnabled);
        request.setMaxTokens(maxTokens);
        request.setTemperature(temperature);
        return request;
    }

    private SpeechAgentConfigRequest buildSpeechRequest(String appId,
                                                        String accessToken,
                                                        String voiceType,
                                                        Integer maxTextChars) {
        SpeechAgentConfigRequest request = new SpeechAgentConfigRequest();
        request.setAppId(appId);
        request.setAccessToken(accessToken);
        TextToSpeechConfigRequest textToSpeech = new TextToSpeechConfigRequest();
        textToSpeech.setVoiceType(voiceType);
        textToSpeech.setMaxTextChars(maxTextChars);
        request.setTextToSpeech(textToSpeech);
        return request;
    }

    private AgentModelSlotConfig buildRuntimeSlot(String modelName,
                                                  boolean reasoningEnabled,
                                                  Integer maxTokens,
                                                  Double temperature) {
        AgentModelSlotConfig slotConfig = new AgentModelSlotConfig();
        slotConfig.setReasoningEnabled(reasoningEnabled);
        slotConfig.setMaxTokens(maxTokens);
        slotConfig.setTemperature(temperature);
        cn.zhangchuangla.medicine.model.cache.AgentModelRuntimeConfig runtimeConfig =
                new cn.zhangchuangla.medicine.model.cache.AgentModelRuntimeConfig();
        runtimeConfig.setModel(modelName);
        runtimeConfig.setSupportReasoning(reasoningEnabled);
        runtimeConfig.setSupportVision("qwen-vl-max".equals(modelName));
        slotConfig.setModel(runtimeConfig);
        return slotConfig;
    }
}
