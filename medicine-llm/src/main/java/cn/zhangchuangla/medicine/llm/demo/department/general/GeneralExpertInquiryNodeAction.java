package cn.zhangchuangla.medicine.llm.demo.department.general;

import cn.zhangchuangla.medicine.common.core.enums.MedicineStateKeyEnum;
import cn.zhangchuangla.medicine.llm.demo.support.WorkflowStateKeys;
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
 * 综合专家追问节点（General Expert Inquiry）
 * <p>
 * 负责：当科室不明确时生成通用追问要点，并累加追问轮次，补充到摘要中供后续诊断使用。
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class GeneralExpertInquiryNodeAction implements NodeAction {

    private final ChatClient chatClient;

    @Override
    public Map<String, Object> apply(OverAllState state) {
        log.info("【工作流】进入节点：{}", WorkflowStateKeys.NODE_GENERAL_EXPERT_INQUIRY);
        String symptomInfo = state.value(MedicineStateKeyEnum.USER_MESSAGE.getKey(), String.class).orElse("");
        Flux<ChatResponse> responseFlux = chatClient.prompt()
                .user(symptomInfo)
                .stream()
                .chatResponse();

        int nextRound = nextRound(state);
        return Map.of(
                WorkflowStateKeys.FINAL_RESULT, responseFlux,
                WorkflowStateKeys.INQUIRY_ROUND, nextRound
        );
    }

    private int nextRound(OverAllState state) {
        Integer value = state.value(WorkflowStateKeys.INQUIRY_ROUND, Integer.class).orElse(0);
        return value + 1;
    }
}
