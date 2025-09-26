package cn.zhangchuangla.medicine.llm.workflow.node;

import cn.zhangchuangla.medicine.constants.PromptConstant;
import cn.zhangchuangla.medicine.enums.ChatStageEnum;
import cn.zhangchuangla.medicine.enums.MedicineStateKeyEnum;
import cn.zhangchuangla.medicine.llm.service.OpenAiClientFactory;
import cn.zhangchuangla.medicine.llm.tools.DateTimeTools;
import cn.zhangchuangla.medicine.llm.tools.UserTools;
import cn.zhangchuangla.medicine.llm.workflow.progress.WorkflowProgressContextHolder;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 药品咨询节点
 * 处理药品相关的用户咨询
 *
 * @author Chuang
 * @since 2025/9/10
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class MedicineNode implements NodeAction {

    private final OpenAiClientFactory openAiClientFactory;
    private final DateTimeTools dateTimeTools;
    private final UserTools userTools;

    @Override
    public Map<String, Object> apply(OverAllState state) {
        String userMessage = String.valueOf(state.value(MedicineStateKeyEnum.USER_MESSAGE.getKey()));
        log.debug("药品咨询节点处理用户消息: {}", userMessage);
        WorkflowProgressContextHolder.publishStage(ChatStageEnum.ROUTE_MEDICINE, ChatStageEnum.ROUTE_MEDICINE.getDescription());

        String prompt = PromptConstant.MEDICINE_PROMPT.formatted(userMessage);

        try {
            ChatClient chatClient = openAiClientFactory.chatClient();
            List<String> parts = chatClient
                    .prompt(prompt)
                    .toolCallbacks(ToolCallbacks.from(dateTimeTools, userTools))
                    .stream()
                    .content()
                    .collectList()
                    .block();
            String reply = (parts == null || parts.isEmpty()) ? null : String.join("", parts);

            if (reply == null || reply.trim().isEmpty()) {
                log.warn("药品咨询节点返回空回复");
                reply = PromptConstant.MEDICINE_ERROR_REPLY;
            }

            log.debug("药品咨询节点生成回复: {}", reply);
            return Map.of(MedicineStateKeyEnum.SYSTEM_RESPONSE.getKey(), reply);
        } catch (Exception ex) {
            log.error("药品咨询节点调用异常，返回兜底文案", ex);
            return Map.of(MedicineStateKeyEnum.SYSTEM_RESPONSE.getKey(), PromptConstant.MEDICINE_ERROR_REPLY);
        }
    }
}
