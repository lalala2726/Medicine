package cn.zhangchuangla.medicine.model.mq;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 知识库导入 result 消息体。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeImportResultMessage implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String message_type;
    private String task_uuid;
    private String biz_key;
    private Long version;
    private String stage;
    private String stage_detail;
    private String message;
    private String knowledge_name;
    private Long document_id;
    private String file_url;
    private String filename;
    private Integer chunk_count;
    private Integer vector_count;
    private String embedding_model;
    private Integer embedding_dim;
    private String occurred_at;
    private Long duration_ms;
}

