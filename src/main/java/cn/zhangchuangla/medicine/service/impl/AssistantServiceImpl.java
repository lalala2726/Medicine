package cn.zhangchuangla.medicine.service.impl;

import cn.zhangchuangla.medicine.common.base.BaseService;
import cn.zhangchuangla.medicine.common.utils.UUIDUtils;
import cn.zhangchuangla.medicine.llm.service.OpenAiClientFactory;
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
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;

import java.util.Collections;
import java.util.Date;
import java.util.List;
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
    private final OpenAiClientFactory openAiClientFactory;

    public AssistantServiceImpl(@Qualifier("medicineWorkflowService") StateGraph writingAssistantGraph,
                                ConversationService conversationService,
                                MessageService messageService, OpenAiClientFactory openAiClientFactory)
            throws GraphStateException {
        this.compiledGraph = writingAssistantGraph.compile();
        this.conversationService = conversationService;
        this.messageService = messageService;
        this.openAiClientFactory = openAiClientFactory;
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

        // 流式调用AI
        return streamAiAndPersist(newUuid, conversation.getId(), Collections.singletonList(userMessage));
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

        // 加载上下文消息（最近N条）
        List<Message> contextMessages = messageService.getConversationMessages(conversation.getId(), 10);

        // 构建对话上下文
        List<String> context = contextMessages.stream()
                .map(msg -> msg.getRole() + ": " + msg.getContent())
                .collect(Collectors.toList());
        context.add("user: " + userMessage);

        // 保存用户消息
        messageService.saveUserMessage(conversation.getId(), userMessage);

        // 更新会话时间
        conversation.setUpdateTime(new Date());
        conversationService.updateById(conversation);

        // 流式调用AI
        return streamAiAndPersist(uuid, conversation.getId(), context);
    }

    /**
     * 调用AI生成回复
     */
    private Flux<StreamChatResponse> streamAiAndPersist(String uuid, Long conversationId, List<String> context) {
        String fullContext = String.join("\n", context);
        ChatClient chatClient = openAiClientFactory.chatClient();

        StringBuilder acc = new StringBuilder();
        Flux<String> contentFlux = chatClient.prompt().user(fullContext).stream().content();

        return contentFlux
                .map(chunk -> {
                    acc.append(chunk);
                    return StreamChatResponse.builder()
                            .uuid(uuid)
                            .content(chunk)
                            .finished(false)
                            .build();
                })
                .concatWith(Flux.defer(() -> {
                    // 在流完成时持久化整条助手消息并返回结束事件
                    Message assistantMsg = messageService.saveAssistantMessage(conversationId, acc.toString());
                    StreamChatResponse end = StreamChatResponse.builder()
                            .uuid(uuid)
                            .messageUuid(assistantMsg.getUuid())
                            .content("")
                            .finished(true)
                            .build();
                    // 将uuid与messageUuid编码到最后一个块（可选：使用专用字段）
                    // 这里沿用现有字段集，客户端可在结束时调用历史或使用外层上下文携带uuid
                    return Flux.just(end);
                }));
    }
}
