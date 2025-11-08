package cn.zhangchuangla.medicine.client.model.request;

import cn.zhangchuangla.medicine.common.core.base.PageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户查询售后列表请求
 *
 * @author Chuang
 * @since 2025/11/08
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "查询售后列表请求")
public class AfterSaleListRequest extends PageRequest {

    /**
     * 售后类型
     */
    @Schema(description = "售后类型(REFUND_ONLY/RETURN_REFUND/EXCHANGE)", example = "REFUND_ONLY")
    private String afterSaleType;

    /**
     * 售后状态
     */
    @Schema(description = "售后状态(PENDING/APPROVED/REJECTED/PROCESSING/COMPLETED/CANCELLED)", example = "PENDING")
    private String afterSaleStatus;

    /**
     * 订单编号
     */
    @Schema(description = "订单编号", example = "O20251108123456789012")
    private String orderNo;
}

