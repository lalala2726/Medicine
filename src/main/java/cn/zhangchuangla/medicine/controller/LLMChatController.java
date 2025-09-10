package cn.zhangchuangla.medicine.controller;

import cn.zhangchuangla.medicine.annotation.Anonymous;
import com.alibaba.cloud.ai.graph.CompiledGraph;
import com.alibaba.cloud.ai.graph.StateGraph;
import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import com.alibaba.cloud.ai.graph.exception.GraphStateException;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;


/**
 * @author Chuang
 * <p>
 * created on 2025/9/10 15:21
 */
@RequestMapping("/llm/chat")
@Tag(name = "LLM聊天接口", description = "LLM聊天接口")
@Anonymous
@RestController
public class LLMChatController {

    private final CompiledGraph compiledGraph;

    public LLMChatController(@Qualifier("medicineWorkflowService") StateGraph writingAssistantGraph)
            throws GraphStateException {
        this.compiledGraph = writingAssistantGraph.compile();
    }


    @GetMapping("/test")
    public Map<String, Object> chat(@RequestParam(value = "message", defaultValue = "您好!") String message) throws GraphRunnerException {
        var resultFuture = compiledGraph.invoke(Map.of("userMessage", message));
        var result = resultFuture.get();
        return result.data();

    }
}
