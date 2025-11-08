package cn.zhangchuangla.medicine.admin.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 订单地址信息VO
 *
 * @author Chuang
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "订单地址信息")
public class OrderAddressVo {

    /**
     * 订单ID
     */
    @Schema(description = "订单ID",example = "1")
    private Long orderId;

    /**
     * 订单号
     */
    @Schema(description = "订单号")
    private String orderNo;

    /**
     * 订单状态
     */
    @Schema(description = "订单状态")
    private String orderStatus;

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

    /**
     * 收货详细地址
     */
    @Schema(description = "收货详细地址")
    private String receiverDetail;

    /**
     * 配送方式
     */
    @Schema(description = "配送方式")
    private String deliveryType;
}

