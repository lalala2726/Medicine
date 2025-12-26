package cn.zhangchuangla.medicine.llm.demo.department.general;

import cn.zhangchuangla.medicine.llm.demo.support.DepartmentDiagnosisAction;
import cn.zhangchuangla.medicine.llm.demo.support.WorkflowStateKeys;
import cn.zhangchuangla.medicine.llm.exection.LLMParamException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

/**
 * 综合诊断实现（General Expert Diagnosis）
 * <p>
 * 负责：当科室不明确或兜底时，基于摘要生成综合门诊方向的谨慎诊断与就医建议。
 */
@Component(WorkflowStateKeys.ROUTE_GENERAL)
@RequiredArgsConstructor
@Slf4j
public class GeneralExpertDiagnosisAction implements DepartmentDiagnosisAction {

    private final ChatClient chatClient;

    @Override
    public String diagnose(String summary) {
        log.info("【工作流】进入综合诊断实现：{}", WorkflowStateKeys.ROUTE_GENERAL);

        String content = chatClient.prompt()
                .user(summary)
                .call()
                .content();
        if (content == null) {
            throw new LLMParamException("综合诊断为空");
        }
        return content;
    }
}
