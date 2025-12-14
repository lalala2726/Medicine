package cn.zhangchuangla.medicine.llm.workflow.node.diagnosis;

import cn.zhangchuangla.medicine.common.core.enums.MedicineStateKeyEnum;
import cn.zhangchuangla.medicine.llm.exection.LLMParamException;
import cn.zhangchuangla.medicine.llm.prompt.DiagnosisWorkflowPrompt;
import cn.zhangchuangla.medicine.llm.workflow.support.WorkflowStateKeys;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Map;

/**
 * 基础问诊节点（Pre-Diagnosis）
 * <p>
 * 负责：对用户描述进行初步摘要整理，并进行科室方向路由判断，初始化追问轮次信息。
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PreDiagnosisNodeAction implements NodeAction {

    private final ChatClient chatClient;

    @Override
    public Map<String, Object> apply(OverAllState state) {
        log.info("【工作流】进入节点：{}", WorkflowStateKeys.NODE_PRE_DIAGNOSIS);
        String userMessage = state.value(MedicineStateKeyEnum.USER_MESSAGE.getKey(), String.class).orElse("");
        String route = chatClient.prompt(DiagnosisWorkflowPrompt.ROUTE_PROMPT)
                .user(userMessage)
                .call()
                .content();
        if (route == null) {
            throw new LLMParamException("科室路由结果为空");
        }

        String normalizedRoute = route.trim().toUpperCase(Locale.ROOT);
        normalizedRoute = switch (normalizedRoute) {
            case WorkflowStateKeys.ROUTE_INTERNAL_MEDICINE,
                 WorkflowStateKeys.ROUTE_SURGERY,
                 WorkflowStateKeys.ROUTE_DERMATOLOGY,
                 WorkflowStateKeys.ROUTE_GENERAL -> normalizedRoute;
            default -> WorkflowStateKeys.ROUTE_GENERAL;
        };
        return Map.of(
                WorkflowStateKeys.ROUTE, normalizedRoute,
                WorkflowStateKeys.INQUIRY_ROUND, 0
        );
    }
}
