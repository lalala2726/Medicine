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
    private Integer id;

    @Schema(description = "知识库名称", example = "常见用药知识库")
    private String name;

    @Schema(description = "知识库描述", example = "覆盖常见用药相关的问答内容")
    private String description;

    @Schema(description = "封面", example = "https://example.com/cover.jpg")
    private String cover;

    @Schema(description = "更新时间")
    private Date updateTime;
}
