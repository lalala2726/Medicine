package cn.zhangchuangla.medicine.model.cache;

import lombok.Data;

import java.io.Serializable;

/**
 * 聊天标题生成 Agent 配置。
 */
@Data
public class ChatTitleAgentConfig implements Serializable {

    /**
     * 聊天标题模型配置
     */
    private AgentModelSlotConfig chatTitleModel;
}
