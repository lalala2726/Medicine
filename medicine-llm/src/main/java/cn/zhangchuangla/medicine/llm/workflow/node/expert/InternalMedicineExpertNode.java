package cn.zhangchuangla.medicine.llm.workflow.node.expert;

import cn.zhangchuangla.medicine.llm.exection.LLMParamException;
import cn.zhangchuangla.medicine.llm.prompt.DiagnosisWorkflowPrompt;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 内科专家节点
 *
 * @author Chuang
 * <p>
 * created on 2025/11/20
 */
@Component
@RequiredArgsConstructor
public class InternalMedicineExpertNode implements NodeAction {

    private static final String PROMPT = DiagnosisWorkflowPrompt.INTERNAL_MEDICINE_EXPERT_PROMPT;
    private final ChatClient chatClient;

    @Override
    public Map<String, Object> apply(OverAllState state) throws Exception {
        String summer = String.valueOf(state.value("summer"));
        String content = chatClient.prompt(PROMPT)
                .user(summer)
                .call()
                .content();
        if (content == null) {
            throw new LLMParamException("诊断为空");
        }
        return Map.of("diagnosisResult", content);
    }
}
