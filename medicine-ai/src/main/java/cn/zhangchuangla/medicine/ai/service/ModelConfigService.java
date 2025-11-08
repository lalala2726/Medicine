package cn.zhangchuangla.medicine.ai.service;

import cn.zhangchuangla.medicine.model.dto.ModelConfigDto;
import cn.zhangchuangla.medicine.model.entity.ModelConfig;

/**
 * @author Chuang
 * <p>
 * created on 2025/9/9 
 */
public interface ModelConfigService {

    /**
     * Redis key storing the active chat configuration.
     */
    String CHAT_CONFIG_KEY = "llm:config";

    /**
     * Update the active LLM chat configuration.
     *
     * @param modelConfigDto configuration payload coming from admin UI
     */
    void updateChatConfig(ModelConfigDto modelConfigDto);

    /**
     * Fetch the currently active LLM chat configuration.
     *
     * @return active model configuration
     */
    ModelConfig getChatConfig();
}
