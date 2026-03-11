package cn.zhangchuangla.medicine.model.cache;

import lombok.Data;

import java.io.Serializable;

/**
 * Agent 模型槽位配置。
 * <p>
 * 用于表达某个业务场景槽位最终使用的模型及运行参数。
 */
@Data
public class AgentModelSlotConfig implements Serializable {

    /**
     * 是否开启深度思考
     */
    private Boolean reasoningEnabled;

    /**
     * 最大 token 数
     */
    private Integer maxTokens;

    /**
     * 模型温度
     */
    private Double temperature;

    /**
     * 绑定的运行时模型配置
     */
    private AgentModelRuntimeConfig model;
}
