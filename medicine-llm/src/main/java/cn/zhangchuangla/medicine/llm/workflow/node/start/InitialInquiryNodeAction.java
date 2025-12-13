package cn.zhangchuangla.medicine.llm.workflow.node.start;

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
 * 初始询问节点（Initial Inquiry）
 * <p>
 * 负责：从用户输入中识别意图，分流为【非诊断业务咨询】或【进入诊断流程】。
 */
@Component
@RequiredArgsConstructor
public class InitialInquiryNodeAction implements NodeAction {

    private static final String PROMPT = DiagnosisWorkflowPrompt.INITIAL_INQUIRY_PROMPT;
    private final ChatClient chatClient;

    @Override
    public Map<String, Object> apply(OverAllState state) {
        String userMessage = state.value(MedicineStateKeyEnum.USER_MESSAGE.getKey(), String.class).orElse("");
        String content = chatClient.prompt(PROMPT)
                .user(userMessage)
                .call()
                .content();
        if (content == null) {
            throw new LLMParamException("初始询问意图识别结果为空");
        }

        String intent = content.trim().toUpperCase();
        if (!WorkflowStateKeys.USER_INTENT_GENERAL_SERVICE.equals(intent)
                && !WorkflowStateKeys.USER_INTENT_DIAGNOSIS.equals(intent)) {
            intent = WorkflowStateKeys.USER_INTENT_DIAGNOSIS;
        }

        return Map.of(MedicineStateKeyEnum.USER_INTENT.getKey(), intent);
    }
}
