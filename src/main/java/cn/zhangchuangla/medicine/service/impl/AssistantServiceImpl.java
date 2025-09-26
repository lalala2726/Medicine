package cn.zhangchuangla.medicine.service.impl;

import cn.zhangchuangla.medicine.common.base.BaseService;
import cn.zhangchuangla.medicine.common.utils.UUIDUtils;
import cn.zhangchuangla.medicine.enums.ChatStageEnum;
import cn.zhangchuangla.medicine.enums.MedicineStateKeyEnum;
import cn.zhangchuangla.medicine.llm.workflow.progress.DefaultWorkflowProgressReporter;
import cn.zhangchuangla.medicine.llm.workflow.progress.WorkflowProgressContextHolder;
import cn.zhangchuangla.medicine.model.entity.Conversation;
import cn.zhangchuangla.medicine.model.entity.Message;
import cn.zhangchuangla.medicine.model.request.assistant.HistoryRequest;
import cn.zhangchuangla.medicine.model.vo.chat.StreamChatResponse;
import cn.zhangchuangla.medicine.model.vo.llm.chat.ChatHistoryResponse;
import cn.zhangchuangla.medicine.model.vo.llm.chat.UserMessageRequest;
import cn.zhangchuangla.medicine.service.AssistantService;
import cn.zhangchuangla.medicine.service.ConversationService;
import cn.zhangchuangla.medicine.service.MessageService;
import com.alibaba.cloud.ai.graph.CompiledGraph;
import com.alibaba.cloud.ai.graph.NodeOutput;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.StateGraph;
import com.alibaba.cloud.ai.graph.async.AsyncGenerator;
import com.alibaba.cloud.ai.graph.exception.GraphStateException;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.scheduler.Schedulers;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author Chuang
 * <p>
 * created on 2025/9/16 10:49
 */
@Slf4j
@Service
public class AssistantServiceImpl implements AssistantService, BaseService {

    /**
     * Interval for sending SSE heartbeat messages (milliseconds).
     */
    private static final long HEARTBEAT_INTERVAL_MILLIS = 10_000L;

    private final CompiledGraph compiledGraph;
    private final ConversationService conversationService;
    private final MessageService messageService;

    public AssistantServiceImpl(@Qualifier("medicineWorkflowService") StateGraph writingAssistantGraph,
                                ConversationService conversationService,
                                MessageService messageService)
            throws GraphStateException {
        this.compiledGraph = writingAssistantGraph.compile();
        this.conversationService = conversationService;
        this.messageService = messageService;
    }

    @Override
    public Flux<StreamChatResponse> chat(UserMessageRequest userMessageRequest) {
        String uuid = userMessageRequest.getUuid();
        String userMessage = userMessageRequest.getMessage();

        if (!StringUtils.hasText(uuid)) {
            return streamNewConversation(userMessage);
        } else {
            return streamExistingConversation(uuid, userMessage);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ChatHistoryResponse history(HistoryRequest request) {
        if (request == null || !StringUtils.hasText(request.getUuid())) {
            throw new IllegalArgumentException("uuid不能为空");
        }

        String uuid = request.getUuid();
        Long cursor = request.getCursor();
        int limit = request.getLimit() != null && request.getLimit() > 0 ? request.getLimit() : 20;

        LambdaQueryWrapper<Conversation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Conversation::getUuid, uuid)
                .eq(Conversation::getIsDelete, 0);
        Conversation conversation = conversationService.getOne(queryWrapper);
        if (conversation == null) {
            throw new RuntimeException("会话不存在: " + uuid);
        }

        List<Message> messages = messageService.getConversationMessagesCursor(conversation.getId(), cursor, limit);
        List<ChatHistoryResponse.MessageVO> vos = messages.stream()
                .map(msg -> {
                    ChatHistoryResponse.MessageVO vo = new ChatHistoryResponse.MessageVO();
                    vo.setId(msg.getId());
                    vo.setRole(String.valueOf(msg.getRole()));
                    vo.setContent(msg.getContent());
                    vo.setCreateTime(msg.getCreateTime());
                    return vo;
                })
                .collect(Collectors.toList());

        ChatHistoryResponse response = new ChatHistoryResponse();
        response.setUuid(uuid);
        response.setMessages(vos);

        if (!vos.isEmpty()) {
            Long lastMessageId = vos.get(vos.size() - 1).getId();
            boolean hasMore = messageService.hasMoreMessages(conversation.getId(), lastMessageId);
            response.setHasMore(hasMore);
            response.setNextCursor(hasMore ? lastMessageId : null);
        } else {
            response.setHasMore(false);
            response.setNextCursor(null);
        }

        return response;
    }

    /**
     * 创建新会话
     */
    private Flux<StreamChatResponse> streamNewConversation(String userMessage) {
        String newUuid = UUIDUtils.simple();
        Long userId = getUserId();

        // 创建会话记录
        Conversation conversation = Conversation.builder()
                .uuid(newUuid)
                .userId(userId)
                .build();
        conversationService.createConversation(conversation);

        // 保存用户消息
        messageService.saveUserMessage(conversation.getId(), userMessage);

        // 基于工作流调用并以SSE切片流式返回
        return streamWorkflowAndPersist(newUuid, conversation.getId(), userMessage);
    }

    /**
     * 处理现有会话
     */
    private Flux<StreamChatResponse> streamExistingConversation(String uuid, String userMessage) {
        // 查询会话
        LambdaQueryWrapper<Conversation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Conversation::getUuid, uuid)
                .eq(Conversation::getIsDelete, 0);
        Conversation conversation = conversationService.getOne(queryWrapper);

        if (conversation == null) {
            throw new RuntimeException("会话不存在: " + uuid);
        }

        // 保存用户消息
        messageService.saveUserMessage(conversation.getId(), userMessage);

        // 更新会话时间
        conversation.setUpdateTime(new Date());
        conversationService.updateById(conversation);

        // 基于工作流调用并以SSE切片流式返回（上下文目前由工作流内部自行处理）
        return streamWorkflowAndPersist(uuid, conversation.getId(), userMessage);
    }

