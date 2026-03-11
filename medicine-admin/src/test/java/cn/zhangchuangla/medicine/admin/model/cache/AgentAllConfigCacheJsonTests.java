package cn.zhangchuangla.medicine.admin.model.cache;

import cn.zhangchuangla.medicine.model.cache.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AgentAllConfigCacheJsonTests {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void agentAllConfigCache_ShouldSerializeToExpectedStructure() throws Exception {
        AgentAllConfigCache cache = new AgentAllConfigCache();
        cache.setUpdatedAt("2026-03-11T10:30:00+08:00");
        cache.setUpdatedBy("admin");
        cache.setKnowledgeBase(buildKnowledgeBaseConfig());
        cache.setAdminAssistant(buildAdminAssistantConfig());
        cache.setImageRecognition(buildImageRecognitionConfig());
        cache.setChatHistorySummary(buildChatHistorySummaryConfig());
        cache.setChatTitle(buildChatTitleConfig());

        String json = objectMapper.writeValueAsString(cache);
        JsonNode root = objectMapper.readTree(json);

        assertTrue(root.has("knowledgeBase"));
        assertTrue(root.has("adminAssistant"));
        assertTrue(root.has("imageRecognition"));
        assertTrue(root.has("chatHistorySummary"));
        assertTrue(root.has("chatTitle"));
        assertEquals("EMBEDDING", root.path("knowledgeBase").path("embeddingModel").path("model").path("modelType").asText());
        assertEquals("CHAT", root.path("adminAssistant").path("chatModel").path("model").path("modelType").asText());
        assertEquals("qwen2.5-vl-72b-instruct",
                root.path("imageRecognition").path("imageRecognitionModel").path("model").path("model").asText());
        assertEquals("gpt-4.1-mini",
                root.path("chatHistorySummary").path("chatHistorySummaryModel").path("model").path("model").asText());
        assertEquals(32,
                root.path("chatTitle").path("chatTitleModel").path("maxTokens").asInt());
        assertFalse(json.contains("\"providerId\""));
        assertFalse(json.contains("\"modelId\""));
        assertFalse(json.contains("\"enabled\""));
    }

    private KnowledgeBaseAgentConfig buildKnowledgeBaseConfig() {
        KnowledgeBaseAgentConfig config = new KnowledgeBaseAgentConfig();
        config.setEmbeddingDim(1024);
        config.setEmbeddingModel(buildSlot("openai", "text-embedding-3-large", "EMBEDDING",
                "https://api.openai.com/v1", "sk-embedding", false, false, false, 2048, 0.0));
        config.setRerankModel(buildSlot("qwen", "gte-rerank-v2", "RERANK",
                "https://dashscope.aliyuncs.com/compatible-mode/v1", "sk-rerank", false, false, false, 512, 0.0));
        return config;
    }

    private AdminAssistantAgentConfig buildAdminAssistantConfig() {
        AdminAssistantAgentConfig config = new AdminAssistantAgentConfig();
        config.setRouteModel(buildSlot("openai", "gpt-4.1-mini", "CHAT",
                "https://api.openai.com/v1", "sk-route", true, false, false, 1024, 0.0));
        config.setBusinessNodeSimpleModel(buildSlot("openai", "gpt-4.1-mini", "CHAT",
                "https://api.openai.com/v1", "sk-simple", true, false, false, 2048, 0.3));
        config.setBusinessNodeComplexModel(buildSlot("openai", "gpt-4.1", "CHAT",
                "https://api.openai.com/v1", "sk-complex", true, false, true, 8192, 0.2));
        config.setChatModel(buildSlot("qwen", "qwen-max", "CHAT",
                "https://dashscope.aliyuncs.com/compatible-mode/v1", "sk-chat", true, true, true, 8192, 0.7));
        return config;
    }

    private ImageRecognitionAgentConfig buildImageRecognitionConfig() {
        ImageRecognitionAgentConfig config = new ImageRecognitionAgentConfig();
        config.setImageRecognitionModel(buildSlot("qwen", "qwen2.5-vl-72b-instruct", "CHAT",
                "https://dashscope.aliyuncs.com/compatible-mode/v1", "sk-recognition", true, true, true, 4096, 0.2));
        return config;
    }

    private ChatHistorySummaryAgentConfig buildChatHistorySummaryConfig() {
        ChatHistorySummaryAgentConfig config = new ChatHistorySummaryAgentConfig();
        config.setChatHistorySummaryModel(buildSlot("openai", "gpt-4.1-mini", "CHAT",
                "https://api.openai.com/v1", "sk-summary", true, false, false, 4096, 0.3));
        return config;
    }

    private ChatTitleAgentConfig buildChatTitleConfig() {
        ChatTitleAgentConfig config = new ChatTitleAgentConfig();
        config.setChatTitleModel(buildSlot("openai", "gpt-4.1-mini", "CHAT",
                "https://api.openai.com/v1", "sk-title", true, false, false, 32, 0.2));
        return config;
    }

    private AgentModelSlotConfig buildSlot(String provider, String model, String modelType, String baseUrl, String apiKey,
                                           boolean supportReasoning, boolean supportVision, boolean reasoningEnabled,
                                           Integer maxTokens, Double temperature) {
        AgentModelRuntimeConfig runtimeConfig = new AgentModelRuntimeConfig();
        runtimeConfig.setProvider(provider);
        runtimeConfig.setModel(model);
        runtimeConfig.setModelType(modelType);
        runtimeConfig.setBaseUrl(baseUrl);
        runtimeConfig.setApiKey(apiKey);
        runtimeConfig.setSupportReasoning(supportReasoning);
        runtimeConfig.setSupportVision(supportVision);

        AgentModelSlotConfig slotConfig = new AgentModelSlotConfig();
        slotConfig.setReasoningEnabled(reasoningEnabled);
        slotConfig.setMaxTokens(maxTokens);
        slotConfig.setTemperature(temperature);
        slotConfig.setModel(runtimeConfig);
        return slotConfig;
    }
}
