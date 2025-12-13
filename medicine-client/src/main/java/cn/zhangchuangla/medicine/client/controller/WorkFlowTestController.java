package cn.zhangchuangla.medicine.client.controller;

import cn.zhangchuangla.medicine.common.core.enums.MedicineStateKeyEnum;
import cn.zhangchuangla.medicine.common.security.base.BaseController;
import cn.zhangchuangla.medicine.llm.model.response.ChatResponse;
import cn.zhangchuangla.medicine.llm.workflow.support.WorkflowStateKeys;
import com.alibaba.cloud.ai.graph.CompileConfig;
import com.alibaba.cloud.ai.graph.CompiledGraph;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.StateGraph;
import com.alibaba.cloud.ai.graph.action.InterruptionMetadata;
import com.alibaba.cloud.ai.graph.exception.GraphStateException;
import com.alibaba.cloud.ai.graph.streaming.StreamingOutput;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
    private final CompileConfig expertDiagnosisCompileConfig;

    public WorkFlowTestController(
            @Qualifier("llmExpertDiagnosisWorkflow") StateGraph expertDiagnosisWorkflow,
            @Qualifier("llmExpertDiagnosisWorkflowCompileConfig") CompileConfig expertDiagnosisCompileConfig
    ) {
        this.expertDiagnosisWorkflow = expertDiagnosisWorkflow;
        this.expertDiagnosisCompileConfig = expertDiagnosisCompileConfig;
    }


    /**
     * 单接口两段式：
     * <p>
     * 1) 第一次调用：输入用户主诉，工作流运行到“诊断节点前中断”，返回专家追问问题；
     * 2) 第二次调用：携带同一个 threadId 再次提交 message（作为追问回答），工作流恢复并流式返回最终诊断结果。
     */
    @PostMapping(value = "/expert/run/ask", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "单接口运行（先追问再诊断）", description = "首次返回追问；再次提交同 threadId 返回诊断结果（SSE）")
    public ResponseEntity<Flux<ChatResponse>> ask(@RequestBody WorkFlowAskRequest request) throws GraphStateException {
        CompiledGraph compiledGraph = expertDiagnosisWorkflow.compile(expertDiagnosisCompileConfig);
        String threadId = String.valueOf(getUserId());

        RunnableConfig runnableConfig = RunnableConfig.builder().threadId(threadId).build();
        Flux<ChatResponse> flux;

        if (isWaitingForInquiryAnswer(compiledGraph, runnableConfig)) {
            flux = Flux.defer(() -> {
                        try {
                            RunnableConfig updatedConfig = compiledGraph.updateState(
                                    runnableConfig,
                                    Map.of(WorkflowStateKeys.INQUIRY_ANSWER, request.getMessage())
                            );
                            RunnableConfig resumeConfig = RunnableConfig.builder(updatedConfig)
                                    .addMetadata(RunnableConfig.HUMAN_FEEDBACK_METADATA_KEY, "placeholder")
                                    .build();

                            return compiledGraph.stream(Map.of(), resumeConfig);
                        } catch (Exception e) {
                            return Flux.error(e);
                        }
                    })
                    .filter(output -> output instanceof StreamingOutput)
                    .cast(StreamingOutput.class)
                    .map(streamingOutput -> {
                        String chunk = streamingOutput.chunk();
                        ChatResponse resp = new ChatResponse();
                        resp.setContent(chunk != null ? chunk : "");
                        return resp;
                    });
        } else {
            Map<String, Object> initialState = new HashMap<>();
            initialState.put(MedicineStateKeyEnum.USER_MESSAGE.getKey(), request.getMessage());

            flux = compiledGraph.stream(initialState, runnableConfig)
                    .flatMap(output -> {
                        if (output instanceof StreamingOutput streamingOutput) {
                            ChatResponse resp = new ChatResponse();
                            resp.setContent(streamingOutput.chunk() != null ? streamingOutput.chunk() : "");
                            return Flux.just(resp);
                        }
                        if (output instanceof InterruptionMetadata interruption) {
                            String questions = interruption.state()
                                    .value(WorkflowStateKeys.INQUIRY_QUESTIONS, String.class)
                                    .orElse("");
                            ChatResponse resp = new ChatResponse();
                            resp.setContent(questions);
                            resp.setIsFinish(true);
                            return Flux.just(resp);
                        }
                        return Flux.empty();
                    });
        }

        return ResponseEntity.ok()
                .header("X-Thread-Id", threadId)
                .body(flux);
    }

    private boolean isWaitingForInquiryAnswer(CompiledGraph compiledGraph, RunnableConfig runnableConfig) {
        return compiledGraph.stateOf(runnableConfig)
                .map(snapshot -> WorkflowStateKeys.NODE_EXPERT_DIAGNOSIS.equals(snapshot.next()))
                .orElse(false);
    }

    @Data
    public static class WorkFlowAskRequest {
        private String message;
    }
}
