package cn.zhangchuangla.medicine.admin.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @author Chuang
 * <p>
 * created on 2025/12/5
 */
@Data
@Schema(description = "知识库文档")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class KnowledgeBaseDocumentVo {

    @Schema(description = "文档ID", example = "1")
    private Long id;

    @Schema(description = "知识库ID", example = "1")
    private Long knowledgeBaseId;

    @Schema(description = "文件名", example = "1.pdf")
    private String fileName;

    @Schema(description = "文件 URL", example = "https://example.com/file.pdf")
    private String fileUrl;

    @Schema(description = "文件大小（字节）", example = "1024")
    private Long fileSize;

    @Schema(description = "文件 MIME 类型", example = "application/pdf")
    private String mimeType;

    @Schema(description = "索引阶段，取值见 KbDocumentStageEnum", example = "PENDING")
    private String stage;

    @Schema(description = "最近一次处理失败错误信息", example = "文件解析失败")
    private String lastError;

    @Schema(description = "创建时间", example = "2025-12-05 00:00:00")
    private Date createdAt;

    @Schema(description = "更新时间", example = "2025-12-05 00:00:00")
    private Date updatedAt;

}
