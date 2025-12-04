package cn.zhangchuangla.medicine.client.controller;

import cn.zhangchuangla.medicine.common.core.enums.MedicineStateKeyEnum;
import cn.zhangchuangla.medicine.common.security.annotation.Anonymous;
import cn.zhangchuangla.medicine.common.security.base.BaseController;
import cn.zhangchuangla.medicine.llm.model.response.ChatResponse;
import com.alibaba.cloud.ai.graph.CompiledGraph;
import com.alibaba.cloud.ai.graph.StateGraph;
import com.alibaba.cloud.ai.graph.exception.GraphStateException;
import com.alibaba.cloud.ai.graph.streaming.StreamingOutput;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/workflow")
@Tag(name = "工作流测试")
public class WorkFlowTestController extends BaseController {

    private final StateGraph expertDiagnosisWorkflow;

    public WorkFlowTestController(@Qualifier("llmExpertDiagnosisWorkflow") StateGraph expertDiagnosisWorkflow) {
        this.expertDiagnosisWorkflow = expertDiagnosisWorkflow;
    }


    /**
     * 流式输出接口
     * produces = MediaType.TEXT_EVENT_STREAM_VALUE 用于 SSE (Server-Sent Events)
     */
    @PostMapping(value = "/expert/run/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "流式运行专家诊断工作流", description = "返回 SSE 流式数据")
    @Anonymous
    public Flux<ChatResponse> runStream(@RequestBody WorkFlowTestRequest request) throws GraphStateException {
        CompiledGraph compiledGraph = expertDiagnosisWorkflow.compile();

        Map<String, Object> initialState = new HashMap<>();
        initialState.put(MedicineStateKeyEnum.USER_MESSAGE.getKey(), request.getMessage());
        initialState.put("summer", request.getMessage());

        // 使用 stream 方法而不是 invoke
        return compiledGraph.stream(initialState)
                .filter(output -> output instanceof StreamingOutput) // 只保留流式输出帧
                .cast(StreamingOutput.class)
                .map(streamingOutput -> {
                    String chunk = streamingOutput.chunk();
                    ChatResponse resp = new ChatResponse();
                    resp.setContent(chunk != null ? chunk : "");
                    return resp;
                });
    }

    @Data
    public static class WorkFlowTestRequest {
        private String message;
    }
}
