package cn.zhangchuangla.medicine.client.service.impl;

import cn.zhangchuangla.medicine.client.mapper.NotifyMessageMapper;
import cn.zhangchuangla.medicine.client.model.request.NotifyMessageListRequest;
import cn.zhangchuangla.medicine.client.model.vo.NotifyMessageDetailVo;
import cn.zhangchuangla.medicine.client.model.vo.NotifyMessageListVo;
import cn.zhangchuangla.medicine.client.service.NotifyMessageService;
import cn.zhangchuangla.medicine.client.service.UserNotifyMessageService;
import cn.zhangchuangla.medicine.common.core.enums.ResponseCode;
import cn.zhangchuangla.medicine.common.core.exception.ServiceException;
import cn.zhangchuangla.medicine.common.core.utils.Assert;
import cn.zhangchuangla.medicine.common.security.base.BaseService;
import cn.zhangchuangla.medicine.model.entity.NotifyMessage;
import cn.zhangchuangla.medicine.model.entity.UserNotifyMessage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotifyMessageServiceImpl extends ServiceImpl<NotifyMessageMapper, NotifyMessage>
        implements NotifyMessageService, BaseService {

    private static final String RECEIVER_ALL_USER = "ALL_USER";
    private static final String RECEIVER_DESIGNATED_USER = "DESIGNATED_USER";

    private final UserNotifyMessageService userNotifyMessageService;

    @Override
    public Page<NotifyMessageListVo> listUserMessages(NotifyMessageListRequest request) {
        Page<NotifyMessage> page = request.toPage();
        Long userId = getUserId();
        Page<NotifyMessage> messagePage = baseMapper.selectUserMessagePage(page, request, userId);
        List<NotifyMessage> records = messagePage.getRecords();
        Map<Long, UserNotifyMessage> userMessageMap = loadUserMessages(userId, records);

        List<NotifyMessageListVo> rows = records.stream()
                .map(message -> {
                    NotifyMessageListVo vo = new NotifyMessageListVo();
                    BeanUtils.copyProperties(message, vo);
                    vo.setContentSummary(buildContentSummary(message.getContent()));
                    UserNotifyMessage userMessage = userMessageMap.get(message.getId());
                    vo.setIsRead(userMessage == null ? 0 : Optional.ofNullable(userMessage.getIsRead()).orElse(0));
                    return vo;
                })
                .toList();

        Page<NotifyMessageListVo> result = new Page<>(messagePage.getCurrent(), messagePage.getSize(), messagePage.getTotal());
        result.setRecords(rows);
        return result;
    }

    @Override
    public NotifyMessageDetailVo getMessageDetail(Long notifyId) {
        Assert.isPositive(notifyId, "通知ID不能为空");
        Long userId = getUserId();
        NotifyMessage message = lambdaQuery()
                .eq(NotifyMessage::getId, notifyId)
                .eq(NotifyMessage::getIsDeleted, 0)
                .one();
        if (message == null) {
            throw new ServiceException(ResponseCode.NOT_FOUND, "通知消息不存在");
        }
        UserNotifyMessage userMessage = userNotifyMessageService.lambdaQuery()
                .eq(UserNotifyMessage::getNotifyId, notifyId)
                .eq(UserNotifyMessage::getUserId, userId)
                .one();
        ensureUserCanView(message, userMessage);

        NotifyMessageDetailVo detailVo = new NotifyMessageDetailVo();
        BeanUtils.copyProperties(message, detailVo);
        return detailVo;
    }

    @Override
    public boolean deleteMessage(Long notifyId) {
        Assert.isPositive(notifyId, "通知ID不能为空");
        Long userId = getUserId();
        NotifyMessage message = lambdaQuery()
                .eq(NotifyMessage::getId, notifyId)
                .eq(NotifyMessage::getIsDeleted, 0)
                .one();
        if (message == null) {
            throw new ServiceException(ResponseCode.NOT_FOUND, "通知消息不存在");
        }
        String receiverType = message.getReceiverType();
        Date now = new Date();
        if (RECEIVER_ALL_USER.equals(receiverType)) {
            // 广播消息：用用户消息表标记删除，避免影响其他用户。
            UserNotifyMessage record = userNotifyMessageService.lambdaQuery()
                    .eq(UserNotifyMessage::getNotifyId, notifyId)
                    .eq(UserNotifyMessage::getUserId, userId)
                    .one();
            if (record == null) {
                UserNotifyMessage created = UserNotifyMessage.builder()
                        .notifyId(notifyId)
                        .userId(userId)
                        .isRead(0)
                        .isDeleted(1)
                        .createTime(now)
                        .build();
                return userNotifyMessageService.save(created);
            }
            record.setIsDeleted(1);
            return userNotifyMessageService.updateById(record);
        }
        if (RECEIVER_DESIGNATED_USER.equals(receiverType)) {
            // 指定消息：仅删除用户关联记录。
            UserNotifyMessage record = userNotifyMessageService.lambdaQuery()
                    .eq(UserNotifyMessage::getNotifyId, notifyId)
                    .eq(UserNotifyMessage::getUserId, userId)
                    .eq(UserNotifyMessage::getIsDeleted, 0)
                    .one();
            if (record == null) {
                throw new ServiceException(ResponseCode.NOT_FOUND, "通知消息不存在");
            }
            record.setIsDeleted(1);
            return userNotifyMessageService.updateById(record);
        }
        throw new ServiceException(ResponseCode.PARAM_ERROR, "接收者类型不支持");
    }

    private boolean markAsReadIfNeeded(NotifyMessage message, UserNotifyMessage userMessage, Long userId) {
        if (userMessage != null && userMessage.getIsRead() != null && userMessage.getIsRead() == 1) {
            return false;
        }
        Long notifyId = message.getId();
        Date now = new Date();
        UserNotifyMessage record = userMessage;
        if (record == null) {
            // 广播消息首次阅读时补齐用户记录，记录阅读时间。
            UserNotifyMessage created = UserNotifyMessage.builder()
                    .notifyId(notifyId)
                    .userId(userId)
                    .isRead(1)
                    .readTime(now)
                    .isDeleted(0)
                    .createTime(now)
                    .build();
            userNotifyMessageService.save(created);
            return true;
        }
        if (record.getIsRead() == null || record.getIsRead() == 0) {
            record.setIsRead(1);
            record.setReadTime(now);
            userNotifyMessageService.updateById(record);
        }
        return true;
    }

    private void ensureUserCanView(NotifyMessage message, UserNotifyMessage userMessage) {
        String receiverType = message.getReceiverType();
        if (RECEIVER_ALL_USER.equals(receiverType)) {
            if (userMessage != null && userMessage.getIsDeleted() != null && userMessage.getIsDeleted() == 1) {
                throw new ServiceException(ResponseCode.NOT_FOUND, "通知消息不存在");
            }
            return;
        }
        if (RECEIVER_DESIGNATED_USER.equals(receiverType)) {
            if (userMessage == null || (userMessage.getIsDeleted() != null && userMessage.getIsDeleted() == 1)) {
                throw new ServiceException(ResponseCode.NOT_FOUND, "通知消息不存在");
            }
            return;
        }
        throw new ServiceException(ResponseCode.PARAM_ERROR, "接收者类型不支持");
    }

    private Map<Long, UserNotifyMessage> loadUserMessages(Long userId, List<NotifyMessage> messages) {
        if (messages == null || messages.isEmpty()) {
            return Map.of();
        }
        List<Long> ids = messages.stream()
                .map(NotifyMessage::getId)
                .filter(id -> id != null && id > 0)
                .distinct()
                .toList();
        if (ids.isEmpty()) {
            return Map.of();
        }
        return userNotifyMessageService.lambdaQuery()
                .eq(UserNotifyMessage::getUserId, userId)
                .in(UserNotifyMessage::getNotifyId, ids)
                .list()
                .stream()
                .collect(Collectors.toMap(UserNotifyMessage::getNotifyId, Function.identity(), (a, b) -> a));
    }

    private String buildContentSummary(String content) {
        if (content == null || content.isBlank()) {
            return "";
        }
        String normalized = content.trim();
        return normalized.length() <= 25 ? normalized : normalized.substring(0, 25);
    }
}
