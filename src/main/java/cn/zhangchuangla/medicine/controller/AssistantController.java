package cn.zhangchuangla.medicine.controller;

import cn.zhangchuangla.medicine.annotation.Anonymous;
import cn.zhangchuangla.medicine.common.base.AjaxResult;
import cn.zhangchuangla.medicine.common.base.BaseController;
import cn.zhangchuangla.medicine.model.vo.chat.StreamChatResponse;
import cn.zhangchuangla.medicine.model.vo.llm.chat.ChatHistoryResponse;
import cn.zhangchuangla.medicine.model.vo.llm.chat.UserMessageRequest;
import cn.zhangchuangla.medicine.service.AssistantService;
import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

/**
 * @author Chuang
 * <p>
 * created on 2025/9/10 15:21
 */
@RequestMapping("/llm/chat")
@Tag(name = "LLM聊天接口", description = "LLM聊天接口")
@Anonymous
@RestController
public class AssistantController extends BaseController {

    @Autowired
    private AssistantService assistantService;

    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "发送聊天消息(流式)", description = "以SSE流式返回消息块，避免长等待超时")
    public Flux<StreamChatResponse> chat(@Validated @RequestBody UserMessageRequest request)
            throws GraphRunnerException {
        return assistantService.chat(request);
    }

    @GetMapping("/history")
    @Operation(summary = "获取历史消息", description = "根据会话UUID返回默认50条历史消息，可通过limit自定义")
    public AjaxResult<ChatHistoryResponse> history(@RequestParam("uuid") String uuid,
                                                   @RequestParam(value = "limit", required = false, defaultValue = "50") Integer limit) {
        try {
            ChatHistoryResponse response = assistantService.history(uuid, limit);
            return success(response);
        } catch (Exception e) {
            return error("获取历史消息失败: " + e.getMessage());
        }
    }
}
