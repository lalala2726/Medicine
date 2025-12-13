package cn.zhangchuangla.medicine.llm.workflow.department.dermatology;

import cn.zhangchuangla.medicine.llm.exection.LLMParamException;
import cn.zhangchuangla.medicine.llm.prompt.DiagnosisWorkflowPrompt;
import cn.zhangchuangla.medicine.llm.workflow.support.DepartmentDiagnosisAction;
import cn.zhangchuangla.medicine.llm.workflow.support.WorkflowStateKeys;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

/**
 * 皮肤科诊断实现（Dermatology Diagnosis）
 * <p>
 * 负责：使用皮肤科专家提示词，根据摘要生成皮肤科方向的诊断建议。
 */
@Component(WorkflowStateKeys.ROUTE_DERMATOLOGY)
@RequiredArgsConstructor
public class DermatologyDiagnosisAction implements DepartmentDiagnosisAction {

    private static final String PROMPT = DiagnosisWorkflowPrompt.DERMATOLOGY_EXPERT_PROMPT;
    private final ChatClient chatClient;

    @Override
    public String diagnose(String summary) {
        String content = chatClient.prompt(PROMPT)
                .user(summary)
                .call()
                .content();
        if (content == null) {
            throw new LLMParamException("皮肤科诊断为空");
        }
        return content;
    }
}
