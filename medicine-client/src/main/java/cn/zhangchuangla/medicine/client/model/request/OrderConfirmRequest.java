package cn.zhangchuangla.medicine.client.model.request;

import cn.zhangchuangla.medicine.model.enums.PayTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @author Chuang
 * <p>
 * created on 2025/11/1 01:42
 */
@Data
@Schema(description = "确认订单请求参数")
public class OrderConfirmRequest {


    @Schema(description = "订单编号")
    @NotBlank(message = "订单编号不能为空")
    private String orderNo;

    @Schema(description = "支付方式", example = "ALIPAY")
    @NotNull(message = "支付方式不能为空")
    private PayTypeEnum payMethod;

}
