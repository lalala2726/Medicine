package cn.zhangchuangla.medicine.admin.model.request;

import cn.zhangchuangla.medicine.common.core.base.PageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 知识库查询请求对象
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "查询知识库请求对象")
public class KnowledgeBaseListRequest extends PageRequest {

    @Schema(description = "知识库名称", example = "常见用药知识库")
    private String name;

    @Schema(description = "知识库描述", example = "覆盖常见用药相关的问答内容")
    private String description;

}
