package cn.zhangchuangla.medicine.model.mq.message;

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

    /**
     * 知识库 ID。
     */
    private Integer knowledgeBaseId;

    /**
     * 文档 ID。
     */
    private Long documentId;

    /**
     * 切片 ID。
     */
    private Long chunkId;

    /**
     * 切片序号。
     */
    private Integer chunkIndex;

    /**
     * 向量 ID（用于更新）。
     */
    private Long vectorId;

    /**
     * 切片内容。
     */
    private String content;
}
