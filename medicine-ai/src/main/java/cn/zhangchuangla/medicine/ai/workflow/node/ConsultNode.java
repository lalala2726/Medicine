package cn.zhangchuangla.medicine.ai.workflow.node;

import cn.zhangchuangla.medicine.ai.service.OpenAiClientFactory;
import cn.zhangchuangla.medicine.ai.tools.DateTimeTools;
import cn.zhangchuangla.medicine.ai.tools.UserTools;
import cn.zhangchuangla.medicine.ai.workflow.progress.WorkflowProgressContextHolder;
import cn.zhangchuangla.medicine.common.core.constants.PromptConstant;
import cn.zhangchuangla.medicine.common.core.enums.MedicineStateKeyEnum;
import cn.zhangchuangla.medicine.enums.ChatStageEnum;
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
 * 健康咨询节点
 * 处理健康相关的用户咨询
 *
 * @author Chuang
 * @since 2025/9/10
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class ConsultNode implements NodeAction {

    private final OpenAiClientFactory openAiClientFactory;
    private final DateTimeTools dateTimeTools;
    private final UserTools userTools;

    @Override
    public Map<String, Object> apply(OverAllState state) {
        String userMessage = String.valueOf(state.value(MedicineStateKeyEnum.USER_MESSAGE.getKey()));
        log.debug("健康咨询节点处理用户消息: {}", userMessage);
        WorkflowProgressContextHolder.publishStage(ChatStageEnum.ROUTE_CONSULT, ChatStageEnum.ROUTE_CONSULT.getDescription());

        String prompt = PromptConstant.CONSULT_PROMPT.formatted(userMessage);
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
            log.warn("健康咨询节点返回空回复");
            reply = PromptConstant.CONSULT_ERROR_REPLY;
        }

        log.debug("健康咨询节点生成回复: {}", reply);
        return Map.of(MedicineStateKeyEnum.SYSTEM_RESPONSE.getKey(), reply);
    }
}
