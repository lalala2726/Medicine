package cn.zhangchuangla.medicine.model.request.medicine;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 药品分类修改请求对象
 *
 * @author Chuang
 * created on 2025/9/21 20:17
 */
@Data
@Schema(description = "药品分类修改请求对象")
public class MedicineCategoryUpdateRequest {

    /**
     * 分类ID
     */
    @NotNull(message = "分类ID不能为空")
    @Schema(description = "分类ID", type = "int64", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long id;

    /**
     * 分类名称
     */
    @NotBlank(message = "分类名称不能为空")
    @Schema(description = "分类名称", type = "string", example = "中成药", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    /**
     * 父分类ID，0表示顶级分类
     */
    @Schema(description = "父分类ID，0表示顶级分类", type = "int64", example = "0")
    private Long parentId;

    /**
     * 分类描述
     */
    @Schema(description = "分类描述", type = "string", example = "中成药分类")
    private String description;

    /**
     * 排序值，越小越靠前
     */
    @Schema(description = "排序值，越小越靠前", type = "int", example = "1")
    private Integer sort;

}
