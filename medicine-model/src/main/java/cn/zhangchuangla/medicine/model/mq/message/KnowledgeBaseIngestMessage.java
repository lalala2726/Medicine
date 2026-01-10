package cn.zhangchuangla.medicine.model.mq.message;

import lombok.Builder;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 知识库导入异步任务消息。
 */
@Data
@Builder
public class KnowledgeBaseIngestMessage implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 知识库 ID。
     */
    private Integer knowledgeBaseId;

    /**
     * 待导入的文件 URL 列表。
     */
    private List<String> fileUrls;

    /**
     * 发起导入的操作人（用于审计）。
     */
    private String username;
}
