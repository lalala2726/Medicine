package cn.zhangchuangla.medicine.model.request.medicine;

import cn.zhangchuangla.medicine.common.base.BasePageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 药品分类列表查询请求对象
 *
 * @author Chuang
 * created on 2025/9/21 20:18
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "药品分类列表查询请求对象")
public class MedicineCategoryListQueryRequest extends BasePageRequest {

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

}