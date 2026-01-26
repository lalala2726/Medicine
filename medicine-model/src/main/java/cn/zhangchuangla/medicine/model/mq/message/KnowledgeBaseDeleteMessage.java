package cn.zhangchuangla.medicine.model.mq.message;

import lombok.Builder;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 知识库删除异步消息，批量清理文档与切片。
 */
@Data
@Builder
public class KnowledgeBaseDeleteMessage implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 知识库 ID。
     */
    private Integer knowledgeBaseId;

    /**
     * 批量大小（单次删除文档数量）。
     */
    private Integer batchSize;
}
