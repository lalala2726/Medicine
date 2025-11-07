package cn.zhangchuangla.medicine.admin.model.request;

import cn.zhangchuangla.medicine.common.core.base.PageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author Chuang
 * <p>
 * created on 2025/10/31 21:19
 */
@EqualsAndHashCode(callSuper = true)
@Schema(description = "订单列表查询参数")
@Data
public class MallOrderListRequest extends PageRequest {

    /**
     * 订单编号（业务唯一标识）
     */
    @Schema(description = "订单编号")
    private String orderNo;

    /**
     * 支付方式
     */
    @Schema(description = "支付方式编码")
    private String payType;

    /**
     * 订单状态
     */
    @Schema(description = "订单状态编码")
    private String orderStatus;

    /**
     * 配送方式
     */
    @Schema(description = "配送方式编码")
    private String deliveryType;

    /**
     * 收货人姓名
     */
    @Schema(description = "收货人姓名")
    private String receiverName;

    /**
     * 收货人电话
     */
    @Schema(description = "收货人电话")
    private String receiverPhone;

}
