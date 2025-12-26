package cn.zhangchuangla.medicine.llm.service.node;

import cn.zhangchuangla.medicine.llm.tool.ClientConsultationTools;
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

import static cn.zhangchuangla.medicine.llm.prompt.SystemPrompt.CONSULTATION_SYSTEM_PROMPT;

/**
 * @author Chuang
 * <p>
 * created on 2025/12/27
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class EmotionalSupportNode implements NodeAction {

    public static final String NODE_ID = "EmotionalSupportNode";

    private final ChatClient chatClient;
    private final ClientConsultationTools clientConsultationTools;


    @Override
    public Map<String, Object> apply(OverAllState state) throws Exception {

        String userMessage = state.value("userMessage", "");
        Flux<ChatResponse> chatResponseFlux = chatClient.prompt()
                .system(CONSULTATION_SYSTEM_PROMPT)
                .user(userMessage)
                .tools(clientConsultationTools)
                .stream()
                .chatResponse()
                .contextCapture();

        log.info(" 情绪支持节点");

        return Map.of("finalReply", GraphFlux.of(NODE_ID, "finalReply", chatResponseFlux));
    }
}
