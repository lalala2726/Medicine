package cn.zhangchuangla.medicine.common.rabbitmq.message;

import lombok.Builder;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 单个文档切片内容更新后，触发向量重算的消息。
 */
@Data
@Builder
public class KnowledgeBaseChunkUpdateMessage implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Integer knowledgeBaseId;
    private Long documentId;
    private Long chunkId;
    private Integer chunkIndex;
    private Long vectorId;
    private String content;
}

