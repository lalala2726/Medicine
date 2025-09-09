package cn.zhangchuangla.medicine.llm.config;

import cn.zhangchuangla.medicine.model.entity.LlmConfig;
import cn.zhangchuangla.medicine.service.LlmConfigService;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * LLM模型工厂类
 * 使用HashMap存储多个厂商的LLM模型配置，支持热切换
 * 基于Spring AI官方最佳实践实现
 *
 * @author Chuang
 * @since 2025/9/6
 */
@Slf4j
@Component
public class LlmModelFactory {

    /**
     * 存储不同提供商的ChatClient实例
     */
    private final Map<String, ChatClient> chatClientMap = new ConcurrentHashMap<>();

    /**
     * 存储模型配置信息
     */
    private final Map<String, LlmProviderConfig> configMap = new ConcurrentHashMap<>();
    private final ChatModel chatModel;
    private final LlmConfigService llmConfigService;
    /**
     * 当前激活的模型提供商
     */
    @Getter
    private String currentProvider;

    public LlmModelFactory(ChatModel chatModel, LlmConfigService llmConfigService) {
        this.chatModel = chatModel;
        this.llmConfigService = llmConfigService;
        // 从数据库加载配置
        // todo 这边将配置信息放在Redis中
    }


    /**
     * 将数据库配置转换为内部配置格式
     *
     * @param dbConfig 数据库配置
     * @return 内部配置格式
     */
    private LlmProviderConfig convertToProviderConfig(LlmConfig dbConfig) {
        LlmProviderConfig providerConfig = new LlmProviderConfig();
        providerConfig.setProvider(dbConfig.getProvider());
        providerConfig.setModel(dbConfig.getModel());
        providerConfig.setApiKey(dbConfig.getApiKey());
        providerConfig.setBaseUrl(dbConfig.getBaseUrl());
        providerConfig.setEnabled(dbConfig.getStatus() != null && dbConfig.getStatus() == 1);
        return providerConfig;
    }

    /**
     * 初始化默认配置（备用方案）
     * 当数据库无法访问或无配置时使用
     */
    private void initializeDefaultConfigs() {
        log.info("使用默认LLM配置...");

        // DeepSeek配置（使用OpenAI兼容接口）
        LlmProviderConfig deepseekConfig = new LlmProviderConfig(
                "deepseek",
                "deepseek-chat",
                System.getenv("DEEPSEEK_API_KEY"),
                "https://api.deepseek.com"
        );
        configMap.put("deepseek", deepseekConfig);

        // 阿里云通义千问配置
        LlmProviderConfig qwenConfig = new LlmProviderConfig(
                "qwen",
                "qwen-max",
                System.getenv("OPENAI_API_KEY"),
                "https://dashscope.aliyuncs.com/compatible-mode/v1"
        );
        configMap.put("qwen", qwenConfig);

        // 设置默认提供商
        this.currentProvider = "deepseek";
    }

    /**
     * 获取当前激活的ChatClient
     *
     * @return 当前ChatClient实例
     */
    public ChatClient getCurrentChatClient() {
        return getChatClient(currentProvider);
    }

    /**
     * 获取指定提供商的ChatClient
     *
     * @param provider 提供商名称
     * @return ChatClient实例
     */
    public ChatClient getChatClient(String provider) {
        return chatClientMap.computeIfAbsent(provider, this::createChatClient);
    }

    /**
     * 创建ChatClient实例
     * 基于Spring AI官方推荐的实现方式
     *
     * @param provider 提供商名称
     * @return ChatClient实例
     */
    private ChatClient createChatClient(String provider) {
        try {
            LlmProviderConfig config = configMap.get(provider);
            if (config == null) {
                log.warn("未找到提供商 {} 的配置", provider);
                return null;
            }

            log.info("为提供商 {} 创建ChatClient实例", provider);

            // 目前使用同一个ChatModel，实际使用时可以根据配置切换
            // 使用Spring AI推荐的ChatClient创建方式
            return ChatClient.builder(chatModel).build();
        } catch (Exception e) {
            log.error("创建ChatClient失败，提供商: {}", provider, e);
            return null;
        }
    }

    /**
     * 切换到指定的模型提供商
     *
     * @param provider 提供商名称
     * @return 切换是否成功
     */
    public boolean switchProvider(String provider) {
        if (configMap.containsKey(provider)) {
            this.currentProvider = provider;
            log.info("已切换到模型提供商: {}", provider);
            return true;
        }
        log.warn("切换失败，不支持的模型提供商: {}", provider);
        return false;
    }

    /**
     * 获取所有可用的提供商列表
     *
     * @return 提供商列表
     */
    public java.util.Set<String> getAvailableProviders() {
        return configMap.keySet();
    }

    /**
     * 获取指定提供商的配置
     *
     * @param provider 提供商名称
     * @return 配置信息
     */
    public LlmProviderConfig getProviderConfig(String provider) {
        return configMap.get(provider);
    }

    /**
     * 添加或更新提供商配置
     *
     * @param provider 提供商名称
     * @param config   配置信息
     */
    public void addOrUpdateProviderConfig(String provider, LlmProviderConfig config) {
        configMap.put(provider, config);
        // 如果该提供商已有ChatClient实例，移除它以便下次重新创建
        chatClientMap.remove(provider);
        log.info("已更新提供商 {} 的配置", provider);
    }

    /**
     * 移除提供商配置
     *
     * @param provider 提供商名称
     * @return 移除是否成功
     */
    public boolean removeProvider(String provider) {
        LlmProviderConfig removed = configMap.remove(provider);
        chatClientMap.remove(provider);
        if (removed != null) {
            log.info("已移除提供商 {} 的配置", provider);
        }
        return removed != null;
    }

    /**
     * 获取所有提供商配置
     *
     * @return 配置映射
     */
    public Map<String, LlmProviderConfig> getAllProviderConfigs() {
        return new ConcurrentHashMap<>(configMap);
    }


    /**
     * 获取指定提供商的配置（直接从数据库查询）
     * 用于实时获取最新配置
     *
     * @param provider 提供商名称
     * @return 最新配置信息
     */
    public LlmProviderConfig getLatestProviderConfig(String provider) {
        try {
            // 从数据库获取最新配置
            LlmConfig dbConfig = llmConfigService.getLlmConfigByProvider(provider);
            if (dbConfig != null) {
                return convertToProviderConfig(dbConfig);
            }
        } catch (Exception e) {
            log.error("获取提供商 {} 的最新配置失败", provider, e);
        }
        return getProviderConfig(provider);
    }

}
