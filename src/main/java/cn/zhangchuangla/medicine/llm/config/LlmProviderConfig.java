package cn.zhangchuangla.medicine.llm.config;

import lombok.Data;

/**
 * LLM模型配置类
 * 
 * @author Chuang
 * @since 2025/9/6
 */
@Data
public class LlmProviderConfig {
    
    /**
     * 模型提供商名称
     */
    private String provider;
    
    /**
     * 模型名称
     */
    private String model;
    
    /**
     * API密钥
     */
    private String apiKey;
    
    /**
     * API基础URL
     */
    private String baseUrl;
    
    
    /**
     * 是否启用
     */
    private Boolean enabled = true;
    
    public LlmProviderConfig() {}
    
    public LlmProviderConfig(String provider, String model, String apiKey, String baseUrl) {
        this.provider = provider;
        this.model = model;
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
    }
}