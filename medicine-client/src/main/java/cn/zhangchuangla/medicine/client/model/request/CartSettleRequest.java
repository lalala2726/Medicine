package cn.zhangchuangla.medicine.client.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 购物车结算请求
 *
 * @author Chuang
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "购物车结算请求参数")
public class CartSettleRequest {

    @NotEmpty(message = "购物车商品ID列表不能为空")
    @Schema(description = "购物车商品ID列表", requiredMode = Schema.RequiredMode.REQUIRED, example = "[1, 2, 3]")
    private List<Long> cartIds;

    @NotNull(message = "收货地址不能为空")
    @Schema(description = "收货地址", requiredMode = Schema.RequiredMode.REQUIRED, example = "中国广东省深圳市南山区XX路XX号")
    private String address;

    @Schema(description = "订单备注", example = "请尽快发货")
    private String remark;
}

