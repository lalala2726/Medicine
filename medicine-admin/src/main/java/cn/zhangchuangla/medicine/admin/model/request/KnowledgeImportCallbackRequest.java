package cn.zhangchuangla.medicine.admin.model.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.OffsetDateTime;

/**
 * 功能描述：
 * 知识库导入结果回调请求参数实体（GET Query 参数绑定）。
 * 字段校验失败时由 Spring Validation 抛出绑定异常。
 */
@Data
@ToString
public class KnowledgeImportCallbackRequest {

    /**
     * 任务唯一标识
     */
    @NotBlank(message = "task_uuid 不能为空")
    private String task_uuid;

    /**
     * 知识库名称
     */
    @NotBlank(message = "knowledge_name 不能为空")
    private String knowledge_name;

    /**
     * 文档 ID
     */
    @Min(value = 1, message = "document_id 必须大于 0")
    private Long document_id;

    /**
     * 文件 URL
     */
    @NotBlank(message = "file_url 不能为空")
    private String file_url;

    /**
     * 导入状态：SUCCESS/FAILED
     */
    @NotBlank(message = "status 不能为空")
    private String status;

    /**
     * 状态消息
     */
    @NotBlank(message = "message 不能为空")
    private String message;

    /**
     * 向量模型名称
     */
    @NotBlank(message = "embedding_model 不能为空")
    private String embedding_model;

    /**
     * 向量维度
     */
    @Min(value = 0, message = "embedding_dim 不能小于 0")
    private Integer embedding_dim;

    /**
     * 切片策略
     */
    @NotBlank(message = "chunk_strategy 不能为空")
    private String chunk_strategy;

    /**
     * 切片大小
     */
    @Min(value = 1, message = "chunk_size 必须大于 0")
    private Integer chunk_size;

    /**
     * token 大小
     */
    @Min(value = 1, message = "token_size 必须大于 0")
    private Integer token_size;

    /**
     * 切片数量
     */
    @Min(value = 0, message = "chunk_count 不能小于 0")
    private Integer chunk_count;

    /**
     * 向量数量
     */
    @Min(value = 0, message = "vector_count 不能小于 0")
    private Integer vector_count;

    /**
     * 开始时间（ISO8601）
     */
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private OffsetDateTime started_at;

    /**
     * 结束时间（ISO8601）
     */
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private OffsetDateTime finished_at;

    /**
     * 总耗时（毫秒）
     */
    @Min(value = 0, message = "duration_ms 不能小于 0")
    private Long duration_ms;
}
