package cn.zhangchuangla.medicine.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 订单物流信息VO
 *
 * @author Chuang
 * created 2025/11/08
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "订单物流信息VO")
public class OrderShippingVo {

    /**
     * 订单ID
     */
    @Schema(description = "订单ID", example = "1")
    private Long orderId;

    /**
     * 订单号
     */
    @Schema(description = "订单号", example = "2025000000000001")
    private String orderNo;

    /**
     * 订单状态
     */
    @Schema(description = "订单状态", example = "PENDING_RECEIPT")
    private String orderStatus;

    /**
     * 订单状态名称
     */
    @Schema(description = "订单状态名称", example = "待收货")
    private String orderStatusName;

    /**
     * 物流公司
     */
    @Schema(description = "物流公司", example = "顺丰速运")
    private String logisticsCompany;

    /**
     * 物流单号
     */
    @Schema(description = "物流单号", example = "SF1234567890")
    private String trackingNumber;

    /**
     * 发货备注
     */
    @Schema(description = "发货备注", example = "已发货，请注意查收")
    private String shipmentNote;

    /**
     * 发货时间
     */
    @Schema(description = "发货时间", example = "2025-11-08 10:00:00")
    private Date deliverTime;

    /**
     * 签收时间
     */
    @Schema(description = "签收时间", example = "2025-11-15 14:30:00")
    private Date receiveTime;

    /**
     * 物流状态
     */
    @Schema(description = "物流状态", example = "IN_TRANSIT")
    private String status;

    /**
     * 物流状态名称
     */
    @Schema(description = "物流状态名称", example = "运输中")
    private String statusName;

    /**
     * 收货人信息
     */
    @Schema(description = "收货人信息")
    private ReceiverInfo receiverInfo;

    /**
     * 收货人信息内部类
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "收货人信息")
    public static class ReceiverInfo {

        @Schema(description = "收货人姓名", example = "张三")
        private String receiverName;

        @Schema(description = "收货人电话", example = "13800138000")
        private String receiverPhone;

        @Schema(description = "收货详细地址", example = "广东省深圳市南山区科技园xxx号")
        private String receiverDetail;

        @Schema(description = "配送方式", example = "EXPRESS")
        private String deliveryType;

        @Schema(description = "配送方式名称", example = "快递配送")
        private String deliveryTypeName;
    }
}

