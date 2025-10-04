package cn.zhangchuangla.medicine.model.request.mall;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 商城商品详情修改请求对象
 *
 * @author Chuang
 * created on 2025/10/4 02:25
 */
@Data
@Schema(description = "商城商品详情修改请求对象")
public class MallProductDetailUpdateRequest {

    /**
     * 主键ID
     */
    @NotNull(message = "详情ID不能为空")
    @Schema(description = "主键ID", type = "int64", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long id;

    /**
     * 商品ID，关联 mall_product
     */
    @NotNull(message = "商品ID不能为空")
    @Schema(description = "商品ID", type = "int64", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long productId;

    /**
     * 商品详情HTML或Markdown内容
     */
    @NotBlank(message = "商品详情内容不能为空")
    @Schema(description = "商品详情内容", type = "string", example = "<p>商品详细描述...</p>", requiredMode = Schema.RequiredMode.REQUIRED)
    private String content;

}