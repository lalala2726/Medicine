package cn.zhangchuangla.medicine.common.milvus.config;

import io.milvus.param.MetricType;
import io.milvus.v2.common.IndexParam;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

import java.util.Locale;

/**
 * Milvus 连接与集合配置
 */
@Data
@ConfigurationProperties(prefix = "milvus")
public class MilvusProperties {

    /**
     * Milvus 服务地址，例如：http://localhost:19530
     */
    private String uri;

    /**
     * 认证 token，格式通常为 root:Milvus
     */
    private String token;

    /**
     * 数据库名称
     */
    private String database = "default";

    /**
     * 知识库集合前缀，便于按 ID 构建唯一集合名
     */
    private String collectionPrefix = "kb_";

    /**
     * 向量维度，需与后续嵌入模型维度保持一致
     */
    private Integer vectorDimension = 1024;

    /**
     * 文本字段最大长度
     */
    private Integer contentMaxLength = 2048;

    /**
     * 近似向量检索的相似度度量方式，默认 COSINE
     */
    private String metricType = "COSINE";

    /**
     * 默认知识库 ID，便于在调用方未传入时使用固定集合。
     */
    private Integer defaultKnowledgeBaseId = 8;

    public IndexParam.MetricType getMetricTypeEnum() {
        String type = StringUtils.hasText(metricType) ? metricType : "COSINE";
        try {
            return IndexParam.MetricType.valueOf(type.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            return IndexParam.MetricType.COSINE;
        }
    }

    public MetricType getMetricTypeV1() {
        String type = StringUtils.hasText(metricType) ? metricType : "COSINE";
        try {
            return MetricType.valueOf(type.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            return MetricType.COSINE;
        }
    }
}
