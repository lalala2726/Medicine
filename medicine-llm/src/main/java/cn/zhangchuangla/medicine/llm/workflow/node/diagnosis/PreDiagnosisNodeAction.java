package cn.zhangchuangla.medicine.llm.workflow.node.diagnosis;

import cn.zhangchuangla.medicine.common.core.enums.MedicineStateKeyEnum;
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
 * 基础问诊节点（Pre-Diagnosis）
 * <p>
 * 负责：对用户描述进行初步摘要整理，并进行科室方向路由判断，初始化追问轮次信息。
 */
@Component
@RequiredArgsConstructor
public class PreDiagnosisNodeAction implements NodeAction {

    private final ChatClient chatClient;

    @Override
    public Map<String, Object> apply(OverAllState state) {
        String userMessage = state.value(MedicineStateKeyEnum.USER_MESSAGE.getKey(), String.class).orElse("");
        String summary = chatClient.prompt(DiagnosisWorkflowPrompt.SUMMARY_COLLATION_PROMPT)
                .user(userMessage)
                .call()
                .content();
        if (summary == null) {
            throw new LLMParamException("基础问诊摘要为空");
        }

        String route = chatClient.prompt(DiagnosisWorkflowPrompt.ROUTE_PROMPT)
                .user(summary)
                .call()
                .content();
        if (route == null) {
            throw new LLMParamException("科室路由结果为空");
        }

        String normalizedRoute = route.trim().toUpperCase();
        if (!WorkflowStateKeys.ROUTE_INTERNAL_MEDICINE.equals(normalizedRoute)
                && !WorkflowStateKeys.ROUTE_SURGERY.equals(normalizedRoute)
                && !WorkflowStateKeys.ROUTE_DERMATOLOGY.equals(normalizedRoute)
                && !WorkflowStateKeys.ROUTE_GENERAL.equals(normalizedRoute)) {
            normalizedRoute = WorkflowStateKeys.ROUTE_GENERAL;
        }

        return Map.of(
                WorkflowStateKeys.SUMMARY, summary,
                WorkflowStateKeys.ROUTE, normalizedRoute,
                WorkflowStateKeys.INQUIRY_ROUND, 0
        );
    }
}
