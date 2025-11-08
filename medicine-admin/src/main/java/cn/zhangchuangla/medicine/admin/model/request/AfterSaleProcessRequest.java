package cn.zhangchuangla.medicine.admin.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

/**
 * 管理员处理售后请求(退款/换货)
 *
 * @author Chuang
 * @since 2025/11/08
 */
@Data
@Schema(description = "处理售后请求")
public class AfterSaleProcessRequest {

    /**
     * 售后申请ID
     */
    @NotNull(message = "售后申请ID不能为空")
    @Positive(message = "售后申请ID必须为正数")
    @Schema(description = "售后申请ID", example = "1")
    private Long afterSaleId;

    /**
     * 处理备注
     */
    @Schema(description = "处理备注", example = "已完成退款处理")
    private String processRemark;

    /**
     * 换货物流信息
     */
    @Schema(description = "换货物流公司(换货时填写)", example = "顺丰速运")
    private String logisticsCompany;

    /**
     * 换货物流单号
     */
    @Schema(description = "换货物流单号(换货时填写)", example = "SF1234567890")
    private String trackingNumber;
}

