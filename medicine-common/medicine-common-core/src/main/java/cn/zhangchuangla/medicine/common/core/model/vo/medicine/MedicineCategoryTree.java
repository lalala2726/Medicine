package cn.zhangchuangla.medicine.common.core.model.vo.medicine;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * @author Chuang
 * <p>
 * created on 2025/9/24 15:20
 */
@Data
@JsonInclude(value = JsonInclude.Include.NON_EMPTY)
public class MedicineCategoryTree {

    /**
     * 分类ID
     */
    @Schema(description = "分类ID", type = "int64", example = "1")
    private Long id;

    /**
     * 分类名称
     */
    @Schema(description = "分类名称", type = "string", example = "中成药")
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
     * 子分类列表
     */
    @Schema(description = "子分类列表", type = "array")
    private List<MedicineCategoryTree> children;

}
