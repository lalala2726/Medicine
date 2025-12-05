package cn.zhangchuangla.medicine.llm.config;

import cn.zhangchuangla.medicine.common.core.exception.ServiceException;
import cn.zhangchuangla.medicine.common.milvus.config.MilvusProperties;
import cn.zhangchuangla.medicine.common.milvus.service.MilvusKnowledgeBaseService;
import io.milvus.client.MilvusServiceClient;
import io.milvus.param.IndexType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.generation.augmentation.ContextualQueryAugmenter;
import org.springframework.ai.rag.generation.augmentation.QueryAugmenter;
import org.springframework.ai.rag.retrieval.search.DocumentRetriever;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.milvus.MilvusVectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * RAG 配置：基于 Milvus 向量库的检索增强。
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class RagConfiguration {

    private static final int DEFAULT_TOP_K = 6;
    private static final double DEFAULT_SIMILARITY_THRESHOLD = 0.6d;

    private final MilvusProperties milvusProperties;
    private final MilvusKnowledgeBaseService milvusKnowledgeBaseService;

    @Bean
    public MilvusVectorStore knowledgeBaseVectorStore(MilvusServiceClient milvusServiceClient,
                                                      EmbeddingModel embeddingModel) {
        Integer knowledgeBaseId = milvusProperties.getDefaultKnowledgeBaseId();
        milvusKnowledgeBaseService.createKnowledgeBaseSpace(knowledgeBaseId);
        String collectionName = milvusKnowledgeBaseService.buildCollectionName(knowledgeBaseId);

        MilvusVectorStore vectorStore = MilvusVectorStore.builder(milvusServiceClient, embeddingModel)
                .databaseName(milvusProperties.getDatabase())
                .collectionName(collectionName)
                .embeddingDimension(milvusProperties.getVectorDimension())
                .metricType(milvusProperties.getMetricTypeV1())
                .indexType(IndexType.AUTOINDEX)
                .initializeSchema(true)
                .build();
        try {
            vectorStore.afterPropertiesSet();
        } catch (Exception ex) {
            log.error("初始化 Milvus 向量存储失败", ex);
            throw new ServiceException("初始化 Milvus 向量存储失败");
        }
        return vectorStore;
    }

    @Bean
    public DocumentRetriever knowledgeBaseDocumentRetriever(MilvusVectorStore knowledgeBaseVectorStore) {
        return VectorStoreDocumentRetriever.builder()
                .vectorStore(knowledgeBaseVectorStore)
                .topK(DEFAULT_TOP_K)
                .similarityThreshold(DEFAULT_SIMILARITY_THRESHOLD)
                .build();
    }

    @Bean
    public QueryAugmenter knowledgeBaseQueryAugmenter() {
        return ContextualQueryAugmenter.builder()
                .documentFormatter(this::formatDocuments)
                .build();
    }

    @Bean
    public RetrievalAugmentationAdvisor knowledgeBaseRetrievalAdvisor(
            DocumentRetriever knowledgeBaseDocumentRetriever,
            QueryAugmenter knowledgeBaseQueryAugmenter) {
        return RetrievalAugmentationAdvisor.builder()
                .documentRetriever(knowledgeBaseDocumentRetriever)
                .queryAugmenter(knowledgeBaseQueryAugmenter)
                .build();
    }

    private String formatDocuments(List<Document> documents) {
        List<String> samples = documents.stream()
                .map(doc -> {
                    String source = extractSource(doc);
                    String score = doc.getScore() == null ? "-" : String.format("%.4f", doc.getScore());
                    String snippet = doc.getText() == null ? "" : doc.getText().replaceAll("\\s+", " ");
                    return "id=" + doc.getId()
                            + ", source=" + source
                            + ", score=" + score
                            + ", text=\"" + snippet + "\"";
                })
                .toList();
        log.info("RAG 检索到 {} 条文档，示例: {}", documents.size(), samples);
        return documents.stream()
                .map(this::formatDocument)
                .collect(Collectors.joining("\n\n---\n\n"));
    }

    private String formatDocument(Document document) {
        Map<String, Object> metadata = document.getMetadata();
        String source = extractSource(metadata);
        return "来源: " + source + "\n" + document.getText();
    }

    private String extractSource(Document document) {
        Map<String, Object> metadata = document.getMetadata();
        return extractSource(metadata);
    }

    private String extractSource(Map<String, Object> metadata) {
        if (metadata == null) {
            return "unknown";
        }
        return Stream.of("filename", "source", "url")
                .map(metadata::get)
                .filter(Objects::nonNull)
                .map(Object::toString)
                .findFirst()
                .orElse("unknown");
    }
}
