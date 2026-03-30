package cn.zhangchuangla.medicine.admin.service.impl;

import cn.zhangchuangla.medicine.admin.model.request.*;
import cn.zhangchuangla.medicine.admin.service.AgentConfigRuntimeSyncService;
import cn.zhangchuangla.medicine.admin.service.KbBaseService;
import cn.zhangchuangla.medicine.admin.service.LlmProviderModelService;
import cn.zhangchuangla.medicine.admin.service.LlmProviderService;
import cn.zhangchuangla.medicine.common.core.exception.ParamException;
import cn.zhangchuangla.medicine.common.core.exception.ServiceException;
import cn.zhangchuangla.medicine.model.cache.*;
import cn.zhangchuangla.medicine.model.constants.LlmModelTypeConstants;
import cn.zhangchuangla.medicine.model.entity.KbBase;
import cn.zhangchuangla.medicine.model.entity.LlmProvider;
import cn.zhangchuangla.medicine.model.entity.LlmProviderModel;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AgentConfigServiceImplTests {

    @Mock
    private KbBaseService kbBaseService;

    @Mock
    private LlmProviderService llmProviderService;

    @Mock
    private LlmProviderModelService llmProviderModelService;

    @Mock
    private AgentConfigRuntimeSyncService agentConfigRuntimeSyncService;

    @Test
    void getKnowledgeBaseConfig_ShouldMapKnowledgeNamesAndLookupCapabilitiesFromDatabase() {
        AgentConfigServiceImpl service = newService();
        AgentAllConfigCache cache = new AgentAllConfigCache();
        KnowledgeBaseAgentConfig knowledgeBase = new KnowledgeBaseAgentConfig();
        knowledgeBase.setEnabled(true);
        knowledgeBase.setKnowledgeNames(List.of("common_medicine_kb", "otc_guide_kb"));
        knowledgeBase.setEmbeddingDim(1024);
        knowledgeBase.setTopK(10);
        knowledgeBase.setEmbeddingModel("text-embedding-3-large");
        knowledgeBase.setRankingEnabled(false);
        knowledgeBase.setRankingModel(null);
        cache.setKnowledgeBase(knowledgeBase);
        when(agentConfigRuntimeSyncService.readCache()).thenReturn(cache);
        stubProviderQuery(List.of(buildEnabledProvider()));
        stubModelQueries(List.of(buildModel("text-embedding-3-large", LlmModelTypeConstants.EMBEDDING, 0, 0, 0)));

        var result = service.getKnowledgeBaseConfig();

        assertEquals(Boolean.TRUE, result.getEnabled());
        assertEquals(List.of("common_medicine_kb", "otc_guide_kb"), result.getKnowledgeNames());
        assertEquals(1024, result.getEmbeddingDim());
        assertEquals(10, result.getTopK());
        assertEquals("text-embedding-3-large", result.getEmbeddingModel().getModelName());
        assertEquals(Boolean.FALSE, result.getEmbeddingModel().getSupportReasoning());
        assertEquals(Boolean.FALSE, result.getEmbeddingModel().getSupportVision());
        assertEquals(Boolean.FALSE, result.getRankingEnabled());
        assertNull(result.getRankingModel());
    }

    @Test
    void listKnowledgeBaseOptions_ShouldReturnEnabledKnowledgeBases() {
        AgentConfigServiceImpl service = newService();
        KbBase first = new KbBase();
        first.setKnowledgeName("common_medicine_kb");
        first.setDisplayName("常见用药知识库");
        first.setEmbeddingModel("text-embedding-3-large");
        first.setEmbeddingDim(1024);
        KbBase second = new KbBase();
        second.setKnowledgeName("otc_guide_kb");
        second.setDisplayName("OTC 指南知识库");
        second.setEmbeddingModel("text-embedding-3-large");
        second.setEmbeddingDim(1024);
        when(kbBaseService.listEnabledKnowledgeBases()).thenReturn(List.of(first, second));

        var result = service.listKnowledgeBaseOptions();

        assertEquals(2, result.size());
        assertEquals("common_medicine_kb", result.getFirst().getKnowledgeName());
        assertEquals("常见用药知识库", result.getFirst().getDisplayName());
        assertEquals("text-embedding-3-large", result.getFirst().getEmbeddingModel());
        assertEquals(1024, result.getFirst().getEmbeddingDim());
    }

    @Test
    void saveKnowledgeBaseConfig_ShouldWriteKnowledgeNamesAndRankingEnabled() {
        AgentConfigServiceImpl service = newService();
        when(agentConfigRuntimeSyncService.readCache()).thenReturn(new AgentAllConfigCache());
        stubProviderQuery(List.of(buildEnabledProvider()));
        when(kbBaseService.listEnabledKnowledgeBasesByNames(List.of("common_medicine_kb", "otc_guide_kb")))
                .thenReturn(List.of(
                        buildKnowledgeBase("common_medicine_kb", "text-embedding-3-large", 1024),
                        buildKnowledgeBase("otc_guide_kb", "text-embedding-3-large", 1024)
                ));
        stubModelQueries(
                List.of(buildModel("text-embedding-3-large", LlmModelTypeConstants.EMBEDDING, 0, 0, 0)),
                List.of(buildModel("gpt-4.1-mini", LlmModelTypeConstants.CHAT, 0, 0, 0))
        );

        KnowledgeBaseAgentConfigRequest request = new KnowledgeBaseAgentConfigRequest();
        request.setEnabled(true);
        request.setKnowledgeNames(List.of("common_medicine_kb", "otc_guide_kb"));
        request.setEmbeddingDim(1024);
        request.setEmbeddingModel(buildSelection("text-embedding-3-large", false, 2048, 0.0));
        request.setTopK(10);
        request.setRankingEnabled(true);
        request.setRankingModel(buildSelection("gpt-4.1-mini", false, 512, 0.0));

        boolean result = service.saveKnowledgeBaseConfig(request);

        assertTrue(result);
        ArgumentCaptor<AgentAllConfigCache> cacheCaptor = ArgumentCaptor.forClass(AgentAllConfigCache.class);
        verify(agentConfigRuntimeSyncService).saveCache(cacheCaptor.capture(), any(), any());
        KnowledgeBaseAgentConfig saved = cacheCaptor.getValue().getKnowledgeBase();
        assertEquals(Boolean.TRUE, saved.getEnabled());
        assertEquals(List.of("common_medicine_kb", "otc_guide_kb"), saved.getKnowledgeNames());
        assertEquals(10, saved.getTopK());
        assertEquals(Boolean.TRUE, saved.getRankingEnabled());
        assertEquals("text-embedding-3-large", saved.getEmbeddingModel());
        assertEquals("gpt-4.1-mini", saved.getRankingModel());
    }

    @Test
    void saveKnowledgeBaseConfig_WhenRankingDisabledAndModelProvided_ShouldThrow() {
        AgentConfigServiceImpl service = newService();
        when(agentConfigRuntimeSyncService.readCache()).thenReturn(new AgentAllConfigCache());
        stubProviderQuery(List.of(buildEnabledProvider()));
        when(kbBaseService.listEnabledKnowledgeBasesByNames(List.of("common_medicine_kb")))
                .thenReturn(List.of(buildKnowledgeBase("common_medicine_kb", "text-embedding-3-large", 1024)));
        stubModelQueries(List.of(buildModel("text-embedding-3-large", LlmModelTypeConstants.EMBEDDING, 0, 0, 0)));

        KnowledgeBaseAgentConfigRequest request = new KnowledgeBaseAgentConfigRequest();
        request.setEnabled(true);
        request.setKnowledgeNames(List.of("common_medicine_kb"));
        request.setEmbeddingDim(1024);
        request.setEmbeddingModel(buildSelection("text-embedding-3-large", false, 2048, 0.0));
        request.setTopK(10);
        request.setRankingEnabled(false);
        request.setRankingModel(buildSelection("gpt-4.1-mini", false, 512, 0.0));

        ParamException exception = assertThrows(ParamException.class, () -> service.saveKnowledgeBaseConfig(request));

        assertEquals("关闭排序时不允许选择排序模型", exception.getMessage());
        verify(agentConfigRuntimeSyncService, never()).saveCache(any(), any(), any());
    }

    @Test
    void saveKnowledgeBaseConfig_WhenKnowledgeBaseEmbeddingMismatch_ShouldThrow() {
        AgentConfigServiceImpl service = newService();
        when(agentConfigRuntimeSyncService.readCache()).thenReturn(new AgentAllConfigCache());
        stubProviderQuery(List.of(buildEnabledProvider()));
        when(kbBaseService.listEnabledKnowledgeBasesByNames(List.of("common_medicine_kb", "otc_guide_kb")))
                .thenReturn(List.of(
                        buildKnowledgeBase("common_medicine_kb", "text-embedding-3-large", 1024),
                        buildKnowledgeBase("otc_guide_kb", "text-embedding-3-small", 1024)
                ));
        stubModelQueries(List.of(buildModel("text-embedding-3-large", LlmModelTypeConstants.EMBEDDING, 0, 0, 0)));

        KnowledgeBaseAgentConfigRequest request = new KnowledgeBaseAgentConfigRequest();
        request.setEnabled(true);
        request.setKnowledgeNames(List.of("common_medicine_kb", "otc_guide_kb"));
        request.setEmbeddingDim(1024);
        request.setEmbeddingModel(buildSelection("text-embedding-3-large", false, 2048, 0.0));
        request.setTopK(10);
        request.setRankingEnabled(false);

        ServiceException exception = assertThrows(ServiceException.class, () -> service.saveKnowledgeBaseConfig(request));

        assertEquals("知识库向量模型必须与第一个知识库保持一致：otc_guide_kb", exception.getMessage());
        verify(agentConfigRuntimeSyncService, never()).saveCache(any(), any(), any());
    }

    @Test
    void saveKnowledgeBaseConfig_WhenKnowledgeBaseCountExceedsMaxLimit_ShouldThrow() {
        AgentConfigServiceImpl service = newService();
        when(agentConfigRuntimeSyncService.readCache()).thenReturn(new AgentAllConfigCache());

        java.util.List<String> knowledgeNames = java.util.stream.IntStream.rangeClosed(1, 6)
                .mapToObj(index -> "knowledge_" + index)
                .toList();

        KnowledgeBaseAgentConfigRequest request = new KnowledgeBaseAgentConfigRequest();
        request.setEnabled(true);
        request.setKnowledgeNames(knowledgeNames);
        request.setEmbeddingDim(1024);
        request.setEmbeddingModel(buildSelection("text-embedding-3-large", false, 2048, 0.0));
        request.setTopK(10);
        request.setRankingEnabled(false);

        ParamException exception = assertThrows(ParamException.class, () -> service.saveKnowledgeBaseConfig(request));

        assertEquals("知识库最多支持5个", exception.getMessage());
        verify(agentConfigRuntimeSyncService, never()).saveCache(any(), any(), any());
    }

    @Test
    void saveKnowledgeBaseConfig_WhenTopKIsZero_ShouldPersistAsNull() {
        AgentConfigServiceImpl service = newService();
        when(agentConfigRuntimeSyncService.readCache()).thenReturn(new AgentAllConfigCache());
        stubProviderQuery(List.of(buildEnabledProvider()));
        when(kbBaseService.listEnabledKnowledgeBasesByNames(List.of("common_medicine_kb")))
                .thenReturn(List.of(buildKnowledgeBase("common_medicine_kb", "text-embedding-3-large", 1024)));
        stubModelQueries(List.of(buildModel("text-embedding-3-large", LlmModelTypeConstants.EMBEDDING, 0, 0, 0)));

        KnowledgeBaseAgentConfigRequest request = new KnowledgeBaseAgentConfigRequest();
        request.setEnabled(true);
        request.setKnowledgeNames(List.of("common_medicine_kb"));
        request.setEmbeddingDim(1024);
        request.setEmbeddingModel(buildSelection("text-embedding-3-large", false, 2048, 0.0));
        request.setTopK(0);
        request.setRankingEnabled(false);

        boolean result = service.saveKnowledgeBaseConfig(request);

        assertTrue(result);
        ArgumentCaptor<AgentAllConfigCache> cacheCaptor = ArgumentCaptor.forClass(AgentAllConfigCache.class);
        verify(agentConfigRuntimeSyncService).saveCache(cacheCaptor.capture(), any(), any());
        assertNull(cacheCaptor.getValue().getKnowledgeBase().getTopK());
    }

    @Test
    void saveKnowledgeBaseConfig_WhenDisabled_ShouldPersistDisabledStateWithoutProviderValidation() {
        AgentConfigServiceImpl service = newService();
        AgentAllConfigCache cache = new AgentAllConfigCache();
        KnowledgeBaseAgentConfig existing = new KnowledgeBaseAgentConfig();
        existing.setEnabled(true);
        existing.setKnowledgeNames(List.of("common_medicine_kb"));
        existing.setEmbeddingDim(1024);
        existing.setEmbeddingModel("text-embedding-3-large");
        existing.setTopK(10);
        existing.setRankingEnabled(true);
        existing.setRankingModel("gpt-4.1-mini");
        cache.setKnowledgeBase(existing);
        when(agentConfigRuntimeSyncService.readCache()).thenReturn(cache);
        stubProviderQuery(List.of());

        KnowledgeBaseAgentConfigRequest request = new KnowledgeBaseAgentConfigRequest();
        request.setEnabled(false);

        boolean result = service.saveKnowledgeBaseConfig(request);

        assertTrue(result);
        ArgumentCaptor<AgentAllConfigCache> cacheCaptor = ArgumentCaptor.forClass(AgentAllConfigCache.class);
        verify(agentConfigRuntimeSyncService).saveCache(cacheCaptor.capture(), isNull(), any());
        KnowledgeBaseAgentConfig saved = cacheCaptor.getValue().getKnowledgeBase();
        assertEquals(Boolean.FALSE, saved.getEnabled());
        assertEquals(List.of("common_medicine_kb"), saved.getKnowledgeNames());
        assertEquals("text-embedding-3-large", saved.getEmbeddingModel());
        verify(kbBaseService, never()).listEnabledKnowledgeBasesByNames(any());
        verify(llmProviderModelService, never()).lambdaQuery();
    }

    @Test
    void getClientAssistantConfig_ShouldPopulateCapabilitiesFromDatabase() {
        AgentConfigServiceImpl service = newService();
        AgentAllConfigCache cache = new AgentAllConfigCache();
        ClientAssistantAgentConfig clientAssistant = new ClientAssistantAgentConfig();
        clientAssistant.setRouteModel(buildSlot("gpt-4.1-mini", false, 1024, 0.0));
        clientAssistant.setChatModel(buildSlot("gpt-4.1", true, 4096, 0.7));
        clientAssistant.setOrderModel(buildSlot("gpt-4.1-mini", false, 2048, 0.3));
        clientAssistant.setProductModel(buildSlot("gpt-4.1-mini", false, 2048, 0.3));
        clientAssistant.setAfterSaleModel(buildSlot("gpt-4.1-mini", false, 2048, 0.3));
        clientAssistant.setConsultationComfortModel(buildSlot("gpt-4.1-mini", false, 2048, 1.2));
        clientAssistant.setConsultationQuestionModel(buildSlot("gpt-4.1", true, 4096, 0.2));
        clientAssistant.setConsultationFinalDiagnosisModel(buildSlot("gpt-4.1", true, 4096, 0.2));
        cache.setClientAssistant(clientAssistant);
        when(agentConfigRuntimeSyncService.readCache()).thenReturn(cache);
        stubProviderQuery(List.of(buildEnabledProvider()));
        stubModelQueries(
                List.of(buildModel("gpt-4.1-mini", LlmModelTypeConstants.CHAT, 0, 0, 0)),
                List.of(buildModel("gpt-4.1", LlmModelTypeConstants.CHAT, 0, 1, 0)),
                List.of(buildModel("gpt-4.1-mini", LlmModelTypeConstants.CHAT, 0, 0, 0)),
                List.of(buildModel("gpt-4.1-mini", LlmModelTypeConstants.CHAT, 0, 0, 0)),
                List.of(buildModel("gpt-4.1-mini", LlmModelTypeConstants.CHAT, 0, 0, 0)),
                List.of(buildModel("gpt-4.1-mini", LlmModelTypeConstants.CHAT, 0, 0, 0)),
                List.of(buildModel("gpt-4.1", LlmModelTypeConstants.CHAT, 0, 1, 0)),
                List.of(buildModel("gpt-4.1", LlmModelTypeConstants.CHAT, 0, 1, 0))
        );

        var result = service.getClientAssistantConfig();

        assertEquals("gpt-4.1-mini", result.getRouteModel().getModelName());
        assertEquals(Boolean.TRUE, result.getChatModel().getSupportReasoning());
        assertEquals("gpt-4.1-mini", result.getAfterSaleModel().getModelName());
        assertEquals("gpt-4.1", result.getConsultationFinalDiagnosisModel().getModelName());
    }

    @Test
    void saveClientAssistantConfig_ShouldPersistAllClientSlots() {
        AgentConfigServiceImpl service = newService();
        when(agentConfigRuntimeSyncService.readCache()).thenReturn(new AgentAllConfigCache());
        stubProviderQuery(List.of(buildEnabledProvider()));
        stubModelQueries(
                List.of(buildModel("gpt-4.1-mini", LlmModelTypeConstants.CHAT, 0, 0, 0)),
                List.of(buildModel("gpt-4.1", LlmModelTypeConstants.CHAT, 0, 1, 0)),
                List.of(buildModel("gpt-4.1-mini", LlmModelTypeConstants.CHAT, 0, 0, 0)),
                List.of(buildModel("gpt-4.1-mini", LlmModelTypeConstants.CHAT, 0, 0, 0)),
                List.of(buildModel("gpt-4.1-mini", LlmModelTypeConstants.CHAT, 0, 0, 0)),
                List.of(buildModel("gpt-4.1-mini", LlmModelTypeConstants.CHAT, 0, 0, 0)),
                List.of(buildModel("gpt-4.1", LlmModelTypeConstants.CHAT, 0, 1, 0)),
                List.of(buildModel("gpt-4.1", LlmModelTypeConstants.CHAT, 0, 1, 0))
        );

        ClientAssistantAgentConfigRequest request = new ClientAssistantAgentConfigRequest();
        request.setRouteModel(buildSelection("gpt-4.1-mini", false, 1024, 0.0));
        request.setChatModel(buildSelection("gpt-4.1", true, 4096, 0.7));
        request.setOrderModel(buildSelection("gpt-4.1-mini", false, 2048, 0.3));
        request.setProductModel(buildSelection("gpt-4.1-mini", false, 2048, 0.3));
        request.setAfterSaleModel(buildSelection("gpt-4.1-mini", false, 2048, 0.3));
        request.setConsultationComfortModel(buildSelection("gpt-4.1-mini", false, 2048, 1.2));
        request.setConsultationQuestionModel(buildSelection("gpt-4.1", true, 4096, 0.2));
        request.setConsultationFinalDiagnosisModel(buildSelection("gpt-4.1", true, 4096, 0.2));

        boolean result = service.saveClientAssistantConfig(request);

        assertTrue(result);
        ArgumentCaptor<AgentAllConfigCache> cacheCaptor = ArgumentCaptor.forClass(AgentAllConfigCache.class);
        verify(agentConfigRuntimeSyncService).saveCache(cacheCaptor.capture(), any(), any());
        ClientAssistantAgentConfig saved = cacheCaptor.getValue().getClientAssistant();
        assertEquals("gpt-4.1-mini", saved.getRouteModel().getModelName());
        assertEquals("gpt-4.1", saved.getChatModel().getModelName());
        assertEquals("gpt-4.1-mini", saved.getOrderModel().getModelName());
        assertEquals("gpt-4.1-mini", saved.getAfterSaleModel().getModelName());
        assertEquals("gpt-4.1", saved.getConsultationQuestionModel().getModelName());
        assertEquals("gpt-4.1", saved.getConsultationFinalDiagnosisModel().getModelName());
    }

    @Test
    void getImageRecognitionConfig_ShouldPopulateVisionCapabilitiesFromDatabase() {
        AgentConfigServiceImpl service = newService();
        AgentAllConfigCache cache = new AgentAllConfigCache();
        ImageRecognitionAgentConfig imageRecognition = new ImageRecognitionAgentConfig();
        imageRecognition.setImageRecognitionModel(buildSlot("qwen-vl-max", true, 4096, 0.2));
        cache.setImageRecognition(imageRecognition);
        when(agentConfigRuntimeSyncService.readCache()).thenReturn(cache);
        stubProviderQuery(List.of(buildEnabledProvider()));
        stubModelQueries(List.of(buildModel("qwen-vl-max", LlmModelTypeConstants.CHAT, 0, 1, 1)));

        var result = service.getImageRecognitionConfig();

        assertEquals("qwen-vl-max", result.getImageRecognitionModel().getModelName());
        assertEquals(Boolean.TRUE, result.getImageRecognitionModel().getSupportReasoning());
        assertEquals(Boolean.TRUE, result.getImageRecognitionModel().getSupportVision());
    }

    @Test
    void getSpeechConfig_ShouldHideAccessToken() {
        AgentConfigServiceImpl service = newService();
        AgentAllConfigCache cache = new AgentAllConfigCache();
        SpeechAgentConfig speech = new SpeechAgentConfig();
        speech.setProvider("volcengine");
        speech.setAppId("speech-app-id");
        speech.setAccessToken("secret-token");
        TextToSpeechAgentConfig textToSpeech = new TextToSpeechAgentConfig();
        textToSpeech.setResourceId("seed-tts-2.0");
        textToSpeech.setVoiceType("zh_female_xiaohe_uranus_bigtts");
        textToSpeech.setMaxTextChars(300);
        speech.setTextToSpeech(textToSpeech);
        cache.setSpeech(speech);
        when(agentConfigRuntimeSyncService.readCache()).thenReturn(cache);

        var result = service.getSpeechConfig();

        assertEquals("speech-app-id", result.getAppId());
        assertNull(result.getAccessToken());
        assertEquals("zh_female_xiaohe_uranus_bigtts", result.getTextToSpeech().getVoiceType());
    }

    @Test
    void saveSpeechConfig_ShouldReuseExistingTokenAndKeepSpeechIndependent() {
        AgentConfigServiceImpl service = newService();
        AgentAllConfigCache cache = new AgentAllConfigCache();
        SpeechAgentConfig speech = new SpeechAgentConfig();
        speech.setAccessToken("existing-token");
        cache.setSpeech(speech);
        when(agentConfigRuntimeSyncService.readCache()).thenReturn(cache);
        stubProviderQuery(List.of());

        SpeechAgentConfigRequest request = new SpeechAgentConfigRequest();
        request.setAppId("speech-app-id");
        request.setAccessToken("   ");
        TextToSpeechConfigRequest textToSpeech = new TextToSpeechConfigRequest();
        textToSpeech.setVoiceType("voice-type");
        textToSpeech.setMaxTextChars(300);
        request.setTextToSpeech(textToSpeech);

        boolean result = service.saveSpeechConfig(request);

        assertTrue(result);
        ArgumentCaptor<AgentAllConfigCache> cacheCaptor = ArgumentCaptor.forClass(AgentAllConfigCache.class);
        verify(agentConfigRuntimeSyncService).saveCache(cacheCaptor.capture(), isNull(), any());
        assertEquals("existing-token", cacheCaptor.getValue().getSpeech().getAccessToken());
        assertEquals("voice-type", cacheCaptor.getValue().getSpeech().getTextToSpeech().getVoiceType());
    }

    private AgentConfigServiceImpl newService() {
        return new AgentConfigServiceImpl(
                kbBaseService,
                llmProviderService,
                llmProviderModelService,
                agentConfigRuntimeSyncService
        );
    }

    @SuppressWarnings("unchecked")
    private LambdaQueryChainWrapper<LlmProvider> mockProviderWrapper(List<LlmProvider> providers) {
        return mock(LambdaQueryChainWrapper.class, invocation -> {
            String methodName = invocation.getMethod().getName();
            if ("list".equals(methodName)) {
                return providers;
            }
            if ("toString".equals(methodName)) {
                return "providerQueryWrapper";
            }
            if ("hashCode".equals(methodName)) {
                return System.identityHashCode(invocation.getMock());
            }
            if ("equals".equals(methodName)) {
                return invocation.getMock() == invocation.getArgument(0);
            }
            return invocation.getMock();
        });
    }

    @SuppressWarnings("unchecked")
    private LambdaQueryChainWrapper<LlmProviderModel> mockModelWrapper(List<LlmProviderModel> models) {
        return mock(LambdaQueryChainWrapper.class, invocation -> {
            String methodName = invocation.getMethod().getName();
            if ("list".equals(methodName)) {
                return models;
            }
            if ("toString".equals(methodName)) {
                return "modelQueryWrapper";
            }
            if ("hashCode".equals(methodName)) {
                return System.identityHashCode(invocation.getMock());
            }
            if ("equals".equals(methodName)) {
                return invocation.getMock() == invocation.getArgument(0);
            }
            return invocation.getMock();
        });
    }

    /**
     * 模拟提供商查询链路。
     *
     * @param providers 需要返回的提供商列表
     */
    private void stubProviderQuery(List<LlmProvider> providers) {
        LambdaQueryChainWrapper<LlmProvider> wrapper = mockProviderWrapper(providers);
        when(llmProviderService.lambdaQuery()).thenReturn(wrapper);
    }

    /**
     * 按调用顺序模拟模型查询链路。
     *
     * @param modelGroups 每次查询需要返回的模型列表
     */
    @SafeVarargs
    private final void stubModelQueries(List<LlmProviderModel>... modelGroups) {
        LambdaQueryChainWrapper<LlmProviderModel>[] wrappers = java.util.Arrays.stream(modelGroups)
                .map(this::mockModelWrapper)
                .toArray(LambdaQueryChainWrapper[]::new);
        when(llmProviderModelService.lambdaQuery()).thenReturn(wrappers[0], java.util.Arrays.copyOfRange(wrappers, 1,
                wrappers.length));
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

    private LlmProviderModel buildModel(String modelName,
                                        String modelType,
                                        Integer enabled,
                                        Integer supportReasoning,
                                        Integer supportVision) {
        return LlmProviderModel.builder()
                .providerId(1L)
                .modelName(modelName)
                .modelType(modelType)
                .enabled(enabled)
                .supportReasoning(supportReasoning)
                .supportVision(supportVision)
                .build();
    }

    private KbBase buildKnowledgeBase(String knowledgeName, String embeddingModel, Integer embeddingDim) {
        KbBase kbBase = new KbBase();
        kbBase.setKnowledgeName(knowledgeName);
        kbBase.setEmbeddingModel(embeddingModel);
        kbBase.setEmbeddingDim(embeddingDim);
        kbBase.setStatus(0);
        return kbBase;
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
}
