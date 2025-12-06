package cn.zhangchuangla.medicine.common.rabbitmq.message;

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

    private Integer knowledgeBaseId;
    private List<String> fileUrls;
    private String username;
}
