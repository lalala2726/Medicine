package cn.zhangchuangla.medicine.admin.service.impl;

import cn.zhangchuangla.medicine.admin.mapper.KbDocumentChunkMapper;
import cn.zhangchuangla.medicine.admin.mapper.KbDocumentMapper;
import cn.zhangchuangla.medicine.admin.model.request.DocumentChunkListRequest;
import cn.zhangchuangla.medicine.admin.model.request.DocumentChunkUpdateContentRequest;
import cn.zhangchuangla.medicine.admin.model.request.DocumentChunkUpdateStatusRequest;
import cn.zhangchuangla.medicine.admin.publisher.KnowledgeChunkRebuildPublisher;
import cn.zhangchuangla.medicine.admin.service.KbBaseService;
import cn.zhangchuangla.medicine.admin.service.KbDocumentChunkHistoryService;
import cn.zhangchuangla.medicine.admin.service.KbDocumentChunkService;
import cn.zhangchuangla.medicine.common.core.enums.ResponseCode;
import cn.zhangchuangla.medicine.common.core.exception.ServiceException;
import cn.zhangchuangla.medicine.common.core.utils.Assert;
import cn.zhangchuangla.medicine.common.redis.core.RedisCache;
import cn.zhangchuangla.medicine.common.security.base.BaseService;
import cn.zhangchuangla.medicine.model.entity.KbBase;
import cn.zhangchuangla.medicine.model.entity.KbDocument;
import cn.zhangchuangla.medicine.model.entity.KbDocumentChunk;
import cn.zhangchuangla.medicine.model.entity.KbDocumentChunkHistory;
import cn.zhangchuangla.medicine.model.mq.KnowledgeChunkRebuildCommandMessage;
import cn.zhangchuangla.medicine.model.mq.KnowledgeChunkRebuildResultMessage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 知识库文档切片服务。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KbDocumentChunkServiceImpl extends ServiceImpl<KbDocumentChunkMapper, KbDocumentChunk>
        implements KbDocumentChunkService, BaseService {

    /**
     * 批量保存切片时的分批大小。
     */
    private static final int BATCH_SIZE = 500;

    /**
     * 切片启用状态。
     */
    private static final int CHUNK_STATUS_ENABLED = 0;

    /**
     * 切片禁用状态。
     */
    private static final int CHUNK_STATUS_DISABLED = 1;

    /**
     * 切片编辑待处理状态。
     */
    private static final String EDIT_STATUS_PENDING = "PENDING";

    /**
     * 切片编辑已开始处理状态。
     */
    private static final String EDIT_STATUS_STARTED = "STARTED";

    /**
     * 切片编辑已完成状态。
     */
    private static final String EDIT_STATUS_COMPLETED = "COMPLETED";

    /**
     * 切片编辑失败状态。
     */
    private static final String EDIT_STATUS_FAILED = "FAILED";

    /**
     * 切片正在等待重建或处理中时的提示文案。
     */
    private static final String EDIT_IN_PROGRESS_MESSAGE = "当前切片已提交修改，必须等待完成后才能继续修改";

    /**
     * 单切片重建 command 消息类型。
     */
    private static final String CHUNK_REBUILD_COMMAND_MESSAGE_TYPE = "knowledge_chunk_rebuild_command";

    /**
     * AI 返回“旧版本已被替代”时的关键字。
     */
    private static final String STALE_RESULT_MESSAGE = "已被更新版本替代";

    /**
     * 单切片编辑最新版本 Redis key 前缀。
     */
    private static final String CHUNK_EDIT_LATEST_VERSION_KEY_PREFIX = "kb:chunk_edit:latest_version:";

    /**
     * 单切片编辑最新版本 Redis key 保留天数。
     */
    private static final long CHUNK_EDIT_LATEST_VERSION_TTL_DAYS = 7L;

    private final KbDocumentMapper kbDocumentMapper;
    private final KbBaseService kbBaseService;
    private final KbDocumentChunkHistoryService kbDocumentChunkHistoryService;
    private final RedisCache redisCache;
    private final KnowledgeChunkRebuildPublisher knowledgeChunkRebuildPublisher;
    private final PlatformTransactionManager transactionManager;

    /**
     * 分页查询指定文档下的切片列表。
     *
     * @param request 查询参数
     * @return 切片分页结果
     */
    @Override
    public Page<KbDocumentChunk> listDocumentChunk(Long documentId, DocumentChunkListRequest request) {
        Assert.notNull(request, "查询参数不能为空");
        Assert.isPositive(documentId, "文档ID必须大于0");
        Assert.isTrue(kbDocumentMapper.selectById(documentId) != null, "文档不存在");
        return baseMapper.listDocumentChunk(request.toPage(), documentId, request);
    }

    /**
     * 根据切片ID查询切片详情。
     *
     * @param id 切片ID
     * @return 切片详情
     */
    @Override
    public KbDocumentChunk getDocumentChunkById(Long id) {
        Assert.isPositive(id, "切片ID必须大于0");
        KbDocumentChunk chunk = baseMapper.selectById(id);
        Assert.isTrue(chunk != null, "文档切片不存在");
        return chunk;
    }

    /**
     * 修改切片内容，并通过 Redis + MQ 触发 AI 侧单条向量重建。
     *
     * @param request 更新请求
     * @return true 表示处理成功
     */
    @Override
    public boolean updateDocumentChunkContent(DocumentChunkUpdateContentRequest request) {
        Assert.notNull(request, "切片内容更新请求不能为空");
        KbDocumentChunk chunk = getDocumentChunkById(request.getId());

        String content = request.getContent() == null ? null : request.getContent().trim();
        Assert.notEmpty(content, "切片内容不能为空");
        assertChunkEditable(chunk);
        if (Objects.equals(content, chunk.getContent())) {
            return true;
        }

        KbDocument document = getDocument(chunk.getDocumentId());
        KbBase kbBase = kbBaseService.getKnowledgeBaseById(document.getKnowledgeBaseId());
        Assert.notEmpty(kbBase.getKnowledgeName(), "知识库名称不能为空");
        Assert.notEmpty(kbBase.getEmbeddingModel(), "知识库向量模型未配置");

        long vectorId = parseStoredPositiveLong(chunk.getVectorId());
        String taskUuid = UUID.randomUUID().toString();
        persistChunkEdit(chunk, kbBase, content, vectorId, taskUuid);

        try {
            long version = nextChunkEditVersionAndSetLatest(vectorId);
            log.info("切片内容更新已落库，准备发布重建命令: chunk_id={}, document_id={}, vector_id={}, version={}, task_uuid={};",
                    chunk.getId(), chunk.getDocumentId(), vectorId, version, taskUuid);
            knowledgeChunkRebuildPublisher.publishCommand(buildChunkRebuildCommand(
                    kbBase, chunk.getDocumentId(), vectorId, version, content, taskUuid));
            return true;
        } catch (Exception ex) {
            markChunkEditFailed(chunk.getId());
            throw new ServiceException(ResponseCode.OPERATION_ERROR,
                    "内容已保存，但向量重建未成功提交: " + extractErrorMessage(ex));
        }
    }

    /**
     * 修改切片状态。
     *
     * @param request 更新请求
     * @return 永远不会返回；当前固定抛出未开放异常
     */
    @Override
    public boolean updateDocumentChunkStatus(DocumentChunkUpdateStatusRequest request) {
        Assert.notNull(request, "切片状态更新请求不能为空");
        Assert.isPositive(request.getId(), "切片ID必须大于0");
        validateChunkStatus(request.getStatus());
        throw new ServiceException(ResponseCode.OPERATION_ERROR, "切片状态变更暂未开放");
    }

    /**
     * 删除单个切片。
     *
     * @param id 切片ID
     * @return 永远不会返回；当前固定抛出未开放异常
     */
    @Override
    public boolean deleteDocumentChunk(Long id) {
        Assert.isPositive(id, "切片ID必须大于0");
        throw new ServiceException(ResponseCode.OPERATION_ERROR, "切片删除暂未开放");
    }

    /**
     * 处理 AI 回传的单切片重建结果，只回写本地编辑状态。
     *
     * @param message AI 回传的结果消息
     */
    @Override
    public void handleChunkRebuildResult(KnowledgeChunkRebuildResultMessage message) {
        if (message == null) {
            return;
        }
        Long documentId = message.getDocument_id();
        Long vectorId = message.getVector_id();
        if (documentId == null || documentId <= 0 || vectorId == null || vectorId <= 0) {
            log.warn("忽略无效切片重建结果: task_uuid={}, document_id={}, vector_id={}",
                    message.getTask_uuid(), documentId, vectorId);
            return;
        }
        if (isStaleResultMessage(message)) {
            return;
        }

        String editStatus = normalizeEditStatus(message.getStage());
        if (!StringUtils.hasText(editStatus)) {
            log.warn("忽略未知切片重建阶段: task_uuid={}, stage={}", message.getTask_uuid(), message.getStage());
            return;
        }

        KbDocumentChunk chunk = baseMapper.selectOne(Wrappers.<KbDocumentChunk>lambdaQuery()
                .eq(KbDocumentChunk::getDocumentId, documentId)
                .eq(KbDocumentChunk::getVectorId, String.valueOf(vectorId))
                .last("limit 1"));
        if (chunk == null) {
            log.warn("切片重建结果未命中本地切片: task_uuid={}, document_id={}, vector_id={}",
                    message.getTask_uuid(), documentId, vectorId);
            return;
        }

        KbDocumentChunk updateEntity = new KbDocumentChunk();
        updateEntity.setId(chunk.getId());
        updateEntity.setEditStatus(editStatus);
        updateEntity.setUpdatedAt(new Date());
        if (baseMapper.updateById(updateEntity) <= 0) {
            log.warn("更新切片编辑状态失败: chunk_id={}, task_uuid={}, stage={}",
                    chunk.getId(), message.getTask_uuid(), message.getStage());
            return;
        }

        if (EDIT_STATUS_FAILED.equals(editStatus)) {
            if (containsStaleReplacementMessage(message.getMessage())) {
                log.info("切片重建任务被新版本替代: chunk_id={}, task_uuid={}, vector_id={}, version={}, message={}",
                        chunk.getId(), message.getTask_uuid(), vectorId, message.getVersion(), message.getMessage());
            } else {
                log.warn("切片重建失败: chunk_id={}, task_uuid={}, vector_id={}, version={}, message={}",
                        chunk.getId(), message.getTask_uuid(), vectorId, message.getVersion(), message.getMessage());
            }
            return;
        }

        log.info("切片重建结果已回写: chunk_id={}, task_uuid={}, vector_id={}, version={}, stage={}",
                chunk.getId(), message.getTask_uuid(), vectorId, message.getVersion(), editStatus);
    }

    /**
     * 按文档ID替换切片数据，先删后插。
     *
     * @param documentId 文档ID
     * @param chunks     新切片列表
     */
    @Override
    public void replaceByDocumentId(Long documentId, List<KbDocumentChunk> chunks) {
        Assert.isPositive(documentId, "文档ID不能为空");

        lambdaUpdate()
                .eq(KbDocumentChunk::getDocumentId, documentId)
                .remove();

        if (CollectionUtils.isEmpty(chunks)) {
            return;
        }
        boolean saved = saveBatch(chunks, BATCH_SIZE);
        if (!saved) {
            throw new ServiceException(ResponseCode.OPERATION_ERROR, "保存文档切片失败");
        }
    }

    /**
     * 按文档ID集合批量删除本地切片数据。
     *
     * @param documentIds 文档ID集合
     * @return true 表示删除成功
     */
    @Override
    public boolean removeByDocumentIds(List<Long> documentIds) {
        Assert.notEmpty(documentIds, "文档ID不能为空");
        return lambdaUpdate()
                .in(KbDocumentChunk::getDocumentId, documentIds)
                .remove();
    }

    /**
     * 在一个本地事务内完成切片内容更新和历史记录落库。
     *
     * @param chunk    原切片实体
     * @param kbBase   所属知识库
     * @param content  新切片内容
     * @param vectorId 向量ID
     * @param taskUuid 本次编辑任务ID
     */
    private void persistChunkEdit(KbDocumentChunk chunk, KbBase kbBase, String content, long vectorId, String taskUuid) {
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.executeWithoutResult(status -> {
            Date now = new Date();

            KbDocumentChunk updateEntity = KbDocumentChunk.builder()
                    .id(chunk.getId())
                    .content(content)
                    .charCount(content.length())
                    .editStatus(EDIT_STATUS_PENDING)
                    .updatedAt(now)
                    .build();
            Assert.isTrue(baseMapper.updateById(updateEntity) > 0, "更新文档切片内容失败");

            KbDocumentChunkHistory history = KbDocumentChunkHistory.builder()
                    .documentId(chunk.getDocumentId())
                    .chunkId(chunk.getId())
                    .knowledgeName(kbBase.getKnowledgeName())
                    .vectorId(vectorId)
                    .oldContent(chunk.getContent())
                    .taskId(taskUuid)
                    .operatorId(resolveCurrentOperatorId())
                    .createdAt(now)
                    .build();
            Assert.isTrue(kbDocumentChunkHistoryService.save(history), "保存文档切片历史失败");
        });
    }

    /**
     * 仅允许已完成或失败态的切片再次发起修改；等待中或处理中必须串行化。
     *
     * @param chunk 当前切片
     */
    private void assertChunkEditable(KbDocumentChunk chunk) {
        if (chunk == null) {
            return;
        }
        String editStatus = chunk.getEditStatus();
        if (EDIT_STATUS_PENDING.equalsIgnoreCase(editStatus) || EDIT_STATUS_STARTED.equalsIgnoreCase(editStatus)) {
            throw new ServiceException(ResponseCode.OPERATION_ERROR, EDIT_IN_PROGRESS_MESSAGE);
        }
    }

    /**
     * 版本号以同一个 vector_id 为粒度递增，不是全局递增。
     * 发布 MQ 前必须先写 Redis latest-version，AI 侧也不会主动删除该 key。
     *
     * @param vectorId 向量ID
     * @return 新生成的最新版本号
     */
    private long nextChunkEditVersionAndSetLatest(long vectorId) {
        String latestKey = latestChunkEditVersionKey(vectorId);
        Long version = redisCache.increment(latestKey);
        if (version == null || version <= 0) {
            throw new ServiceException(ResponseCode.OPERATION_ERROR, "生成切片重建版本号失败");
        }
        redisCache.expire(latestKey, CHUNK_EDIT_LATEST_VERSION_TTL_DAYS, TimeUnit.DAYS);
        return version;
    }

    /**
     * 读取指定向量当前记录的最新切片编辑版本号。
     *
     * @param vectorId 向量ID
     * @return 最新版本号；不存在或无法解析时返回 null
     */
    private Long getLatestChunkEditVersion(long vectorId) {
        Object value = redisCache.getCacheObject(latestChunkEditVersionKey(vectorId));
        if (value == null) {
            return null;
        }
        try {
            if (value instanceof Number number) {
                return number.longValue();
            }
            return Long.parseLong(String.valueOf(value));
        } catch (Exception ex) {
            log.error("解析切片最新版本失败: vector_id={}, value={}", vectorId, value, ex);
            return null;
        }
    }

    /**
     * 生成单切片编辑 latest-version Redis key。
     *
     * @param vectorId 向量ID
     * @return Redis key
     */
    private String latestChunkEditVersionKey(long vectorId) {
        return CHUNK_EDIT_LATEST_VERSION_KEY_PREFIX + vectorId;
    }

    /**
     * 判断当前回调结果是否已落后于 Redis 中记录的最新版本。
     *
     * @param message 回调结果消息
     * @return true 表示旧版本消息，应直接丢弃
     */
    private boolean isStaleResultMessage(KnowledgeChunkRebuildResultMessage message) {
        Long vectorId = message.getVector_id();
        Long messageVersion = message.getVersion();
        if (vectorId == null || vectorId <= 0) {
            return false;
        }
        Long latestVersion = getLatestChunkEditVersion(vectorId);
        if (latestVersion != null && messageVersion != null && messageVersion < latestVersion) {
            log.info("丢弃旧版本切片重建结果: task_uuid={}, vector_id={}, message_version={}, latest_version={}, stage={}",
                    message.getTask_uuid(), vectorId, messageVersion, latestVersion, message.getStage());
            return true;
        }
        return false;
    }

    /**
     * 组装单切片重建 command 消息。
     *
     * @param kbBase     知识库实体
     * @param documentId 文档ID
     * @param vectorId   向量ID
     * @param version    当前版本号
     * @param content    新切片内容
     * @param taskUuid   任务ID
     * @return 可发送到 MQ 的 command 消息体
     */
    private KnowledgeChunkRebuildCommandMessage buildChunkRebuildCommand(KbBase kbBase, Long documentId,
                                                                         long vectorId, long version,
                                                                         String content, String taskUuid) {
        return KnowledgeChunkRebuildCommandMessage.builder()
                .message_type(CHUNK_REBUILD_COMMAND_MESSAGE_TYPE)
                .task_uuid(taskUuid)
                .knowledge_name(kbBase.getKnowledgeName())
                .document_id(documentId)
                .vector_id(vectorId)
                .version(version)
                .content(content)
                .embedding_model(kbBase.getEmbeddingModel())
                .created_at(DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(OffsetDateTime.now(ZoneOffset.UTC)))
                .build();
    }

    /**
     * 当 MQ 提交失败时，将本地切片编辑状态回写为 FAILED。
     *
     * @param chunkId 切片ID
     */
    private void markChunkEditFailed(Long chunkId) {
        KbDocumentChunk updateEntity = new KbDocumentChunk();
        updateEntity.setId(chunkId);
        updateEntity.setEditStatus(EDIT_STATUS_FAILED);
        updateEntity.setUpdatedAt(new Date());
        if (baseMapper.updateById(updateEntity) <= 0) {
            log.warn("切片重建提交失败后回写 FAILED 失败: chunk_id={}", chunkId);
        }
    }

    /**
     * 校验切片状态是否在允许范围内。
     *
     * @param status 切片状态，只允许 0 或 1
     */
    private void validateChunkStatus(Integer status) {
        Assert.notNull(status, "切片状态不能为空");
        Assert.isParamTrue(status == CHUNK_STATUS_ENABLED || status == CHUNK_STATUS_DISABLED, "切片状态只允许为0或1");
    }

    /**
     * 根据文档ID查询文档实体。
     *
     * @param documentId 文档ID
     * @return 文档实体
     */
    private KbDocument getDocument(Long documentId) {
        Assert.isPositive(documentId, "文档ID必须大于0");
        KbDocument document = kbDocumentMapper.selectById(documentId);
        Assert.isTrue(document != null, "文档不存在");
        return document;
    }

    /**
     * 尝试读取当前操作人ID；未登录或无法获取时返回 null。
     *
     * @return 当前操作人ID
     */
    private Long resolveCurrentOperatorId() {
        try {
            return getUserId();
        } catch (Exception ignored) {
            return null;
        }
    }

    /**
     * 将数据库中字符串形式保存的向量ID解析为正整数。
     *
     * @param value 数据库存储的向量ID
     * @return 解析后的向量ID
     */
    private long parseStoredPositiveLong(String value) {
        if (!StringUtils.hasText(value)) {
            throw new ServiceException(ResponseCode.OPERATION_ERROR, "向量ID无效");
        }
        try {
            long parsed = Long.parseLong(value.trim());
            if (parsed <= 0) {
                throw new NumberFormatException("not positive");
            }
            return parsed;
        } catch (NumberFormatException ex) {
            throw new ServiceException(ResponseCode.OPERATION_ERROR, "向量ID无效");
        }
    }

    /**
     * 将 AI 回调阶段标准化为本地切片编辑状态。
     *
     * @param stage AI 回调阶段
     * @return 标准化后的编辑状态；无法识别时返回 null
     */
    private String normalizeEditStatus(String stage) {
        if (!StringUtils.hasText(stage)) {
            return null;
        }
        if (EDIT_STATUS_STARTED.equalsIgnoreCase(stage)) {
            return EDIT_STATUS_STARTED;
        }
        if (EDIT_STATUS_COMPLETED.equalsIgnoreCase(stage)) {
            return EDIT_STATUS_COMPLETED;
        }
        if (EDIT_STATUS_FAILED.equalsIgnoreCase(stage)) {
            return EDIT_STATUS_FAILED;
        }
        return null;
    }

    /**
     * 判断失败信息是否表示旧任务已被新版本替代。
     *
     * @param message AI 返回的失败信息
     * @return true 表示旧任务被新版本覆盖
     */
    private boolean containsStaleReplacementMessage(String message) {
        return StringUtils.hasText(message) && message.contains(STALE_RESULT_MESSAGE);
    }

    /**
     * 提取异常消息，便于返回给上层接口。
     *
     * @param ex 异常对象
     * @return 优先返回异常 message，无值时返回异常类型名
     */
    private String extractErrorMessage(Exception ex) {
        String message = ex.getMessage();
        if (StringUtils.hasText(message)) {
            return message;
        }
        return ex.getClass().getSimpleName();
    }
}
