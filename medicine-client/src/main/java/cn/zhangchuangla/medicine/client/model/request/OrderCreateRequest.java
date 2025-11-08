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
 * created on 2025/10/31
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "创建订单请求参数")
public class OrderCreateRequest {

    @NotNull(message = "商品ID不能为空")
    @Min(value = 1, message = "商品ID不能小于1")
    @Schema(description = "商品ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long productId;

    @NotNull(message = "购买数量不能为空")
    @Min(value = 1, message = "购买数量不能小于1")
    @Schema(description = "购买数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer quantity;

    @NotNull(message = "收货地址不能为空")
    @Schema(description = "收货地址", requiredMode = Schema.RequiredMode.REQUIRED, example = "中国")
    private String address;

    @Schema(description = "订单备注", requiredMode = Schema.RequiredMode.NOT_REQUIRED, example = "请尽快发货")
    private String remark;

}
