package cn.zhangchuangla.medicine.admin.model.request;

import cn.zhangchuangla.medicine.common.core.base.PageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author Chuang
 * <p>
 * created on 2025/10/31
 */
@EqualsAndHashCode(callSuper = true)
@Schema(description = "订单列表查询参数")
@Data
public class MallOrderListRequest extends PageRequest {
    @Schema(description = "订单编号")
    private String orderNo;

    @Schema(description = "支付方式编码")
    private String payType;

    @Schema(description = "订单状态编码")
    private String orderStatus;

    @Schema(description = "配送方式编码")
    private String deliveryType;

    @Schema(description = "收货人姓名")
    private String receiverName;

    @Schema(description = "收货人电话")
    private String receiverPhone;

}
