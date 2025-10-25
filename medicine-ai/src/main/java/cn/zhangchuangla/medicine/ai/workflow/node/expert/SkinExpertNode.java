package cn.zhangchuangla.medicine.ai.workflow.node.expert;

import cn.zhangchuangla.medicine.ai.factory.OpenAiClientFactory;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;

import java.util.Map;

/**
 * 皮肤科专家节点
 *
 * @author Chuang
 * <p>
 * created on 2025/10/25 05:06
 */
public class SkinExpertNode implements NodeAction {

    private final OpenAiClientFactory openAiClientFactory;

    public SkinExpertNode(OpenAiClientFactory openAiClientFactory) {
        this.openAiClientFactory = openAiClientFactory;
    }

    @Override
    public Map<String, Object> apply(OverAllState state) {
        return Map.of();
    }
}
