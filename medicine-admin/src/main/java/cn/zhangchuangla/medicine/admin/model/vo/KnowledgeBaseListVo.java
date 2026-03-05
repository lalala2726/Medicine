package cn.zhangchuangla.medicine.admin.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

/**
 * 知识库列表展示对象
 */
@Data
@Schema(description = "知识库列表展示对象")
public class KnowledgeBaseListVo {

    @Schema(description = "主键ID", example = "1")
    private Long id;

    @Schema(description = "知识库唯一名称（业务键）", example = "common_medicine_kb")
    private String knowledgeName;

    @Schema(description = "知识库展示名称", example = "常见用药知识库")
    private String displayName;

    @Schema(description = "知识库描述", example = "覆盖常见用药相关问答内容")
    private String description;

    @Schema(description = "状态（0启用 1停用）", example = "0")
    private Integer status;

    @Schema(description = "创建时间")
    private Date createdAt;

    @Schema(description = "更新时间")
    private Date updatedAt;
}
