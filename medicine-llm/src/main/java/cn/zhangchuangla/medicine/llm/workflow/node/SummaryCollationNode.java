package cn.zhangchuangla.medicine.llm.workflow.node;

import cn.zhangchuangla.medicine.llm.exection.LLMParamException;
import cn.zhangchuangla.medicine.llm.prompt.DiagnosisWorkflowPrompt;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 摘要整理节点
 *
 * @author Chuang
 * <p>
 * created on 2025/12/2
 */
@Component
@RequiredArgsConstructor
public class SummaryCollationNode implements NodeAction {

    private static final String PROMPT = DiagnosisWorkflowPrompt.SUMMARY_COLLATION_PROMPT;
    private final ChatClient chatClient;

    @Override
    public Map<String, Object> apply(OverAllState state) {
        String originalText = String.valueOf(state.value("originalText"));
        String result = chatClient.prompt(PROMPT)
                .user(originalText)
                .call()
                .content();
        if (result == null) {
            throw new LLMParamException("摘要整理结果为空");
        }
        return Map.of("summary", result);
    }
}
