package cn.zhangchuangla.medicine.common.core.model.request.mall;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 商城商品图片添加请求对象
 *
 * @author Chuang
 * created on 2025/10/4 02:26
 */
@Data
@Schema(description = "商城商品图片添加请求对象")
public class MallProductImageAddRequest {

    /**
     * 商品ID，关联 mall_product
     */
    @NotNull(message = "商品ID不能为空")
    @Schema(description = "商品ID", type = "int64", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long productId;

    /**
     * 图片URL
     */
    @NotNull(message = "图片URL不能为空")
    @Schema(description = "图片URL", type = "string", example = "https://example.com/image.jpg", requiredMode = Schema.RequiredMode.REQUIRED)
    private String imageUrl;

    /**
     * 排序值
     */
    @Schema(description = "排序值", type = "int32", example = "1")
    private Integer sort = 0;

}
