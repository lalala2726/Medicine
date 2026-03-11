package cn.zhangchuangla.medicine.model.cache;

import lombok.Data;

import java.io.Serializable;

/**
 * Agent 模型运行时配置。
 * <p>
 * 该对象仅保留 Agent 端实际调用模型所需的最小运行时字段。
 */
@Data
public class AgentModelRuntimeConfig implements Serializable {

    /**
     * 提供商标识
     */
    private String provider;

    /**
     * 模型标识
     */
    private String model;

    /**
     * 模型类型
     */
    private String modelType;

    /**
     * 调用基础地址
     */
    private String baseUrl;

    /**
     * 提供商 API Key
     */
    private String apiKey;

    /**
     * 是否支持深度思考
     */
    private Boolean supportReasoning;

    /**
     * 是否支持图片识别
     */
    private Boolean supportVision;
}
