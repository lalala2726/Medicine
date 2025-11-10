package cn.zhangchuangla.medicine.client.model.request;

import cn.zhangchuangla.medicine.model.enums.AfterSaleReasonEnum;
import cn.zhangchuangla.medicine.model.enums.AfterSaleTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 用户申请售后请求
 *
 * @author Chuang
 * created 2025/11/08
 */
@Data
@Schema(description = "申请售后请求")
public class AfterSaleApplyRequest {

    @NotNull(message = "订单项ID不能为空")
    @Positive(message = "订单项ID必须为正数")
    @Schema(description = "订单项ID", example = "1")
    private Long orderItemId;

    @NotNull(message = "售后类型不能为空")
    @Schema(description = "售后类型(REFUND_ONLY-仅退款, RETURN_REFUND-退货退款, EXCHANGE-换货)", example = "REFUND_ONLY")
    private AfterSaleTypeEnum afterSaleType;

    @NotNull(message = "退款金额不能为空")
    @Positive(message = "退款金额必须大于0")
    @Schema(description = "退款金额", example = "99.99")
    private BigDecimal refundAmount;

    @NotNull(message = "申请原因不能为空")
    @Schema(description = "申请原因(ADDRESS_ERROR/NOT_AS_DESCRIBED/INFO_ERROR/DAMAGED/DELAYED/OTHER)", example = "DAMAGED")
    private AfterSaleReasonEnum applyReason;

    @Schema(description = "详细说明", example = "商品包装破损，内部商品有损坏")
    private String applyDescription;

    @Schema(description = "凭证图片URL列表", example = "[\"https://example.com/image1.jpg\", \"https://example.com/image2.jpg\"]")
    private List<String> evidenceImages;

    @NotBlank(message = "收货状态不能为空")
    @Schema(description = "收货状态(RECEIVED-已收到货, NOT_RECEIVED-未收到货)", example = "RECEIVED")
    private String receiveStatus;
}

