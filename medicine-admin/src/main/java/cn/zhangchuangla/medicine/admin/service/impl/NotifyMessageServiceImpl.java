package cn.zhangchuangla.medicine.admin.service.impl;

import cn.zhangchuangla.medicine.admin.mapper.NotifyMessageMapper;
import cn.zhangchuangla.medicine.admin.model.request.NotifyMessageListRequest;
import cn.zhangchuangla.medicine.admin.model.request.NotifyMessageSendRequest;
import cn.zhangchuangla.medicine.admin.model.request.NotifyMessageSystemPushRequest;
import cn.zhangchuangla.medicine.admin.model.request.NotifyMessageUpdateRequest;
import cn.zhangchuangla.medicine.admin.model.vo.NotifyMessageDetailVo;
import cn.zhangchuangla.medicine.admin.service.NotifyMessageService;
import cn.zhangchuangla.medicine.admin.service.UserNotifyMessageService;
import cn.zhangchuangla.medicine.common.core.enums.ResponseCode;
import cn.zhangchuangla.medicine.common.core.exception.ServiceException;
import cn.zhangchuangla.medicine.common.core.utils.Assert;
import cn.zhangchuangla.medicine.common.rabbitmq.message.NotifyMessagePushMessage;
import cn.zhangchuangla.medicine.common.rabbitmq.publisher.NotifyMessagePublisher;
import cn.zhangchuangla.medicine.common.security.utils.SecurityUtils;
import cn.zhangchuangla.medicine.model.entity.NotifyMessage;
import cn.zhangchuangla.medicine.model.entity.UserNotifyMessage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotifyMessageServiceImpl extends ServiceImpl<NotifyMessageMapper, NotifyMessage>
        implements NotifyMessageService {

    private static final String RECEIVER_ALL_USER = "ALL_USER";
    private static final String RECEIVER_DESIGNATED_USER = "DESIGNATED_USER";
    private static final String SENDER_SYSTEM = "SYSTEM";
    private static final String SENDER_ADMIN = "ADMIN";
    private static final String SYSTEM_SENDER_NAME = "系统通知";
    private static final int BATCH_SIZE = 500;

    private final NotifyMessagePublisher notifyMessagePublisher;
    private final UserNotifyMessageService userNotifyMessageService;

    @Override
    public Page<NotifyMessage> listNotifyMessages(NotifyMessageListRequest request) {
        Page<NotifyMessage> page = request.toPage();
        return baseMapper.selectNotifyMessagePage(page, request);
    }

    @Override
    public NotifyMessageDetailVo getNotifyMessageDetail(Long id) {
        Assert.isPositive(id, "通知ID不能为空");
        NotifyMessage message = getById(id);
        if (message == null || message.getIsDeleted() != null && message.getIsDeleted() != 0) {
            throw new ServiceException(ResponseCode.NOT_FOUND, "通知消息不存在");
        }
        NotifyMessageDetailVo detailVo = new NotifyMessageDetailVo();
        BeanUtils.copyProperties(message, detailVo);
        if (RECEIVER_DESIGNATED_USER.equalsIgnoreCase(message.getReceiverType())) {
            List<Long> receiverIds = userNotifyMessageService.lambdaQuery()
                    .select(UserNotifyMessage::getUserId)
                    .eq(UserNotifyMessage::getNotifyId, message.getId())
                    .eq(UserNotifyMessage::getIsDeleted, 0)
                    .list()
                    .stream()
                    .map(UserNotifyMessage::getUserId)
                    .filter(Objects::nonNull)
                    .distinct()
                    .toList();
            detailVo.setReceiverIds(receiverIds);
        }
        return detailVo;
    }

    @Override
    public boolean sendAdminMessage(NotifyMessageSendRequest request) {
        validateSendRequest(request.getTitle(), request.getContent(), request.getType(), request.getReceiverType());
        String receiverType = request.getReceiverType().trim();
        if (!RECEIVER_ALL_USER.equals(receiverType) && !RECEIVER_DESIGNATED_USER.equals(receiverType)) {
            throw new ServiceException(ResponseCode.PARAM_ERROR, "接收者类型不支持");
        }
        boolean sendAsSystem = !StringUtils.hasText(request.getSenderName());
        String senderType = sendAsSystem ? SENDER_SYSTEM : SENDER_ADMIN;
        Long senderId = sendAsSystem ? 0L : SecurityUtils.getUserId();
        String senderName = sendAsSystem ? SYSTEM_SENDER_NAME : request.getSenderName().trim();
        Date now = new Date();

        if (RECEIVER_ALL_USER.equals(receiverType)) {
            // 全员消息直接落库，避免写入用户关联表。
            NotifyMessage message = buildNotifyMessage(request, senderType, senderId, senderName, now);
            return save(message);
        }
        List<Long> receiverIds = filterReceiverIds(request.getReceiverIds());
        if (receiverIds.isEmpty()) {
            throw new ServiceException(ResponseCode.PARAM_ERROR, "指定用户不能为空");
        }
        // 指定用户通过 MQ 异步落库与分批写入关联表。
        NotifyMessagePushMessage pushMessage = buildPushMessage(request, senderType, senderId, senderName, now, receiverIds);
        notifyMessagePublisher.publish(pushMessage);
        return true;
    }

    @Override
    public boolean updateAdminMessage(NotifyMessageUpdateRequest request) {
        Assert.notNull(request, "请求参数不能为空");
        Assert.isPositive(request.getId(), "通知ID不能为空");
        NotifyMessage message = getById(request.getId());
        if (message == null || message.getIsDeleted() != null && message.getIsDeleted() != 0) {
            throw new ServiceException(ResponseCode.NOT_FOUND, "通知消息不存在");
        }
        if (!SENDER_ADMIN.equalsIgnoreCase(message.getSenderType())) {
            throw new ServiceException(ResponseCode.OPERATION_ERROR, "仅支持编辑管理员发送的消息");
        }
        message.setTitle(request.getTitle().trim());
        message.setContent(request.getContent());
        message.setType(request.getType().trim());
        return updateById(message);
    }

    @Override
    public boolean pushMessageAsync(NotifyMessageSystemPushRequest request) {
        validateSendRequest(request.getTitle(), request.getContent(), request.getType(), request.getReceiverType());
        String receiverType = request.getReceiverType().trim();
        if (!RECEIVER_ALL_USER.equals(receiverType) && !RECEIVER_DESIGNATED_USER.equals(receiverType)) {
            throw new ServiceException(ResponseCode.PARAM_ERROR, "接收者类型不支持");
        }
        List<Long> receiverIds = RECEIVER_DESIGNATED_USER.equals(receiverType)
                ? filterReceiverIds(request.getReceiverIds())
                : List.of();
        if (RECEIVER_DESIGNATED_USER.equals(receiverType) && receiverIds.isEmpty()) {
            throw new ServiceException(ResponseCode.PARAM_ERROR, "指定用户不能为空");
        }
        NotifyMessagePushMessage pushMessage = NotifyMessagePushMessage.builder()
                .title(request.getTitle().trim())
                .content(request.getContent())
                .type(request.getType().trim())
                .receiverType(receiverType)
                .receiverIds(receiverIds)
                .senderType(SENDER_SYSTEM)
                .senderId(0L)
                .senderName(SYSTEM_SENDER_NAME)
                .publishTime(new Date())
                .build();
        // 系统推送统一走 MQ，减少高峰期写入压力。
        notifyMessagePublisher.publish(pushMessage);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handlePushMessage(NotifyMessagePushMessage message) {
        if (message == null || !StringUtils.hasText(message.getTitle()) || !StringUtils.hasText(message.getReceiverType())) {
            log.warn("Skip notify message push, payload invalid: {}", message);
            return;
        }
        String receiverType = message.getReceiverType().trim();
        if (!RECEIVER_ALL_USER.equals(receiverType) && !RECEIVER_DESIGNATED_USER.equals(receiverType)) {
            log.warn("Skip notify message push, receiver type invalid: {}", message);
            return;
        }
        List<Long> receiverIds = RECEIVER_DESIGNATED_USER.equals(receiverType)
                ? filterReceiverIds(message.getReceiverIds())
                : List.of();
        if (RECEIVER_DESIGNATED_USER.equals(receiverType) && receiverIds.isEmpty()) {
            log.warn("Skip notify message push, receiverIds empty: {}", message);
            return;
        }
        Date now = new Date();
        // MQ 消费统一落库，必要时再补充用户关联表。
        NotifyMessage notifyMessage = buildNotifyMessage(message, now);
        boolean saved = save(notifyMessage);
        if (!saved) {
            throw new ServiceException(ResponseCode.OPERATION_ERROR, "通知消息保存失败");
        }
        if (RECEIVER_DESIGNATED_USER.equals(receiverType)) {
            saveUserNotifyMessages(notifyMessage.getId(), receiverIds, now);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteAdminMessages(List<Long> ids) {
        Assert.notEmpty(ids, "通知ID不能为空");
        List<Long> normalized = ids.stream()
                .filter(id -> id != null && id > 0)
                .distinct()
                .toList();
        if (normalized.isEmpty()) {
            throw new ServiceException(ResponseCode.PARAM_ERROR, "通知ID不能为空");
        }
        List<NotifyMessage> messages = listByIds(normalized);
        if (messages.size() != normalized.size()) {
            throw new ServiceException(ResponseCode.NOT_FOUND, "通知消息不存在");
        }
        // 系统消息或非管理员来源不允许删除，避免误删全局公告。
        boolean hasNonAdminMessage = messages.stream()
                .anyMatch(message -> !SENDER_ADMIN.equalsIgnoreCase(message.getSenderType()));
        if (hasNonAdminMessage) {
            throw new ServiceException(ResponseCode.OPERATION_ERROR, "仅支持删除管理员发送的消息");
        }
        // 管理端删除仅做软删除，客户端不可再查询到。
        return lambdaUpdate()
                .set(NotifyMessage::getIsDeleted, 1)
                .in(NotifyMessage::getId, normalized)
                .update();
    }

    private void validateSendRequest(String title, String content, String type, String receiverType) {
        Assert.notEmpty(title, "通知标题不能为空");
        Assert.notEmpty(content, "通知内容不能为空");
        Assert.notEmpty(type, "通知类型不能为空");
        Assert.notEmpty(receiverType, "接收者类型不能为空");
    }

    private NotifyMessage buildNotifyMessage(NotifyMessageSendRequest request,
                                             String senderType,
                                             Long senderId,
                                             String senderName,
                                             Date now) {
        return NotifyMessage.builder()
                .title(request.getTitle().trim())
                .content(request.getContent())
                .senderType(senderType)
                .senderId(senderId)
                .senderName(senderName)
                .receiverType(request.getReceiverType().trim())
                .type(request.getType().trim())
                .publishTime(now)
                .isDeleted(0)
                .createTime(now)
                .build();
    }

    private NotifyMessagePushMessage buildPushMessage(NotifyMessageSendRequest request,
                                                      String senderType,
                                                      Long senderId,
                                                      String senderName,
                                                      Date now,
                                                      List<Long> receiverIds) {
        return NotifyMessagePushMessage.builder()
                .title(request.getTitle().trim())
                .content(request.getContent())
                .senderType(senderType)
                .senderId(senderId)
                .senderName(senderName)
                .receiverType(request.getReceiverType().trim())
                .type(request.getType().trim())
                .publishTime(now)
                .receiverIds(receiverIds)
                .build();
    }

    private NotifyMessage buildNotifyMessage(NotifyMessagePushMessage message, Date now) {
        String senderType = StringUtils.hasText(message.getSenderType()) ? message.getSenderType().trim() : SENDER_SYSTEM;
        return NotifyMessage.builder()
                .title(message.getTitle() == null ? null : message.getTitle().trim())
                .content(message.getContent())
                .senderType(senderType)
                .senderId(message.getSenderId() == null ? 0L : message.getSenderId())
                .senderName(StringUtils.hasText(message.getSenderName()) ? message.getSenderName().trim() : SYSTEM_SENDER_NAME)
                .receiverType(message.getReceiverType() == null ? null : message.getReceiverType().trim())
                .type(message.getType() == null ? null : message.getType().trim())
                .publishTime(message.getPublishTime() == null ? now : message.getPublishTime())
                .isDeleted(0)
                .createTime(now)
                .build();
    }

    private List<Long> filterReceiverIds(List<Long> receiverIds) {
        if (CollectionUtils.isEmpty(receiverIds)) {
            return List.of();
        }
        return new ArrayList<>(new LinkedHashSet<>(
                receiverIds.stream()
                        .filter(id -> id != null && id > 0)
                        .toList()
        ));
    }

    private void saveUserNotifyMessages(Long notifyId, List<Long> receiverIds, Date now) {
        int total = receiverIds.size();
        for (int i = 0; i < total; i += BATCH_SIZE) {
            int end = Math.min(i + BATCH_SIZE, total);
            List<UserNotifyMessage> batch = new ArrayList<>(end - i);
            for (int j = i; j < end; j++) {
                Long userId = receiverIds.get(j);
                // 批量创建用户消息关联，默认未读。
                UserNotifyMessage record = UserNotifyMessage.builder()
                        .notifyId(notifyId)
                        .userId(userId)
                        .isRead(0)
                        .isDeleted(0)
                        .createTime(now)
                        .build();
                batch.add(record);
            }
            userNotifyMessageService.saveBatch(batch);
        }
    }
}
