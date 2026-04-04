package cn.zhangchuangla.medicine.admin.model.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AgentConfigRequestJsonTests {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void knowledgeBaseRequest_ShouldDeserialize() throws Exception {
        String json = """
                {
                  "enabled": true,
                  "knowledgeNames": ["common_medicine_kb", "otc_guide_kb"],
                  "embeddingDim": 1024,
                  "embeddingModel": {
                    "modelName": "text-embedding-3-large",
                    "reasoningEnabled": false,
                    "maxTokens": 2048,
                    "temperature": 0.0
                  },
                  "topK": 10,
                  "rankingEnabled": false,
                  "rankingModel": null
                }
                """;

        KnowledgeBaseAgentConfigRequest request = objectMapper.readValue(json, KnowledgeBaseAgentConfigRequest.class);

        assertEquals(Boolean.TRUE, request.getEnabled());
        assertEquals(2, request.getKnowledgeNames().size());
        assertEquals("common_medicine_kb", request.getKnowledgeNames().getFirst());
        assertEquals(1024, request.getEmbeddingDim());
        assertNotNull(request.getEmbeddingModel());
        assertEquals(Boolean.FALSE, request.getEmbeddingModel().getReasoningEnabled());
        assertEquals("text-embedding-3-large", request.getEmbeddingModel().getModelName());
        assertEquals(10, request.getTopK());
        assertEquals(Boolean.FALSE, request.getRankingEnabled());
        assertNull(request.getRankingModel());
    }

    @Test
    void adminAssistantRequest_ShouldDeserialize() throws Exception {
        String json = """
                {
                  "adminNodeModel": {
                    "modelName": "qwen-max",
                    "reasoningEnabled": true,
                    "maxTokens": 8192,
                    "temperature": 0.7
                  }
                }
                """;

        AdminAssistantAgentConfigRequest request = objectMapper.readValue(json, AdminAssistantAgentConfigRequest.class);

        assertEquals("qwen-max", request.getAdminNodeModel().getModelName());
        assertEquals(8192, request.getAdminNodeModel().getMaxTokens());
    }

    @Test
    void clientAssistantRequest_ShouldDeserialize() throws Exception {
        String json = """
                {
                  "routeModel": {
                    "modelName": "gpt-4.1-mini",
                    "reasoningEnabled": false,
                    "maxTokens": 1024,
                    "temperature": 0.0
                  },
                  "businessNodeModel": {
                    "modelName": "gpt-4.1-mini",
                    "reasoningEnabled": false,
                    "maxTokens": 2048,
                    "temperature": 0.3
                  },
                  "chatModel": {
                    "modelName": "gpt-4.1",
                    "reasoningEnabled": true,
                    "maxTokens": 4096,
                    "temperature": 0.7
                  },
                  "diagnosisNodeModel": {
                    "modelName": "gpt-4.1",
                    "reasoningEnabled": true,
                    "maxTokens": 4096,
                    "temperature": 0.2
                  }
                }
                """;

        ClientAssistantAgentConfigRequest request = objectMapper.readValue(json, ClientAssistantAgentConfigRequest.class);

        assertEquals("gpt-4.1-mini", request.getRouteModel().getModelName());
        assertEquals("gpt-4.1-mini", request.getBusinessNodeModel().getModelName());
        assertEquals("gpt-4.1", request.getChatModel().getModelName());
        assertEquals("gpt-4.1", request.getDiagnosisNodeModel().getModelName());
        assertEquals(4096, request.getDiagnosisNodeModel().getMaxTokens());
    }

    @Test
    void commonCapabilityRequest_ShouldDeserialize() throws Exception {
        String json = """
                {
                  "imageRecognitionModel": {
                    "modelName": "qwen2.5-vl-72b-instruct",
                    "reasoningEnabled": true,
                    "maxTokens": 4096,
                    "temperature": 0.2
                  },
                  "chatHistorySummaryModel": {
                    "modelName": "gpt-4.1-mini",
                    "reasoningEnabled": false,
                    "maxTokens": 4096,
                    "temperature": 0.3
                  },
                  "chatTitleModel": {
                    "modelName": "gpt-4.1-mini",
                    "reasoningEnabled": false,
                    "maxTokens": 32,
                    "temperature": 0.2
                  }
                }
                """;

        CommonCapabilityAgentConfigRequest request = objectMapper.readValue(json, CommonCapabilityAgentConfigRequest.class);

        assertNotNull(request.getImageRecognitionModel());
        assertEquals("qwen2.5-vl-72b-instruct", request.getImageRecognitionModel().getModelName());
        assertEquals(Boolean.TRUE, request.getImageRecognitionModel().getReasoningEnabled());
        assertNotNull(request.getChatHistorySummaryModel());
        assertEquals("gpt-4.1-mini", request.getChatHistorySummaryModel().getModelName());
        assertEquals(4096, request.getChatHistorySummaryModel().getMaxTokens());
        assertNotNull(request.getChatTitleModel());
        assertEquals("gpt-4.1-mini", request.getChatTitleModel().getModelName());
        assertEquals(32, request.getChatTitleModel().getMaxTokens());
    }

    @Test
    void speechRequest_ShouldDeserialize() throws Exception {
        String json = """
                {
                  "appId": "speech-app-id",
                  "accessToken": "speech-token",
                  "textToSpeech": {
                    "voiceType": "zh_female_xiaohe_uranus_bigtts",
                    "maxTextChars": 300
                  }
                }
                """;

        SpeechAgentConfigRequest request = objectMapper.readValue(json, SpeechAgentConfigRequest.class);

        assertEquals("speech-app-id", request.getAppId());
        assertEquals("speech-token", request.getAccessToken());
        assertNotNull(request.getTextToSpeech());
        assertEquals("zh_female_xiaohe_uranus_bigtts", request.getTextToSpeech().getVoiceType());
        assertEquals(300, request.getTextToSpeech().getMaxTextChars());
    }
}
