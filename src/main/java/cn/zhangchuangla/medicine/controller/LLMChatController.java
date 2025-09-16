package cn.zhangchuangla.medicine.controller;

import cn.zhangchuangla.medicine.annotation.Anonymous;
import cn.zhangchuangla.medicine.common.base.AjaxResult;
import cn.zhangchuangla.medicine.common.base.BaseController;
import cn.zhangchuangla.medicine.model.vo.llm.chat.UserMessageRequest;
import com.alibaba.cloud.ai.graph.CompiledGraph;
import com.alibaba.cloud.ai.graph.StateGraph;
import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import com.alibaba.cloud.ai.graph.exception.GraphStateException;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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
public class LLMChatController extends BaseController {

    private final CompiledGraph compiledGraph;

    public LLMChatController(@Qualifier("medicineWorkflowService") StateGraph writingAssistantGraph)
            throws GraphStateException {
        this.compiledGraph = writingAssistantGraph.compile();
    }

    @PostMapping
    public AjaxResult<Map<String, Object>> chat(@Validated @RequestBody UserMessageRequest request)
            throws GraphRunnerException {
        var resultFuture = compiledGraph.invoke(Map.of("userMessage", request.getMessage()));
        var result = resultFuture.get();
        Map<String, Object> data = result.data();
        return success(data);

    }
}
