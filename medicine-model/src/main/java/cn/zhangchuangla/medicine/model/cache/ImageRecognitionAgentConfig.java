package cn.zhangchuangla.medicine.model.cache;

import lombok.Data;

import java.io.Serializable;

/**
 * 图片识别 Agent 配置。
 */
@Data
public class ImageRecognitionAgentConfig implements Serializable {

    /**
     * 图片识别模型配置
     */
    private AgentModelSlotConfig imageRecognitionModel;
}
