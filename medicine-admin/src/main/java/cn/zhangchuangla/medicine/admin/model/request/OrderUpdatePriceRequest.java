package cn.zhangchuangla.medicine.admin.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * @author Chuang
 * <p>
 * created on 2025/11/1 02:09
 */
@Data
public class OrderUpdatePriceRequest {

    @Schema(description = "订单号")
    @NotBlank(message = "订单号不能为空")
    private String OrderNo;

    @Schema(description = "价格")
    @NotBlank(message = "价格不能为空")
    private String price;


}
