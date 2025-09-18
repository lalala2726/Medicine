package cn.zhangchuangla.medicine.controller;

import cn.zhangchuangla.medicine.common.base.AjaxResult;
import cn.zhangchuangla.medicine.common.base.BaseController;
import cn.zhangchuangla.medicine.model.request.assistant.HistoryRequest;
import cn.zhangchuangla.medicine.model.vo.chat.StreamChatResponse;
import cn.zhangchuangla.medicine.model.vo.llm.chat.ChatHistoryResponse;
import cn.zhangchuangla.medicine.model.vo.llm.chat.UserMessageRequest;
import cn.zhangchuangla.medicine.service.AssistantService;
import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

/**
 * @author Chuang
 * <p>
 * created on 2025/9/10 15:21
 */
@RequestMapping("/assistant")
@Tag(name = "医疗助手", description = "医疗助手接口")
@RestController
public class AssistantController extends BaseController {

    private final AssistantService assistantService;

    public AssistantController(AssistantService assistantService) {
        this.assistantService = assistantService;
    }

    /**
     * 发送聊天消息(流式聊天)
     *
     * @param request 请求体
     * @return 返回流式聊天结果
     * @throws GraphRunnerException 运行时异常
     */
    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "发送聊天消息(流式)", description = "以SSE流式返回消息块，避免长等待超时")
    public Flux<StreamChatResponse> chat(@Validated @RequestBody UserMessageRequest request)
            throws GraphRunnerException {
        return assistantService.chat(request);
    }

    /**
     * 获取历史消息（分页）
     *
     * @param historyRequest 分页请求参数
     * @return 分页历史消息
     */
    @GetMapping("/history")
    @Operation(summary = "获取历史消息（分页）", description = "使用游标分页方式加载历史消息，支持指定游标位置和每页条数")
    public AjaxResult<ChatHistoryResponse> history(@Validated HistoryRequest historyRequest) {
        ChatHistoryResponse response = assistantService.history(historyRequest);
        return success(response);
    }
}
