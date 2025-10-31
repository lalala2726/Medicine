package cn.zhangchuangla.medicine.admin.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author Chuang
 * <p>
 * created on 2025/11/1 02:14
 */
@Data
@Schema(description = "订单退款请求参数")
public class OrderRefundRequest {

    /**
     * 订单编号
     */
    @Schema(description = "订单编号")
    @NotBlank(message = "订单编号不能为空")
    private String orderNo;

    /**
     * 退款金额
     */
    @Schema(description = "退款金额")
    @NotBlank(message = "退款金额不能为空")
    private BigDecimal refundAmount;

    /**
     * 退款原因
     */
    @Schema(description = "退款原因")
    private String refundReason;

}
