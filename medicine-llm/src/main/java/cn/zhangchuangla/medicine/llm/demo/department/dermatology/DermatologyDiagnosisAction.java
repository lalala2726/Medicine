package cn.zhangchuangla.medicine.llm.demo.department.dermatology;

import cn.zhangchuangla.medicine.llm.demo.support.DepartmentDiagnosisAction;
import cn.zhangchuangla.medicine.llm.demo.support.WorkflowStateKeys;
import cn.zhangchuangla.medicine.llm.exection.LLMParamException;
import cn.zhangchuangla.medicine.llm.prompt.DiagnosisWorkflowPrompt;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

/**
 * 皮肤科诊断实现（Dermatology Diagnosis）
 * <p>
 * 负责：使用皮肤科专家提示词，根据摘要生成皮肤科方向的诊断建议。
 */
@Component(WorkflowStateKeys.ROUTE_DERMATOLOGY)
@RequiredArgsConstructor
@Slf4j
public class DermatologyDiagnosisAction implements DepartmentDiagnosisAction {

    private static final String PROMPT = DiagnosisWorkflowPrompt.DERMATOLOGY_EXPERT_PROMPT;
    private final ChatClient chatClient;

    @Override
    public String diagnose(String summary) {
        log.info("【工作流】进入皮肤科诊断实现：{}", WorkflowStateKeys.ROUTE_DERMATOLOGY);
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
