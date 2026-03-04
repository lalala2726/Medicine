package cn.zhangchuangla.medicine.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 文档索引快照表
 */
@TableName(value = "kb_document_index")
@Data
public class KbDocumentIndex {

    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 知识库ID
     */
    private Long knowledgeBaseId;

    /**
     * 文档ID
     */
    private Long documentId;

    /**
     * 索引版本
     */
    private Integer indexVersion;

    /**
     * 索引状态
     */
    private String indexStatus;

    /**
     * 切片数量
     */
    private Integer chunkCount;

    /**
     * Token 数量
     */
    private Integer tokenCount;

    /**
     * 切片策略
     */
    private String chunkStrategy;

    /**
     * 切片大小
     */
    private Integer chunkSize;

    /**
     * Token 大小
     */
    private Integer tokenSize;

    /**
     * 切片重叠
     */
    private Integer chunkOverlap;

    /**
     * 向量模型
     */
    private String embeddingModel;

    /**
     * 向量维度
     */
    private Integer embeddingDim;

    /**
     * Milvus 集合名称
     */
    private String milvusCollectionName;

    /**
     * 源内容哈希
     */
    private String sourceHash;

    /**
     * 最近一次索引时间
     */
    private Date lastIndexedAt;

    /**
     * 索引失败错误
     */
    private String errorMessage;

    /**
     * 创建人账号
     */
    private String createBy;

    /**
     * 最后更新人账号
     */
    private String updateBy;

    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 最后更新时间
     */
    private Date updatedAt;

    /**
     * 逻辑删除标记
     */
    private Integer isDeleted;

    /**
     * 逻辑删除时间
     */
    private Date deletedAt;
}
