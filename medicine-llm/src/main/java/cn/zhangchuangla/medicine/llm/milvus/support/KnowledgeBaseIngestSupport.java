package cn.zhangchuangla.medicine.llm.milvus.support;

import cn.zhangchuangla.medicine.common.core.enums.ResponseCode;
import cn.zhangchuangla.medicine.common.core.exception.ServiceException;
import cn.zhangchuangla.medicine.common.milvus.config.MilvusProperties;
import cn.zhangchuangla.medicine.llm.milvus.service.MilvusKnowledgeBaseService;
import cn.zhangchuangla.medicine.processing.FileTextExtractor;
import cn.zhangchuangla.medicine.processing.model.FileParseResult;
import cn.zhangchuangla.medicine.processing.model.PageTextResult;
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
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 知识库导入/切片/向量写入的通用支撑，抽离到 milvus 模块复用。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KnowledgeBaseIngestSupport {

    private final MilvusProperties milvusProperties;
    private final MilvusKnowledgeBaseService milvusKnowledgeBaseService;
    private final EmbeddingModel embeddingModel;
    private final FileTextExtractor fileTextExtractor = new FileTextExtractor();

    /**
     * 构造标准切片器，按 token 控制切片。
     */
    public TokenTextSplitter buildSplitter(int chunkSize,
                                           int minChunkSizeChars,
                                           int minChunkLengthToEmbed,
                                           int maxChunks,
                                           boolean keepSeparator) {
        return TokenTextSplitter.builder()
                .withChunkSize(chunkSize)
                .withMinChunkSizeChars(minChunkSizeChars)
                .withMinChunkLengthToEmbed(minChunkLengthToEmbed)
                .withMaxNumChunks(maxChunks)
                .withKeepSeparator(keepSeparator)
                .build();
    }

    /**
     * 默认的 token 统计器。
     */
    public TokenCountEstimator buildTokenEstimator() {
        return new JTokkitTokenCountEstimator();
    }

    /**
     * 使用 file-processing 模块解析文件，返回按页/节切分的 Document。
     */
    public List<Document> parseDocuments(Resource resource) {
        String filename = resource.getFilename();
        try (InputStream inputStream = resource.getInputStream()) {
            FileParseResult result = fileTextExtractor.parse(filename, inputStream);
            List<PageTextResult> pages = result.getPages();
            if (CollectionUtils.isEmpty(pages)) {
                throw new ServiceException(ResponseCode.OPERATION_ERROR, "未能读取到文件内容");
            }
            List<Document> documents = new ArrayList<>();
            for (PageTextResult page : pages) {
                if (!StringUtils.hasText(page.getText())) {
                    continue;
                }
                Document.Builder builder = Document.builder()
                        .text(page.getText());
                if (page.getPageNumber() != null) {
                    builder.metadata("pageNumber", page.getPageNumber());
                }
                if (StringUtils.hasText(page.getSectionLabel())) {
                    builder.metadata("sectionLabel", page.getSectionLabel());
                }
                if (StringUtils.hasText(filename)) {
                    builder.metadata("fileName", filename);
                }
                documents.add(builder.build());
            }
            if (CollectionUtils.isEmpty(documents)) {
                throw new ServiceException(ResponseCode.OPERATION_ERROR, "文件内容为空，无法导入");
            }
            return documents;
        } catch (IOException e) {
            throw new ServiceException(ResponseCode.OPERATION_ERROR, "读取文件失败");
        }
    }

    /**
     * 兼容旧逻辑：合并文本返回单个 Document。
     */
    public Document parseDocument(Resource resource) {
        List<Document> documents = parseDocuments(resource);
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

    /**
     * 过滤/截断超长切片。
     */
    public List<Document> sanitizeDocuments(List<Document> splitDocuments, int maxChunkCharLength) {
        List<Document> sanitized = new ArrayList<>(splitDocuments.size());
        for (Document doc : splitDocuments) {
            String text = doc.getText();
            if (!StringUtils.hasText(text)) {
                continue;
            }
            if (text.length() > maxChunkCharLength) {
                Map<String, Object> meta = doc.getMetadata();
                log.warn("切片长度过长，已截断。length={}, limit={}, kbId={}, docId={}",
                        text.length(), maxChunkCharLength,
                        meta.get("kbId"),
                        meta.get("docId"));
                Document truncated = doc.mutate()
                        .text(text.substring(0, maxChunkCharLength))
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
     * 分批写入向量。
     */
    public void addVectorsInBatches(MilvusVectorStore vectorStore, List<Document> vectorDocuments, int batchSize) {
        List<String> addedIds = new ArrayList<>();
        for (int i = 0; i < vectorDocuments.size(); i += batchSize) {
            List<Document> batch = vectorDocuments.subList(i, Math.min(i + batchSize, vectorDocuments.size()));
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

    /**
     * 构建 Milvus 客户端。
     */
    public MilvusServiceClient buildMilvusClient() {
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

    /**
     * 构建 VectorStore，与 MilvusKnowledgeBaseService 的 schema 保持一致。
     */
    public MilvusVectorStore buildVectorStore(MilvusServiceClient milvusServiceClient, Integer kbId) {
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
                .initializeSchema(false)
                .build();
    }

    /**
     * 解析 Milvus 度量方式。
     */
    public MetricType resolveMetricType() {
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

    /**
     * 安全关闭客户端。
     */
    public void closeQuietly(MilvusServiceClient milvusServiceClient) {
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
