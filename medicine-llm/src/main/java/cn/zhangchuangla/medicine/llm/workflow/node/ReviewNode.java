package cn.zhangchuangla.medicine.llm.workflow.node;

import cn.zhangchuangla.medicine.llm.prompt.DiagnosisWorkflowPrompt;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class ReviewNode implements NodeAction {

    public static final String PROMPT = DiagnosisWorkflowPrompt.REVIEW_PROMPT;
    private final ChatClient chatClient;

    @Override
    public Map<String, Object> apply(OverAllState state) throws Exception {
        String diagnosisResult = String.valueOf(state.value("diagnosisResult"));

        Flux<ChatResponse> responseFlux = chatClient.prompt(PROMPT)
                .user(diagnosisResult)
                .stream()
                .chatResponse();

        return Map.of("finalResult", responseFlux);
    }
}
