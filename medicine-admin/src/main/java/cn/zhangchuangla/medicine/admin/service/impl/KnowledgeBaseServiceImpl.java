package cn.zhangchuangla.medicine.admin.service.impl;

import cn.zhangchuangla.medicine.admin.common.storage.model.MinioFileObject;
import cn.zhangchuangla.medicine.admin.common.storage.service.MinioStorageService;
import cn.zhangchuangla.medicine.admin.mapper.KnowledgeBaseMapper;
import cn.zhangchuangla.medicine.admin.model.dto.KnowledgeBaseStatsDto;
import cn.zhangchuangla.medicine.admin.model.request.*;
import cn.zhangchuangla.medicine.admin.model.vo.DocumentSliceListVo;
import cn.zhangchuangla.medicine.admin.model.vo.KnowledgeBaseDocumentVo;
import cn.zhangchuangla.medicine.admin.model.vo.KnowledgeBaseListVo;
import cn.zhangchuangla.medicine.admin.service.KbDocumentChunkService;
import cn.zhangchuangla.medicine.admin.service.KbDocumentService;
import cn.zhangchuangla.medicine.admin.service.KnowledgeBaseService;
import cn.zhangchuangla.medicine.common.core.enums.ResponseCode;
import cn.zhangchuangla.medicine.common.core.exception.ServiceException;
import cn.zhangchuangla.medicine.common.core.utils.Assert;
import cn.zhangchuangla.medicine.common.core.utils.UUIDUtils;
import cn.zhangchuangla.medicine.common.milvus.config.MilvusProperties;
import cn.zhangchuangla.medicine.common.milvus.service.MilvusKnowledgeBaseService;
import cn.zhangchuangla.medicine.common.milvus.support.KnowledgeBaseIngestSupport;
import cn.zhangchuangla.medicine.common.rabbitmq.message.KnowledgeBaseChunkUpdateMessage;
import cn.zhangchuangla.medicine.common.rabbitmq.publisher.KnowledgeBaseChunkUpdatePublisher;
import cn.zhangchuangla.medicine.common.rabbitmq.publisher.KnowledgeBaseDeletePublisher;
import cn.zhangchuangla.medicine.common.rabbitmq.publisher.KnowledgeBaseIngestPublisher;
import cn.zhangchuangla.medicine.common.rabbitmq.publisher.KnowledgeBaseVectorDeletePublisher;
import cn.zhangchuangla.medicine.common.security.base.BaseService;
import cn.zhangchuangla.medicine.common.security.utils.SecurityUtils;
import cn.zhangchuangla.medicine.model.entity.KbDocument;
import cn.zhangchuangla.medicine.model.entity.KbDocumentChunk;
import cn.zhangchuangla.medicine.model.entity.KnowledgeBase;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.milvus.client.MilvusServiceClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.tokenizer.JTokkitTokenCountEstimator;
import org.springframework.ai.tokenizer.TokenCountEstimator;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.milvus.MilvusVectorStore;
import org.springframework.beans.BeanUtils;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 知识库 Service 实现
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class KnowledgeBaseServiceImpl extends ServiceImpl<KnowledgeBaseMapper, KnowledgeBase>
        implements KnowledgeBaseService, BaseService {

    /**
     * 目标切片 token 大小（近似），便于向量召回效果。
     */
    private static final int DEFAULT_CHUNK_SIZE = 400;

    /**
     * 切片时的最小字符数，避免过短片段。
     */
    private static final int MIN_CHUNK_SIZE_CHARS = 120;

    /**
     * 参与向量化的最小文本长度，过滤极短文本。
     */
    private static final int MIN_CHUNK_LENGTH_TO_EMBED = 80;

    /**
     * 单文件允许的最大切片数量，防止超大文件拖垮处理。
     */
    private static final int MAX_CHUNKS = 5000;

    /**
     * 向量写入的批量大小，控制单次 embedding 数量。
     */
    private static final int EMBEDDING_BATCH_SIZE = 10;

    /**
     * 防止写入 Milvus 的 VarChar 超长，按字符截断；65535 是常见上限，这里预留安全余量。
     */
    private static final int MAX_CHUNK_CHAR_LENGTH = 2000;
    private static final String STATUS_PROCESSING = "PROCESSING";
    private static final String STATUS_SUCCESS = "SUCCESS";

    private final MilvusKnowledgeBaseService milvusKnowledgeBaseService;
    private final MinioStorageService minioStorageService;
    private final KbDocumentService kbDocumentService;
    private final KbDocumentChunkService kbDocumentChunkService;
    private final EmbeddingModel embeddingModel;
    private final MilvusProperties milvusProperties;
    private final KnowledgeBaseIngestSupport knowledgeBaseIngestSupport;
    private final KnowledgeBaseIngestPublisher knowledgeBaseIngestPublisher;
    private final KnowledgeBaseDeletePublisher knowledgeBaseDeletePublisher;
    private final KnowledgeBaseChunkUpdatePublisher knowledgeBaseChunkUpdatePublisher;
    private final KnowledgeBaseVectorDeletePublisher knowledgeBaseVectorDeletePublisher;

    /**
     * 分页查询知识库列表，附带统计信息。
     */
    @Override
    public Page<KnowledgeBaseListVo> listKnowledgeBase(KnowledgeBaseListRequest request) {
        Page<KnowledgeBaseStatsDto> dtoPage = baseMapper.selectPageWithStats(request.toPage(), request);
        List<KnowledgeBaseListVo> records = copyListProperties(dtoPage.getRecords(), KnowledgeBaseListVo.class);
        Page<KnowledgeBaseListVo> resultPage = new Page<>(dtoPage.getCurrent(), dtoPage.getSize(), dtoPage.getTotal());
        resultPage.setRecords(records);
        return resultPage;
    }

    /**
     * 新增知识库并在 Milvus 创建集合。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addKnowledgeBase(KnowledgeBaseAddRequest request) {
        if (isNameDuplicated(request.getName(), null)) {
            throw new ServiceException("知识库名称已存在");
        }
        KnowledgeBase knowledgeBase = new KnowledgeBase();
        BeanUtils.copyProperties(request, knowledgeBase);

        Date now = new Date();
        String username = getUsername();
        knowledgeBase.setCreateTime(now);
        knowledgeBase.setUpdateTime(now);
        knowledgeBase.setCreateBy(username);
        knowledgeBase.setUpdateBy(username);

        boolean saved = save(knowledgeBase);
        if (!saved) {
            return false;
        }

        // 同步创建 Milvus 集合，以便后续写入知识库的向量数据
        milvusKnowledgeBaseService.createKnowledgeBaseSpace(knowledgeBase.getId());
        return true;
    }

    /**
     * 获取知识库详情，不存在则抛异常。
     */
    @Override
    public KnowledgeBase getKnowledgeBase(Integer id) {
        KnowledgeBase knowledgeBase = getById(id);
        if (knowledgeBase == null) {
            throw new ServiceException("知识库不存在");
        }
        return knowledgeBase;
    }

    /**
     * 发起知识库导入：校验参数后发送 MQ 进行异步处理。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean importKnowledgeBase(KnowledgeBaseImportRequest request) {
        KnowledgeBase knowledgeBase = getKnowledgeBase(request.getKnowledgeBaseId());
        if (CollectionUtils.isEmpty(request.getFileUrls())) {
            throw new ServiceException(ResponseCode.PARAM_ERROR, "请至少提供一个文本文件地址");
        }
        knowledgeBaseIngestPublisher.publish(knowledgeBase.getId(), request.getFileUrls(), getUsername());
        return true;
    }

    /**
     * 删除知识库文档：
     * 1. 校验知识库与文档归属关系。
     * 2. 立即删除数据库中的文档与切片记录。
     * 3. 通过 MQ 异步删除 Milvus 中的向量，避免阻塞请求。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteDocument(DocumentDeleteRequest request) {
        Assert.notNull(request, "请求参数不能为空");
        Assert.notNull(request.getKnowledgeBaseId(), "知识库ID不能为空");
        Assert.notNull(request.getDocumentId(), "文档ID不能为空");

        KnowledgeBase knowledgeBase = getKnowledgeBase(request.getKnowledgeBaseId());
        Long documentId = request.getDocumentId();

        KbDocument document = kbDocumentService.getDocumentById(documentId);
        if (document == null || !Objects.equals(document.getKbId(), knowledgeBase.getId())) {
            throw new ServiceException("文档不存在");
        }

        List<KbDocumentChunk> chunks = kbDocumentChunkService.lambdaQuery()
                .eq(KbDocumentChunk::getDocId, documentId)
                .list();
        List<String> vectorIds = chunks.stream()
                .map(KbDocumentChunk::getVectorId)
                .filter(Objects::nonNull)
                .map(String::valueOf)
                .toList();

        kbDocumentChunkService.lambdaUpdate()
                .eq(KbDocumentChunk::getDocId, documentId)
                .remove();

        boolean removed = kbDocumentService.removeById(documentId);
        if (!removed) {
            throw new ServiceException(ResponseCode.OPERATION_ERROR, "删除文档失败");
        }

        // 异步删除 Milvus 向量，避免阻塞数据库删除流程
        knowledgeBaseVectorDeletePublisher.publish(knowledgeBase.getId(), vectorIds, documentId);
        return true;
    }

    /**
     * 异步导入知识库：由 MQ 触发，串行处理文件解析、切片、向量写入与数据库落库。
     *
     * @param knowledgeBaseId 知识库 ID
     * @param fileUrls        文件地址
     * @param username        操作人（MQ 侧传入）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void ingestKnowledgeBase(Integer knowledgeBaseId, List<String> fileUrls, String username) {
        KnowledgeBase knowledgeBase = getKnowledgeBase(knowledgeBaseId);
        if (CollectionUtils.isEmpty(fileUrls)) {
            throw new ServiceException(ResponseCode.PARAM_ERROR, "请至少提供一个文本文件地址");
        }
        String operator = StringUtils.hasText(username) ? username : "system";
        // 预先准备好分片与 token 统计工具，保证所有文件使用一致策略
        // Spring AI 的 TokenTextSplitter 会按照 token 近似长度切片，避免过长文本直接入库
        TokenTextSplitter splitter = knowledgeBaseIngestSupport.buildSplitter(
                DEFAULT_CHUNK_SIZE,
                MIN_CHUNK_SIZE_CHARS,
                MIN_CHUNK_LENGTH_TO_EMBED,
                MAX_CHUNKS,
                true
        );
        TokenCountEstimator tokenCountEstimator = knowledgeBaseIngestSupport.buildTokenEstimator();

        // 确保集合存在，但不再删除旧数据，避免导入时覆盖历史内容
        milvusKnowledgeBaseService.createKnowledgeBaseSpace(knowledgeBase.getId());
        // VectorStore 需要手动初始化 schema（未启用自动装配），因此手动构建客户端与集合
        MilvusServiceClient milvusServiceClient = knowledgeBaseIngestSupport.buildMilvusClient();
        MilvusVectorStore vectorStore = knowledgeBaseIngestSupport.buildVectorStore(milvusServiceClient, knowledgeBase.getId());
        try {
            vectorStore.afterPropertiesSet();
            for (String fileUrl : fileUrls) {
                importFile(knowledgeBase.getId(), fileUrl, splitter, tokenCountEstimator, vectorStore, operator);
            }
        } catch (Exception ex) {
            log.error("知识库导入失败", ex);
            throw ex instanceof ServiceException ? (ServiceException) ex
                    : new ServiceException(ResponseCode.OPERATION_ERROR, "知识库导入失败，请稍后重试");
        } finally {
            knowledgeBaseIngestSupport.closeQuietly(milvusServiceClient);
        }
    }

    /**
     * 异步删除向量：消费 MQ 消息，连接 Milvus 并删除指定向量 ID 列表。
     *
     * @param knowledgeBaseId 知识库 ID，用于确定集合名
     * @param vectorIds       待删除的向量 ID
     */
    @Override
    public void deleteDocumentVectors(Integer knowledgeBaseId, List<String> vectorIds) {
        if (CollectionUtils.isEmpty(vectorIds)) {
            return;
        }
        MilvusServiceClient milvusServiceClient = knowledgeBaseIngestSupport.buildMilvusClient();
        MilvusVectorStore vectorStore = knowledgeBaseIngestSupport.buildVectorStore(milvusServiceClient, knowledgeBaseId);
        try {
            vectorStore.afterPropertiesSet();
            vectorStore.delete(vectorIds);
        } catch (Exception ex) {
            log.error("向量删除失败, kbId={}, vectors={}", knowledgeBaseId, vectorIds, ex);
            throw ex instanceof ServiceException ? (ServiceException) ex
                    : new ServiceException(ResponseCode.OPERATION_ERROR, "向量删除失败，请稍后重试");
        } finally {
            knowledgeBaseIngestSupport.closeQuietly(milvusServiceClient);
        }
    }

    /**
     * MQ 消费：重算并写入单个切片向量。
     */
    @Override
    public void updateDocumentChunkVector(KnowledgeBaseChunkUpdateMessage message) {
        if (message == null || message.getKnowledgeBaseId() == null || message.getVectorId() == null) {
            log.warn("跳过切片向量更新，参数为空: {}", message);
            return;
        }
        String text = message.getContent();
        if (!StringUtils.hasText(text)) {
            log.warn("跳过切片向量更新，内容为空: {}", message.getChunkId());
            return;
        }
        if (text.length() > MAX_CHUNK_CHAR_LENGTH) {
            text = text.substring(0, MAX_CHUNK_CHAR_LENGTH);
        }
        String vectorId = String.valueOf(message.getVectorId());
        Document vectorDocument = Document.builder()
                .id(vectorId)
                .text(text)
                .metadata("chunkIndex", message.getChunkIndex())
                .metadata("kbId", message.getKnowledgeBaseId())
                .metadata("docId", message.getDocumentId())
                .build();

        MilvusServiceClient milvusServiceClient = knowledgeBaseIngestSupport.buildMilvusClient();
        MilvusVectorStore vectorStore = knowledgeBaseIngestSupport.buildVectorStore(milvusServiceClient, message.getKnowledgeBaseId());
        try {
            vectorStore.afterPropertiesSet();
            try {
                vectorStore.delete(List.of(vectorId));
            } catch (Exception ignore) {
                log.debug("删除旧向量失败或不存在，继续写入, vectorId={}", vectorId);
            }
            vectorStore.add(List.of(vectorDocument));
        } catch (Exception ex) {
            log.error("切片向量更新失败, chunkId={}, vectorId={}", message.getChunkId(), vectorId, ex);
            throw ex instanceof ServiceException ? (ServiceException) ex
                    : new ServiceException(ResponseCode.OPERATION_ERROR, "向量更新失败，请稍后重试");
        } finally {
            knowledgeBaseIngestSupport.closeQuietly(milvusServiceClient);
        }
    }

    /**
     * 查询知识库下的文档列表。
     */
    @Override
    public Page<KnowledgeBaseDocumentVo> documentList(Integer id, DocumentListRequest request) {
        Assert.isPositive(id, "知识库ID不能为空");
        Page<KbDocument> page = kbDocumentService.documentPage(id, request);

        List<KnowledgeBaseDocumentVo> knowledgeBaseDocumentVos = page.getRecords().stream()
                .map(document -> KnowledgeBaseDocumentVo.builder()
                        .id(document.getId())
                        .knowledgeBaseId(document.getKbId())
                        .chunk(document.getChunkCount())
                        .fileName(document.getFilename())
                        .fileSize(document.getFileSize())
                        .uploadTime(document.getUploadTime())
                        .updateTime(document.getUpdateTime())
                        .status(document.getStatus())
                        .fileType(document.getFileType())
                        .build())
                .collect(Collectors.toList());

        Page<KnowledgeBaseDocumentVo> resultPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        resultPage.setRecords(knowledgeBaseDocumentVos);
        return resultPage;
    }

    @Override
    public Page<DocumentSliceListVo> documentSliceList(Long documentId, DocumentSliceListRequest request) {
        Assert.notNull(documentId, "文档ID不能为空");
        Page<KbDocumentChunk> chunkPage = kbDocumentChunkService.documentSliceList(documentId, request);
        List<DocumentSliceListVo> records = chunkPage.getRecords().stream()
                .map(chunk -> DocumentSliceListVo.builder()
                        .id(chunk.getId())
                        .uuid(chunk.getUuid())
                        .documentId(chunk.getDocId())
                        .context(chunk.getContent())
                        .createTime(chunk.getCreateTime())
                        .updateTime(chunk.getUpdateTime())
                        .createBy(chunk.getCreateBy())
                        .updateBy(chunk.getUpdateBy())
                        .build())
                .toList();
        Page<DocumentSliceListVo> resultPage = new Page<>(chunkPage.getCurrent(), chunkPage.getSize(), chunkPage.getTotal());
        resultPage.setRecords(records);
        return resultPage;
    }

    /**
     * 更新文档切片，立即更新数据库并异步重算向量。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateDocumentChunk(DocumentSliceUpdateRequest request) {
        Assert.notNull(request, "请求参数不能为空");
        Assert.notNull(request.getChunkId(), "切片ID不能为空");

        KbDocumentChunk chunk = kbDocumentChunkService.getById(request.getChunkId());
        if (chunk == null) {
            throw new ServiceException(ResponseCode.NOT_FOUND, "切片不存在");
        }
        KbDocument document = kbDocumentService.getById(chunk.getDocId());
        if (document == null) {
            throw new ServiceException(ResponseCode.NOT_FOUND, "文档不存在");
        }
        KnowledgeBase knowledgeBase = getKnowledgeBase(document.getKbId());

        String newContent = request.getContent();
        int tokenCount = new JTokkitTokenCountEstimator().estimate(newContent);
        Date now = new Date();
        String username = getUsername();

        KbDocumentChunk update = new KbDocumentChunk();
        update.setId(chunk.getId());
        update.setContent(newContent);
        update.setTokenCount(tokenCount);
        update.setUpdateTime(now);
        update.setUpdateBy(username);
        kbDocumentChunkService.updateById(update);

        kbDocumentService.lambdaUpdate()
                .eq(KbDocument::getId, document.getId())
                .set(KbDocument::getUpdateTime, now)
                .set(KbDocument::getUpdateBy, username)
                .update();

        // 发布异步向量重算
        KnowledgeBaseChunkUpdateMessage message = KnowledgeBaseChunkUpdateMessage.builder()
                .knowledgeBaseId(knowledgeBase.getId())
                .documentId(document.getId())
                .chunkId(chunk.getId())
                .chunkIndex(chunk.getChunkIndex())
                .vectorId(chunk.getVectorId())
                .content(newContent)
                .build();
        knowledgeBaseChunkUpdatePublisher.publish(message);
        return true;
    }


    /**
     * 更新知识库信息。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateKnowledgeBase(KnowledgeBaseUpdateRequest request) {
        KnowledgeBase knowledgeBase = getKnowledgeBase(request.getId());
        if (isNameDuplicated(request.getName(), request.getId())) {
            throw new ServiceException("知识库名称已存在");
        }

        BeanUtils.copyProperties(request, knowledgeBase);
        knowledgeBase.setUpdateTime(new Date());
        knowledgeBase.setUpdateBy(SecurityUtils.getUsername());

        return updateById(knowledgeBase);
    }

    /**
     * 删除知识库并删除对应 Milvus 集合。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteKnowledgeBase(Integer id) {
        KnowledgeBase knowledgeBase = getKnowledgeBase(id);

        // 删除向量库集合，保证数据库和向量库一致
        milvusKnowledgeBaseService.dropKnowledgeBaseSpace(id);
        boolean removed = removeById(knowledgeBase.getId());
        if (!removed) {
            throw new ServiceException(ResponseCode.OPERATION_ERROR, "删除知识库失败");
        }
        // 异步删除知识库关联的文档与切片（分批）
        knowledgeBaseDeletePublisher.publish(id, null);
        return true;
    }

    /**
     * 批量删除知识库关联的文档与切片，供 MQ 消费者调用。
     */
    @Override
    public void deleteKnowledgeBaseData(Integer knowledgeBaseId, Integer batchSize) {
        Assert.notNull(knowledgeBaseId, "知识库ID不能为空");
        int size = (batchSize == null || batchSize <= 0) ? 500 : batchSize;
        while (true) {
            List<KbDocument> docs = kbDocumentService.lambdaQuery()
                    .eq(KbDocument::getKbId, knowledgeBaseId)
                    .orderByAsc(KbDocument::getId)
                    .last("limit " + size)
                    .list();
            if (CollectionUtils.isEmpty(docs)) {
                break;
            }
            List<Long> docIds = docs.stream().map(KbDocument::getId).toList();
            kbDocumentChunkService.lambdaUpdate()
                    .in(KbDocumentChunk::getDocId, docIds)
                    .remove();
            kbDocumentService.removeBatchByIds(docIds);
        }
    }

    private boolean isNameDuplicated(String name, Integer excludeId) {
        if (!StringUtils.hasText(name)) {
            return false;
        }
        LambdaQueryWrapper<KnowledgeBase> wrapper = new LambdaQueryWrapper<KnowledgeBase>()
                .eq(KnowledgeBase::getName, name);
        if (excludeId != null) {
            wrapper.ne(KnowledgeBase::getId, excludeId);
        }
        return count(wrapper) > 0;
    }

    /**
     * 单个文件的导入流程：
     * 1. 从 MinIO 读取文件 -> 通过 Tika 抽取纯文本
     * 2. 使用 TokenTextSplitter 切片，添加 chunk 序号与元数据
     * 3. 调用 Spring AI 向量存储，先生成 embedding 再写入 Milvus
     * 4. 将文档与切片元数据落库，保证向量与数据库一一对应
     *
     * @param kbId                知识库 ID
     * @param fileUrl             文件地址
     * @param splitter            文本切片器
     * @param tokenCountEstimator token 统计器
     * @param vectorStore         向量存储
     * @param username            操作人
     */
    private void importFile(Integer kbId,
                            String fileUrl,
                            TokenTextSplitter splitter,
                            TokenCountEstimator tokenCountEstimator,
                            MilvusVectorStore vectorStore,
                            String username) {
        if (!StringUtils.hasText(fileUrl)) {
            throw new ServiceException(ResponseCode.PARAM_ERROR, "文件地址不能为空");
        }

        MinioFileObject fileObject = minioStorageService.fetchFileByUrl(fileUrl);
        Document extracted = knowledgeBaseIngestSupport.parseDocument(buildResource(fileObject));
        if (!StringUtils.hasText(extracted.getText())) {
            throw new ServiceException(ResponseCode.PARAM_ERROR, "未能解析出文本内容，请确认文件是否受支持或文本是否为空");
        }

        long documentId = IdWorker.getId();
        Map<String, Object> metadata = new HashMap<>();
        if (!CollectionUtils.isEmpty(extracted.getMetadata())) {
            metadata.putAll(extracted.getMetadata());
        }
        metadata.put("kbId", kbId);
        metadata.put("docId", documentId);
        metadata.put("filename", fileObject.getFilename());
        metadata.put("objectPath", fileObject.getObjectName());

        // 构造基础 Document（带文件元数据），便于切片后继承这些 metadata
        Document baseDoc = Document.builder()
                .id(String.valueOf(documentId))
                .text(extracted.getText())
                .metadata(metadata)
                .build();

        List<Document> splitDocuments = splitter.apply(List.of(baseDoc));
        splitDocuments = knowledgeBaseIngestSupport.sanitizeDocuments(splitDocuments, MAX_CHUNK_CHAR_LENGTH);
        if (splitDocuments.isEmpty()) {
            throw new ServiceException(ResponseCode.OPERATION_ERROR, "未能从文件中切分出有效文本");
        }

        List<Document> vectorDocuments = new ArrayList<>(splitDocuments.size());
        List<KbDocumentChunk> chunkEntities = new ArrayList<>(splitDocuments.size());
        int chunkIndex = 0;
        Date now = new Date();
        for (Document splitDocument : splitDocuments) {
            long vectorId = IdWorker.getId();
            // 向量存储的 Document 需要：唯一 id、正文、元数据（kbId/docId/chunkIndex）
            Document vectorDocument = splitDocument.mutate()
                    .id(String.valueOf(vectorId))
                    .metadata("chunkIndex", chunkIndex)
                    .metadata("kbId", kbId)
                    .metadata("docId", documentId)
                    .build();
            vectorDocuments.add(vectorDocument);

            // DB 切片记录：保留原文、token 数、向量 id（用于删除/更新）
            KbDocumentChunk chunk = KbDocumentChunk.builder()
                    .docId(documentId)
                    .chunkIndex(chunkIndex)
                    .content(splitDocument.getText())
                    .tokenCount(tokenCountEstimator.estimate(splitDocument.getText()))
                    // todo 如果是PDF这边需要标明PDF的页数
                    .pageNum(null)
                    .vectorId(vectorId)
                    .createTime(now)
                    .uuid(UUIDUtils.simple())
                    .createBy(username)
                    .updateBy(username)
                    .build();
            chunkEntities.add(chunk);
            chunkIndex++;
        }

        KbDocument kbDocument = KbDocument.builder()
                .id(documentId)
                .kbId(kbId)
                .filename(fileObject.getFilename())
                .filePath(fileObject.getObjectName())
                .fileSize(formatFileSizeInMb(fileObject))
                .fileType(resolveFileType(fileObject))
                .chunkCount(chunkEntities.size())
                .uploadTime(now)
                .updateTime(now)
                .uploadBy(username)
                .updateBy(username)
                .status(STATUS_PROCESSING)
                .build();

        if (!kbDocumentService.save(kbDocument)) {
            throw new ServiceException(ResponseCode.OPERATION_ERROR, "保存文件元数据失败");
        }

        try {
            // 先写向量，确保成功后再批量入库 chunk 记录；分批写入避免一次请求超量
            knowledgeBaseIngestSupport.addVectorsInBatches(vectorStore, vectorDocuments, EMBEDDING_BATCH_SIZE);
        } catch (Exception ex) {
            log.error("向量写入失败，kbId={}, file={}", kbId, fileObject.getFilename(), ex);
            throw new ServiceException(ResponseCode.OPERATION_ERROR, "向量写入失败，请稍后重试");
        }

        try {
            if (!kbDocumentChunkService.saveKbDocuments(chunkEntities)) {
                vectorStore.delete(vectorDocuments.stream().map(Document::getId).toList());
                throw new ServiceException(ResponseCode.OPERATION_ERROR, "保存文本切片失败");
            }
            kbDocumentService.lambdaUpdate()
                    .eq(KbDocument::getId, documentId)
                    .set(KbDocument::getStatus, STATUS_SUCCESS)
                    .set(KbDocument::getUpdateTime, new Date())
                    .set(KbDocument::getUpdateBy, username)
                    .update();
        } catch (RuntimeException ex) {
            vectorStore.delete(vectorDocuments.stream().map(Document::getId).toList());
            log.error("保存文本切片失败，kbId={}, file={}", kbId, fileObject.getFilename(), ex);
            throw ex;
        }
    }

    private Resource buildResource(MinioFileObject fileObject) {
        return new ByteArrayResource(fileObject.getData()) {
            @Override
            public String getFilename() {
                return fileObject.getFilename();
            }

            @NotNull
            @Override
            public String getDescription() {
                return fileObject.getObjectName();
            }
        };
    }

    /**
     * 推断文件类型（优先文件名后缀，其次 Content-Type）。
     */
    private String resolveFileType(MinioFileObject fileObject) {
        String filename = fileObject.getFilename();
        if (StringUtils.hasText(filename)) {
            int lastDot = filename.lastIndexOf('.');
            if (lastDot >= 0 && lastDot < filename.length() - 1) {
                return filename.substring(lastDot + 1).toLowerCase(Locale.ROOT);
            }
        }
        String contentType = fileObject.getContentType();
        if (StringUtils.hasText(contentType)) {
            int slashIndex = contentType.lastIndexOf('/');
            if (slashIndex >= 0 && slashIndex < contentType.length() - 1) {
                return contentType.substring(slashIndex + 1).toLowerCase(Locale.ROOT);
            }
            return contentType.toLowerCase(Locale.ROOT);
        }
        return "unknown";
    }

    /**
     * 将文件大小格式化为 MB 文本。
     */
    private String formatFileSizeInMb(MinioFileObject fileObject) {
        byte[] data = fileObject.getData();
        long sizeInBytes = data == null ? 0 : data.length;
        double sizeInMb = sizeInBytes / (1024.0 * 1024.0);
        return String.format(Locale.ROOT, "%.2fMB", sizeInMb);
    }

}
