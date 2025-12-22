package cn.zhangchuangla.medicine.llm.workflow.node.review;

import cn.zhangchuangla.medicine.llm.prompt.DiagnosisWorkflowPrompt;
import cn.zhangchuangla.medicine.llm.workflow.support.WorkflowStateKeys;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.Map;

/**
 * 诊断审核节点（Diagnosis Review / Risk Control）
 * <p>
 * 负责：对上游诊断建议进行合规与安全审核/改写，并输出最终可发送给用户的流式结果。
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DiagnosisReviewNodeAction implements NodeAction {

    private static final String PROMPT = DiagnosisWorkflowPrompt.REVIEW_PROMPT;
    private final ChatClient chatClient;

    @Override
    public Map<String, Object> apply(OverAllState state) {
        log.info("【工作流】进入节点：{}", WorkflowStateKeys.NODE_DIAGNOSIS_REVIEW);
        String diagnosisResult = state.value(WorkflowStateKeys.DIAGNOSIS_RESULT, String.class).orElse("");
        Flux<ChatResponse> responseFlux = chatClient.prompt(PROMPT)
                .user(diagnosisResult)
                .stream()
                .chatResponse();
        return Map.of(WorkflowStateKeys.FINAL_RESULT, responseFlux);
    }
}
