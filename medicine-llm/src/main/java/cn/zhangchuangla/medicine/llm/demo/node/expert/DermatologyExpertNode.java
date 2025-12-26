package cn.zhangchuangla.medicine.llm.demo.node.expert;

import cn.zhangchuangla.medicine.llm.prompt.DiagnosisWorkflowPrompt;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.Map;

/**
 *
 * 皮肤科专家节点
 *
 * @author Chuang
 * <p>
 * created on 2025/12/2
 */
@Component
@RequiredArgsConstructor
public class DermatologyExpertNode implements NodeAction {

    private static final String PROMPT = DiagnosisWorkflowPrompt.DERMATOLOGY_EXPERT_PROMPT;
    private final ChatClient chatClient;

    @Override
    public Map<String, Object> apply(OverAllState state) {
        String userMessage = state.value("userMessage", "");
        Flux<ChatResponse> chatResponseFlux = chatClient.prompt(PROMPT)
                .user(userMessage)
                .stream()
                .chatResponse();
        return Map.of("diagnosisResult", chatResponseFlux);
    }
}
