package cn.zhangchuangla.medicine.admin.model.vo.medicine;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

/**
 * 药品分类列表视图对象
 *
 * @author Chuang
 * created on 2025/9/21 19:45
 */
@Data
@Schema(description = "药品分类列表视图对象")
public class CategoryListVo {

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
    @Schema(description = "分类描述", type = "string", example = "中成药")
    private String description;

    /**
     * 排序值，越小越靠前
     */
    @Schema(description = "排序值，越小越靠前", type = "int", example = "1")
    private Integer sort;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间", type = "date", example = "2025-01-01 00:00:00")
    private Date createTime;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间", type = "date", example = "2025-01-01 00:00:00")
    private Date updateTime;

    /**
     * 创建者
     */
    @Schema(description = "创建者", type = "string", example = "admin")
    private String createBy;

    /**
     * 更新者
     */
    @Schema(description = "更新者", type = "string", example = "admin")
    private String updateBy;

}