    /**
     * 通过工作流生成助手回复，并以SSE切片流式返回
     */
    private Flux<StreamChatResponse> streamWorkflowAndPersist(String uuid, Long conversationId, String userMessage) {
        return Flux.create((FluxSink<StreamChatResponse> sink) -> {
            DefaultWorkflowProgressReporter reporter = new DefaultWorkflowProgressReporter(uuid, sink);
            // Heartbeat keeps the SSE connection alive while the workflow performs long-running tasks.
            Disposable heartbeat = Schedulers.parallel().schedulePeriodically(() -> {
                if (!reporter.isCancelled()) {
                    reporter.publishHeartbeat();
                }
            }, HEARTBEAT_INTERVAL_MILLIS, HEARTBEAT_INTERVAL_MILLIS, TimeUnit.MILLISECONDS);
            sink.onCancel(heartbeat);
            sink.onDispose(heartbeat);
            Schedulers.boundedElastic().schedule(() -> {
                WorkflowProgressContextHolder.setReporter(reporter);
                try {
                    reporter.publishStage(ChatStageEnum.RECEIVED, ChatStageEnum.RECEIVED.getDescription());
                    reporter.publishStage(ChatStageEnum.WORKFLOW_START, ChatStageEnum.WORKFLOW_START.getDescription());

                    Map<String, Object> inputs = Map.of(
                            MedicineStateKeyEnum.USER_MESSAGE.getKey(), userMessage);

                    AsyncGenerator<NodeOutput> generator = compiledGraph.stream(inputs);
                    OverAllState lastState = null;
                    for (NodeOutput nodeOutput : generator) {
                        if (reporter.isCancelled()) {
                            return;
                        }
                        lastState = nodeOutput.state();
                        ChatStageEnum.fromNodeId(nodeOutput.node())
                                .ifPresent(stage -> reporter.publishStage(stage, stage.getDescription()));
                    }

                    if (reporter.isCancelled()) {
                        return;
                    }

                    if (lastState == null) {
                        lastState = AsyncGenerator.resultValue(generator)
                                .filter(OverAllState.class::isInstance)
                                .map(OverAllState.class::cast)
                                .orElse(null);
                    }

                    String systemReply = Optional.ofNullable(lastState)
                            .map(OverAllState::data)
                            .map(data -> data.get(MedicineStateKeyEnum.SYSTEM_RESPONSE.getKey()))
                            .map(Object::toString)
                            .filter(StringUtils::hasText)
                            .orElse("抱歉，暂时无法生成有效回复。");

                    reporter.publishStage(ChatStageEnum.RESPONSE_STREAM, ChatStageEnum.RESPONSE_STREAM.getDescription());
                    streamAssistantResponse(reporter, conversationId, systemReply, ChatStageEnum.COMPLETED);
                } catch (Exception ex) {
                    String fallback = "抱歉，服务暂时不可用，请稍后再试。";
                    log.error("workflow execution failed", ex);
                    reporter.publishStage(ChatStageEnum.FAILED, fallback, false);
                    reporter.publishStage(ChatStageEnum.RESPONSE_STREAM, ChatStageEnum.RESPONSE_STREAM.getDescription());
                    streamAssistantResponse(reporter, conversationId, fallback, ChatStageEnum.FAILED);
                } finally {
                    if (!heartbeat.isDisposed()) {
                        heartbeat.dispose();
                    }
                    WorkflowProgressContextHolder.clear();
                    if (!sink.isCancelled()) {
                        sink.complete();
                    }
                }
            });
        }, FluxSink.OverflowStrategy.BUFFER);
    }

    private void streamAssistantResponse(DefaultWorkflowProgressReporter reporter, Long conversationId, String fullText, ChatStageEnum finalStage) {
        String finalText = StringUtils.hasText(fullText) ? fullText : "";
        if (StringUtils.hasText(finalText)) {
            reporter.publishResponseChunk(finalText);
        }
        Message assistantMsg = messageService.saveAssistantMessage(conversationId, finalText);
        reporter.publishResponseCompleted(assistantMsg.getUuid(), finalStage);
    }
}
