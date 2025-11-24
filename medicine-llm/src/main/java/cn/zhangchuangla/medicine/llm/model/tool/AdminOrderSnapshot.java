package cn.zhangchuangla.medicine.llm.model.tool;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 订单核心信息快照，供 LLM 工具回答使用。
 */
@Data
@Schema(description = "订单信息快照")
public class AdminOrderSnapshot {

    @Schema(description = "订单编号")
    private String orderNo;

    @Schema(description = "订单状态")
    private String orderStatus;

    @Schema(description = "支付状态，1 表示已支付")
    private Integer paid;

    @Schema(description = "支付方式")
    private String payType;

    @Schema(description = "应付金额")
    private BigDecimal totalAmount;

    @Schema(description = "实付金额")
    private BigDecimal payAmount;

    @Schema(description = "退款金额")
    private BigDecimal refundAmount;

    @Schema(description = "退款状态，如 SUCCESS / PARTIAL")
    private String refundStatus;

    @Schema(description = "收货人")
    private String receiverName;

    @Schema(description = "收货电话")
    private String receiverPhone;

    @Schema(description = "收货地址")
    private String receiverDetail;

    @Schema(description = "创建时间")
    private Date createTime;

    @Schema(description = "支付时间")
    private Date payTime;

    @Schema(description = "发货时间")
    private Date deliverTime;

    @Schema(description = "收货时间")
    private Date receiveTime;

    @Schema(description = "退款时间")
    private Date refundTime;

    @Schema(description = "订单商品明细")
    private List<AdminOrderItemSnapshot> items;
}
