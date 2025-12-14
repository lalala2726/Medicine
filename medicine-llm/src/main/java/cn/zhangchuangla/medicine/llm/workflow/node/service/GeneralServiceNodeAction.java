package cn.zhangchuangla.medicine.llm.workflow.node.service;

import cn.zhangchuangla.medicine.common.core.enums.MedicineStateKeyEnum;
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
 * 业务咨询节点（General Service）
 * <p>
 * 负责：处理退货/订单/物流/支付/账号/用药说明等非诊断类问题，直接输出结果并结束流程。
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class GeneralServiceNodeAction implements NodeAction {

    private static final String PROMPT = DiagnosisWorkflowPrompt.GENERAL_SERVICE_PROMPT;
    private final ChatClient chatClient;

    @Override
    public Map<String, Object> apply(OverAllState state) {
        log.info("【工作流】进入节点：{}", WorkflowStateKeys.NODE_GENERAL_SERVICE);
        String userMessage = state.value(MedicineStateKeyEnum.USER_MESSAGE.getKey(), String.class).orElse("");
        Flux<ChatResponse> responseFlux = chatClient.prompt(PROMPT)
                .user(userMessage)
                .stream()
                .chatResponse();
        return Map.of(WorkflowStateKeys.FINAL_RESULT, responseFlux);
    }
}
