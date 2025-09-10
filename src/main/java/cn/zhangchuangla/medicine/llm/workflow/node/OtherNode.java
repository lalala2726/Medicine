package cn.zhangchuangla.medicine.llm.workflow.node;

import cn.zhangchuangla.medicine.constants.PromptConstant;
import cn.zhangchuangla.medicine.llm.service.OpenAiClientFactory;
import cn.zhangchuangla.medicine.llm.workflow.enums.MedicineStateKeyEnum;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 其他问题节点
 * 处理无法识别或其他类型的用户问题
 *
 * @author Chuang
 * @since 2025/9/10
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class OtherNode implements NodeAction {

    private final OpenAiClientFactory openAiClientFactory;

    @Override
    public Map<String, Object> apply(OverAllState state) {
        String userMessage = String.valueOf(state.value(MedicineStateKeyEnum.USER_MESSAGE.getKey()));
        log.debug("其他问题节点处理用户消息: {}", userMessage);

        String prompt = PromptConstant.OTHER_PROMPT.formatted(userMessage);

        ChatClient chatClient = openAiClientFactory.chatClient();
        String reply = chatClient.prompt(prompt).call().content();

        if (reply == null || reply.trim().isEmpty()) {
            log.warn("其他问题节点返回空回复");
            reply = PromptConstant.DEFAULT_ERROR_REPLY;
        }

        log.debug("其他问题节点生成回复: {}", reply);
        return Map.of(MedicineStateKeyEnum.SYSTEM_RESPONSE.getKey(), reply);
    }
}
