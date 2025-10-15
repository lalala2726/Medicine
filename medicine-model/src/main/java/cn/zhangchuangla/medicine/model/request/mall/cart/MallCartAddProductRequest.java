package cn.zhangchuangla.medicine.model.request.mall.cart;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @author Chuang
 * <p>
 * created on 2025/10/15 16:34
 */
@Data
@Schema(description = "购物车添加商品请求参数")
public class MallCartAddProductRequest {

    /**
     * 商品ID
     */
    @Schema(description = "商品ID", type = "int", format = "int64", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "商品ID不能为空")
    @Min(value = 1, message = "商品ID不能小于1")
    private Long productId;

    /**
     * 商品数量
     */
    @Schema(description = "商品数量", type = "int", format = "int64", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "商品数量不能为空")
    @Min(value = 1, message = "商品数量不能小于1")
    private Integer count;

}
