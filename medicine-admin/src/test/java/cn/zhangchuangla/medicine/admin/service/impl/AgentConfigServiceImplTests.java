package cn.zhangchuangla.medicine.admin.service.impl;

import cn.zhangchuangla.medicine.admin.model.request.AgentModelSelectionRequest;
import cn.zhangchuangla.medicine.admin.model.request.KnowledgeBaseAgentConfigRequest;
import cn.zhangchuangla.medicine.admin.model.request.SpeechAgentConfigRequest;
import cn.zhangchuangla.medicine.admin.model.request.TextToSpeechConfigRequest;
import cn.zhangchuangla.medicine.admin.service.AgentConfigRuntimeSyncService;
import cn.zhangchuangla.medicine.admin.service.LlmProviderModelService;
import cn.zhangchuangla.medicine.admin.service.LlmProviderService;
import cn.zhangchuangla.medicine.model.cache.*;
import cn.zhangchuangla.medicine.model.constants.LlmModelTypeConstants;
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
    private LlmProviderService llmProviderService;

    @Mock
    private LlmProviderModelService llmProviderModelService;

    @Mock
    private AgentConfigRuntimeSyncService agentConfigRuntimeSyncService;

    @Test
    void getKnowledgeBaseConfig_ShouldMapSlotAndLookupCapabilitiesFromDatabase() {
        AgentConfigServiceImpl service = newService();
        AgentAllConfigCache cache = new AgentAllConfigCache();
        KnowledgeBaseAgentConfig knowledgeBase = new KnowledgeBaseAgentConfig();
        knowledgeBase.setEmbeddingDim(1024);
        knowledgeBase.setEmbeddingModel(buildSlot("text-embedding-3-large", false, 2048, 0.0));
        knowledgeBase.setRerankModel(buildSlot("gte-rerank-v2", false, 512, 0.0));
        cache.setKnowledgeBase(knowledgeBase);
        when(agentConfigRuntimeSyncService.readCache()).thenReturn(cache);
        when(llmProviderService.lambdaQuery()).thenReturn(mockProviderWrapper(List.of(buildEnabledProvider())));
        when(llmProviderModelService.lambdaQuery()).thenReturn(
                mockModelWrapper(List.of(buildModel("text-embedding-3-large", LlmModelTypeConstants.EMBEDDING, 0, 0, 0))),
                mockModelWrapper(List.of(buildModel("gte-rerank-v2", LlmModelTypeConstants.RERANK, 0, 0, 0)))
        );

        var result = service.getKnowledgeBaseConfig();

        assertEquals(1024, result.getEmbeddingDim());
        assertEquals("text-embedding-3-large", result.getEmbeddingModel().getModelName());
        assertEquals(Boolean.FALSE, result.getEmbeddingModel().getSupportReasoning());
        assertEquals(Boolean.FALSE, result.getEmbeddingModel().getSupportVision());
        assertEquals("gte-rerank-v2", result.getRerankModel().getModelName());
    }

    @Test
    void getImageRecognitionConfig_ShouldPopulateVisionCapabilitiesFromDatabase() {
        AgentConfigServiceImpl service = newService();
        AgentAllConfigCache cache = new AgentAllConfigCache();
        ImageRecognitionAgentConfig imageRecognition = new ImageRecognitionAgentConfig();
        imageRecognition.setImageRecognitionModel(buildSlot("qwen-vl-max", true, 4096, 0.2));
        cache.setImageRecognition(imageRecognition);
        when(agentConfigRuntimeSyncService.readCache()).thenReturn(cache);
        when(llmProviderService.lambdaQuery()).thenReturn(mockProviderWrapper(List.of(buildEnabledProvider())));
        when(llmProviderModelService.lambdaQuery()).thenReturn(
                mockModelWrapper(List.of(buildModel("qwen-vl-max", LlmModelTypeConstants.CHAT, 0, 1, 1)))
        );

        var result = service.getImageRecognitionConfig();

        assertEquals("qwen-vl-max", result.getImageRecognitionModel().getModelName());
        assertEquals(Boolean.TRUE, result.getImageRecognitionModel().getSupportReasoning());
        assertEquals(Boolean.TRUE, result.getImageRecognitionModel().getSupportVision());
    }

    @Test
    void saveKnowledgeBaseConfig_ShouldWriteFlattenedSlotConfig() {
        AgentConfigServiceImpl service = newService();
        when(agentConfigRuntimeSyncService.readCache()).thenReturn(new AgentAllConfigCache());
        when(llmProviderService.lambdaQuery()).thenReturn(mockProviderWrapper(List.of(buildEnabledProvider())));
        when(llmProviderModelService.lambdaQuery()).thenReturn(
                mockModelWrapper(List.of(buildModel("text-embedding-3-large", LlmModelTypeConstants.EMBEDDING, 0, 0, 0))),
                mockModelWrapper(List.of(buildModel("gte-rerank-v2", LlmModelTypeConstants.RERANK, 0, 0, 0)))
        );

        KnowledgeBaseAgentConfigRequest request = new KnowledgeBaseAgentConfigRequest();
        request.setEmbeddingDim(1024);
        request.setEmbeddingModel(buildSelection("text-embedding-3-large", false, 2048, 0.0));
        request.setRerankModel(buildSelection("gte-rerank-v2", false, 512, 0.0));

        boolean result = service.saveKnowledgeBaseConfig(request);

        assertTrue(result);
        ArgumentCaptor<AgentAllConfigCache> cacheCaptor = ArgumentCaptor.forClass(AgentAllConfigCache.class);
        ArgumentCaptor<LlmProvider> providerCaptor = ArgumentCaptor.forClass(LlmProvider.class);
        verify(agentConfigRuntimeSyncService).saveCache(cacheCaptor.capture(), providerCaptor.capture(), any());
        KnowledgeBaseAgentConfig saved = cacheCaptor.getValue().getKnowledgeBase();
        assertEquals("text-embedding-3-large", saved.getEmbeddingModel().getModelName());
        assertEquals(Boolean.FALSE, saved.getEmbeddingModel().getReasoningEnabled());
        assertEquals(2048, saved.getEmbeddingModel().getMaxTokens());
        assertEquals("gte-rerank-v2", saved.getRerankModel().getModelName());
        assertEquals("openai", providerCaptor.getValue().getProviderType());
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
        when(llmProviderService.lambdaQuery()).thenReturn(mockProviderWrapper(List.of()));

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
                llmProviderService,
                llmProviderModelService,
                agentConfigRuntimeSyncService
        );
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
