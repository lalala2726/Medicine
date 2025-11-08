package cn.zhangchuangla.medicine.admin.service.impl;

import cn.zhangchuangla.medicine.admin.service.AssistantService;
import cn.zhangchuangla.medicine.admin.service.ConversationService;
import cn.zhangchuangla.medicine.admin.service.MessageService;
import cn.zhangchuangla.medicine.ai.enums.ChatStageEnum;
import cn.zhangchuangla.medicine.ai.workflow.progress.DefaultWorkflowProgressReporter;
import cn.zhangchuangla.medicine.common.core.exception.ServiceException;
import cn.zhangchuangla.medicine.common.core.utils.UUIDUtils;
import cn.zhangchuangla.medicine.common.security.base.BaseService;
import cn.zhangchuangla.medicine.model.entity.Conversation;
import cn.zhangchuangla.medicine.model.entity.Message;
import cn.zhangchuangla.medicine.model.request.assistant.HistoryRequest;
import cn.zhangchuangla.medicine.model.vo.chat.StreamChatResponse;
import cn.zhangchuangla.medicine.model.vo.llm.chat.ChatHistoryResponse;
import cn.zhangchuangla.medicine.model.vo.llm.chat.UserMessageRequest;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Chuang
 * <p>
 * created on 2025/9/16 
 */
@Slf4j
@Service
public class AssistantServiceImpl implements AssistantService, BaseService {

    /**
     * Interval for sending SSE heartbeat messages (milliseconds).
     */
    private static final long HEARTBEAT_INTERVAL_MILLIS = 10_000L;

    private final ConversationService conversationService;
    private final MessageService messageService;

    public AssistantServiceImpl(ConversationService conversationService,
                                MessageService messageService) {
        this.conversationService = conversationService;
        this.messageService = messageService;
    }

    /**
     * 处理用户聊天请求，根据UUID判断是新对话还是已有对话的继续
     *
     * @param userMessageRequest 用户消息请求对象，包含UUID和消息内容
     * @return Flux<StreamChatResponse> 流式聊天响应
     */
    @Override
    public Flux<StreamChatResponse> chat(UserMessageRequest userMessageRequest) {
        String uuid = userMessageRequest.getUuid();
        String userMessage = userMessageRequest.getMessage();

        // 根据UUID是否存在判断是新建对话还是继续已有对话
        if (!StringUtils.hasText(uuid)) {
            return streamNewConversation(userMessage);
        } else {
            return streamExistingConversation(uuid, userMessage);
        }
    }


    /**
     * 获取聊天历史记录
     *
     * @param request 历史记录请求参数，包含会话UUID、游标位置和限制数量
     * @return 聊天历史记录响应对象，包含消息列表和分页信息
     */
    @Override
    @Transactional(readOnly = true)
    public ChatHistoryResponse history(HistoryRequest request) {
        if (request == null || !StringUtils.hasText(request.getUuid())) {
            throw new ServiceException("uuid不能为空");
        }

        String uuid = request.getUuid();
        Long cursor = request.getCursor();
        // 处理分页限制，如果未设置或小于等于0则默认为20
        int limit = request.getLimit() != null && request.getLimit() > 0 ? request.getLimit() : 20;

        // 查询会话信息，确保会话存在且未被删除
        LambdaQueryWrapper<Conversation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Conversation::getUuid, uuid)
                .eq(Conversation::getIsDelete, 0);
        Conversation conversation = conversationService.getOne(queryWrapper);
        if (conversation == null) {
            throw new ServiceException("会话不存在: " + uuid);
        }

        // 根据游标获取会话消息列表
        List<Message> messages = messageService.getConversationMessagesCursor(conversation.getId(), cursor, limit);
        // 将消息实体转换为响应视图对象
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

        // 构建响应结果
        ChatHistoryResponse response = new ChatHistoryResponse();
        response.setUuid(uuid);
        response.setMessages(vos);

        // 设置分页相关信息，判断是否还有更多数据
        if (!vos.isEmpty()) {
            Long lastMessageId = vos.getLast().getId();
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
     *
     * @param userMessage 用户发送的消息内容
     * @return 返回流式的聊天响应数据
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
     *
     * @param uuid        会话唯一标识符
     * @param userMessage 用户发送的消息内容
     * @return 流式返回聊天响应结果
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
     * 通过工作流生成助手回复，并以SSE切片流式返回。
     *
     * @param uuid           请求唯一标识符，用于追踪请求流程
     * @param conversationId 对话ID，用于关联当前对话上下文
     * @param userMessage    用户输入的消息内容
     * @return 返回一个Flux流，其中包含逐步生成的聊天响应片段（StreamChatResponse）
     */
    private Flux<StreamChatResponse> streamWorkflowAndPersist(String uuid, Long conversationId, String userMessage) {
        return null;
    }


    /**
     * 流式传输助手响应消息
     *
     * @param reporter       进度报告器，用于发布响应块和完成状态
     * @param conversationId 对话ID，用于关联消息
     * @param fullText       完整的响应文本内容
     * @param finalStage     最终聊天阶段枚举值
     */
    private void streamAssistantResponse(DefaultWorkflowProgressReporter reporter, Long conversationId, String fullText, ChatStageEnum finalStage) {
        // 处理响应文本，确保不为null
        String finalText = StringUtils.hasText(fullText) ? fullText : "";

        // 如果有文本内容，则发布响应块
        if (StringUtils.hasText(finalText)) {
            reporter.publishResponseChunk(finalText);
        }

        // 保存助手消息并发布响应完成状态
        Message assistantMsg = messageService.saveAssistantMessage(conversationId, finalText);
        reporter.publishResponseCompleted(assistantMsg.getUuid(), finalStage);
    }

}
