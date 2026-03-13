package cn.zhangchuangla.medicine.admin.model.cache;

import cn.zhangchuangla.medicine.model.cache.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AgentAllConfigCacheJsonTests {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void agentAllConfigCache_ShouldSerializeToSchemaVersion2Structure() throws Exception {
        AgentAllConfigCache cache = new AgentAllConfigCache();
        cache.setUpdatedAt("2026-03-13T10:30:00+08:00");
        cache.setUpdatedBy("admin");
        cache.setLlm(buildLlmConfig());
        cache.setKnowledgeBase(buildKnowledgeBaseConfig());
        cache.setAdminAssistant(buildAdminAssistantConfig());
        cache.setImageRecognition(buildImageRecognitionConfig());
        cache.setChatHistorySummary(buildChatHistorySummaryConfig());
        cache.setChatTitle(buildChatTitleConfig());
        cache.setSpeech(buildSpeechConfig());

        String json = objectMapper.writeValueAsString(cache);
        JsonNode root = objectMapper.readTree(json);

        assertEquals(2, root.path("schemaVersion").asInt());
        assertTrue(root.has("llm"));
        assertTrue(root.has("agentConfigs"));
        assertTrue(root.has("speech"));
        assertFalse(root.has("knowledgeBase"));
        assertFalse(root.has("adminAssistant"));
        assertFalse(root.has("imageRecognition"));
        assertFalse(root.has("chatHistorySummary"));
        assertFalse(root.has("chatTitle"));

        assertEquals("openai", root.path("llm").path("providerType").asText());
        assertEquals("https://api.openai.com/v1", root.path("llm").path("baseUrl").asText());
        assertEquals("sk-llm", root.path("llm").path("apiKey").asText());
        assertEquals("text-embedding-3-large",
                root.path("agentConfigs").path("knowledgeBase").path("embeddingModel").path("modelName").asText());
        assertEquals("gpt-4.1-mini",
                root.path("agentConfigs").path("adminAssistant").path("chatModel").path("modelName").asText());
        assertEquals("qwen2.5-vl-72b-instruct",
                root.path("agentConfigs").path("imageRecognition").path("imageRecognitionModel").path("modelName").asText());
        assertEquals("gpt-4.1-mini",
                root.path("agentConfigs").path("chatHistorySummary").path("chatHistorySummaryModel").path("modelName").asText());
        assertEquals(32,
                root.path("agentConfigs").path("chatTitle").path("chatTitleModel").path("maxTokens").asInt());
        assertEquals("seed-tts-2.0",
                root.path("speech").path("textToSpeech").path("resourceId").asText());

        assertFalse(json.contains("\"providerId\""));
        assertFalse(json.contains("\"modelId\""));
        assertFalse(json.contains("\"enabled\""));
        assertFalse(json.contains("\"supportReasoning\""));
        assertFalse(json.contains("\"supportVision\""));
        assertFalse(json.contains("\"modelType\""));
        assertFalse(root.path("agentConfigs").path("knowledgeBase").path("embeddingModel").has("model"));
    }

    private AgentLlmConfig buildLlmConfig() {
        AgentLlmConfig config = new AgentLlmConfig();
        config.setProviderType("openai");
        config.setBaseUrl("https://api.openai.com/v1");
        config.setApiKey("sk-llm");
        return config;
    }

    private KnowledgeBaseAgentConfig buildKnowledgeBaseConfig() {
        KnowledgeBaseAgentConfig config = new KnowledgeBaseAgentConfig();
        config.setEmbeddingDim(1024);
        config.setEmbeddingModel(buildSlot("text-embedding-3-large", false, 2048, 0.0));
        config.setRerankModel(buildSlot("gte-rerank-v2", false, 512, 0.0));
        return config;
    }

    private AdminAssistantAgentConfig buildAdminAssistantConfig() {
        AdminAssistantAgentConfig config = new AdminAssistantAgentConfig();
        config.setRouteModel(buildSlot("gpt-4.1-mini", false, 1024, 0.0));
        config.setBusinessNodeSimpleModel(buildSlot("gpt-4.1-mini", false, 2048, 0.3));
        config.setBusinessNodeComplexModel(buildSlot("gpt-4.1", true, 8192, 0.2));
        config.setChatModel(buildSlot("gpt-4.1-mini", true, 8192, 0.7));
        return config;
    }

    private ImageRecognitionAgentConfig buildImageRecognitionConfig() {
        ImageRecognitionAgentConfig config = new ImageRecognitionAgentConfig();
        config.setImageRecognitionModel(buildSlot("qwen2.5-vl-72b-instruct", true, 4096, 0.2));
        return config;
    }

    private ChatHistorySummaryAgentConfig buildChatHistorySummaryConfig() {
        ChatHistorySummaryAgentConfig config = new ChatHistorySummaryAgentConfig();
        config.setChatHistorySummaryModel(buildSlot("gpt-4.1-mini", false, 4096, 0.3));
        return config;
    }

    private ChatTitleAgentConfig buildChatTitleConfig() {
        ChatTitleAgentConfig config = new ChatTitleAgentConfig();
        config.setChatTitleModel(buildSlot("gpt-4.1-mini", false, 32, 0.2));
        return config;
    }

    private SpeechAgentConfig buildSpeechConfig() {
        SpeechAgentConfig config = new SpeechAgentConfig();
        config.setProvider("volcengine");
        config.setAppId("app-id");
        config.setAccessToken("access-token");
        SpeechRecognitionAgentConfig speechRecognition = new SpeechRecognitionAgentConfig();
        speechRecognition.setResourceId("volc.seedasr.sauc.duration");
        config.setSpeechRecognition(speechRecognition);
        TextToSpeechAgentConfig textToSpeech = new TextToSpeechAgentConfig();
        textToSpeech.setResourceId("seed-tts-2.0");
        textToSpeech.setVoiceType("zh_female_xiaohe_uranus_bigtts");
        textToSpeech.setMaxTextChars(300);
        config.setTextToSpeech(textToSpeech);
        return config;
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
}
