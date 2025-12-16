package cn.zhangchuangla.medicine.llm.workflow.department.surgery;

import cn.zhangchuangla.medicine.llm.exection.LLMParamException;
import cn.zhangchuangla.medicine.llm.prompt.DiagnosisWorkflowPrompt;
import cn.zhangchuangla.medicine.llm.workflow.support.DepartmentDiagnosisAction;
import cn.zhangchuangla.medicine.llm.workflow.support.WorkflowStateKeys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

/**
 * 外科诊断实现（Surgery Diagnosis）
 * <p>
 * 负责：使用外科专家提示词，根据摘要生成外科方向的处置/诊断建议。
 */
@Component(WorkflowStateKeys.ROUTE_SURGERY)
@RequiredArgsConstructor
@Slf4j
public class SurgeryDiagnosisAction implements DepartmentDiagnosisAction {

    private static final String PROMPT = DiagnosisWorkflowPrompt.SURGERY_EXPERT_PROMPT;
    private final ChatClient chatClient;

    @Override
    public String diagnose(String summary) {
        log.info("【工作流】进入外科诊断实现：{}", WorkflowStateKeys.ROUTE_SURGERY);
        String content = chatClient.prompt(PROMPT)
                .user(summary)
                .call()
                .content();
        if (content == null) {
            throw new LLMParamException("外科诊断为空");
        }
        return content;
    }
}
