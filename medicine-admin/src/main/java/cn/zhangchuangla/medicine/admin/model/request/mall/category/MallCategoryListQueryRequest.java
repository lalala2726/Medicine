package cn.zhangchuangla.medicine.admin.model.request.mall.category;

import cn.zhangchuangla.medicine.common.core.base.BasePageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 商城商品分类列表查询请求对象
 *
 * @author Chuang
 * created on 2025/10/4 01:54
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "商城商品分类列表查询请求对象")
public class MallCategoryListQueryRequest extends BasePageRequest {

    /**
     * 分类ID
     */
    @Schema(description = "分类ID", type = "int64", example = "1")
    private Long id;

    /**
     * 分类名称
     */
    @Schema(description = "分类名称", type = "string", example = "保健品")
    private String name;

    /**
     * 父分类ID，0表示顶级分类
     */
    @Schema(description = "父分类ID，0表示顶级分类", type = "int64", example = "0")
    private Long parentId;

    /**
     * 分类描述
     */
    @Schema(description = "分类描述", type = "string", example = "保健品类分类")
    private String description;

}
