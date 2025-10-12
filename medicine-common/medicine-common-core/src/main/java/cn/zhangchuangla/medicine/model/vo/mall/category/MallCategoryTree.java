package cn.zhangchuangla.medicine.model.vo.mall.category;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * @author Chuang
 * <p>
 * created on 2025/10/4 01:45
 */
@Data
@JsonInclude(value = JsonInclude.Include.NON_EMPTY)
public class MallCategoryTree {

    /**
     * ID
     */
    @Schema(description = "ID", type = "int", format = "int64", example = "1")
    private Long id;

    /**
     * 父ID
     */
    @Schema(description = "父ID", type = "int", format = "int64", example = "1")
    private Long parentId;

    /**
     * 分类名称
     */
    @Schema(description = "分类名称", type = "string", example = "分类名称")
    private String categoryName;

    /**
     * 分类描述
     */
    @Schema(description = "分类描述", type = "string", example = "分类描述")
    private String description;

    /**
     * 排序
     */
    @Schema(description = "排序", type = "int", format = "int32", example = "1")
    private Integer sort;

    /**
     * 状态
     */
    @Schema(description = "状态", type = "int", format = "int32", example = "1")
    private Integer status;

    /**
     * 子分类
     */
    @Schema(description = "子分类", type = "object", example = "{}")
    private List<MallCategoryTree> children;
}
