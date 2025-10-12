package cn.zhangchuangla.medicine.admin.ai.service;

import cn.zhangchuangla.medicine.admin.model.entity.ModelConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

@Component
@Slf4j
public class OpenAiClientFactory {

    private final ModelConfigService modelConfigService;

    private final AtomicReference<ChatClient> cachedChatClient = new AtomicReference<>();

    private final AtomicReference<ModelConfig> cachedModelConfig = new AtomicReference<>();

    public OpenAiClientFactory(ModelConfigService modelConfigService) {
        this.modelConfigService = modelConfigService;
    }

    /**
     * 创建OpenAI客户端
     *
     * @return ChatClient
     */
    public ChatClient chatClient() {
        ChatClient existing = cachedChatClient.get();
        if (existing != null) {
            return existing;
        }
        synchronized (this) {
            // 双重检查，避免并发重复创建
            existing = cachedChatClient.get();
            if (existing != null) {
                return existing;
            }
            ModelConfig cfg = modelConfigService.getChatConfig();
            ChatClient created = buildChatClient(cfg);
            cachedModelConfig.set(cfg);
            cachedChatClient.set(created);
            log.info("OpenAI ChatClient 初始化完成, model={}, baseUrl={}", cfg.getModel(), cfg.getBaseUrl());
            return created;
        }
    }

    /**
     * 清空缓存的客户端和配置信息
     * 用于后端更改模型配置后强制获取最新的客户端
     */
    public void clearCache() {
        synchronized (this) {
            cachedChatClient.set(null);
            cachedModelConfig.set(null);
            log.info("OpenAI ChatClient 缓存已清空");
        }
    }

    /**
     * 监听模型配置变更事件，刷新客户端
     */
    @EventListener
    public void onModelConfigUpdated(ModelConfigUpdatedEvent event) {
        ModelConfig newCfg = event.getModelConfig();
        ModelConfig oldCfg = cachedModelConfig.get();
        if (isSameConfig(oldCfg, newCfg)) {
            log.info("收到模型配置更新事件，但配置未变化，跳过刷新");
            return;
        }
        synchronized (this) {
            // 再次比较，防止重复刷新
            oldCfg = cachedModelConfig.get();
            if (isSameConfig(oldCfg, newCfg)) {
                return;
            }
            ChatClient newClient = buildChatClient(newCfg);
            cachedChatClient.set(newClient);
            cachedModelConfig.set(newCfg);
            log.info("OpenAI ChatClient 已刷新, model={}, baseUrl={}", newCfg.getModel(), newCfg.getBaseUrl());
        }
    }

    private boolean isSameConfig(ModelConfig a, ModelConfig b) {
        if (a == null || b == null) {
            return false;
        }
        return Objects.equals(a.getProvider(), b.getProvider())
                && Objects.equals(a.getModel(), b.getModel())
                && Objects.equals(a.getApiKey(), b.getApiKey())
                && Objects.equals(a.getBaseUrl(), b.getBaseUrl())
                && Objects.equals(a.getTemperature(), b.getTemperature())
                && Objects.equals(a.getMaxTokens(), b.getMaxTokens());
    }

    private ChatClient buildChatClient(ModelConfig config) {
        log.info("构建 OpenAI ChatClient, model={}, baseUrl={}", config.getModel(), config.getBaseUrl());
        OpenAiApi api = OpenAiApi.builder()
                .baseUrl(config.getBaseUrl())
                .apiKey(config.getApiKey())
                .build();
        OpenAiChatModel model = OpenAiChatModel.builder()
                .openAiApi(api)
                .defaultOptions(OpenAiChatOptions.builder()
                        .model(config.getModel())
                        .maxTokens(config.getMaxTokens())
                        .temperature(config.getTemperature())
                        .build())
                .build();
        return ChatClient.builder(model).build();
    }

}
