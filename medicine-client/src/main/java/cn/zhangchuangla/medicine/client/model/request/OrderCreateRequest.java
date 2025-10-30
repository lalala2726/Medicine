package cn.zhangchuangla.medicine.client.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Chuang
 * <p>
 * created on 2025/10/31 01:35
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "创建订单请求参数")
public class OrderCreateRequest {

    /**
     * 商品ID
     */
    @NotNull(message = "商品ID不能为空")
    @Min(value = 1, message = "商品ID不能小于1")
    @Schema(description = "商品ID", type = "int", format = "int64", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long productId;

    /**
     * 购买数量
     */
    @NotNull(message = "购买数量不能为空")
    @Min(value = 1, message = "购买数量不能小于1")
    @Schema(description = "购买数量", type = "int", format = "int32", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer quantity;

    /**
     * 收货地址
     */
    @NotNull(message = "收货地址不能为空")
    @Schema(description = "收货地址", type = "string", requiredMode = Schema.RequiredMode.REQUIRED, example = "中国")
    private String address;

    /**
     * 订单备注
     */
    @Schema(description = "订单备注", type = "string", requiredMode = Schema.RequiredMode.NOT_REQUIRED, example = "请尽快发货")
    private String remark;

}
