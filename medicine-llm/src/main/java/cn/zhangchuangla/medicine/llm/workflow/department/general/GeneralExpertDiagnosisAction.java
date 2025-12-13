package cn.zhangchuangla.medicine.llm.workflow.department.general;

import cn.zhangchuangla.medicine.llm.exection.LLMParamException;
import cn.zhangchuangla.medicine.llm.prompt.DiagnosisWorkflowPrompt;
import cn.zhangchuangla.medicine.llm.workflow.support.DepartmentDiagnosisAction;
import cn.zhangchuangla.medicine.llm.workflow.support.WorkflowStateKeys;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

/**
 * 综合诊断实现（General Expert Diagnosis）
 * <p>
 * 负责：当科室不明确或兜底时，基于摘要生成综合门诊方向的谨慎诊断与就医建议。
 */
@Component(WorkflowStateKeys.ROUTE_GENERAL)
@RequiredArgsConstructor
public class GeneralExpertDiagnosisAction implements DepartmentDiagnosisAction {

    private static final String PROMPT = DiagnosisWorkflowPrompt.GENERAL_EXPERT_DIAGNOSIS_PROMPT;
    private final ChatClient chatClient;

    @Override
    public String diagnose(String summary) {
        String content = chatClient.prompt(PROMPT)
                .user(summary)
                .call()
                .content();
        if (content == null) {
            throw new LLMParamException("综合诊断为空");
        }
        return content;
    }
}
