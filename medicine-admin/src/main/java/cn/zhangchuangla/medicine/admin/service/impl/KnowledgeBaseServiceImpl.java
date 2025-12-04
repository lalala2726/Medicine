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
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.tokenizer.JTokkitTokenCountEstimator;
import org.springframework.ai.tokenizer.TokenCountEstimator;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.milvus.MilvusVectorStore;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.*;

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
    private static final int DEFAULT_CHUNK_SIZE = 500;
    private static final int MIN_CHUNK_SIZE_CHARS = 120;
    private static final int MIN_CHUNK_LENGTH_TO_EMBED = 80;
    private static final int MAX_CHUNKS = 5000;

    private final MilvusKnowledgeBaseService milvusKnowledgeBaseService;
    private final MinioStorageService minioStorageService;
    private final KbDocumentService kbDocumentService;
    private final KbDocumentChunkService kbDocumentChunkService;
    private final EmbeddingModel embeddingModel;
    private final MilvusProperties milvusProperties;

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

        // 为确保旧的 Milvus 集合 schema 与当前代码一致，这里先重建集合（会清空旧向量；如需保留请改为迁移方案）
        recreateMilvusCollection(knowledgeBase.getId());

        // VectorStore 需要手动初始化 schema（未启用自动装配），因此手动构建客户端与集合
        MilvusServiceClient milvusServiceClient = buildMilvusClient();
        MilvusVectorStore vectorStore = buildVectorStore(milvusServiceClient, knowledgeBase.getId());
        try {
            vectorStore.afterPropertiesSet();
            for (String fileUrl : request.getFileUrls()) {
                importTxtFile(knowledgeBase.getId(), fileUrl, splitter, tokenCountEstimator, vectorStore);
            }
            return true;
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
     * 单个 TXT 的导入流程：
     * 1. 从 MinIO 读取文件 -> 文本字符串
     * 2. 使用 TokenTextSplitter 切片，添加 chunk 序号与元数据
     * 3. 调用 Spring AI 向量存储，先生成 embedding 再写入 Milvus
     * 4. 将文档与切片元数据落库，保证向量与数据库一一对应
     */
    private void importTxtFile(Integer kbId,
                               String fileUrl,
                               TokenTextSplitter splitter,
                               TokenCountEstimator tokenCountEstimator,
                               MilvusVectorStore vectorStore) {
        if (!StringUtils.hasText(fileUrl)) {
            throw new ServiceException(ResponseCode.PARAM_ERROR, "文件地址不能为空");
        }

        MinioFileObject fileObject = minioStorageService.fetchFileByUrl(fileUrl);
        if (!isTxtFile(fileObject)) {
            throw new ServiceException(ResponseCode.PARAM_ERROR, "当前仅支持 txt 文件导入");
        }

        String content = new String(fileObject.getData(), StandardCharsets.UTF_8);
        if (!StringUtils.hasText(content)) {
            throw new ServiceException(ResponseCode.PARAM_ERROR, "文本内容为空，无法导入");
        }

        // 构造基础 Document（带文件元数据），便于切片后继承这些 metadata
        long documentId = IdWorker.getId();
        Document baseDoc = Document.builder()
                .id(String.valueOf(documentId))
                .text(content)
                .metadata(Map.of(
                        "kbId", kbId,
                        "docId", documentId,
                        "filename", fileObject.getFilename(),
                        "objectPath", fileObject.getObjectName()
                ))
                .build();

        List<Document> splitDocuments = splitter.apply(List.of(baseDoc));
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
                .fileType("txt")
                .chunkCount(chunkEntities.size())
                .status("SUCCESS")
                .build();

        if (!kbDocumentService.save(kbDocument)) {
            throw new ServiceException(ResponseCode.OPERATION_ERROR, "保存文件元数据失败");
        }

        try {
            // 先写向量，确保成功后再批量入库 chunk 记录
            vectorStore.add(vectorDocuments);
        } catch (Exception ex) {
            log.error("向量写入失败",ex);
            log.error("删除向量失败",ex);
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
                .initializeSchema(true)
                .build();
    }

    /**
     * 重新创建知识库对应的 Milvus 集合，避免旧 schema（如 kb_id 主键或缺少 metadata 字段）导致写入失败。
     * 如果需要保留历史向量，请改为迁移方案而非直接删除。
     */
    private void recreateMilvusCollection(Integer kbId) {
        try {
            milvusKnowledgeBaseService.dropKnowledgeBaseSpace(kbId);
        } catch (Exception ex) {
            log.warn("删除 Milvus 集合失败，将继续尝试重新创建。kbId={}", kbId, ex);
        }
        milvusKnowledgeBaseService.createKnowledgeBaseSpace(kbId);
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

    private boolean isTxtFile(MinioFileObject fileObject) {
        String filename = fileObject.getFilename();
        String contentType = fileObject.getContentType();
        boolean byName = StringUtils.hasText(filename) && filename.toLowerCase(Locale.ROOT).endsWith(".txt");
        boolean byContentType = StringUtils.hasText(contentType) && contentType.toLowerCase(Locale.ROOT).contains("text/plain");
        return byName || byContentType;
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
