package cn.zhangchuangla.medicine.admin.controller;

import cn.zhangchuangla.medicine.admin.model.request.*;
import cn.zhangchuangla.medicine.admin.model.vo.*;
import cn.zhangchuangla.medicine.admin.service.AgentConfigService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AgentConfigControllerTests {

    @Mock
    private AgentConfigService agentConfigService;

    @InjectMocks
    private AgentConfigController agentConfigController;

    @Test
    void getKnowledgeBaseConfig_ShouldDelegateToService() {
        KnowledgeBaseAgentConfigVo vo = new KnowledgeBaseAgentConfigVo();
        vo.setEmbeddingDim(1024);
        when(agentConfigService.getKnowledgeBaseConfig()).thenReturn(vo);

        var result = agentConfigController.getKnowledgeBaseConfig();

        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        assertEquals(1024, result.getData().getEmbeddingDim());
        verify(agentConfigService).getKnowledgeBaseConfig();
    }

    @Test
    void saveKnowledgeBaseConfig_ShouldDelegateToService() {
        KnowledgeBaseAgentConfigRequest request = new KnowledgeBaseAgentConfigRequest();
        request.setEmbeddingDim(1024);
        request.setEmbeddingModel(buildSelectionRequest("text-embedding-3-large", false, 2048, 0.0));
        when(agentConfigService.saveKnowledgeBaseConfig(request)).thenReturn(true);

        var result = agentConfigController.saveKnowledgeBaseConfig(request);

        assertEquals(200, result.getCode());
        verify(agentConfigService).saveKnowledgeBaseConfig(request);
    }

    @Test
    void getAdminAssistantConfig_ShouldDelegateToService() {
        AdminAssistantAgentConfigVo vo = new AdminAssistantAgentConfigVo();
        when(agentConfigService.getAdminAssistantConfig()).thenReturn(vo);

        var result = agentConfigController.getAdminAssistantConfig();

        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        verify(agentConfigService).getAdminAssistantConfig();
    }

    @Test
    void saveAdminAssistantConfig_ShouldDelegateToService() {
        AdminAssistantAgentConfigRequest request = new AdminAssistantAgentConfigRequest();
        request.setRouteModel(buildSelectionRequest("gpt-4.1-mini", false, 1024, 0.0));
        request.setBusinessNodeSimpleModel(buildSelectionRequest("gpt-4.1-mini", false, 2048, 0.3));
        request.setBusinessNodeComplexModel(buildSelectionRequest("gpt-4.1", true, 8192, 0.2));
        request.setChatModel(buildSelectionRequest("gpt-4.1", true, 8192, 0.7));
        when(agentConfigService.saveAdminAssistantConfig(request)).thenReturn(true);

        var result = agentConfigController.saveAdminAssistantConfig(request);

        assertEquals(200, result.getCode());
        verify(agentConfigService).saveAdminAssistantConfig(request);
    }

    @Test
    void getImageRecognitionConfig_ShouldDelegateToService() {
        ImageRecognitionAgentConfigVo vo = new ImageRecognitionAgentConfigVo();
        when(agentConfigService.getImageRecognitionConfig()).thenReturn(vo);

        var result = agentConfigController.getImageRecognitionConfig();

        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        verify(agentConfigService).getImageRecognitionConfig();
    }

    @Test
    void saveImageRecognitionConfig_ShouldDelegateToService() {
        ImageRecognitionAgentConfigRequest request = new ImageRecognitionAgentConfigRequest();
        request.setImageRecognitionModel(buildSelectionRequest("qwen-vl-max", true, 4096, 0.2));
        when(agentConfigService.saveImageRecognitionConfig(request)).thenReturn(true);

        var result = agentConfigController.saveImageRecognitionConfig(request);

        assertEquals(200, result.getCode());
        verify(agentConfigService).saveImageRecognitionConfig(request);
    }

    @Test
    void saveSpeechConfig_ShouldDelegateToService() {
        SpeechAgentConfigRequest request = new SpeechAgentConfigRequest();
        request.setAppId("app-id");
        request.setAccessToken("token");
        TextToSpeechConfigRequest textToSpeech = new TextToSpeechConfigRequest();
        textToSpeech.setVoiceType("zh_female_xiaohe_uranus_bigtts");
        textToSpeech.setMaxTextChars(300);
        request.setTextToSpeech(textToSpeech);
        when(agentConfigService.saveSpeechConfig(request)).thenReturn(true);

        var result = agentConfigController.saveSpeechConfig(request);

        assertEquals(200, result.getCode());
        verify(agentConfigService).saveSpeechConfig(request);
    }

    @Test
    void getChatHistorySummaryConfig_ShouldDelegateToService() {
        ChatHistorySummaryAgentConfigVo vo = new ChatHistorySummaryAgentConfigVo();
        when(agentConfigService.getChatHistorySummaryConfig()).thenReturn(vo);

        var result = agentConfigController.getChatHistorySummaryConfig();

        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        verify(agentConfigService).getChatHistorySummaryConfig();
    }

    @Test
    void saveChatHistorySummaryConfig_ShouldDelegateToService() {
        ChatHistorySummaryAgentConfigRequest request = new ChatHistorySummaryAgentConfigRequest();
        request.setChatHistorySummaryModel(buildSelectionRequest("gpt-4.1-mini", false, 4096, 0.3));
        when(agentConfigService.saveChatHistorySummaryConfig(request)).thenReturn(true);

        var result = agentConfigController.saveChatHistorySummaryConfig(request);

        assertEquals(200, result.getCode());
        verify(agentConfigService).saveChatHistorySummaryConfig(request);
    }

    @Test
    void getChatTitleConfig_ShouldDelegateToService() {
        ChatTitleAgentConfigVo vo = new ChatTitleAgentConfigVo();
        when(agentConfigService.getChatTitleConfig()).thenReturn(vo);

        var result = agentConfigController.getChatTitleConfig();

        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        verify(agentConfigService).getChatTitleConfig();
    }

    @Test
    void saveChatTitleConfig_ShouldDelegateToService() {
        ChatTitleAgentConfigRequest request = new ChatTitleAgentConfigRequest();
        request.setChatTitleModel(buildSelectionRequest("gpt-4.1-mini", false, 32, 0.2));
        when(agentConfigService.saveChatTitleConfig(request)).thenReturn(true);

        var result = agentConfigController.saveChatTitleConfig(request);

        assertEquals(200, result.getCode());
        verify(agentConfigService).saveChatTitleConfig(request);
    }

    @Test
    void listEmbeddingModelOptions_ShouldDelegateToService() {
        AgentModelOptionVo option = buildOption("text-embedding-3-large", false, false);
        when(agentConfigService.listEmbeddingModelOptions()).thenReturn(java.util.List.of(option));

        var result = agentConfigController.listEmbeddingModelOptions();

        assertEquals(200, result.getCode());
        assertEquals("text-embedding-3-large", result.getData().getFirst().getValue());
        assertEquals(Boolean.FALSE, result.getData().getFirst().getSupportReasoning());
        verify(agentConfigService).listEmbeddingModelOptions();
    }

    @Test
    void listRerankModelOptions_ShouldDelegateToService() {
        AgentModelOptionVo option = buildOption("gte-rerank-v2", false, false);
        when(agentConfigService.listRerankModelOptions()).thenReturn(java.util.List.of(option));

        var result = agentConfigController.listRerankModelOptions();

        assertEquals(200, result.getCode());
        assertEquals("gte-rerank-v2", result.getData().getFirst().getLabel());
        verify(agentConfigService).listRerankModelOptions();
    }

    @Test
    void listChatModelOptions_ShouldDelegateToService() {
        AgentModelOptionVo option = buildOption("gpt-4.1", true, false);
        when(agentConfigService.listChatModelOptions()).thenReturn(java.util.List.of(option));

        var result = agentConfigController.listChatModelOptions();

        assertEquals(200, result.getCode());
        assertEquals("gpt-4.1", result.getData().getFirst().getValue());
        assertEquals(Boolean.TRUE, result.getData().getFirst().getSupportReasoning());
        verify(agentConfigService).listChatModelOptions();
    }

    @Test
    void listVisionModelOptions_ShouldDelegateToService() {
        AgentModelOptionVo option = buildOption("qwen-vl-max", true, true);
        when(agentConfigService.listVisionModelOptions()).thenReturn(java.util.List.of(option));

        var result = agentConfigController.listVisionModelOptions();

        assertEquals(200, result.getCode());
        assertEquals("qwen-vl-max", result.getData().getFirst().getLabel());
        assertEquals(Boolean.TRUE, result.getData().getFirst().getSupportVision());
        verify(agentConfigService).listVisionModelOptions();
    }

    private AgentModelSelectionRequest buildSelectionRequest(String modelName,
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

    private AgentModelOptionVo buildOption(String value, boolean supportReasoning, boolean supportVision) {
        AgentModelOptionVo option = new AgentModelOptionVo();
        option.setLabel(value);
        option.setValue(value);
        option.setSupportReasoning(supportReasoning);
        option.setSupportVision(supportVision);
        return option;
    }
}
