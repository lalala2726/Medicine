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
    private Integer knowledgeBaseId;

    @Schema(description = "文件名", example = "1.pdf")
    private String fileName;

    @Schema(description = "文件大小", example = "1.0MB")
    private String fileSize;

    @Schema(description = "文件分块数", example = "1")
    private Integer chunk;

    @Schema(description = "文件状态", example = "分片中")
    private String status;

    @Schema(description = "文件类型", example = "pdf")
    private String fileType;

    @Schema(description = "上传时间", example = "2025-12-05 00:00:00")
    private Date uploadTime;

    @Schema(description = "更新时间", example = "2025-12-05 00:00:00")
    private Date updateTime;

}
