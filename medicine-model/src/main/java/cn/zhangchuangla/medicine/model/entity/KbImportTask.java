package cn.zhangchuangla.medicine.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 知识库导入任务表
 */
@TableName(value = "kb_import_task")
@Data
public class KbImportTask {

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
     * 任务 UUID
     */
    private String taskUuid;

    /**
     * 任务触发来源
     */
    private String triggerSource;

    /**
     * 任务状态
     */
    private String status;

    /**
     * 任务进度百分比
     */
    private BigDecimal progress;

    /**
     * 切片策略快照
     */
    private String chunkStrategy;

    /**
     * 字符切片大小快照
     */
    private Integer chunkSize;

    /**
     * Token 切片大小快照
     */
    private Integer tokenSize;

    /**
     * 切片重叠大小快照
     */
    private Integer chunkOverlap;

    /**
     * 任务开始时间
     */
    private Date startedAt;

    /**
     * 任务结束时间
     */
    private Date finishedAt;

    /**
     * 任务失败错误详情
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
