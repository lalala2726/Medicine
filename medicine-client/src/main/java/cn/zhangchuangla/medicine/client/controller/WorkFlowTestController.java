package cn.zhangchuangla.medicine.client.controller;

import cn.zhangchuangla.medicine.common.security.base.BaseController;
import com.alibaba.cloud.ai.graph.CompiledGraph;
import com.alibaba.cloud.ai.graph.StateGraph;
import com.alibaba.cloud.ai.graph.exception.GraphStateException;
import com.alibaba.cloud.ai.graph.streaming.StreamingOutput;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/workflow")
@Tag(name = "工作流测试")
public class WorkFlowTestController extends BaseController {

    private final StateGraph expertDiagnosisWorkflow;

    public WorkFlowTestController(@Qualifier("llmExpertDiagnosisWorkflow") StateGraph expertDiagnosisWorkflow) {
        this.expertDiagnosisWorkflow = expertDiagnosisWorkflow;
    }


    @PostMapping(value = "/expert/run/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> runStream(@RequestBody WorkFlowTestRequest request) throws GraphStateException {
        CompiledGraph compiledGraph = expertDiagnosisWorkflow.compile();
        Map<String, Object> initialState = new HashMap<>();
        initialState.put("userMessage", request.message());

        return compiledGraph.stream(initialState)
                .filter(output -> output instanceof StreamingOutput)
                .cast(StreamingOutput.class)
                .map(streamingOutput -> {
                    Message message = streamingOutput.message();
                    if (message instanceof AssistantMessage assistantMessage) {
                        String content = assistantMessage.getText();
                        return content != null ? content : "";
                    }
                    return "";
                })
                // 过滤掉空串（比如工具调用产生的空文本帧）
                .filter(text -> !text.isEmpty());
    }

    public record WorkFlowTestRequest(String message) {
    }
}


