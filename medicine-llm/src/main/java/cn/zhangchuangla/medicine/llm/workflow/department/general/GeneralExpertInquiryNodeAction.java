package cn.zhangchuangla.medicine.llm.workflow.department.general;

import cn.zhangchuangla.medicine.llm.exection.LLMParamException;
import cn.zhangchuangla.medicine.llm.prompt.DiagnosisWorkflowPrompt;
import cn.zhangchuangla.medicine.llm.workflow.support.WorkflowStateKeys;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 综合专家追问节点（General Expert Inquiry）
 * <p>
 * 负责：当科室不明确时生成通用追问要点，并累加追问轮次，补充到摘要中供后续诊断使用。
 */
@Component
@RequiredArgsConstructor
public class GeneralExpertInquiryNodeAction implements NodeAction {

    private static final String PROMPT = DiagnosisWorkflowPrompt.GENERAL_EXPERT_INQUIRY_PROMPT;
    private final ChatClient chatClient;

    @Override
    public Map<String, Object> apply(OverAllState state) {
        String summary = state.value(WorkflowStateKeys.SUMMARY, String.class).orElse("");
        String inquiryNotes = chatClient.prompt(PROMPT)
                .user(summary)
                .call()
                .content();
        if (inquiryNotes == null) {
            throw new LLMParamException("综合追问结果为空");
        }

        int nextRound = nextRound(state);
        return Map.of(
                WorkflowStateKeys.INQUIRY_QUESTIONS, inquiryNotes,
                WorkflowStateKeys.INQUIRY_ROUND, nextRound
        );
    }

    private int nextRound(OverAllState state) {
        Integer value = state.value(WorkflowStateKeys.INQUIRY_ROUND, Integer.class).orElse(0);
        return value + 1;
    }
}
