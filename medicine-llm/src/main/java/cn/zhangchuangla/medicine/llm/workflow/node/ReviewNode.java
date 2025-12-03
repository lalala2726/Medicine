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
 * @author Chuang
 * <p>
 * created on 2025/12/2
 */
@Component
@RequiredArgsConstructor
public class ReviewNode implements NodeAction {

    public static final String PROMPT = DiagnosisWorkflowPrompt.REVIEW_PROMPT;
    private final ChatClient chatClient;

    @Override
    public Map<String, Object> apply(OverAllState state) throws Exception {
        String diagnosisResult = String.valueOf(state.value("diagnosisResult"));
        String content = chatClient.prompt(PROMPT)
                .user(diagnosisResult)
                .call()
                .content();
        if (content == null) {
            throw new LLMParamException("审核输入内容为空为空!");
        }
        return Map.of("finalResult", content);
    }
}
