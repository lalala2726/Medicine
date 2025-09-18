package cn.zhangchuangla.medicine.service.impl;

import cn.zhangchuangla.medicine.common.base.BaseService;
import cn.zhangchuangla.medicine.common.utils.UUIDUtils;
import cn.zhangchuangla.medicine.model.entity.Conversation;
import cn.zhangchuangla.medicine.model.entity.Message;
import cn.zhangchuangla.medicine.model.vo.chat.StreamChatResponse;
import cn.zhangchuangla.medicine.model.vo.llm.chat.ChatHistoryResponse;
import cn.zhangchuangla.medicine.model.vo.llm.chat.UserMessageRequest;
import cn.zhangchuangla.medicine.service.AssistantService;
import cn.zhangchuangla.medicine.service.ConversationService;
import cn.zhangchuangla.medicine.service.MessageService;
import com.alibaba.cloud.ai.graph.CompiledGraph;
import com.alibaba.cloud.ai.graph.StateGraph;
import com.alibaba.cloud.ai.graph.exception.GraphStateException;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Chuang
 * <p>
 * created on 2025/9/16 10:49
 */
@Service
public class AssistantServiceImpl implements AssistantService, BaseService {

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
    public ChatHistoryResponse history(String uuid, Integer limit) {
        if (!StringUtils.hasText(uuid)) {
            throw new IllegalArgumentException("uuid不能为空");
        }

        LambdaQueryWrapper<Conversation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Conversation::getUuid, uuid)
                .eq(Conversation::getIsDelete, 0);
        Conversation conversation = conversationService.getOne(queryWrapper);
        if (conversation == null) {
            throw new RuntimeException("会话不存在: " + uuid);
        }

        int size = (limit == null || limit <= 0) ? 50 : limit;
        List<Message> messages = messageService.getConversationMessages(conversation.getId(), size);
        List<ChatHistoryResponse.MessageVO> vos = messages.stream()
                .map(msg -> {
                    ChatHistoryResponse.MessageVO vo = new ChatHistoryResponse.MessageVO();
                    vo.setRole(String.valueOf(msg.getRole()));
                    vo.setContent(msg.getContent());
                    vo.setCreateTime(msg.getCreateTime());
                    return vo;
                })
                .collect(Collectors.toList());

        ChatHistoryResponse response = new ChatHistoryResponse();
        response.setUuid(uuid);
        response.setMessages(vos);
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
        return Flux.defer(() -> {
            try {
                // 走工作流，得到最终系统回复（非token级，随后切片成流）
                Map<String, Object> inputs = Map.of(
                        cn.zhangchuangla.medicine.enums.MedicineStateKeyEnum.USER_MESSAGE.getKey(), userMessage
                );
                // 直接使用invoke完成编排（参考 demo 用法）
                var resultOpt = compiledGraph.invoke(inputs);
                if (resultOpt.isEmpty()) {
                    String fallback = "抱歉，暂时无法生成回复，请稍后重试。";
                    return persistAndWrapAsStream(uuid, conversationId, fallback);
                }
                Map<String, Object> data = resultOpt.get().data();
                Object systemReplyObj = data.get(cn.zhangchuangla.medicine.enums.MedicineStateKeyEnum.SYSTEM_RESPONSE.getKey());
                String systemReply = systemReplyObj == null ? "" : String.valueOf(systemReplyObj);
                if (!StringUtils.hasText(systemReply)) {
                    systemReply = "抱歉，暂时无法生成有效回复。";
                }
                return persistAndWrapAsStream(uuid, conversationId, systemReply);
            } catch (Exception ex) {
                String fallback = "抱歉，服务暂时不可用，请稍后再试。";
                return persistAndWrapAsStream(uuid, conversationId, fallback);
            }
        });
    }

    private Flux<StreamChatResponse> persistAndWrapAsStream(String uuid, Long conversationId, String fullText) {
        // 切片为小块以SSE流式输出
        List<String> chunks = splitToChunks(fullText);
        StringBuilder acc = new StringBuilder();
        Flux<StreamChatResponse> body = Flux.fromIterable(chunks)
                .map(part -> {
                    acc.append(part);
                    return StreamChatResponse.builder()
                            .uuid(uuid)
                            .content(part)
                            .finished(false)
                            .build();
                });

        return body.concatWith(Flux.defer(() -> {
            // 完成后统一落库，并返回结束事件
            Message assistantMsg = messageService.saveAssistantMessage(conversationId, acc.toString());
            StreamChatResponse end = StreamChatResponse.builder()
                    .uuid(uuid)
                    .messageUuid(assistantMsg.getUuid())
                    .content("")
                    .finished(true)
                    .build();
            return Flux.just(end);
        }));
    }

    private List<String> splitToChunks(String text) {
        if (!StringUtils.hasText(text)) {
            return List.of("");
        }
        int len = text.length();
        if (len <= 80) {
            return List.of(text);
        }
        int parts = (len + 80 - 1) / 80;
        return java.util.stream.IntStream.range(0, parts)
                .mapToObj(i -> {
                    int start = i * 80;
                    int end = Math.min(start + 80, len);
                    return text.substring(start, end);
                })
                .collect(Collectors.toList());
    }
}
