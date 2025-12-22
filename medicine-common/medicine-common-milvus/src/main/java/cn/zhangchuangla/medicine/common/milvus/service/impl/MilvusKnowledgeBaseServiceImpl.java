package cn.zhangchuangla.medicine.common.milvus.service.impl;

import cn.zhangchuangla.medicine.common.core.exception.ServiceException;
import cn.zhangchuangla.medicine.common.milvus.config.MilvusProperties;
import cn.zhangchuangla.medicine.common.milvus.service.MilvusKnowledgeBaseService;
import io.milvus.v2.client.MilvusClientV2;
import io.milvus.v2.common.DataType;
import io.milvus.v2.common.IndexParam;
import io.milvus.v2.service.collection.CollectionService;
import io.milvus.v2.service.collection.request.AddFieldReq;
import io.milvus.v2.service.collection.request.CreateCollectionReq;
import io.milvus.v2.service.collection.request.DropCollectionReq;
import io.milvus.v2.service.collection.request.HasCollectionReq;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.vectorstore.milvus.MilvusVectorStore;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 使用 Milvus 管理知识库集合的创建与删除。
 * <p>
 * 仅封装新增、删除操作，便于在业务层保持数据库与 Milvus 的一致性。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MilvusKnowledgeBaseServiceImpl implements MilvusKnowledgeBaseService {

    private static final String PRIMARY_FIELD = MilvusVectorStore.DOC_ID_FIELD_NAME;
    private static final String VECTOR_FIELD = MilvusVectorStore.EMBEDDING_FIELD_NAME;
    private static final String CONTENT_FIELD = MilvusVectorStore.CONTENT_FIELD_NAME;
    private static final String METADATA_FIELD = MilvusVectorStore.METADATA_FIELD_NAME;
    private static final int DOC_ID_MAX_LENGTH = 64;

    private final MilvusClientV2 milvusClient;
    private final MilvusProperties milvusProperties;

    @Override
    public void createKnowledgeBaseSpace(Integer knowledgeBaseId) {
        knowledgeBaseId = resolveKnowledgeBaseId(knowledgeBaseId);
        if (!StringUtils.hasText(milvusProperties.getCollectionPrefix())) {
            throw new ServiceException("Milvus 集合前缀未配置");
        }
        if (milvusProperties.getVectorDimension() == null || milvusProperties.getVectorDimension() <= 0) {
            throw new ServiceException("Milvus 向量维度配置不正确");
        }

        String collectionName = buildCollectionName(knowledgeBaseId);
        boolean exists = milvusClient.hasCollection(HasCollectionReq.builder()
                .collectionName(collectionName)
                .build());
        if (exists) {
            log.info("Milvus collection [{}] already exists for knowledge base {}, skip recreate. vectorDimension={}", collectionName, knowledgeBaseId, milvusProperties.getVectorDimension());
            return;
        }

        CreateCollectionReq.CollectionSchema schema =
                CollectionService.createSchema();
        schema.addField(AddFieldReq.builder()
                .fieldName(PRIMARY_FIELD)
                .dataType(DataType.VarChar)
                .maxLength(DOC_ID_MAX_LENGTH)
                .isPrimaryKey(true)
                .autoID(false)
                .build());
        schema.addField(AddFieldReq.builder()
                .fieldName(VECTOR_FIELD)
                .dataType(DataType.FloatVector)
                .dimension(milvusProperties.getVectorDimension())
                .build());
        schema.addField(AddFieldReq.builder()
                .fieldName(CONTENT_FIELD)
                .dataType(DataType.VarChar)
                .maxLength(milvusProperties.getContentMaxLength())
                .isNullable(true)
                .build());
        schema.addField(AddFieldReq.builder()
                .fieldName(METADATA_FIELD)
                .dataType(DataType.JSON)
                .isNullable(true)
                .build());

        // 为主键和向量字段建立索引，使用 Milvus AUTOINDEX + 余弦距离做相似度检索
        List<IndexParam> indexParams = List.of(
                IndexParam.builder()
                        .fieldName(PRIMARY_FIELD)
                        .indexType(IndexParam.IndexType.AUTOINDEX)
                        .build(),
                IndexParam.builder()
                        .fieldName(VECTOR_FIELD)
                        .indexType(IndexParam.IndexType.AUTOINDEX)
                        .metricType(milvusProperties.getMetricTypeEnum())
                        .build()
        );

        milvusClient.createCollection(CreateCollectionReq.builder()
                .collectionName(collectionName)
                .collectionSchema(schema)
                .indexParams(indexParams)
                .build());

        log.info("Created Milvus collection [{}] for knowledge base {}, vectorDimension={}", collectionName, knowledgeBaseId, milvusProperties.getVectorDimension());
    }

    @Override
    public void dropKnowledgeBaseSpace(Integer knowledgeBaseId) {
        knowledgeBaseId = resolveKnowledgeBaseId(knowledgeBaseId);
        String collectionName = buildCollectionName(knowledgeBaseId);
        boolean exists = milvusClient.hasCollection(HasCollectionReq.builder()
                .collectionName(collectionName)
                .build());
        if (!exists) {
            log.info("Skip dropping non-existent Milvus collection [{}]", collectionName);
            return;
        }

        milvusClient.dropCollection(DropCollectionReq.builder()
                .collectionName(collectionName)
                .build());
        log.info("Dropped Milvus collection [{}] for knowledge base {}", collectionName, knowledgeBaseId);
    }

    @Override
    public String buildCollectionName(Integer knowledgeBaseId) {
        return milvusProperties.getCollectionPrefix() + resolveKnowledgeBaseId(knowledgeBaseId);
    }

    /**
     * 优先使用入参的知识库 ID，未传入时回退到配置的默认值。
     */
    private Integer resolveKnowledgeBaseId(Integer knowledgeBaseId) {
        if (knowledgeBaseId != null) {
            return knowledgeBaseId;
        }
        Integer defaultKbId = milvusProperties.getDefaultKnowledgeBaseId();
        if (defaultKbId == null) {
            throw new ServiceException("知识库ID不能为空");
        }
        return defaultKbId;
    }
}
