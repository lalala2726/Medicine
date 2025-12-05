package cn.zhangchuangla.medicine.admin.service.impl;

import cn.zhangchuangla.medicine.admin.common.storage.model.MinioFileObject;
import cn.zhangchuangla.medicine.admin.common.storage.service.MinioStorageService;
import cn.zhangchuangla.medicine.admin.mapper.KnowledgeBaseMapper;
import cn.zhangchuangla.medicine.admin.model.request.KnowledgeBaseAddRequest;
import cn.zhangchuangla.medicine.admin.model.request.KnowledgeBaseImportRequest;
import cn.zhangchuangla.medicine.admin.model.request.KnowledgeBaseListRequest;
import cn.zhangchuangla.medicine.admin.model.request.KnowledgeBaseUpdateRequest;
import cn.zhangchuangla.medicine.admin.service.KbDocumentChunkService;
import cn.zhangchuangla.medicine.admin.service.KbDocumentService;
import cn.zhangchuangla.medicine.admin.service.KnowledgeBaseService;
import cn.zhangchuangla.medicine.common.core.enums.ResponseCode;
import cn.zhangchuangla.medicine.common.core.exception.ServiceException;
import cn.zhangchuangla.medicine.common.milvus.config.MilvusProperties;
import cn.zhangchuangla.medicine.common.milvus.service.MilvusKnowledgeBaseService;
import cn.zhangchuangla.medicine.common.rabbitmq.publisher.KnowledgeBaseIngestPublisher;
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
import io.milvus.param.ConnectParam;
import io.milvus.param.IndexType;
import io.milvus.param.MetricType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.reader.tika.TikaDocumentReader;
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
     * 文本分片的默认参数：尽量让每片在 500 tokens 左右，便于向量召回。
     */
    private static final int DEFAULT_CHUNK_SIZE = 400;
    private static final int MIN_CHUNK_SIZE_CHARS = 120;
    private static final int MIN_CHUNK_LENGTH_TO_EMBED = 80;
    private static final int MAX_CHUNKS = 5000;
    private static final int EMBEDDING_BATCH_SIZE = 10;
    /**
     * 防止写入 Milvus 的 VarChar 超长，按字符截断；65535 是常见上限，这里预留安全余量。
     */
    private static final int MAX_CHUNK_CHAR_LENGTH = 2000;

    private final MilvusKnowledgeBaseService milvusKnowledgeBaseService;
    private final MinioStorageService minioStorageService;
    private final KbDocumentService kbDocumentService;
    private final KbDocumentChunkService kbDocumentChunkService;
    private final EmbeddingModel embeddingModel;
    private final MilvusProperties milvusProperties;
    private final KnowledgeBaseIngestPublisher knowledgeBaseIngestPublisher;

    @Override
    public Page<KnowledgeBase> listKnowledgeBase(KnowledgeBaseListRequest request) {
        LambdaQueryWrapper<KnowledgeBase> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(request.getName())) {
            wrapper.like(KnowledgeBase::getName, request.getName());
        }
        if (StringUtils.hasText(request.getDescription())) {
            wrapper.like(KnowledgeBase::getDescription, request.getDescription());
        }
        wrapper.orderByDesc(KnowledgeBase::getCreateTime);
        return page(request.toPage(), wrapper);
    }

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

    @Override
    public KnowledgeBase getKnowledgeBase(Integer id) {
        KnowledgeBase knowledgeBase = getById(id);
        if (knowledgeBase == null) {
            throw new ServiceException("知识库不存在");
        }
        return knowledgeBase;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean importKnowledgeBase(KnowledgeBaseImportRequest request) {
        KnowledgeBase knowledgeBase = getKnowledgeBase(request.getKnowledgeBaseId());
        if (CollectionUtils.isEmpty(request.getFileUrls())) {
            throw new ServiceException(ResponseCode.PARAM_ERROR, "请至少提供一个文本文件地址");
        }
        knowledgeBaseIngestPublisher.publish(knowledgeBase.getId(), request.getFileUrls());
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void ingestKnowledgeBase(Integer knowledgeBaseId, List<String> fileUrls) {
        KnowledgeBase knowledgeBase = getKnowledgeBase(knowledgeBaseId);
        if (CollectionUtils.isEmpty(fileUrls)) {
            throw new ServiceException(ResponseCode.PARAM_ERROR, "请至少提供一个文本文件地址");
        }
        // 预先准备好分片与 token 统计工具，保证所有文件使用一致策略
        // Spring AI 的 TokenTextSplitter 会按照 token 近似长度切片，避免过长文本直接入库
        TokenTextSplitter splitter = TokenTextSplitter.builder()
                .withChunkSize(DEFAULT_CHUNK_SIZE)
                .withMinChunkSizeChars(MIN_CHUNK_SIZE_CHARS)
                .withMinChunkLengthToEmbed(MIN_CHUNK_LENGTH_TO_EMBED)
                .withMaxNumChunks(MAX_CHUNKS)
                .withKeepSeparator(true)
                .build();
        TokenCountEstimator tokenCountEstimator = new JTokkitTokenCountEstimator();

        // 确保集合存在，但不再删除旧数据，避免导入时覆盖历史内容
        milvusKnowledgeBaseService.createKnowledgeBaseSpace(knowledgeBase.getId());
        // VectorStore 需要手动初始化 schema（未启用自动装配），因此手动构建客户端与集合
        MilvusServiceClient milvusServiceClient = buildMilvusClient();
        MilvusVectorStore vectorStore = buildVectorStore(milvusServiceClient, knowledgeBase.getId());
        try {
            vectorStore.afterPropertiesSet();
            for (String fileUrl : fileUrls) {
                importFile(knowledgeBase.getId(), fileUrl, splitter, tokenCountEstimator, vectorStore);
            }
        } catch (Exception ex) {
            log.error("知识库导入失败", ex);
            throw ex instanceof ServiceException ? (ServiceException) ex
                    : new ServiceException(ResponseCode.OPERATION_ERROR, "知识库导入失败，请稍后重试");
        } finally {
            closeQuietly(milvusServiceClient);
        }
    }

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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteKnowledgeBase(Integer id) {
        KnowledgeBase knowledgeBase = getKnowledgeBase(id);

        // 删除向量库集合，保证数据库和向量库一致
        milvusKnowledgeBaseService.dropKnowledgeBaseSpace(id);

        return removeById(knowledgeBase.getId());
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
     */
    private void importFile(Integer kbId,
                            String fileUrl,
                            TokenTextSplitter splitter,
                            TokenCountEstimator tokenCountEstimator,
                            MilvusVectorStore vectorStore) {
        if (!StringUtils.hasText(fileUrl)) {
            throw new ServiceException(ResponseCode.PARAM_ERROR, "文件地址不能为空");
        }

        MinioFileObject fileObject = minioStorageService.fetchFileByUrl(fileUrl);
        Document extracted = parseDocumentWithTika(fileObject);
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
        splitDocuments = sanitizeDocuments(splitDocuments);
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
                    .pageNum(null)
                    .vectorId(vectorId)
                    .createTime(now)
                    .build();
            chunkEntities.add(chunk);
            chunkIndex++;
        }

        KbDocument kbDocument = KbDocument.builder()
                .id(documentId)
                .kbId(kbId)
                .filename(fileObject.getFilename())
                .filePath(fileObject.getObjectName())
                .fileType(resolveFileType(fileObject))
                .chunkCount(chunkEntities.size())
                .status("SUCCESS")
                .build();

        if (!kbDocumentService.save(kbDocument)) {
            throw new ServiceException(ResponseCode.OPERATION_ERROR, "保存文件元数据失败");
        }

        try {
            // 先写向量，确保成功后再批量入库 chunk 记录；分批写入避免一次请求超量
            addVectorsInBatches(vectorStore, vectorDocuments);
        } catch (Exception ex) {
            log.error("向量写入失败，kbId={}, file={}", kbId, fileObject.getFilename(), ex);
            throw new ServiceException(ResponseCode.OPERATION_ERROR, "向量写入失败，请稍后重试");
        }

        try {
            if (!kbDocumentChunkService.saveBatch(chunkEntities)) {
                vectorStore.delete(vectorDocuments.stream().map(Document::getId).toList());
                throw new ServiceException(ResponseCode.OPERATION_ERROR, "保存文本切片失败");
            }
        } catch (RuntimeException ex) {
            vectorStore.delete(vectorDocuments.stream().map(Document::getId).toList());
            throw ex;
        }
    }

    /**
     * 使用 Tika 抽取多种格式的文本内容，并合并为单个 Document。
     */
    private Document parseDocumentWithTika(MinioFileObject fileObject) {
        Resource resource = new ByteArrayResource(fileObject.getData()) {
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

        List<Document> documents = new TikaDocumentReader(resource).get();
        if (CollectionUtils.isEmpty(documents)) {
            throw new ServiceException(ResponseCode.OPERATION_ERROR, "未能读取到文件内容");
        }

        String mergedText = documents.stream()
                .map(Document::getText)
                .filter(StringUtils::hasText)
                .collect(Collectors.joining("\n\n"));
        if (!StringUtils.hasText(mergedText)) {
            throw new ServiceException(ResponseCode.OPERATION_ERROR, "文件内容为空，无法导入");
        }

        Map<String, Object> mergedMetadata = new HashMap<>();
        documents.stream()
                .map(Document::getMetadata)
                .forEach(meta -> meta.forEach(mergedMetadata::putIfAbsent));

        return Document.builder()
                .text(mergedText)
                .metadata(mergedMetadata)
                .build();
    }

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
     * 过滤/截断异常超长的切片，避免超出 Milvus VarChar 长度。
     */
    private List<Document> sanitizeDocuments(List<Document> splitDocuments) {
        List<Document> sanitized = new ArrayList<>(splitDocuments.size());
        for (Document doc : splitDocuments) {
            String text = doc.getText();
            if (!StringUtils.hasText(text)) {
                continue;
            }
            if (text.length() > MAX_CHUNK_CHAR_LENGTH) {
                Map<String, Object> meta = doc.getMetadata();
                log.warn("切片长度过长，已截断。length={}, limit={}, kbId={}, docId={}",
                        text.length(), MAX_CHUNK_CHAR_LENGTH,
                        meta.get("kbId"),
                        meta.get("docId"));
                Document truncated = doc.mutate()
                        .text(text.substring(0, MAX_CHUNK_CHAR_LENGTH))
                        .metadata("truncated", true)
                        .build();
                sanitized.add(truncated);
            } else {
                sanitized.add(doc);
            }
        }
        return sanitized;
    }

    /**
     * 将向量分批写入，避免单次请求传入超过 EMBEDDING_BATCH_SIZE 的切片。
     */
    private void addVectorsInBatches(MilvusVectorStore vectorStore, List<Document> vectorDocuments) {
        List<String> addedIds = new ArrayList<>();
        for (int i = 0; i < vectorDocuments.size(); i += EMBEDDING_BATCH_SIZE) {
            List<Document> batch = vectorDocuments.subList(i, Math.min(i + EMBEDDING_BATCH_SIZE, vectorDocuments.size()));
            try {
                vectorStore.add(batch);
                addedIds.addAll(batch.stream().map(Document::getId).toList());
            } catch (Exception ex) {
                if (!addedIds.isEmpty()) {
                    try {
                        vectorStore.delete(addedIds);
                    } catch (Exception cleanupEx) {
                        log.warn("清理已写入的向量失败，ids={}", addedIds, cleanupEx);
                    }
                }
                throw ex;
            }
        }
    }

    private MilvusServiceClient buildMilvusClient() {
        if (!StringUtils.hasText(milvusProperties.getUri())) {
            throw new ServiceException(ResponseCode.OPERATION_ERROR, "Milvus 连接地址未配置");
        }
        ConnectParam.Builder builder = ConnectParam.newBuilder()
                .withUri(milvusProperties.getUri());
        if (StringUtils.hasText(milvusProperties.getToken())) {
            builder.withToken(milvusProperties.getToken());
        }
        if (StringUtils.hasText(milvusProperties.getDatabase())) {
            builder.withDatabaseName(milvusProperties.getDatabase());
        }
        return new MilvusServiceClient(builder.build());
    }

    private MilvusVectorStore buildVectorStore(MilvusServiceClient milvusServiceClient, Integer kbId) {
        // 通过 Spring AI builder 手动指定集合名/维度/索引，确保与 MilvusKnowledgeBaseService 的 schema 保持一致
        return MilvusVectorStore.builder(milvusServiceClient, embeddingModel)
                .databaseName(milvusProperties.getDatabase())
                .collectionName(milvusKnowledgeBaseService.buildCollectionName(kbId))
                .iDFieldName(MilvusVectorStore.DOC_ID_FIELD_NAME)
                .contentFieldName(MilvusVectorStore.CONTENT_FIELD_NAME)
                .metadataFieldName(MilvusVectorStore.METADATA_FIELD_NAME)
                .embeddingDimension(milvusProperties.getVectorDimension())
                .indexType(IndexType.AUTOINDEX)
                .metricType(resolveMetricType())
                .autoId(false)
                // 集合由 MilvusKnowledgeBaseService 创建，这里不再重新初始化，避免覆盖既有索引/数据
                .initializeSchema(false)
                .build();
    }

    private MetricType resolveMetricType() {
        String metric = milvusProperties.getMetricType();
        if (!StringUtils.hasText(metric)) {
            return MetricType.COSINE;
        }
        try {
            return MetricType.valueOf(metric.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            return MetricType.COSINE;
        }
    }

    private void closeQuietly(MilvusServiceClient milvusServiceClient) {
        if (milvusServiceClient == null) {
            return;
        }
        try {
            milvusServiceClient.close(5L);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Interrupted while closing Milvus client", e);
        } catch (Exception ignored) {
            log.warn("关闭 Milvus 客户端时出现异常", ignored);
        }
    }
}
