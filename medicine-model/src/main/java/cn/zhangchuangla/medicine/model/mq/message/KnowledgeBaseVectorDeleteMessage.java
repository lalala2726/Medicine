package cn.zhangchuangla.medicine.model.mq.message;

import lombok.Builder;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 知识库文档向量异步删除消息。
 */
@Data
@Builder
public class KnowledgeBaseVectorDeleteMessage implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 知识库 ID，用于定位 Milvus 集合。
     */
    private Integer knowledgeBaseId;
    /**
     * 需要删除的向量 ID 列表（字符串形式，与写入时的 id 保持一致）。
     */
    private List<String> vectorIds;
    /**
     * 关联的文档 ID，仅用于日志追踪。
     */
    private Long documentId;
}
