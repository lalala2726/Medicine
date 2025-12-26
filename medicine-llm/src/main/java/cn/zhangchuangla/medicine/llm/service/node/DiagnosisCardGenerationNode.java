package cn.zhangchuangla.medicine.llm.service.node;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import com.alibaba.cloud.ai.graph.streaming.GraphFlux;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.Map;

import static cn.zhangchuangla.medicine.llm.prompt.SystemPrompt.CONSULTATION_FAST_SUPPORT_PROMPT;

/**
 * @author Chuang
 * <p>
 * created on 2025/12/27
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DiagnosisCardGenerationNode implements NodeAction {

    public static final String NODE_ID = "DiagnosisCardGenerationNode";
    private static final String PROMPT = CONSULTATION_FAST_SUPPORT_PROMPT;
    private final ChatClient chatClient;

    @Override
    public Map<String, Object> apply(OverAllState state) throws Exception {

        String userMessage = state.value("userMessage", "");
        Flux<ChatResponse> chatResponseFlux = chatClient.prompt(PROMPT)
                .user(userMessage)
                .stream()
                .chatResponse()
                .contextCapture();


        log.info("快速支持节点");

        return Map.of("quickReply", GraphFlux.of(NODE_ID, "quickReply", chatResponseFlux));
    }

}
