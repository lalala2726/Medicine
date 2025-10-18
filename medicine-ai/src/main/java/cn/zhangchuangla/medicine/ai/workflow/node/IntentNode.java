package cn.zhangchuangla.medicine.ai.workflow.node;

import cn.zhangchuangla.medicine.ai.enums.ChatStageEnum;
import cn.zhangchuangla.medicine.ai.enums.UserIntentEnum;
import cn.zhangchuangla.medicine.ai.factory.OpenAiClientFactory;
import cn.zhangchuangla.medicine.ai.workflow.progress.WorkflowProgressContextHolder;
import cn.zhangchuangla.medicine.common.core.constants.PromptConstant;
import cn.zhangchuangla.medicine.common.core.enums.MedicineStateKeyEnum;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 意图识别节点
 * 识别用户输入的意图类型
 *
 * @author Chuang
 * @since 2025/9/10
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class IntentNode implements NodeAction {

    private final OpenAiClientFactory openAiClientFactory;

    @Override
    public Map<String, Object> apply(OverAllState state) {
        String userMessage = String.valueOf(state.value(MedicineStateKeyEnum.USER_MESSAGE.getKey()));
        log.debug("意图识别节点处理用户消息: {}", userMessage);
        WorkflowProgressContextHolder.publishStage(ChatStageEnum.INTENT_ANALYSIS, ChatStageEnum.INTENT_ANALYSIS.getDescription());

        String prompt = PromptConstant.INTENT_PROMPT.formatted(userMessage);

        ChatClient chatClient = openAiClientFactory.chatClient();
        String content = chatClient.prompt(prompt).call().content();

        // 使用枚举进行类型安全的意图识别
        UserIntentEnum intent = UserIntentEnum.fromString(content);
        log.debug("识别到用户意图: {}", intent);

        return Map.of(MedicineStateKeyEnum.USER_INTENT.getKey(), intent.getIntent());
    }
}
