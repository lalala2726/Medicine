package cn.zhangchuangla.medicine.llm.workflow.node;

import cn.zhangchuangla.medicine.common.core.enums.MedicineStateKeyEnum;
import cn.zhangchuangla.medicine.llm.exection.LLMParamException;
import cn.zhangchuangla.medicine.llm.prompt.DiagnosisWorkflowPrompt;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author Chuang
 * <p>
 * created on 2025/12/2
 */
@Component
@RequiredArgsConstructor
public class RouteNode implements NodeAction {

    private final static String PROMPT = DiagnosisWorkflowPrompt.ROUTE_PROMPT;
    private final ChatClient chatClient;

    @Override
    public Map<String, Object> apply(OverAllState state) throws Exception {
        String summary = String.valueOf(state.value(MedicineStateKeyEnum.USER_MESSAGE.getKey()));
        String content = chatClient.prompt(PROMPT)
                .user(summary)
                .call()
                .content();
        if (content == null){
            throw new LLMParamException("路由结果为空");
        }
        return Map.of("route", content);
    }
}
