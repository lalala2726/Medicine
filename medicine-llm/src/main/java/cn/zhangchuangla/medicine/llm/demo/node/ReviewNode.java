package cn.zhangchuangla.medicine.llm.demo.node;

import cn.zhangchuangla.medicine.llm.prompt.DiagnosisWorkflowPrompt;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class ReviewNode implements NodeAction {

    public static final String PROMPT = DiagnosisWorkflowPrompt.REVIEW_PROMPT;
    private final ChatClient chatClient;

    @Override
    public Map<String, Object> apply(OverAllState state) {
        String diagnosisResult = state.value("diagnosisResult", "");

        Flux<String> content = chatClient.prompt(PROMPT)
                .user(diagnosisResult)
                .stream()
                .content();

        return Map.of("finalResult", content);
    }
}
