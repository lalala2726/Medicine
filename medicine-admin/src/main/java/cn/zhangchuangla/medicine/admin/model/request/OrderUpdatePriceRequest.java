package cn.zhangchuangla.medicine.admin.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @author Chuang
 * <p>
 * created on 2025/11/1 02:09
 */
@Data
@Schema(description = "修改订单价格请求参数")
public class OrderUpdatePriceRequest {

    /**
     * 订单ID
     */
    @Schema(description = "订单ID", example = "2025110100000001")
    @NotNull(message = "订单号不能为空")
    private Long orderId;

    /**
     * 价格
     */
    @Schema(description = "价格", example = "10.00")
    @NotBlank(message = "价格不能为空")
    private String price;


}
