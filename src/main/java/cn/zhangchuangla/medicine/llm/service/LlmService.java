package cn.zhangchuangla.medicine.llm.service;

import cn.zhangchuangla.medicine.llm.config.LlmModelFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * LLM服务类
 * 提供多厂商模型对话服务
 *
 * @author Chuang
 * @since 2025/9/6
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LlmService {

    private final LlmModelFactory modelFactory;

    /**
     * 使用当前激活的模型进行对话
     *
     * @param message 用户消息
     * @return 模型回复
     */
    public String chat(String message) {
        try {
            ChatClient chatClient = modelFactory.getCurrentChatClient();
            if (chatClient == null) {
                return String.format("当前模型 %s 暂未配置，请检查配置信息", modelFactory.getCurrentProvider());
            }

            return chatClient.prompt()
                    .user(message)
                    .call()
                    .content();
        } catch (Exception e) {
            log.error("LLM对话失败", e);
            return "对话失败：" + e.getMessage();
        }
    }

    /**
     * 使用指定模型进行对话
     *
     * @param provider 提供商名称
     * @param message  用户消息
     * @return 模型回复
     */
    public String chatWithProvider(String provider, String message) {
        try {
            if (!modelFactory.getAvailableProviders().contains(provider)) {
                return String.format("不支持的模型提供商: %s", provider);
            }

            ChatClient chatClient = modelFactory.getChatClient(provider);
            if (chatClient == null) {
                return String.format("模型 %s 暂未配置，请检查配置信息", provider);
            }

            return chatClient.prompt()
                    .user(message)
                    .call()
                    .content();
        } catch (Exception e) {
            log.error("LLM对话失败，提供商: {}", provider, e);
            return "对话失败：" + e.getMessage();
        }
    }

    /**
     * 切换模型提供商
     *
     * @param provider 提供商名称
     * @return 切换结果
     */
    public String switchProvider(String provider) {
        boolean success = modelFactory.switchProvider(provider);
        if (success) {
            return String.format("已切换到模型: %s", provider);
        } else {
            return String.format("切换失败，不支持的模型: %s", provider);
        }
    }

    /**
     * 获取当前激活的模型提供商
     *
     * @return 当前提供商名称
     */
    public String getCurrentProvider() {
        return modelFactory.getCurrentProvider();
    }

    /**
     * 获取所有可用的模型提供商
     *
     * @return 提供商列表
     */
    public java.util.Set<String> getAvailableProviders() {
        return modelFactory.getAvailableProviders();
    }

    /**
     * 获取所有模型配置信息
     *
     * @return 配置信息映射
     */
    public java.util.Map<String, Object> getAllProviderConfigs() {
        Map<String, Object> result = new HashMap<>();
        modelFactory.getAllProviderConfigs().forEach((key, config) -> {
            Map<String, Object> configInfo = new HashMap<>();
            configInfo.put("provider", config.getProvider());
            configInfo.put("model", config.getModel());
            configInfo.put("baseUrl", config.getBaseUrl());
            configInfo.put("maxTokens", config.getMaxTokens());
            configInfo.put("temperature", config.getTemperature());
            configInfo.put("enabled", config.getEnabled());
            result.put(key, configInfo);
        });
        return result;
    }
}
