package cn.zhangchuangla.medicine.client.model.request;

import cn.zhangchuangla.medicine.model.enums.AfterSaleReasonEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 整单退款申请请求
 */
@Data
@Schema(description = "整单退款申请请求")
public class OrderRefundApplyRequest {

    @NotBlank(message = "订单编号不能为空")
    @Schema(description = "订单编号", example = "202501010001", requiredMode = Schema.RequiredMode.REQUIRED)
    private String orderNo;

    @NotNull(message = "退款原因不能为空")
    @Schema(description = "退款原因", example = "DAMAGED", requiredMode = Schema.RequiredMode.REQUIRED)
    private AfterSaleReasonEnum applyReason;

    @Schema(description = "详细说明", example = "商品存在质量问题，希望退回全部订单")
    private String applyDescription;

    @Schema(description = "凭证图片URL列表", example = "[\"https://img.example.com/a.png\"]")
    private List<String> evidenceImages;
}
