package cn.zhangchuangla.medicine.admin.service.impl;

import cn.zhangchuangla.medicine.admin.mapper.KbDocumentMapper;
import cn.zhangchuangla.medicine.admin.model.request.KnowledgeBaseImportRequest;
import cn.zhangchuangla.medicine.admin.publisher.KnowledgeImportPublisher;
import cn.zhangchuangla.medicine.admin.service.KbBaseService;
import cn.zhangchuangla.medicine.admin.service.KbDocumentService;
import cn.zhangchuangla.medicine.common.core.enums.ResponseCode;
import cn.zhangchuangla.medicine.common.core.exception.ServiceException;
import cn.zhangchuangla.medicine.common.core.utils.Assert;
import cn.zhangchuangla.medicine.common.redis.core.RedisCache;
import cn.zhangchuangla.medicine.common.security.base.BaseService;
import cn.zhangchuangla.medicine.model.entity.KbBase;
import cn.zhangchuangla.medicine.model.entity.KbDocument;
import cn.zhangchuangla.medicine.model.mq.KnowledgeImportCommandMessage;
import cn.zhangchuangla.medicine.model.mq.KnowledgeImportResultMessage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;

import java.net.URI;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @author Chuang
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KbDocumentServiceImpl extends ServiceImpl<KbDocumentMapper, KbDocument>
        implements KbDocumentService, BaseService {

    private static final String STATUS_PENDING = "PENDING";
    private static final String STATUS_FAILED = "FAILED";
    private static final String REDIS_LATEST_VERSION_KEY_PREFIX = "kb:latest:";
    private static final long REDIS_LATEST_VERSION_TTL_DAYS = 7L;
    private static final String COMMAND_MESSAGE_TYPE = "knowledge_import_command";
    private static final String SYSTEM_UPDATER = "system";

    private final KbBaseService kbBaseService;
    private final RedisCache redisCache;
    private final KnowledgeImportPublisher knowledgeImportPublisher;

    /**
     * 发起知识库文档导入。
     * <p>
     * 流程：按知识库名称读取配置 -> 为每个 URL 新建文档记录 ->
     * 生成/写入最新版本号 -> 发送 MQ command。
     * </p>
     *
     * @param request 导入请求参数（知识库名称、文件地址集合、切片参数）
     */
    @Override
    public void importKnowledge(@Validated KnowledgeBaseImportRequest request) {
        KbBase kbBase = findKnowledgeBaseByName(request.getKnowledgeName());
        Assert.isTrue(kbBase != null, "知识库不存在");
        Assert.notEmpty(kbBase.getEmbeddingModel(), "知识库向量模型未配置");

        String username = getUsername();
        for (String rawFileUrl : request.getFileUrls()) {
            String fileUrl = rawFileUrl == null ? null : rawFileUrl.trim();
            Assert.notEmpty(fileUrl, "文件地址不能为空");

            KbDocument document = buildPendingDocument(kbBase.getId(), fileUrl, username);
            boolean saved = save(document);
            Assert.isTrue(saved && document.getId() != null, "创建导入文档失败");

            String bizKey = buildBizKey(kbBase.getKnowledgeName(), document.getId());
            Long version = nextVersionAndSetLatest(bizKey);
            KnowledgeImportCommandMessage command = buildCommandMessage(request, kbBase, document, bizKey, version);
            try {
                knowledgeImportPublisher.publishCommand(command);
            } catch (Exception ex) {
                markDocumentFailed(document.getId(), ex.getMessage(), username);
                throw ex;
            }
        }
    }

    /**
     * 处理知识库导入结果消息。
     * <p>
     * 仅处理最新版本事件；若收到旧版本消息则直接丢弃。
     * 对最新事件，回写文档状态与错误信息。
     * </p>
     *
     * @param message AI 回传的导入结果消息
     */
    @Override
    public void handleImportResult(KnowledgeImportResultMessage message) {
        if (message == null) {
            return;
        }

        String bizKey = message.getBiz_key();
        Long messageVersion = message.getVersion();
        Long latestVersion = getLatestVersion(bizKey);
        if (latestVersion != null && messageVersion != null && messageVersion < latestVersion) {
            log.info("丢弃知识库导入旧结果: task_uuid={}, biz_key={}, version={}, latest_version={}, stage={}",
                    message.getTask_uuid(), bizKey, messageVersion, latestVersion, message.getStage());
            return;
        }

        log.info("接收知识库导入结果: task_uuid={}, biz_key={}, version={}, stage={}, message={}",
                message.getTask_uuid(), bizKey, messageVersion, message.getStage(), message.getMessage());

        Long documentId = message.getDocument_id();
        if (documentId == null || documentId <= 0) {
            log.warn("知识库导入结果缺少有效 document_id: task_uuid={}, biz_key={}", message.getTask_uuid(), bizKey);
            return;
        }

        KbDocument document = getById(documentId);
        if (document == null) {
            log.warn("知识库导入结果对应文档不存在: document_id={}, task_uuid={}", documentId, message.getTask_uuid());
            return;
        }

        String stage = normalizeStage(message.getStage());
        if (StringUtils.hasText(stage)) {
            document.setStatus(stage);
        }
        if (STATUS_FAILED.equalsIgnoreCase(stage)) {
            document.setLastError(message.getMessage());
        } else {
            document.setLastError(null);
        }
        document.setUpdateBy(SYSTEM_UPDATER);
        document.setUpdatedAt(new Date());
        boolean updated = updateById(document);
        if (!updated) {
            log.warn("更新知识库导入状态失败: document_id={}, task_uuid={}", documentId, message.getTask_uuid());
        }
    }

    /**
     * 构建待导入文档的初始记录（PENDING）。
     *
     * @param knowledgeBaseId 知识库主键
     * @param fileUrl         文件访问地址
     * @param username        操作人账号
     * @return 待持久化的文档实体
     */
    private KbDocument buildPendingDocument(Long knowledgeBaseId, String fileUrl, String username) {
        Date now = new Date();
        KbDocument document = new KbDocument();
        document.setKnowledgeBaseId(knowledgeBaseId);
        document.setFileUrl(fileUrl);
        document.setFileName(extractFileName(fileUrl));
        document.setStatus(STATUS_PENDING);
        document.setCreateBy(username);
        document.setUpdateBy(username);
        document.setCreatedAt(now);
        document.setUpdatedAt(now);
        return document;
    }

    /**
     * 组装知识库导入 command 消息体。
     *
     * @param request  导入请求
     * @param kbBase   知识库配置实体
     * @param document 文档实体（含 documentId）
     * @param bizKey   业务键
     * @param version  当前版本号
     * @return 可投递到 MQ 的 command 消息
     */
    private KnowledgeImportCommandMessage buildCommandMessage(KnowledgeBaseImportRequest request, KbBase kbBase,
                                                              KbDocument document, String bizKey, Long version) {
        return KnowledgeImportCommandMessage.builder()
                .message_type(COMMAND_MESSAGE_TYPE)
                .task_uuid(UUID.randomUUID().toString())
                .biz_key(bizKey)
                .version(version)
                .knowledge_name(kbBase.getKnowledgeName())
                .document_id(document.getId())
                .file_url(document.getFileUrl())
                .embedding_model(kbBase.getEmbeddingModel())
                .chunk_strategy(request.getChunkStrategy())
                .chunk_size(request.getChunkSize())
                .token_size(request.getTokenSize())
                .created_at(DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(OffsetDateTime.now(ZoneOffset.UTC)))
                .build();
    }

    /**
     * 生成导入判旧业务键。
     *
     * @param knowledgeName 知识库名称
     * @param documentId    文档ID
     * @return 业务键（knowledgeName:documentId）
     */
    private String buildBizKey(String knowledgeName, Long documentId) {
        return knowledgeName + ":" + documentId;
    }

    /**
     * 生成并写入 bizKey 的最新版本号。
     * <p>
     * 使用 Redis 自增保证版本单调递增，并刷新 latest key 的 TTL。
     * </p>
     *
     * @param bizKey 业务键
     * @return 新版本号（>=1）
     */
    private Long nextVersionAndSetLatest(String bizKey) {
        String latestKey = latestVersionKey(bizKey);
        Long version = redisCache.increment(latestKey);
        if (version == null || version <= 0) {
            throw new ServiceException(ResponseCode.OPERATION_ERROR, "生成导入版本号失败");
        }
        redisCache.expire(latestKey, REDIS_LATEST_VERSION_TTL_DAYS, TimeUnit.DAYS);
        return version;
    }

    /**
     * 读取业务键对应的最新版本号。
     *
     * @param bizKey 业务键
     * @return 最新版本号；无值或无法解析时返回 null
     */
    private Long getLatestVersion(String bizKey) {
        if (!StringUtils.hasText(bizKey)) {
            return null;
        }
        Object value = redisCache.getCacheObject(latestVersionKey(bizKey));
        if (value == null) {
            return null;
        }
        try {
            if (value instanceof Number number) {
                return number.longValue();
            }
            return Long.parseLong(String.valueOf(value));
        } catch (Exception ex) {
            log.error("解析知识库最新版本失败: biz_key={}, value={}", bizKey, value, ex);
            return null;
        }
    }

    /**
     * 构建最新版本 Redis Key。
     *
     * @param bizKey 业务键
     * @return latest 版本 key
     */
    private String latestVersionKey(String bizKey) {
        return REDIS_LATEST_VERSION_KEY_PREFIX + bizKey;
    }

    /**
     * 将指定文档标记为失败状态。
     *
     * @param documentId   文档ID
     * @param errorMessage 错误信息
     * @param username     操作人账号
     */
    private void markDocumentFailed(Long documentId, String errorMessage, String username) {
        KbDocument failedDocument = getById(documentId);
        if (failedDocument == null) {
            return;
        }
        failedDocument.setStatus(STATUS_FAILED);
        failedDocument.setLastError(errorMessage);
        failedDocument.setUpdateBy(username);
        failedDocument.setUpdatedAt(new Date());
        updateById(failedDocument);
    }

    /**
     * 从 URL 中提取文件名。
     *
     * @param fileUrl 文件地址
     * @return 提取出的文件名；提取失败时回退为原始 URL
     */
    private String extractFileName(String fileUrl) {
        try {
            String path = URI.create(fileUrl).getPath();
            if (!StringUtils.hasText(path)) {
                return fileUrl;
            }
            int index = path.lastIndexOf('/');
            String fileName = index >= 0 ? path.substring(index + 1) : path;
            return StringUtils.hasText(fileName) ? fileName : fileUrl;
        } catch (Exception ignored) {
            return fileUrl;
        }
    }

    /**
     * 标准化 stage 字段，统一转大写。
     *
     * @param stage 原始状态值
     * @return 标准化后的状态；为空时返回 null
     */
    private String normalizeStage(String stage) {
        if (!StringUtils.hasText(stage)) {
            return null;
        }
        return stage.trim().toUpperCase(Locale.ROOT);
    }

    /**
     * 按业务名称查询知识库配置。
     *
     * @param knowledgeName 知识库名称
     * @return 知识库实体，不存在时返回 null
     */
    KbBase findKnowledgeBaseByName(String knowledgeName) {
        return kbBaseService.lambdaQuery()
                .eq(KbBase::getKnowledgeName, knowledgeName)
                .one();
    }
}
