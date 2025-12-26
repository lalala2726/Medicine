package cn.zhangchuangla.medicine.llm.demo.node.start;

import cn.zhangchuangla.medicine.common.core.enums.MedicineStateKeyEnum;
import cn.zhangchuangla.medicine.llm.demo.support.WorkflowStateKeys;
import cn.zhangchuangla.medicine.llm.exection.LLMParamException;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Map;

/**
 * 初始询问节点（Initial Inquiry）
 * <p>
 * 负责：从用户输入中识别意图，分流为【非诊断业务咨询】或【进入诊断流程】。
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class InitialInquiryNodeAction implements NodeAction {

    private final ChatClient chatClient;

    @Override
    public Map<String, Object> apply(OverAllState state) {
        log.info("【工作流】进入节点：{}", WorkflowStateKeys.NODE_INITIAL_INQUIRY);
        String userMessage = state.value(MedicineStateKeyEnum.USER_MESSAGE.getKey(), String.class).orElse("");
        String content = chatClient.prompt()
                .user(userMessage)
                .call()
                .content();
        if (content == null) {
            throw new LLMParamException("初始询问意图识别结果为空");
        }

        String normalizedIntent = content.trim().toUpperCase(Locale.ROOT);
        String intent = switch (normalizedIntent) {
            case WorkflowStateKeys.USER_INTENT_GENERAL_SERVICE, WorkflowStateKeys.USER_INTENT_DIAGNOSIS ->
                    normalizedIntent;
            default -> WorkflowStateKeys.USER_INTENT_DIAGNOSIS;
        };

        return Map.of(MedicineStateKeyEnum.USER_INTENT.getKey(), intent);
    }
}
