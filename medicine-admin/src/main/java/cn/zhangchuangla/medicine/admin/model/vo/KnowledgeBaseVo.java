package cn.zhangchuangla.medicine.admin.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

/**
 * 知识库详情视图对象
 */
@Data
@Schema(description = "知识库详情视图对象")
public class KnowledgeBaseVo {

    @Schema(description = "主键ID", example = "1")
    private Integer id;

    @Schema(description = "知识库名称", example = "常见用药知识库")
    private String name;

    @Schema(description = "知识库描述", example = "覆盖常见用药相关的问答内容")
    private String description;

    @Schema(description = "封面", example = "https://example.com/cover.jpg")
    private String cover;

    @Schema(description = "创建时间")
    private Date createTime;

    @Schema(description = "更新时间")
    private Date updateTime;

    @Schema(description = "创建人")
    private String createBy;

    @Schema(description = "修改人")
    private String updateBy;
}
