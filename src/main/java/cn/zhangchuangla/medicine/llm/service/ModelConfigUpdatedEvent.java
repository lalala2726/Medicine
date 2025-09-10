package cn.zhangchuangla.medicine.llm.service;

import cn.zhangchuangla.medicine.model.entity.ModelConfig;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * 模型配置更新事件
 */
@Getter
public class ModelConfigUpdatedEvent extends ApplicationEvent {

    private final ModelConfig modelConfig;

    public ModelConfigUpdatedEvent(Object source, ModelConfig modelConfig) {
        super(source);
        this.modelConfig = modelConfig;
    }
}
