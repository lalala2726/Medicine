package cn.zhangchuangla.medicine.llm.workflow.node;

import cn.zhangchuangla.medicine.constants.PromptConstant;
import cn.zhangchuangla.medicine.enums.ChatStageEnum;
import cn.zhangchuangla.medicine.enums.MedicineStateKeyEnum;
import cn.zhangchuangla.medicine.llm.service.OpenAiClientFactory;
import cn.zhangchuangla.medicine.llm.tools.DateTimeTools;
import cn.zhangchuangla.medicine.llm.tools.UserTools;
import cn.zhangchuangla.medicine.llm.workflow.progress.WorkflowProgressContextHolder;
import cn.zhangchuangla.medicine.security.entity.SysUserDetails;
import cn.zhangchuangla.medicine.utils.SecurityUtils;
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
    private final DateTimeTools dateTimeTools;
    private final UserTools userTools;

    @Override
    public Map<String, Object> apply(OverAllState state) {
        String userMessage = String.valueOf(state.value(MedicineStateKeyEnum.USER_MESSAGE.getKey()));
        WorkflowProgressContextHolder.publishStage(ChatStageEnum.ROUTE_OTHER, ChatStageEnum.ROUTE_OTHER.getDescription());
        // 获取当前用户信息，并传递给工具
        SysUserDetails loginUser = SecurityUtils.getLoginUser();
        try {
            // 设置用户信息到工具中
            userTools.setCurrentUser(loginUser);
            String prompt = PromptConstant.OTHER_PROMPT.formatted(userMessage);
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
                log.warn("其他问题节点返回空回复");
                reply = PromptConstant.DEFAULT_ERROR_REPLY;
            }

            log.debug("其他问题节点生成回复: {}", reply);
            return Map.of(MedicineStateKeyEnum.SYSTEM_RESPONSE.getKey(), reply);
        } finally {
            // 确保清理ThreadLocal，避免内存泄漏
            userTools.clearCurrentUser();
        }
    }
}
